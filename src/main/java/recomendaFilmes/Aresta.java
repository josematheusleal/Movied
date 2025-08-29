package recomendaFilmes;

public class Aresta {
    private final Vertice origem;
    private final Vertice destino;
    private final double peso;

    public Aresta(Vertice origem, Vertice destino, double peso) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
    }

    public Vertice getOrigem() { return origem; }
    public Vertice getDestino() { return destino; }
    public double getPeso() { return peso; }
}