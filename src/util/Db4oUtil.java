package util;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet; // <-- IMPORT ADDED HERE
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import com.db4o.config.ObjectField;
import com.db4o.cs.Db4oClientServer;
import com.db4o.cs.config.ClientConfiguration;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.query.Query;

import modelo.Pessoa;
import modelo.Vacinacao;
import modelo.Vacina;

import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeMap;

public class Db4oUtil {
    private static final String DATABASE_FILE = "vacina_express.db4o";
    // Variáveis estáticas manager e ipservidor movidas para dentro da classe ControleID
    // conforme o exemplo, se a conexão remota for necessária.
    // Se usar apenas local, podem ser removidas ou adaptadas.

    public static ObjectContainer abrirDB() {
        // Usando conectarBancoLocal como padrão, igual ao seu original
        ObjectContainer manager = conectarBancoLocal();

        // Ativa a geração automática de IDs para classes com atributo "int id"
        ControleID.ativar(true, manager); // Adicionado conforme exemplo
        return manager;
    }

    // Método conectarBancoLocal adaptado do exemplo e do seu original
    private static ObjectContainer conectarBancoLocal() {
        // Se já houver uma conexão (ControleID pode manter), retorne-a.
        // Adaptado: Se precisar de apenas uma instância, considere usar um Singleton
        // ou verificar manager em ControleID se ele for implementado lá.
        // Por ora, mantém a lógica de criar uma nova conexão localmente.

        EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
        config.common().messageLevel(0); // mensagens na tela 0(desliga),1,2,3...

        // Configurar apenas cascadeOnUpdate (mantido do seu original)
        ObjectClass pessoaClass = config.common().objectClass(Pessoa.class);
        pessoaClass.objectField("listaVacinacao").cascadeOnUpdate(true);

        ObjectClass vacinacaoClass = config.common().objectClass(Vacinacao.class);
        vacinacaoClass.cascadeOnUpdate(true);

        ObjectClass vacinaClass = config.common().objectClass(Vacina.class);
        vacinaClass.objectField("listaVacinacao").cascadeOnUpdate(true);

        // Abrir banco local
        ObjectContainer manager = Db4oEmbedded.openFile(config, DATABASE_FILE);
        return manager;
    }


    public static void fecharDB(ObjectContainer db) {
        if (db != null && !db.ext().isClosed()) {
            // A lógica de fechar o banco de sequência é tratada pelo trigger em ControleID
            db.close();
        }
    }

    // === INÍCIO DAS CLASSES INTERNAS COPIADAS DO EXEMPLO ===
    // (ControleID e RegistroID)

    // **********************************************
    // classe interna
    // Controla a geração automatica de IDs para
    // as classes que possuem um atributo id
    // **********************************************
    static class ControleID {
        private static ObjectContainer sequencia; // bd auxiliar de sequencias DE IDs
        private static TreeMap<String, RegistroID> registros = new TreeMap<String, RegistroID>(); // cache de registros de ids
        private static boolean salvar; // indica se precisa salvar os registros de id
        private static String ipservidor; // Adicionado para compatibilidade com conexão remota do exemplo
        private static ObjectContainer manager; // Adicionado para manter a referência

        public static void ativar(boolean ativa, ObjectContainer manager) {
            if (!ativa)
                return; // controle de ids nao será feito
            if (manager == null)
                throw new RuntimeException("Ativar controle de id - manager desconhecido"); // desativado

            ControleID.manager = manager; // Guarda a referência

            if (manager instanceof EmbeddedObjectContainer) {
                // banco de sequencia no local
                EmbeddedConfiguration configSeq = Db4oEmbedded.newConfiguration();
                configSeq.common().messageLevel(0); // Desligar mensagens para o banco de sequência
                sequencia = Db4oEmbedded.openFile(configSeq, "sequencia.db4o");
                // System.out.println("conectou sequencia local");
            } else {
                // banco de sequencia no servidor remoto
                // Assume que ipservidor foi definido em conectarBancoRemoto()
                if (ipservidor == null) {
                    // Tenta obter do manager se for conexão remota já estabelecida
                    // (Isso pode não funcionar dependendo da implementação exata de conexão remota)
                    // Uma abordagem mais robusta seria passar o IP explicitamente ou obtê-lo
                    // de uma configuração centralizada.
                     throw new RuntimeException("IP do servidor remoto não definido para banco de sequência.");
                    // ipservidor = manager.ext().configure().clientServer().host(); // Exemplo, pode não ser a API correta
                }
                ClientConfiguration configSeq = Db4oClientServer.newClientConfiguration();
                configSeq.common().messageLevel(0);
                sequencia = Db4oClientServer.openClient(configSeq, ipservidor, 35000,
                        "usuario0", "senha0");
                // System.out.println("conectou no banco de sequencia remoto ip=" + ipservidor);
            }
            lerRegistrosID(); // ler do banco os registros de id

            // CRIAR GERENTE DE TRIGGERS PARA O MANAGER
            EventRegistry eventRegistry = EventRegistryFactory.forObjectContainer(manager);

            // Resgistrar trigger "BEFORE PERSIST" causado pelo manager.store(objeto)
            eventRegistry.creating().addListener((event, args) -> {
                try {
                    // System.out.println("trigger creating");
                    Object objeto = args.object(); // objeto que esta sendo gravado
                    Field field = objeto.getClass().getDeclaredField("id"); // Busca por "id"
                    if (field != null && field.getType().equals(int.class)) { // Verifica se tem campo "int id"
                        String nomedaclasse = objeto.getClass().getName();
                        RegistroID registro = obterRegistroID(nomedaclasse); // pega id da tabela
                        registro.incrementarID(); // incrementa o id
                        field.setAccessible(true); // habilita acesso ao campo id do objeto
                        field.setInt(objeto, registro.getid()); // atualiza o id do objeto
                        registros.put(nomedaclasse, registro); // atualiza tabela de id
                        salvar = true;
                    }
                } catch (NoSuchFieldException e) {
                    // Ignora classes que não têm o campo "id"
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println("Erro ao acessar/definir campo 'id' via reflection: " + e.getMessage());
                }
            });

            // Registrar trigger "AFTER COMMIT" causado pelo manager.commit()
             eventRegistry.committed().addListener((event, args) -> { // Usar committed em vez de created
                 // System.out.println("trigger commit");
                 salvarRegistrosID(); // salvar registros de id alterados
             });


            // Resgistrar trigger "BEFORE CLOSE" causado pelo manager.close()
            eventRegistry.closing().addListener((event, args) -> {
                // System.out.println("trigger close");
                 salvarRegistrosID(); // Garante que IDs sejam salvos mesmo se não houver commit explícito antes de fechar
                if (sequencia != null && !sequencia.ext().isClosed())
                    sequencia.close(); // fecha o banco de sequencias
            });
        }

