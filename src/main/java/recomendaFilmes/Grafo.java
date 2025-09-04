package recomendaFilmes;

import java.util.*;
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
                adicionarAresta(v1, v2, similaridade);
                adicionarAresta(v2, v1, similaridade);
            }
        }
    }

    private double calcularSimilaridade(Vertice v1, Vertice v2) {
        boolean atorEmComum = Objects.equals(v1.getAtorPrincipal(), v2.getAtorPrincipal());
        boolean diretorEmComum = Objects.equals(v1.getDiretor(), v2.getDiretor());
        boolean produtoraEmComum = Objects.equals(v1.getProdutora(), v2.getProdutora());
        boolean keywordsEmComum = !Collections.disjoint(v1.getKeywords(), v2.getKeywords());

        if (!atorEmComum && !diretorEmComum && !produtoraEmComum && !keywordsEmComum) {
            return 0.0;
        }

        double pesoDiretor = 0.25;
        double pesoAtor = 0.15;
        double pesoKeywords = 0.40;
        double pesoProdutora = 0.20;

        double similaridadeDiretor = diretorEmComum ? 1.0 : 0.0;
        double similaridadeAtor = atorEmComum ? 1.0 : 0.0;
        double similaridadeKeywords = calcularSimilaridadeDeLista(v1.getKeywords(), v2.getKeywords());
        double similaridadeProdutora = produtoraEmComum ? 1.0 : 0.0;

        return (pesoDiretor * similaridadeDiretor) +
               (pesoAtor * similaridadeAtor) +
               (pesoKeywords * similaridadeKeywords) +
               (pesoProdutora * similaridadeProdutora);
    }

    private double calcularSimilaridadeDeLista(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return 0.0;
        }
        Set<String> set1 = new HashSet<>(list1.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet()));
        Set<String> set2 = new HashSet<>(list2.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet()));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
   public Optional<Vertice> getVerticePorNome(String nome) {
    // Optional.ofNullable cria um Optional que pode conter um valor ou ser vazio (se get(nome) retornar null).
    return Optional.ofNullable(indiceVertices.get(nome));
}

    public List<Vertice> getVertices() {
        return vertices;
    }

    public List<Aresta> getArestas() {
        return arestas;
    }

    /**
     * Record público que descreve o breakdown (componentes) da similaridade entre 2 vértices.
     * weight: valor entre 0.0 e 1.0 já ponderado pelos pesos definidos no grafo.
     */
    public static record SimilarityBreakdown(
            boolean sameActor,
            boolean sameDirector,
            boolean sameProducer,
            int sharedKeywordsCount,
            int totalKeywordsUnion,
            double keywordFraction,         // 0..1
            double actorContribution,       // contribuição numérica (ex: 0.15 se houver mesma ator)
            double directorContribution,    // contribuição numérica (ex: 0.25 se houver mesmo diretor)
            double producerContribution,    // contribuição numérica (ex: 0.20 se houver mesma produtora)
            double keywordsContribution,    // contribuição numérica (fraction * pesoKeywords)
            double weight,
            int sharedGenresCount,
            int totalGenresUnion,
            double genreFraction,
            double genreContribution                 // soma das contribuições (0..1)
    ) {}

    /**
     * Calcula e retorna o breakdown completo entre v1 e v2 usando a mesma fórmula interna que gerarArestasSimilaridade.
     */
    public SimilarityBreakdown calcularBreakdown(Vertice v1, Vertice v2) {
        // pesos do grafo (mesmos usados em calcularSimilaridade)
        double pesoDiretor = 0.15;
        double pesoAtor = 0.15;
        double pesoKeywords = 0.35;
        double pesoProdutora = 0.15;
        double pesoGenero = 0.20;

        boolean sameActor = v1.getAtorPrincipal() != null && v1.getAtorPrincipal().equalsIgnoreCase(v2.getAtorPrincipal());
        boolean sameDirector = v1.getDiretor() != null && v1.getDiretor().equalsIgnoreCase(v2.getDiretor());
        boolean sameProducer = v1.getProdutora() != null && v1.getProdutora().equalsIgnoreCase(v2.getProdutora());

        double genreFraction = calcularSimilaridadeDeLista(v1.getGeneros(), v2.getGeneros());
        Set<String> generos1 = new HashSet<>(v1.getGeneros());
        Set<String> generos2 = new HashSet<>(v2.getGeneros());
        Set<String> generosIntersection = new HashSet<>(generos1);
        generosIntersection.retainAll(generos2);
        Set<String> generosUnion = new HashSet<>(generos1);
        generosUnion.addAll(generos2);

        // keywords -> lowercase sets for robust comparison
        List<String> k1 = v1.getKeywords() == null ? Collections.emptyList() : v1.getKeywords();
        List<String> k2 = v2.getKeywords() == null ? Collections.emptyList() : v2.getKeywords();
        Set<String> set1 = new HashSet<>(k1.stream().filter(Objects::nonNull).map(s -> s.toLowerCase().trim()).collect(Collectors.toSet()));
        Set<String> set2 = new HashSet<>(k2.stream().filter(Objects::nonNull).map(s -> s.toLowerCase().trim()).collect(Collectors.toSet()));

        Set<String> inter = new HashSet<>(set1);
        inter.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        int sharedKeywords = inter.size();
        int totalKeywords = union.size() == 0 ? Math.max(set1.size(), set2.size()) : union.size();
        double keywordFraction = totalKeywords == 0 ? 0.0 : ((double) sharedKeywords) / totalKeywords;

        double actorContrib = sameActor ? pesoAtor : 0.0;
        double directorContrib = sameDirector ? pesoDiretor : 0.0;
        double producerContrib = sameProducer ? pesoProdutora : 0.0;
        double keywordsContrib = keywordFraction * pesoKeywords;
        double genreContribution = pesoGenero * genreFraction;
        double weight = actorContrib + directorContrib + producerContrib + keywordsContrib + genreContribution;

        return new SimilarityBreakdown(
                sameActor,
                sameDirector,
                sameProducer,
                sharedKeywords,
                totalKeywords,
                keywordFraction,
                actorContrib,
                directorContrib,
                producerContrib,
                keywordsContrib,
                weight,
                generosIntersection.size(),
                generosUnion.size(),
                genreFraction,
                genreContribution
        );
    }
}