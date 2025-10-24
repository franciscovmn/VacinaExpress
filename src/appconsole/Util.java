package appconsole; 

import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeMap;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet; 
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import com.db4o.cs.Db4oClientServer;
import com.db4o.cs.config.ClientConfiguration;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.query.Query;

import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;

public class Util { 
    private static ObjectContainer manager;
    private static String ipservidor; 
    private static final String DATABASE_FILE = "vacina_express.db4o"; 

    public static ObjectContainer conectarBanco() {
        if (manager == null || manager.ext().isClosed()) {
             manager = conectarBancoLocal(); 

            // Ativa geração de IDs automáticos para as classes com atributo "int id"
            ControleID.ativar(true, manager);
        }
        return manager;
    }

    private static ObjectContainer conectarBancoLocal() {
        EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
        config.common().messageLevel(0);

        ObjectClass pessoaClass = config.common().objectClass(Pessoa.class);
        pessoaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        pessoaClass.objectField("listaVacinacao").cascadeOnActivate(true);
        pessoaClass.objectField("localizacao").cascadeOnUpdate(true); 
        pessoaClass.objectField("localizacao").cascadeOnActivate(true);

        ObjectClass vacinaClass = config.common().objectClass(Vacina.class);
        vacinaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        vacinaClass.objectField("listaVacinacao").cascadeOnActivate(true);

        ObjectClass vacinacaoClass = config.common().objectClass(Vacinacao.class);
        vacinacaoClass.cascadeOnUpdate(true);
        vacinacaoClass.cascadeOnActivate(true);

        // Abrir banco local
        manager = Db4oEmbedded.openFile(config, DATABASE_FILE);
        return manager;
    }

    private static ObjectContainer conectarBancoRemoto() {
		if (manager != null && !manager.ext().isClosed()) {
			return manager; // ja tem uma conexao
		}

		ClientConfiguration config = Db4oClientServer.newClientConfiguration();
		config.common().messageLevel(0);

        ObjectClass pessoaClass = config.common().objectClass(Pessoa.class);
        pessoaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        pessoaClass.objectField("listaVacinacao").cascadeOnActivate(true);
        pessoaClass.objectField("localizacao").cascadeOnUpdate(true);
        pessoaClass.objectField("localizacao").cascadeOnActivate(true);

        ObjectClass vacinaClass = config.common().objectClass(Vacina.class);
        vacinaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        vacinaClass.objectField("listaVacinacao").cascadeOnActivate(true);

        ObjectClass vacinacaoClass = config.common().objectClass(Vacinacao.class);
        vacinacaoClass.cascadeOnUpdate(true);
        vacinacaoClass.cascadeOnActivate(true);

		try {

            ipservidor = "127.0.0.1";
            System.out.println("Tentando conectar ao servidor remoto: " + ipservidor);

			manager = Db4oClientServer.openClient(config, ipservidor, 34000, "usuario1", "senha1");
            System.out.println("Conectou no banco remoto ip=" + ipservidor);
			return manager;
		} catch (Exception e) {
            System.err.println("Erro ao conectar no banco remoto ip=" + ipservidor + "\n" + e.getMessage());
			return null;
		}
	}

    public static void desconectar() {
        if (manager != null && !manager.ext().isClosed()) {
            manager.close();
            manager = null; 
        }
    }

    static String getIPservidor() {
        return ipservidor;
    }

    static ObjectContainer getManager() {
        return manager;
    }

    // classe interna
    // Controla a geração automatica de IDs para
    // as classes que possuem um atributo id
    static class ControleID {
        private static ObjectContainer sequencia; 
        private static TreeMap<String, RegistroID> registros = new TreeMap<>();
        private static boolean salvar; 

