package recomendaFilmes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Grafo {
    private final List<Vertice> vertices;
    private final List<Aresta> arestas;
    private final Map<String, Vertice> indiceVertices;
    private final Map<Vertice, List<Aresta>> adjacencias;

    public Grafo() {
        this.vertices = new ArrayList<>();
        this.arestas = new ArrayList<>();
        this.indiceVertices = new HashMap<>();
        this.adjacencias = new HashMap<>();
    }

    public void adicionarVertice(Vertice vertice) {
        if (!indiceVertices.containsKey(vertice.getNome())) {
            vertices.add(vertice);
            indiceVertices.put(vertice.getNome(), vertice);
            adjacencias.put(vertice, new ArrayList<>());
        }
    }

    public void adicionarAresta(Vertice origem, Vertice destino, double peso) {
        if (peso > 0) {
            Aresta aresta = new Aresta(origem, destino, peso);
            arestas.add(aresta);
            adjacencias.get(origem).add(aresta);
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
                adicionarAresta(v2, v1, similaridade);
            }
        }
    }

    private double calcularSimilaridade(Vertice v1, Vertice v2) {
        boolean generosEmComum = !Collections.disjoint(v1.getGeneros(), v2.getGeneros());
        
        boolean atorEmComum = Objects.equals(v1.getAtorPrincipal(), v2.getAtorPrincipal());
        boolean diretorEmComum = Objects.equals(v1.getDiretor(), v2.getDiretor());
        boolean produtoraEmComum = Objects.equals(v1.getProdutora(), v2.getProdutora());
        boolean outraCaracteristicaEmComum = atorEmComum || diretorEmComum || produtoraEmComum;

        if (!generosEmComum || !outraCaracteristicaEmComum) {
            return 0.0;
        }

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

        return (0.40 * similaridadeGeneros) +
               (0.25 * similaridadeAtor) +
               (0.25 * similaridadeDiretor) +
               (0.10 * similaridadeProdutora);
    }

    public Vertice getVerticePorNome(String nome) {
        return indiceVertices.get(nome);
    }
    
    public List<Vertice> getVertices() {
        return new ArrayList<>(vertices);
    }
    
    public List<Aresta> getArestas() {
        return new ArrayList<>(arestas);
    }
    
    public List<Vertice> getVizinhos(Vertice vertice) {
        List<Vertice> vizinhos = new ArrayList<>();
        List<Aresta> arestasDoVertice = adjacencias.get(vertice);
        
        if (arestasDoVertice != null) {
            for (Aresta aresta : arestasDoVertice) {
                if (aresta.getPeso() > 0) {
                    vizinhos.add(aresta.getDestino());
                }
            }
        }
        
        return vizinhos;
    }
    
    public List<Aresta> getArestasDoVertice(Vertice vertice) {
        List<Aresta> resultado = adjacencias.get(vertice);
        return resultado != null ? new ArrayList<>(resultado) : new ArrayList<>();
    }
    
    public double getDensidade() {
        int n = vertices.size();
        if (n <= 1) return 0;
        
        int maxArestas = n * (n - 1);
        return (double) arestas.size() / maxArestas;
    }
    
    public double getGrauMedio() {
        if (vertices.isEmpty()) return 0;
        
        double somaGraus = 0;
        for (Vertice v : vertices) {
            somaGraus += getVizinhos(v).size();
        }
        
        return somaGraus / vertices.size();
    }
    
    public Map<String, Integer> getDistribuicaoGeneros() {
        Map<String, Integer> distribuicao = new HashMap<>();
        
        for (Vertice v : vertices) {
            for (String genero : v.getGeneros()) {
                distribuicao.put(genero, distribuicao.getOrDefault(genero, 0) + 1);
            }
        }
        
        return distribuicao;
    }
    
    public Map<String, Integer> getDistribuicaoDiretores() {
        Map<String, Integer> distribuicao = new HashMap<>();
        
        for (Vertice v : vertices) {
            String diretor = v.getDiretor();
            if (diretor != null) {
                distribuicao.put(diretor, distribuicao.getOrDefault(diretor, 0) + 1);
            }
        }
        
        return distribuicao;
    }
    
    public Map<String, Integer> getDistribuicaoAtores() {
        Map<String, Integer> distribuicao = new HashMap<>();
        
        for (Vertice v : vertices) {
            String ator = v.getAtorPrincipal();
            if (ator != null) {
                distribuicao.put(ator, distribuicao.getOrDefault(ator, 0) + 1);
            }
        }
        
        return distribuicao;
    }
    
    public List<Vertice> getFilmesSemConexoes() {
        List<Vertice> isolados = new ArrayList<>();
        
        for (Vertice v : vertices) {
            if (getVizinhos(v).isEmpty()) {
                isolados.add(v);
            }
        }
        
        return isolados;
    }
    
    public List<Map.Entry<Vertice, Integer>> getFilmesMaisConectados(int topN) {
        Map<Vertice, Integer> graus = new HashMap<>();
        
        for (Vertice v : vertices) {
            graus.put(v, getVizinhos(v).size());
        }
        
        return graus.entrySet().stream()
                .sorted(Map.Entry.<Vertice, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}