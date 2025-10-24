package appconsole;

import java.util.Scanner; 
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;



public class Apagar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = null; 

        try {
            db = Util.conectarBanco();

            System.out.println("=== APAGAR ===");
            System.out.println("1 - Apagar pessoa por CPF (apaga vacinacoes relacionadas)");
            System.out.println("2 - Apagar vacina por id (desvincula/apaga vacinacoes relacionadas)");
            System.out.println("3 - Apagar vacinação por id");
            System.out.print("Escolha: ");
            int opc = 0;
            try {
                opc = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                if (db != null) Util.desconectar();
                sc.close();
                return;
            }


            if (opc == 1) {
                System.out.print("CPF: ");
                String cpf = sc.nextLine().trim();
                Query qPessoa = db.query();
                qPessoa.constrain(Pessoa.class);
                qPessoa.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> resPessoa = qPessoa.execute();

                if (resPessoa.isEmpty()) {
                    System.out.println("Pessoa com CPF " + cpf + " não encontrada.");
                } else {
                    Pessoa p = resPessoa.get(0);

                    System.out.println("Buscando vacinações relacionadas à pessoa " + p.getCpf() + "...");
                    Query qVacinacoes = db.query();
                    qVacinacoes.constrain(Vacinacao.class);
                    qVacinacoes.descend("pessoa").constrain(p); 
                    ObjectSet<Vacinacao> vacinacoesParaApagar = qVacinacoes.execute();

                    System.out.println(vacinacoesParaApagar.size() + " vacinações encontradas para remover.");

                    for (Vacinacao vac : vacinacoesParaApagar) {
                        System.out.println("  Removendo Vacinacao id=" + vac.getId());
                        Vacina vacinaAssociada = vac.getVacina();
                        if (vacinaAssociada != null) {
                            vacinaAssociada.removerVacinacao(vac);
                            db.store(vacinaAssociada); 
                        }

                        db.delete(vac); 
                    }

                    System.out.println("Apagando a pessoa...");
                    db.delete(p);
                    db.commit();
                    System.out.println("Pessoa e vacinações relacionadas apagadas com sucesso.");
                }
            } else if (opc == 2) {
                System.out.print("Id da vacina: ");
                int idVac = 0;
                try {
                    idVac = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e){
                    System.out.println("ID da vacina inválido.");
                    return; 
                }
                Query qVacina = db.query();
                qVacina.constrain(Vacina.class);
                qVacina.descend("id").constrain(idVac);
                ObjectSet<Vacina> resVacina = qVacina.execute();

                if (resVacina.isEmpty()) {
                    System.out.println("Vacina com id=" + idVac + " não encontrada.");
                } else {
                    Vacina vac = resVacina.get(0);

                    System.out.println("Buscando vacinações relacionadas à vacina " + vac.getNome() + " (id=" + vac.getId() + ")...");
                    Query qVacinacoes = db.query();
                    qVacinacoes.constrain(Vacinacao.class);
                    qVacinacoes.descend("vacina").constrain(vac);
                    ObjectSet<Vacinacao> vacinacoesParaApagar = qVacinacoes.execute();

                    System.out.println(vacinacoesParaApagar.size() + " vacinações encontradas para remover.");

                    for (Vacinacao v : vacinacoesParaApagar) {
                        System.out.println("  Removendo Vacinacao id=" + v.getId());
                        Pessoa pessoaAssociada = v.getPessoa();
                        if (pessoaAssociada != null) {
                            pessoaAssociada.removerVacinacao(v);
                            db.store(pessoaAssociada);
                        }

                        db.delete(v); 
                    }

                     System.out.println("Apagando a vacina...");
                    db.delete(vac);
                    db.commit(); 
                    System.out.println("Vacina e vacinações relacionadas apagadas com sucesso.");
                }
            } else if (opc == 3) {
                System.out.print("Id da vacinação: ");
                int idV = 0;
                try {
                    idV = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e){
                   System.out.println("ID da vacinação inválido.");
                   return; 
                }
                Query q = db.query();
                q.constrain(Vacinacao.class);
                q.descend("id").constrain(idV);
                ObjectSet<Vacinacao> res = q.execute();
                if (res.isEmpty()) {
                    System.out.println("Vacinacao com id=" + idV + " não encontrada.");
                } else {
                    Vacinacao v = res.get(0);
                     System.out.println("Removendo vacinação: " + v);
                    Pessoa p = v.getPessoa();
                    if (p != null) {
                        p.removerVacinacao(v);
                        db.store(p);
                    }
                    Vacina vac = v.getVacina();
                    if (vac != null) {
                        vac.removerVacinacao(v);
                        db.store(vac);
                    }
                    db.delete(v);
                    db.commit();
                    System.out.println("Vacinacao apagada com sucesso.");
                }
            } else {
                System.out.println("Opção inválida.");
            }
        } catch (Exception e) {
             System.out.println("Ocorreu um erro durante a operação no banco de dados: " + e.getMessage());

        } finally {
             if (db != null) { 
                Util.desconectar();
             }
            sc.close(); 
        }
    }
}