package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.query.Query;
import com.db4o.ObjectSet;
import modelo.Pessoa;
import modelo.Vacinacao;
import modelo.Vacina;
import util.Db4oUtil;

import java.util.Iterator;
import java.util.Scanner;

public class Apagar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Db4oUtil.abrirDB();

        try {
            System.out.println("=== APAGAR ===");
            System.out.println("1 - Apagar pessoa por CPF (apaga vacinacoes relacionadas)");
            System.out.println("2 - Apagar vacina por id (desvincula/apaga vacinacoes relacionadas)");
            System.out.println("3 - Apagar vacinação por id");
            System.out.print("Escolha: ");
            int opc = Integer.parseInt(sc.nextLine().trim());

            if (opc == 1) {
                System.out.print("CPF: ");
                String cpf = sc.nextLine().trim();
                Query q = db.query();
                q.constrain(Pessoa.class);
                q.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> res = q.execute();
                if (res.isEmpty()) {
                    System.out.println("Pessoa não encontrada.");
                } else {
                    Pessoa p = res.get(0);
                    // apagar vacinacoes relacionadas
                    for (Iterator<Vacinacao> it = p.getListaVacinacao().iterator(); it.hasNext();) {
                        Vacinacao vac = it.next();
                        // remover da vacina
                        if (vac.getVacina() != null) {
                            vac.getVacina().removerVacinacao(vac);
                            db.store(vac.getVacina());
                        }
                        db.delete(vac);
                        it.remove();
                    }
                    db.delete(p);
                    db.commit();
                    System.out.println("Pessoa e vacinacoes relacionadas apagadas.");
                }
            } else if (opc == 2) {
                System.out.print("Id da vacina: ");
                int idVac = Integer.parseInt(sc.nextLine().trim());
                Query q = db.query();
                q.constrain(Vacina.class);
                q.descend("id").constrain(idVac);
                ObjectSet<Vacina> res = q.execute();
                if (res.isEmpty()) {
                    System.out.println("Vacina não encontrada.");
                } else {
                    Vacina vac = res.get(0);
                    // apagar ou desvincular vacinacoes
                    for (Iterator<Vacinacao> it = vac.getListaVacinacao().iterator(); it.hasNext();) {
                        Vacinacao v = it.next();
                        if (v.getPessoa() != null) {
                            v.getPessoa().removerVacinacao(v);
                            db.store(v.getPessoa());
                        }
                        db.delete(v);
                        it.remove();
                    }
                    db.delete(vac);
                    db.commit();
                    System.out.println("Vacina e vacinacoes relacionadas apagadas.");
                }
            } else if (opc == 3) {
                System.out.print("Id da vacinação: ");
                int idV = Integer.parseInt(sc.nextLine().trim());
                Query q = db.query();
                q.constrain(Vacinacao.class);
                q.descend("id").constrain(idV);
                ObjectSet<Vacinacao> res = q.execute();
                if (res.isEmpty()) {
                    System.out.println("Vacinacao não encontrada.");
                } else {
                    Vacinacao v = res.get(0);
                    if (v.getPessoa() != null) {
                        v.getPessoa().removerVacinacao(v);
                        db.store(v.getPessoa());
                    }
                    if (v.getVacina() != null) {
                        v.getVacina().removerVacinacao(v);
                        db.store(v.getVacina());
                    }
                    db.delete(v);
                    db.commit();
                    System.out.println("Vacinacao apagada.");
                }
            } else {
                System.out.println("Opção inválida.");
            }
        } finally {
            Db4oUtil.fecharDB(db);
            sc.close();
        }
    }
}
