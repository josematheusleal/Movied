package recomendaFilmes;

import java.util.List;
import java.util.Objects;

public class Vertice {
    private String nome;
    private int idTmdb;
    private List<String> generos;
    private String atorPrincipal;
    private String diretor;
    private String produtora;
    private List<String> keywords;

    public Vertice(String nome, int idTmdb, List<String> generos, String atorPrincipal, 
                   String diretor, String produtora, List<String> keywords) {
        this.nome = nome;
        this.idTmdb = idTmdb;
        this.generos = generos;
        this.atorPrincipal = atorPrincipal;
        this.diretor = diretor;
        this.produtora = produtora;
        this.keywords = keywords;
    }

    public String getNome() { return nome; }
    public int getIdTmdb() { return idTmdb; }
    public List<String> getGeneros() { return generos; }
    public String getAtorPrincipal() { return atorPrincipal; }
    public String getDiretor() { return diretor; }
    public String getProdutora() { return produtora; }
    public List<String> getKeywords() { return keywords; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertice vertice = (Vertice) o;
        return idTmdb == vertice.idTmdb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTmdb);
    }
}