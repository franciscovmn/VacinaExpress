package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet; // Import necessário
import com.db4o.query.Query; // Import necessário

import modelo.Localizacao;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;
import util.Db4oUtil;

import java.util.Scanner;

public class Cadastrar {

    // REMOVIDOS os métodos proximoIdVacinacao e proximoIdVacina

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Db4oUtil.abrirDB(); // abreDB() agora ativa o ControleID

        try {
            // garantir vacinas iniciais
            if (db.query(Vacina.class).isEmpty()) {
                // Passa 0 como ID inicial, ControleID irá gerar o ID correto
                db.store(new Vacina(0, "CoronaVac"));
                db.store(new Vacina(0, "Pfizer"));
                db.store(new Vacina(0, "AstroVac"));
                db.commit();
                System.out.println("Vacinas iniciais cadastradas.");
            }

            System.out.println("=== CADASTRAR ===");
            System.out.println("1 - Cadastrar Pessoa");
            System.out.println("2 - Cadastrar Vacina (nova)");
            System.out.println("3 - Cadastrar Vacinacao");
            System.out.print("Escolha uma opção: ");
            int opc = Integer.parseInt(sc.nextLine().trim());

            if (opc == 1) {
                System.out.print("CPF: ");
                String cpf = sc.nextLine().trim();
                System.out.print("Data nascimento (YYYY-MM-DD): ");
                String dt = sc.nextLine().trim();
                System.out.print("Latitude: ");
                double lat = Double.parseDouble(sc.nextLine().trim());
                System.out.print("Longitude: ");
                double lon = Double.parseDouble(sc.nextLine().trim());

                // verificar se já existe pessoa com CPF
                Query q = db.query();
                q.constrain(Pessoa.class);
                q.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> exists = q.execute();
                if (!exists.isEmpty()) {
                    System.out.println("Pessoa com esse CPF já existe.");
                } else {
                    Pessoa p = new Pessoa(cpf, dt, new Localizacao(lat, lon));
                    db.store(p); // ControleID não age aqui, pois Pessoa não tem 'int id'
                    db.commit();
                    System.out.println("Pessoa cadastrada com sucesso.");
                }
            } else if (opc == 2) {
                System.out.print("Nome da Vacina: ");
                String nome = sc.nextLine().trim();
                // int id = proximoIdVacina(db); // REMOVIDO
                // Passa 0 como ID inicial, ControleID irá gerar o ID correto
                Vacina v = new Vacina(0, nome);
                db.store(v); // Trigger do ControleID atuará aqui para definir o ID
                db.commit(); // Salva o objeto com o ID gerado e salva o estado do ID
                // Para mostrar o ID gerado, é preciso recuperar o objeto após o commit
                // ou buscar o último ID salvo (ControleID não expõe isso diretamente)
                // Vamos exibir uma mensagem genérica ou buscar o objeto recém-criado:
                 Query qVac = db.query();
                 qVac.constrain(Vacina.class);
                 qVac.descend("nome").constrain(nome); // Supõe nome único para exemplo
                 ObjectSet<Vacina> resultVac = qVac.execute();
                 if(resultVac.hasNext()){
                     System.out.println("Vacina cadastrada com id=" + resultVac.next().getId());
                 } else {
                     System.out.println("Vacina cadastrada (ID gerado automaticamente).");
                 }

            } else if (opc == 3) {
                System.out.print("CPF da Pessoa: ");
                String cpf = sc.nextLine().trim();

                // buscar pessoa
                Query qPessoa = db.query();
                qPessoa.constrain(Pessoa.class);
                qPessoa.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> ps = qPessoa.execute();
                if (ps.isEmpty()) {
                    System.out.println("Pessoa não encontrada. Cadastre a pessoa primeiro.");
                } else {
                    Pessoa p = ps.get(0);

                    // listar vacinas existentes
                    ObjectSet<Vacina> vacs = db.query(Vacina.class);
                    System.out.println("Vacinas disponíveis:");
                    for (Vacina vac : vacs) {
                        System.out.println("  id=" + vac.getId() + " nome=" + vac.getNome());
                    }
                    System.out.print("Id da vacina escolhida: ");
                    int idVac = Integer.parseInt(sc.nextLine().trim());
                    Vacina vacinaEscolhida = null;
                    // Busca a vacina pelo ID escolhido
                    Query qVacina = db.query();
                    qVacina.constrain(Vacina.class);
                    qVacina.descend("id").constrain(idVac);
                    ObjectSet<Vacina> resultVac = qVacina.execute();
                    if(resultVac.hasNext()){
                        vacinaEscolhida = resultVac.next();
                    }


                    if (vacinaEscolhida == null) {
                        System.out.println("Vacina não encontrada.");
                    } else {
                        System.out.print("Data da vacinação (YYYY-MM-DD): ");
                        String data = sc.nextLine().trim();
                        // int idVacn = proximoIdVacinacao(db); // REMOVIDO
                        // Passa 0 como ID inicial, ControleID irá gerar o ID correto
                        Vacinacao v = new Vacinacao(0, data, p, vacinaEscolhida);

                        // relacionar
                        p.adicionarVacinacao(v);
                        vacinaEscolhida.adicionarVacinacao(v);

                        db.store(v); // Trigger do ControleID atuará aqui para definir o ID de Vacinacao
                        db.store(p); // Atualiza Pessoa com a nova Vacinacao na lista
                        db.store(vacinaEscolhida); // Atualiza Vacina com a nova Vacinacao na lista
                        db.commit(); // Salva tudo e atualiza os contadores de ID
                        System.out.println("Vacinacao cadastrada com id=" + v.getId()); // Agora v tem o ID gerado
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            ex.printStackTrace(); // Ajuda a depurar
        } finally {
            Db4oUtil.fecharDB(db);
            sc.close();
        }
    }
}