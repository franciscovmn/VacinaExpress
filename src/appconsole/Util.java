package appconsole; // Ou package appconsole; dependendo de onde colocar o arquivo

import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeMap;

// Remova imports de javax.swing se não for usar conectarBancoRemoto com GUI
// import javax.swing.JComboBox;
// import javax.swing.JOptionPane;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet; // Import necessário para ControleID
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import com.db4o.cs.Db4oClientServer;
import com.db4o.cs.config.ClientConfiguration;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.query.Query;

// Importe suas classes do modelo
import modelo.Pessoa;
import modelo.Vacina;
// import modelo.Localizacao; // Não precisa configurar cascata explicitamente se Pessoa já cascateia
import modelo.Vacinacao;

public class Util { // Classe renomeada
    private static ObjectContainer manager;
    private static String ipservidor; // Mantido para compatibilidade com o exemplo
    private static final String DATABASE_FILE = "vacina_express.db4o"; // Nome do seu banco

    // Método renomeado de abrirDB
    public static ObjectContainer conectarBanco() {
        // Decide se conecta local ou remoto (mantendo a lógica do exemplo)
        // Para seu appconsole, conectarBancoLocal é o mais provável
        if (manager == null || manager.ext().isClosed()) {
             manager = conectarBancoLocal(); // banco local (pasta do projeto)
            // manager = conectarBancoRemoto(); // banco remoto (precisa de um servidor ativo e GUI)

            // Ativa geração de IDs automáticos para as classes com atributo "int id"
            ControleID.ativar(true, manager);
        }
        return manager;
    }

    private static ObjectContainer conectarBancoLocal() {
        // Configuração adaptada para suas classes
        EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
        config.common().messageLevel(0); // mensagens na tela 0(desliga),1,2,3...

        // Configurar cascata para suas classes (seguindo o padrão do exemplo)
        // CascadeOnUpdate é o mais importante para o problema de Alterar.java
        // CascadeOnDelete=false evita apagar Vacinas ao apagar Pessoas/Vacinacoes
        // CascadeOnActivate=true carrega objetos relacionados automaticamente

        ObjectClass pessoaClass = config.common().objectClass(Pessoa.class);
        pessoaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        pessoaClass.objectField("listaVacinacao").cascadeOnActivate(true);
        pessoaClass.objectField("localizacao").cascadeOnUpdate(true); // Essencial para Alterar.java
        pessoaClass.objectField("localizacao").cascadeOnActivate(true);
        // pessoaClass.cascadeOnDelete(false); // Opcional, depende da lógica desejada

        ObjectClass vacinaClass = config.common().objectClass(Vacina.class);
        vacinaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        vacinaClass.objectField("listaVacinacao").cascadeOnActivate(true);
        // vacinaClass.cascadeOnDelete(false); // Opcional

        ObjectClass vacinacaoClass = config.common().objectClass(Vacinacao.class);
        // Vacinacao pode herdar cascata de Pessoa/Vacina, mas configurar explicitamente não prejudica
        vacinacaoClass.cascadeOnUpdate(true);
        vacinacaoClass.cascadeOnActivate(true);
        // vacinacaoClass.cascadeOnDelete(false); // Opcional

        // Abrir banco local
        manager = Db4oEmbedded.openFile(config, DATABASE_FILE);
        return manager;
    }

    // O método conectarBancoRemoto do professor usa Swing (JComboBox, JOptionPane).
    // Se sua aplicação é puramente console, você pode remover ou simplificar este método.
    // Mantenho aqui para ficar igual ao exemplo, mas comentei as partes de GUI.
    private static ObjectContainer conectarBancoRemoto() {
		if (manager != null && !manager.ext().isClosed()) {
			return manager; // ja tem uma conexao
		}

		ClientConfiguration config = Db4oClientServer.newClientConfiguration();
		config.common().messageLevel(0);

        // --- ADAPTAR CASCATAS PARA SEU MODELO ---
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
        // --- FIM DA ADAPTAÇÃO DAS CASCATAS ---

		try {
            // Parte de GUI - Incompatível com appconsole puro
			// JComboBox<String> combo = new JComboBox<>(new String[] {"10.0.71.50", "54.163.92.174" });
			// JOptionPane.showConfirmDialog(null, combo, "Selecione o IP do servidor", JOptionPane.DEFAULT_OPTION,
			// 		JOptionPane.QUESTION_MESSAGE);
			// ipservidor = (String) combo.getSelectedItem();

            // Usar um IP fixo ou lido de outra forma para console
            ipservidor = "127.0.0.1"; // Exemplo: localhost
            System.out.println("Tentando conectar ao servidor remoto: " + ipservidor);

            // Usar credenciais do exemplo do professor
			manager = Db4oClientServer.openClient(config, ipservidor, 34000, "usuario1", "senha1");
            System.out.println("Conectou no banco remoto ip=" + ipservidor);
			return manager;
		} catch (Exception e) {
            System.err.println("Erro ao conectar no banco remoto ip=" + ipservidor + "\n" + e.getMessage());
            // JOptionPane.showMessageDialog(null,
			// 		"Erro ao conectar no banco remoto ip=" + ipservidor + "\n" + e.getMessage());
			// System.exit(0); // Talvez não queira sair da aplicação console
			return null;
		}
	}


