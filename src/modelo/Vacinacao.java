package modelo;

public class Vacinacao {
    private int id; // id sequencial (autogerado nos apps)
    private String data; // formato string
    private Pessoa pessoa;
    private Vacina vacina;

    public Vacinacao() {}

    public Vacinacao(int id, String data, Pessoa pessoa, Vacina vacina) {
        this.id = id;
        this.data = data;
        this.pessoa = pessoa;
        this.vacina = vacina;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }

    public Vacina getVacina() { return vacina; }
    public void setVacina(Vacina vacina) { this.vacina = vacina; }

    @Override
    public String toString() {
        return "Vacinacao{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", pessoaCpf=" + (pessoa != null ? pessoa.getCpf() : "null") +
                ", vacina=" + (vacina != null ? vacina.getNome() : "null") +
                '}';
    }
}
