// Conteúdo para Vertice.java

package recomendaFilmes;

import java.util.List;

public class Vertice {
    private String nome;
    private int idTmdb;
    private List<String> generos;
    private String atorPrincipal;
    private String diretor;
    private String produtora;

    public Vertice(String nome, int idTmdb, List<String> generos, String atorPrincipal, String diretor, String produtora) {
        this.nome = nome;
        this.idTmdb = idTmdb;
        this.generos = generos;
        this.atorPrincipal = atorPrincipal;
        this.diretor = diretor;
        this.produtora = produtora;
    }

    // Getters
    public String getNome() { return nome; }
    public int getIdTmdb() { return idTmdb; }
    public List<String> getGeneros() { return generos; }
    public String getAtorPrincipal() { return atorPrincipal; }
    public String getDiretor() { return diretor; }
    public String getProdutora() { return produtora; }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertice vertice = (Vertice) obj;
        return nome.equals(vertice.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}