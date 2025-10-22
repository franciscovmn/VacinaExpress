package appconsole;

import java.util.Scanner;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

import modelo.Pessoa;
import modelo.Vacina;
import modelo.Vacinacao;

public class Alterar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectContainer db = Util.conectarBanco();

        try {
            System.out.println("=== ALTERAR ===");
            System.out.println("1 - Alterar dados da pessoa");
            System.out.println("2 - Trocar vacina de uma vacinação (por id de vacinação)");
            System.out.println("3 - Remover vacinação de uma pessoa (apaga a vacinação)");
            System.out.print("Escolha: ");
            int opc = Integer.parseInt(sc.nextLine().trim());

            if (opc == 1) {
                System.out.print("CPF da pessoa a alterar: ");
                String cpf = sc.nextLine().trim();
                Query q = db.query();
                q.constrain(Pessoa.class);
                q.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> res = q.execute();
                if (res.isEmpty()) {
                    System.out.println("Pessoa não encontrada.");
                } else {
                    Pessoa p = res.get(0);
                    System.out.println("Pessoa atual: " + p);
                    System.out.print("Novo dtNascimento (enter para manter): ");
                    String novoDt = sc.nextLine().trim();
                    if (!novoDt.isEmpty()) {
						p.setDtNascimento(novoDt);
					}
                    System.out.print("Nova latitude (enter para manter): ");
                    String latS = sc.nextLine().trim();
                    if (!latS.isEmpty()) {
						p.getLocalizacao().setLatitude(Double.parseDouble(latS));
					}
                    System.out.print("Nova longitude (enter para manter): ");
                    String lonS = sc.nextLine().trim();
                    if (!lonS.isEmpty()) {
						p.getLocalizacao().setLongitude(Double.parseDouble(lonS));
					}

                    db.store(p);
                    db.commit();
                    System.out.println("Pessoa atualizada.");
                }
            } else if (opc == 2) {
                System.out.print("Id da vacinação a alterar: ");
                int idV = Integer.parseInt(sc.nextLine().trim());
                Query qv = db.query();
                qv.constrain(Vacinacao.class);
                qv.descend("id").constrain(idV);
                ObjectSet<Vacinacao> rv = qv.execute();
                if (rv.isEmpty()) {
                    System.out.println("Vacinacao não encontrada.");
                } else {
                    Vacinacao vac = rv.get(0);
                    System.out.println("Vacinacao atual: " + vac);
                    System.out.println("Vacinas disponíveis:");
                    ObjectSet<Vacina> vacs = db.query(Vacina.class);
                    for (Vacina v : vacs) {
						System.out.println(" id=" + v.getId() + " nome=" + v.getNome());
					}
                    System.out.print("Id da nova vacina: ");
                    int novoIdVac = Integer.parseInt(sc.nextLine().trim());
                    Vacina nova = null;
                    for (Vacina v : vacs) {
						if (v.getId() == novoIdVac) {
							nova = v;
						}
					}
                    if (nova == null) {
                        System.out.println("Vacina não encontrada.");
                    } else {
                        // atualizar relacionamentos
                        if (vac.getVacina() != null) {
                            vac.getVacina().removerVacinacao(vac);
                            db.store(vac.getVacina());
                        }
                        vac.setVacina(nova);
                        nova.adicionarVacinacao(vac);
                        db.store(vac);
                        db.store(nova);
                        db.commit();
                        System.out.println("Vacina da vacinação atualizada.");
                    }
                }
            } else if (opc == 3) {
                System.out.print("CPF da pessoa: ");
                String cpf = sc.nextLine().trim();
                System.out.print("Id da vacinação a remover: ");
                int idRem = Integer.parseInt(sc.nextLine().trim());

                Query q = db.query();
                q.constrain(Pessoa.class);
                q.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> rp = q.execute();
                if (rp.isEmpty()) {
                    System.out.println("Pessoa não encontrada.");
                } else {
                    Pessoa p = rp.get(0);
                    Vacinacao alvo = null;
                    for (Vacinacao v : p.getListaVacinacao()) {
                        if (v.getId() == idRem) {
                            alvo = v;
                            break;
                        }
                    }
                    if (alvo == null) {
                        System.out.println("Vacinacao não encontrada na pessoa.");
                    } else {
                        // remover da vacina também
                        if (alvo.getVacina() != null) {
                            alvo.getVacina().removerVacinacao(alvo);
                            db.store(alvo.getVacina());
                        }
                        p.removerVacinacao(alvo);
                        db.delete(alvo); // remove do DB
                        db.store(p);
                        db.commit();
                        System.out.println("Vacinacao removida e apagada do DB.");
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
