package appconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

import modelo.Pessoa;
import modelo.Vacinacao;

public class Consultar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Util.conectarBanco();
        try {
            System.out.println("=== CONSULTAR (SODA) ===");
            System.out.println("1 - Vacinações na data X");
            System.out.println("2 - Pessoas vacinadas na data X");
            System.out.println("3 - Pessoas com pelo menos N vacinações");
            System.out.print("Escolha: ");
            int opc = Integer.parseInt(sc.nextLine().trim());

            if (opc == 1) {
                System.out.print("Data (YYYY-MM-DD): ");
                String dataX = sc.nextLine().trim();
                Query q1 = db.query();
                q1.constrain(Vacinacao.class);
                q1.descend("data").constrain(dataX);
                ObjectSet<Vacinacao> res1 = q1.execute();
                System.out.println("Vacinacoes na data " + dataX + ":");
                for (Vacinacao v : res1) {
					System.out.println(v);
				}
            } else if (opc == 2) {
                System.out.print("Data (YYYY-MM-DD): ");
                String dataX = sc.nextLine().trim();
                Query q2 = db.query();
                q2.constrain(Vacinacao.class);
                q2.descend("data").constrain(dataX);
                ObjectSet<Vacinacao> res2 = q2.execute();
                List<String> cpfs = new ArrayList<>();
                System.out.println("Pessoas vacinadas em " + dataX + ":");
                for (Vacinacao vac : res2) {
                    if (vac.getPessoa() != null && !cpfs.contains(vac.getPessoa().getCpf())) {
                        cpfs.add(vac.getPessoa().getCpf());
                        System.out.println(vac.getPessoa());
                    }
                }
            } else if (opc == 3) {
                System.out.print("N (quantas vacinações no mínimo): ");
                int N = Integer.parseInt(sc.nextLine().trim());
                ObjectSet<Pessoa> pessoas = db.query(Pessoa.class);
                System.out.println("Pessoas com >= " + N + " vacinações:");
                for (Pessoa p : pessoas) {
                    if (p.getListaVacinacao().size() >= N) {
                        System.out.println(p + " -> #vacinacoes=" + p.getListaVacinacao().size());
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }
        } finally {
        	Util.desconectar();
            sc.close();
        }
    }
}
