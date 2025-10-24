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
                    if (p.getLocalizacao() == null) {
                        System.out.println("Atenção: Pessoa sem localização cadastrada. Latitude/Longitude não podem ser alteradas.");
                    } else {
                        System.out.print("Nova latitude (enter para manter): ");
                        String latS = sc.nextLine().trim();
                        if (!latS.isEmpty()) {
                            try {
                                p.getLocalizacao().setLatitude(Double.parseDouble(latS));
                            } catch (NumberFormatException e) {
                                System.out.println("Latitude inválida, mantendo o valor anterior.");
                            }
                        }
                        System.out.print("Nova longitude (enter para manter): ");
                        String lonS = sc.nextLine().trim();
                        if (!lonS.isEmpty()) {
                             try {
                                p.getLocalizacao().setLongitude(Double.parseDouble(lonS));
                            } catch (NumberFormatException e) {
                                System.out.println("Longitude inválida, mantendo o valor anterior.");
                            }
                        }
                    }

                    db.store(p);
                    db.commit();
                    System.out.println("Pessoa atualizada.");
                }
            } else if (opc == 2) {
                System.out.print("Id da vacinação a alterar: ");
                int idV = 0;
                try {
                    idV = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e){
                    System.out.println("ID inválido.");
                    return; 
                }

                Query qv = db.query();
                qv.constrain(Vacinacao.class);
                qv.descend("id").constrain(idV);
                ObjectSet<Vacinacao> rv = qv.execute();

                if (rv.isEmpty()) {
                    System.out.println("Vacinacao com id=" + idV + " não encontrada.");
                } else {
                    Vacinacao vac = rv.get(0);
                    System.out.println("Vacinacao atual: " + vac);

                    System.out.println("Vacinas disponíveis:");
                    ObjectSet<Vacina> vacsDisponiveis = db.query(Vacina.class);
                    if (vacsDisponiveis.isEmpty()){
                        System.out.println("Nenhuma vacina cadastrada para seleção.");
                        return;
                    }
                    for (Vacina vDisp : vacsDisponiveis) {
						System.out.println(" id=" + vDisp.getId() + " nome=" + vDisp.getNome());
					}

                    System.out.print("Id da nova vacina: ");
                    int novoIdVac = 0;
                    try {
                        novoIdVac = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("ID da nova vacina inválido.");
                        return;
                    }


                    Query qNovaVacina = db.query();
                    qNovaVacina.constrain(Vacina.class);
                    qNovaVacina.descend("id").constrain(novoIdVac);
                    ObjectSet<Vacina> resultNovaVacina = qNovaVacina.execute();

                    if (resultNovaVacina.isEmpty()) {
                        System.out.println("Vacina com id=" + novoIdVac + " não encontrada.");
                    } else {
                        Vacina nova = resultNovaVacina.next();

                        if (vac.getVacina() != null && vac.getVacina().getId() == nova.getId()) {
                            System.out.println("A vacinação já está com esta vacina. Nenhuma alteração feita.");
                        } else {
                           
                            Vacina antiga = vac.getVacina();
                            if (antiga != null) {
                                antiga.removerVacinacao(vac);
                                db.store(antiga);
                            }
                            vac.setVacina(nova); 
                            nova.adicionarVacinacao(vac); 
                            db.store(vac);
                            db.store(nova);
                            db.commit();
                            System.out.println("Vacina da vacinação atualizada.");
                        }
                    }
                }
            } else if (opc == 3) {
                System.out.print("CPF da pessoa: ");
                String cpf = sc.nextLine().trim();
                System.out.print("Id da vacinação a remover: ");
                int idRem = 0;
                 try {
                    idRem = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e){
                    System.out.println("ID da vacinação inválido.");
                    return; 
                }

                Query qPessoa = db.query();
                qPessoa.constrain(Pessoa.class);
                qPessoa.descend("cpf").constrain(cpf);
                ObjectSet<Pessoa> rp = qPessoa.execute();

                if (rp.isEmpty()) {
                    System.out.println("Pessoa com CPF " + cpf + " não encontrada.");
                } else {
                    Pessoa p = rp.get(0);

                    Query qVacinacao = db.query();
                    qVacinacao.constrain(Vacinacao.class);
                    qVacinacao.descend("id").constrain(idRem);
                    ObjectSet<Vacinacao> rv = qVacinacao.execute();

                    if (rv.isEmpty()) {
                        System.out.println("Vacinacao com id=" + idRem + " não encontrada.");
                    } else {
                        Vacinacao alvo = rv.get(0);

                        if (alvo.getPessoa() == null || !alvo.getPessoa().getCpf().equals(p.getCpf())) {
                             System.out.println("A Vacinacao com id=" + idRem + " não pertence à pessoa com CPF " + cpf + ".");
                        } else {
                            System.out.println("Removendo vacinação: " + alvo + " da pessoa " + p.getCpf());

                            Vacina vacinaAssociada = alvo.getVacina();
                            if (vacinaAssociada != null) {
                                vacinaAssociada.removerVacinacao(alvo);
                                db.store(vacinaAssociada);
                            }

                            p.removerVacinacao(alvo);
                            db.store(p); 

                            db.delete(alvo);
                            db.commit();
                            System.out.println("Vacinacao removida com sucesso.");
                        }
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (Exception ex) {
             System.out.println("Ocorreu um erro inesperado: " + ex.getMessage());
        } finally {
        	Util.desconectar();
            sc.close();
        }
    }
}