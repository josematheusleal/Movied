// Conteúdo para Grafo.java

package recomendaFilmes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        if (peso > 0) {
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
                
                // Adiciona um print para depuração, se desejar
                if (similaridade > 0) {
                    // System.out.println("Aresta entre " + v1.getNome() + " e " + v2.getNome() + ": " + similaridade);
                }

                adicionarAresta(v1, v2, similaridade);
                adicionarAresta(v2, v1, similaridade); // Grafo não direcionado
            }
        }
    }

    /**
     * CORRIGIDO: A lógica foi flexibilizada. A similaridade só será 0 se não
     * houver NENHUMA característica em comum. A regra do "E" foi removida.
     */
    private double calcularSimilaridade(Vertice v1, Vertice v2) {
        // --- Passo 1: Verificar se há PELO MENOS UMA característica em comum ---
        boolean generosEmComum = !Collections.disjoint(v1.getGeneros(), v2.getGeneros());
        boolean atorEmComum = Objects.equals(v1.getAtorPrincipal(), v2.getAtorPrincipal());
        boolean diretorEmComum = Objects.equals(v1.getDiretor(), v2.getDiretor());
        boolean produtoraEmComum = Objects.equals(v1.getProdutora(), v2.getProdutora());

        // Se não houver absolutamente NADA em comum, a similaridade é 0.
        if (!generosEmComum && !atorEmComum && !diretorEmComum && !produtoraEmComum) {
            return 0.0;
        }

        // --- Passo 2: Se houver algo em comum, calcular o score ponderado ---

        long nGenerosComuns = v1.getGeneros().stream()
                .filter(v2.getGeneros()::contains)
                .count();
        double similaridadeGeneros = 0;
        int maxGeneros = Math.max(v1.getGeneros().size(), v2.getGeneros().size());
        if (maxGeneros > 0) {
            similaridadeGeneros = (double) nGenerosComuns / maxGeneros;
        }

        double similaridadeAtor = atorEmComum ? 1.0 : 0.0;
        double similaridadeDiretor = diretorEmComum ? 1.0 : 0.0;
        double similaridadeProdutora = produtoraEmComum ? 1.0 : 0.0;

        // O cálculo ponderado permanece o mesmo
        return (0.40 * similaridadeGeneros) +
               (0.25 * similaridadeAtor) +
               (0.25 * similaridadeDiretor) +
               (0.10 * similaridadeProdutora);
    }

    public Vertice getVerticePorNome(String nome) {
        return indiceVertices.get(nome);
    }
}