        private static void lerRegistrosID() {
            if (sequencia == null) return;
            Query q = sequencia.query();
            q.constrain(RegistroID.class);
            // Use List<RegistroID> em vez de ObjectSet para compatibilidade com versões anteriores ou clareza
            // ObjectSet é geralmente preferido, mas List funciona aqui também.
            List<RegistroID> resultados = q.execute();
            for (RegistroID reg : resultados) {
                // System.out.println("lendo do bd sequencia: " + reg);
                registros.put(reg.getNomedaclasse(), reg);
            }
            salvar = false;

        }

        private static void salvarRegistrosID() {
            if (salvar && sequencia != null) {
                for (RegistroID reg : registros.values()) {
                    if (reg.isModificado()) {
                        // System.out.println("gravando no bd sequencia: " + reg);
                        sequencia.store(reg);
                        reg.setModificado(false); // Marcar como não modificado após store
                    }
                }
                 sequencia.commit(); // Commit único após salvar todos os registros modificados
                salvar = false;
            }
        }

        private static RegistroID obterRegistroID(String nomeclasse) {
            RegistroID reg = registros.get(nomeclasse);
            if (reg == null) {
                 // Busca no banco de sequência caso não esteja no cache
                 Query q = sequencia.query();
                 q.constrain(RegistroID.class);
                 q.descend("nomedaclasse").constrain(nomeclasse);
                 ObjectSet<RegistroID> result = q.execute(); // AQUI ESTAVA O ERRO (necessita import)
                 if (result.hasNext()) {
                     reg = result.next();
                     registros.put(nomeclasse, reg); // Adiciona ao cache
                 } else {
                     reg = new RegistroID(nomeclasse); // Cria novo se não existir nem no cache nem no banco
                     registros.put(nomeclasse, reg); // Adiciona ao cache
                     // Não precisa marcar 'salvar = true' aqui, será marcado ao incrementar
                 }
            }
            return reg;
        }

        // Métodos getIPservidor e getManager do exemplo original Util.java (se necessários)
        static String getIPservidor() {
            return ipservidor;
        }

        static ObjectContainer getManager() {
            return manager;
        }

    } // fim classe interna ControleID

    // *************************************************************
    // classe interna
    // Encapsula o ultimo ID gerado para uma classe
    // *************************************************************
    static class RegistroID { // Tornar static para ser acessível por ControleID static
        private String nomedaclasse;
        private int ultimoid;
        transient private boolean modificado = false; // nao sera persistido

        // Construtor padrão (necessário para db4o)
        public RegistroID() {}

        public RegistroID(String nomedaclasse) {
            this.nomedaclasse = nomedaclasse;
            this.ultimoid = 0;
            this.modificado = true; // Novo registro precisa ser salvo
        }

        public String getNomedaclasse() {
            return nomedaclasse;
        }

        public int getid() {
            return ultimoid;
        }

        public boolean isModificado() {
            return modificado;
        }

        public void setModificado(boolean modificado) {
            this.modificado = modificado;
        }

        public void incrementarID() {
            ultimoid++;
            setModificado(true);
        }

        @Override
        public String toString() {
            return "RegistroID [nomedaclasse=" + nomedaclasse + ", ultimoid=" + ultimoid + "]";
        }

    } // fim classe RegistroID

    // === FIM DAS CLASSES INTERNAS ===
}