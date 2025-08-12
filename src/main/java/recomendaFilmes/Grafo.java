package recomendaFilmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Grafo {
    private List<Vertice> vertices;
    private List<Aresta> arestas;
    private Map<String, Vertice> indiceVertices;

    public Grafo() {
        this.vertices = new ArrayList<>();
        this.arestas = new ArrayList<>();
        this.indiceVertices = new HashMap<>();
    }

    public void adicionarVertice(Vertice vertice) {
        if (!indiceVertices.containsKey(vertice.getNome())) {
            vertices.add(vertice);
            indiceVertices.put(vertice.getNome(), vertice);
        }
    }

    public void adicionarAresta(Vertice origem, Vertice destino, double peso) {
        if (peso > 0) { // Só cria arestas com similaridade positiva
            arestas.add(new Aresta(origem, destino, peso));
        }
    }

    public List<Vertice> recomendarFilmes(Vertice filmeBase, int quantidade) {
        return arestas.stream()
                .filter(a -> a.getOrigem().equals(filmeBase))
                .sorted((a1, a2) -> Double.compare(a2.getPeso(), a1.getPeso()))
                .limit(quantidade)
                .map(Aresta::getDestino)
                .collect(Collectors.toList());
    }

    public void gerarArestasSimilaridade() {
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertice v1 = vertices.get(i);
                Vertice v2 = vertices.get(j);
                double similaridade = calcularSimilaridade(v1, v2);
                adicionarAresta(v1, v2, similaridade);
                adicionarAresta(v2, v1, similaridade); // Grafo não direcionado
            }
        }
    }

    private double calcularSimilaridade(Vertice v1, Vertice v2) {
        // Similaridade de gêneros (peso 60%)
        long generosComuns = v1.getGeneros().stream()
                .filter(v2.getGeneros()::contains)
                .count();
        double similaridadeGeneros = (double) generosComuns / 
                Math.max(v1.getGeneros().size(), v2.getGeneros().size());

        // Similaridade de atores (peso 40%)
        long atoresComuns = v1.getAtores().stream()
                .filter(v2.getAtores()::contains)
                .count();
        double similaridadeAtores = (double) atoresComuns / 
                Math.max(v1.getAtores().size(), v2.getAtores().size());

        return (0.6 * similaridadeGeneros) + (0.4 * similaridadeAtores);
    }

    public Vertice getVerticePorNome(String nome) {
        return indiceVertices.get(nome);
    }
}