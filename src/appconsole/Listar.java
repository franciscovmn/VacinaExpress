package appconsole;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;

public class Listar {
    public static void main(String[] args) {
        ObjectContainer db = Util.conectarBanco();
        try {
            System.out.println("---- Pessoas ----");
            ObjectSet<Pessoa> pessoas = db.query(Pessoa.class);
            if (pessoas.isEmpty()) {
                System.out.println("Nenhuma pessoa cadastrada.");
            } else {
                for (Pessoa p : pessoas) {
                    System.out.println(p);
                    if (!p.getListaVacinacao().isEmpty()) {
                        System.out.println("   Vacinações:");
                        for (Vacinacao v : p.getListaVacinacao()) {
                            System.out.println("     -> " + v);
                        }
                    } else {
                         System.out.println("   (Nenhuma vacinação registrada)");
                    }
                }
            }

            System.out.println("\n---- Vacinas ----");
            ObjectSet<Vacina> vacinas = db.query(Vacina.class);
             if (vacinas.isEmpty()) {
                System.out.println("Nenhuma vacina cadastrada.");
            } else {
                for (Vacina vac : vacinas) { 
                    System.out.println(vac);
                     if (!vac.getListaVacinacao().isEmpty()) {
                        System.out.println("   Aplicações:");
                        for (Vacinacao v : vac.getListaVacinacao()) {
                            System.out.println("     -> " + v);
                        }
                    } else {
                         System.out.println("   (Nenhuma aplicação registrada)");
                    }
                }
            }

            System.out.println("\n---- Vacinacoes (todas) ----");
            ObjectSet<Vacinacao> vacinacoes = db.query(Vacinacao.class);
             if (vacinacoes.isEmpty()) {
                System.out.println("Nenhuma vacinação cadastrada.");
            } else {
                for (Vacinacao v : vacinacoes) {
                    System.out.println(v);
                }
            }
        } catch (Exception e) { 
            System.out.println("Erro ao listar dados: " + e.getMessage());
        }
        finally {
        	Util.desconectar();
        }
    }
}