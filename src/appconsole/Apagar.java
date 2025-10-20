package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.query.Query;
import com.db4o.ObjectSet;
import modelo.Pessoa;
import modelo.Vacinacao;
import modelo.Vacina;
import util.Db4oUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Apagar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.println("=== APAGAR ===");
            System.out.println("1 - Apagar pessoa por CPF (apaga vacinacoes relacionadas)");
            System.out.println("2 - Apagar vacina por id (desvincula/apaga vacinacoes relacionadas)");
            System.out.println("3 - Apagar vacinação por id");
            System.out.print("Escolha: ");
            int opc = Integer.parseInt(sc.nextLine().trim());

            // abrir DB (ajuste o método se necessário: abrirDB() ou getInstance())
            ObjectContainer db = Db4oUtil.abrirDB();
            try {
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
                        // apagar vacinacoes relacionadas SEM apagar objetos Vacina
                        List<Vacinacao> toRemove = new ArrayList<>(p.getListaVacinacao());
                        for (Vacinacao vac : toRemove) {
                            // remover referência na Vacina, mas NÃO deletar a Vacina
                            if (vac.getVacina() != null) {
                                vac.getVacina().removerVacinacao(vac);
                                db.store(vac.getVacina());
                            }
                            // remover referência na Pessoa (a lista será atualizada)
                            p.removerVacinacao(vac);
                            db.delete(vac);
                        }
                        // apagar a pessoa
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
                        // apagar ou desvincular vacinacoes relacionadas
                        List<Vacinacao> toRemove = new ArrayList<>(vac.getListaVacinacao());
                        for (Vacinacao v : toRemove) {
                            if (v.getPessoa() != null) {
                                v.getPessoa().removerVacinacao(v);
                                db.store(v.getPessoa());
                            }
                            vac.removerVacinacao(v);
                            db.delete(v);
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
            }
        } finally {
            sc.close();
        }
    }
}