    // Método renomeado de fecharDB
    public static void desconectar() {
        if (manager != null && !manager.ext().isClosed()) {
            // O ControleID cuida de fechar o banco de sequência no evento closing
            manager.close();
            manager = null; // Garante que uma nova conexão seja criada na próxima chamada
        }
    }

    // Métodos de acesso mantidos para ControleID (se usar conexão remota)
    static String getIPservidor() {
        return ipservidor;
    }

    static ObjectContainer getManager() {
        return manager;
    }

    // =========================================================================
    // == CLASSES INTERNAS ControleID e RegistroID (COPIADAS E ADAPTADAS) =====
    // =========================================================================

    // **********************************************
    // classe interna
    // Controla a geração automatica de IDs para
    // as classes que possuem um atributo id
    // **********************************************
    static class ControleID {
        private static ObjectContainer sequencia; // bd auxiliar de sequencias DE IDs
        private static TreeMap<String, RegistroID> registros = new TreeMap<>(); // cache de registros de ids
        private static boolean salvar; // indica se precisa salvar os registros de id
        // ipservidor e manager são referenciados da classe externa Util

        public static void ativar(boolean ativa, ObjectContainer manager) {
            if (!ativa) {
				return; // controle de ids nao será feito
			}
            if (manager == null) {
				throw new RuntimeException("Ativar controle de id - manager desconhecido");
			}

            // Não precisa mais guardar 'manager' aqui, usa Util.getManager() se necessário

            if (manager instanceof EmbeddedObjectContainer) {
                // banco de sequencia no local
                EmbeddedConfiguration configSeq = Db4oEmbedded.newConfiguration();
                configSeq.common().messageLevel(0); // Desligar mensagens para o banco de sequência
                sequencia = Db4oEmbedded.openFile(configSeq, "sequencia.db4o");
                // System.out.println("conectou sequencia local");
            } else {
                // banco de sequencia no servidor remoto
                String ipservidorRemoto = Util.getIPservidor(); // Pega da classe externa
                if (ipservidorRemoto == null) {
                     throw new RuntimeException("IP do servidor remoto não definido para banco de sequência.");
                }
                ClientConfiguration configSeq = Db4oClientServer.newClientConfiguration();
                configSeq.common().messageLevel(0);
                // Usar credenciais do exemplo do professor para o banco de sequência remoto
                sequencia = Db4oClientServer.openClient(configSeq, ipservidorRemoto, 35000, "usuario0", "senha0");
                // System.out.println("conectou no banco de sequencia remoto ip=" + ipservidorRemoto);
            }
            lerRegistrosID(); // ler do banco os registros de id

            EventRegistry eventRegistry = EventRegistryFactory.forObjectContainer(manager);

            eventRegistry.creating().addListener((event, args) -> {
                try {
                    // System.out.println("trigger creating");
                    Object objeto = args.object();
                    Field field = objeto.getClass().getDeclaredField("id");
                    // Adicionada verificação do tipo int para evitar erro com outros campos 'id'
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
                   // Ignora classes sem campo 'id' int
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println("Erro ao acessar/definir campo 'id' via reflection: " + e.getMessage());
                }
            });

            // Usar committed() como na sua versão original, parece mais correto que created()
            eventRegistry.committed().addListener((event, args) -> {
                // System.out.println("trigger commit");
                salvarRegistrosID();
            });

            eventRegistry.closing().addListener((event, args) -> {
                // System.out.println("trigger close");
                salvarRegistrosID(); // Garante salvamento antes de fechar
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
             List<RegistroID> resultados = q.execute(); // Usar List ou ObjectSet
             registros.clear(); // Limpa o cache antes de reler
             for (RegistroID reg : resultados) {
                 // System.out.println("lendo do bd sequencia: " + reg);
                 registros.put(reg.getNomedaclasse(), reg);
             }
             salvar = false;
        }

        // Lógica de salvar da sua versão (commit único fora do loop) é mais eficiente
        private static void salvarRegistrosID() {
            if (salvar && sequencia != null) {
                boolean houveModificacao = false;
                for (RegistroID reg : registros.values()) {
                    if (reg.isModificado()) {
                        // System.out.println("gravando no bd sequencia: " + reg);
                        sequencia.store(reg);
                        reg.setModificado(false); // Marcar como não modificado após store
                        houveModificacao = true;
                    }
                }
                if (houveModificacao) {
                    sequencia.commit(); // Commit único após salvar todos os registros modificados
                }
                salvar = false;
            }
        }


        // Lógica para obterRegistroID da sua versão (busca no BD se não achar no cache)
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
                     reg = new RegistroID(nomeclasse); // Cria novo se não existir
                 }
                 registros.put(nomeclasse, reg); // Adiciona ao cache
            }
            return reg;
        }

    } // fim classe interna ControleID

    // *************************************************************
    // classe interna
    // Encapsula o ultimo ID gerado para uma classe
    // *************************************************************
    // Tornar static para ControleID static poder referenciá-la diretamente
    static class RegistroID {
        private String nomedaclasse;
        private int ultimoid;
        transient private boolean modificado = false;

        // Construtor padrão (necessário para db4o)
        public RegistroID() {}

        public RegistroID(String nomedaclasse) {
            this.nomedaclasse = nomedaclasse;
            this.ultimoid = 0;
            this.modificado = true; // Precisa salvar o novo registro
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
    } // fim classe RegistroID

} // fim da classe Util