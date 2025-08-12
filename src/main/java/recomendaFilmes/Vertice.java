package recomendaFilmes;

import java.util.List;

public class Vertice {
    private String nome;
    private int idTmdb;
    private List<String> generos;
    private List<String> atores;

    public Vertice(String nome, int idTmdb, List<String> generos, List<String> atores) {
        this.nome = nome;
        this.idTmdb = idTmdb;
        this.generos = generos;
        this.atores = atores;
    }

    // Getters
    public String getNome() { return nome; }
    public int getIdTmdb() { return idTmdb; }
    public List<String> getGeneros() { return generos; }
    public List<String> getAtores() { return atores; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vertice)) return false;
        Vertice other = (Vertice) obj;
        return this.nome.equals(other.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}