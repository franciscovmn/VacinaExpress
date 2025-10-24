package appconsole;

import java.util.Scanner;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet; // Import necessário
import com.db4o.query.Query; // Import necessário

import modelo.Localizacao;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;

public class Cadastrar {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Util.conectarBanco(); 

        try {
            // garantir vacinas iniciais
            if (db.query(Vacina.class).isEmpty()) {
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
                    db.store(p); 
                    db.commit();
                    System.out.println("Pessoa cadastrada com sucesso.");
                }
            } else if (opc == 2) {
                System.out.print("Nome da Vacina: ");
                String nome = sc.nextLine().trim();
                Vacina v = new Vacina(0, nome);
                db.store(v); 
                db.commit(); 

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
                        Vacinacao v = new Vacinacao(0, data, p, vacinaEscolhida);

                        // relacionar
                        p.adicionarVacinacao(v);
                        vacinaEscolhida.adicionarVacinacao(v);

                        db.store(v); 
                        db.store(p);
                        db.store(vacinaEscolhida); 
                        db.commit(); 
                        System.out.println("Vacinacao cadastrada com id=" + v.getId());
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            ex.printStackTrace(); //depurar
        } finally {
        	Util.desconectar();
            sc.close();
        }
    }
}