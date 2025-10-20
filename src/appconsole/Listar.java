package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;
import util.Db4oUtil;

public class Listar {
    public static void main(String[] args) {
        ObjectContainer db = Db4oUtil.abrirDB();
        try {
            System.out.println("---- Pessoas ----");
            ObjectSet<Pessoa> pessoas = db.query(Pessoa.class);
            for (Pessoa p : pessoas) {
                System.out.println(p);
                for (Vacinacao v : p.getListaVacinacao()) {
                    System.out.println("   -> " + v);
                }
            }

            System.out.println("\n---- Vacinas ----");
            ObjectSet<Vacina> vacinas = db.query(Vacina.class);
            for (Vacina v : vacinas) {
                System.out.println(v);
                for (Vacinacao vac : v.getListaVacinacao()) {
                    System.out.println("   -> " + vac);
                }
            }

            System.out.println("\n---- Vacinacoes (todas) ----");
            ObjectSet<Vacinacao> vacinacoes = db.query(Vacinacao.class);
            for (Vacinacao v : vacinacoes) {
                System.out.println(v);
            }
        } finally {
            Db4oUtil.fecharDB(db);
        }
    }
}
