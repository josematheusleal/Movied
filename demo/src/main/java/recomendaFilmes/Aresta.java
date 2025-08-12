package recomendaFilmes;

public class Aresta {
    private Vertice origem;
    private Vertice destino;
    private double peso; // Similaridade entre 0 e 1

    public Aresta(Vertice origem, Vertice destino, double peso) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
    }

    // Getters
    public Vertice getOrigem() { return origem; }
    public Vertice getDestino() { return destino; }
    public double getPeso() { return peso; }
}