        public static void ativar(boolean ativa, ObjectContainer manager) {
            if (!ativa) {
				return; 
			}
            if (manager == null) {
				throw new RuntimeException("Ativar controle de id - manager desconhecido");
			}


            if (manager instanceof EmbeddedObjectContainer) {
                EmbeddedConfiguration configSeq = Db4oEmbedded.newConfiguration();
                configSeq.common().messageLevel(0); 
                sequencia = Db4oEmbedded.openFile(configSeq, "sequencia.db4o");
            } else {
                String ipservidorRemoto = Util.getIPservidor(); 
                if (ipservidorRemoto == null) {
                     throw new RuntimeException("IP do servidor remoto não definido para banco de sequência.");
                }
                ClientConfiguration configSeq = Db4oClientServer.newClientConfiguration();
                configSeq.common().messageLevel(0);
                sequencia = Db4oClientServer.openClient(configSeq, ipservidorRemoto, 35000, "usuario0", "senha0");
            }
            lerRegistrosID();

            EventRegistry eventRegistry = EventRegistryFactory.forObjectContainer(manager);

            eventRegistry.creating().addListener((event, args) -> {
                try {
                    Object objeto = args.object();
                    Field field = objeto.getClass().getDeclaredField("id");
                    if (field != null && field.getType().equals(int.class)) {
                        String nomedaclasse = objeto.getClass().getName();
                        RegistroID registro = obterRegistroID(nomedaclasse);
                        registro.incrementarID();
                        field.setAccessible(true);
                        field.setInt(objeto, registro.getid());
                        registros.put(nomedaclasse, registro);
                        salvar = true;
                    }
                } catch (NoSuchFieldException e) {
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println("Erro ao acessar/definir campo 'id' via reflection: " + e.getMessage());
                }
            });

            eventRegistry.committed().addListener((event, args) -> {
                salvarRegistrosID();
            });

            eventRegistry.closing().addListener((event, args) -> {
                salvarRegistrosID();
                if (sequencia != null && !sequencia.ext().isClosed()) {
					sequencia.close();
				}
            });
        }

        private static void lerRegistrosID() {
             if (sequencia == null) {
				return;
			 }
             Query q = sequencia.query();
             q.constrain(RegistroID.class);
             List<RegistroID> resultados = q.execute();
             registros.clear(); 
             for (RegistroID reg : resultados) {
                 registros.put(reg.getNomedaclasse(), reg);
             }
             salvar = false;
        }

        private static void salvarRegistrosID() {
            if (salvar && sequencia != null) {
                boolean houveModificacao = false;
                for (RegistroID reg : registros.values()) {
                    if (reg.isModificado()) {
                        sequencia.store(reg);
                        reg.setModificado(false); 
                        houveModificacao = true;
                    }
                }
                if (houveModificacao) {
                    sequencia.commit(); 
                }
                salvar = false;
            }
        }
        private static RegistroID obterRegistroID(String nomeclasse) {
            RegistroID reg = registros.get(nomeclasse);
            if (reg == null) {
                 Query q = sequencia.query();
                 q.constrain(RegistroID.class);
                 q.descend("nomedaclasse").constrain(nomeclasse);
                 ObjectSet<RegistroID> result = q.execute();
                 if (result.hasNext()) {
                     reg = result.next();
                 } else {
                     reg = new RegistroID(nomeclasse);
                 }
                 registros.put(nomeclasse, reg);
            }
            return reg;
        }

    } 
    // classe interna
    // Encapsula o ultimo ID gerado para uma classe
    // Tornar static para ControleID static poder referenciá-la diretamente
    static class RegistroID {
        private String nomedaclasse;
        private int ultimoid;
        transient private boolean modificado = false;

        public RegistroID() {}

        public RegistroID(String nomedaclasse) {
            this.nomedaclasse = nomedaclasse;
            this.ultimoid = 0;
            this.modificado = true; 
        }

        public String getNomedaclasse() { return nomedaclasse; }
        public int getid() { return ultimoid; }
        public boolean isModificado() { return modificado; }
        public void setModificado(boolean modificado) { this.modificado = modificado; }

        public void incrementarID() {
            ultimoid++;
            setModificado(true);
        }

        @Override
        public String toString() {
            return "RegistroID [nomedaclasse=" + nomedaclasse + ", ultimoid=" + ultimoid + "]";
        }
    } 

} 