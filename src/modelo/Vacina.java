package modelo;

import java.util.ArrayList;
import java.util.List;

public class Vacina {
    private int id;
    private String nome;
    private List<Vacinacao> listaVacinacao = new ArrayList<>();

    public Vacina() {}

    public Vacina(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public List<Vacinacao> getListaVacinacao() { return listaVacinacao; }
    public void setListaVacinacao(List<Vacinacao> listaVacinacao) { this.listaVacinacao = listaVacinacao; }

    public void adicionarVacinacao(Vacinacao v) {
        if (v != null && !listaVacinacao.contains(v)) {
            listaVacinacao.add(v);
        }
    }

    public void removerVacinacao(Vacinacao v) {
        listaVacinacao.remove(v);
    }

    @Override
    public String toString() {
        return "Vacina{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", #vacinacoes=" + listaVacinacao.size() +
                '}';
    }
}
