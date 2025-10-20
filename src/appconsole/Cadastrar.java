package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.query.Query;
import com.db4o.ObjectSet;
import modelo.Localizacao;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;
import util.Db4oUtil;

import java.util.Scanner;

public class Cadastrar {

    private static int proximoIdVacinacao(ObjectContainer db) {
        Query q = db.query();
        q.constrain(Vacinacao.class);
        ObjectSet<Vacinacao> res = q.execute();
        int max = 0;
        for (Vacinacao v : res) if (v.getId() > max) max = v.getId();
        return max + 1;
    }

    private static int proximoIdVacina(ObjectContainer db) {
        Query q = db.query();
        q.constrain(Vacina.class);
        ObjectSet<Vacina> res = q.execute();
        int max = 0;
        for (Vacina v : res) if (v.getId() > max) max = v.getId();
        return max + 1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Db4oUtil.abrirDB();

        try {
            // garantir vacinas iniciais
            if (db.query(Vacina.class).isEmpty()) {
                db.store(new Vacina(proximoIdVacina(db), "CoronaVac"));
                db.store(new Vacina(proximoIdVacina(db), "Pfizer"));
                db.store(new Vacina(proximoIdVacina(db), "AstroVac"));
                db.commit();
                System.out.println("Vacinas iniciais cadastradas.");
            }

            System.out.println("=== CADASTRAR ===");
            System.out.println("1 - Cadastrar Pessoa");
            System.out.println("2 - Cadastrar Vacina (nova)");
            System.out.println("3 - Cadastrar Vacinacao");
            System.out.print("Escolha uma opção: ");
            int opc = Integer.parseInt(sc.nextLine());

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
                int id = proximoIdVacina(db);
                Vacina v = new Vacina(id, nome);
                db.store(v);
                db.commit();
                System.out.println("Vacina cadastrada com id=" + id);
            } else if (opc == 3) {
                System.out.print("CPF da Pessoa: ");
                String cpf = sc.nextLine().trim();

                // buscar pessoa
                Query q = db.query();
                q.constrain(Pessoa.class);
                q.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> ps = q.execute();
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
                    for (Vacina vac : vacs) if (vac.getId() == idVac) vacinaEscolhida = vac;

                    if (vacinaEscolhida == null) {
                        System.out.println("Vacina não encontrada.");
                    } else {
                        System.out.print("Data da vacinação (YYYY-MM-DD): ");
                        String data = sc.nextLine().trim();
                        int idVacn = proximoIdVacinacao(db);
                        Vacinacao v = new Vacinacao(idVacn, data, p, vacinaEscolhida);

                        // relacionar
                        p.adicionarVacinacao(v);
                        vacinaEscolhida.adicionarVacinacao(v);

                        db.store(v);
                        db.store(p);
                        db.store(vacinaEscolhida);
                        db.commit();
                        System.out.println("Vacinacao cadastrada com id=" + idVacn);
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
        } finally {
            Db4oUtil.fecharDB(db);
            sc.close();
        }
    }
}
