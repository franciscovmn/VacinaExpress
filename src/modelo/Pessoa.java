package modelo;

import java.util.ArrayList;
import java.util.List;

public class Pessoa {
    private String cpf;
    private String dtNascimento; // data como String (enunciado)
    private Localizacao localizacao;
    private List<Vacinacao> listaVacinacao = new ArrayList<>();

    public Pessoa() {}

    public Pessoa(String cpf, String dtNascimento, Localizacao localizacao) {
        this.cpf = cpf;
        this.dtNascimento = dtNascimento;
        this.localizacao = localizacao;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(String dtNascimento) { this.dtNascimento = dtNascimento; }

    public Localizacao getLocalizacao() { return localizacao; }
    public void setLocalizacao(Localizacao localizacao) { this.localizacao = localizacao; }

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
        return "Pessoa{" +
                "cpf='" + cpf + '\'' +
                ", dtNascimento='" + dtNascimento + '\'' +
                ", localizacao=" + localizacao +
                ", #vacinacoes=" + listaVacinacao.size() +
                '}';
    }
}
