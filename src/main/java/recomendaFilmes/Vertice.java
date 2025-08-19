<<<<<<< HEAD
// Conteúdo para Vertice.java

=======
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
package recomendaFilmes;

import java.util.List;

public class Vertice {
    private String nome;
    private int idTmdb;
    private List<String> generos;
<<<<<<< HEAD
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
=======
    private List<String> atores;

    public Vertice(String nome, int idTmdb, List<String> generos, List<String> atores) {
        this.nome = nome;
        this.idTmdb = idTmdb;
        this.generos = generos;
        this.atores = atores;
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
    }

    // Getters
    public String getNome() { return nome; }
    public int getIdTmdb() { return idTmdb; }
    public List<String> getGeneros() { return generos; }
<<<<<<< HEAD
    public String getAtorPrincipal() { return atorPrincipal; }
    public String getDiretor() { return diretor; }
    public String getProdutora() { return produtora; }

=======
    public List<String> getAtores() { return atores; }
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
<<<<<<< HEAD
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertice vertice = (Vertice) obj;
        return nome.equals(vertice.nome);
=======
        if (!(obj instanceof Vertice)) return false;
        Vertice other = (Vertice) obj;
        return this.nome.equals(other.nome);
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}