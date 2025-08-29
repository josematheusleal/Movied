package recomendaFilmes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeradorRelatorio {
    private final Grafo grafo;
    private final AlgoritmosGrafo algoritmos;
    
    public GeradorRelatorio(Grafo grafo) {
        this.grafo = grafo;
        this.algoritmos = new AlgoritmosGrafo(grafo);
    }
    
    public void gerarRelatorioCompleto() {
        System.out.println("\n========================================");
        System.out.println("     RELATÓRIO DE ANÁLISE DO GRAFO     ");
        System.out.println("========================================");
        System.out.println("Data: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println();
        
        exibirEstatisticasGerais();
        exibirDistribuicoes();
        exibirFilmesMaisConectados();
        exibirComponentesConectados();
        exibirHubs();
        analisarIsolados();
    }
    
    private void exibirEstatisticasGerais() {
        System.out.println("ESTATÍSTICAS GERAIS");
        System.out.println("-------------------");
        System.out.println("Total de filmes: " + grafo.getVertices().size());
        System.out.println("Total de conexões: " + grafo.getArestas().size());
        System.out.printf("Densidade do grafo: %.4f\n", grafo.getDensidade());
        System.out.printf("Grau médio: %.2f\n", grafo.getGrauMedio());
        System.out.println();
    }
    
    private void exibirDistribuicoes() {
        System.out.println("DISTRIBUIÇÃO POR CATEGORIA");
        System.out.println("--------------------------");
        
        Map<String, Integer> generos = grafo.getDistribuicaoGeneros();
        System.out.println("\nTop 5 Gêneros:");
        generos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.printf("  - %s: %d filmes\n", e.getKey(), e.getValue()));
        
        Map<String, Integer> diretores = grafo.getDistribuicaoDiretores();
        System.out.println("\nTop 3 Diretores:");
        diretores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.printf("  - %s: %d filmes\n", e.getKey(), e.getValue()));
        
        Map<String, Integer> atores = grafo.getDistribuicaoAtores();
        System.out.println("\nTop 3 Atores:");
        atores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.printf("  - %s: %d filmes\n", e.getKey(), e.getValue()));
        System.out.println();
    }
    
    private void exibirFilmesMaisConectados() {
        System.out.println("FILMES MAIS CONECTADOS");
        System.out.println("----------------------");
        
        List<Map.Entry<Vertice, Integer>> topFilmes = grafo.getFilmesMaisConectados(5);
        for (int i = 0; i < topFilmes.size(); i++) {
            Map.Entry<Vertice, Integer> entry = topFilmes.get(i);
            System.out.printf("%d. %s - %d conexões\n", 
                i + 1, entry.getKey().getNome(), entry.getValue());
        }
        System.out.println();
    }
    
    private void exibirComponentesConectados() {
        System.out.println("ANÁLISE DE COMPONENTES");
        System.out.println("----------------------");
        
        List<Set<Vertice>> componentes = algoritmos.encontrarComponentesConectados();
        System.out.println("Total de componentes conectados: " + componentes.size());
        
        if (componentes.size() > 1) {
            System.out.println("\nTamanho dos componentes:");
            for (int i = 0; i < Math.min(5, componentes.size()); i++) {
                System.out.printf("  Componente %d: %d filmes\n", i + 1, componentes.get(i).size());
            }
        }
        System.out.println();
    }
    
    private void exibirHubs() {
        System.out.println("HUBS DO GRAFO");
        System.out.println("-------------");
        System.out.println("(Filmes com maior centralidade de grau)");
        
        List<Vertice> hubs = algoritmos.detectarHubs(3);
        for (int i = 0; i < hubs.size(); i++) {
            Vertice hub = hubs.get(i);
            System.out.printf("%d. %s\n", i + 1, hub.getNome());
            System.out.printf("   Gêneros: %s\n", String.join(", ", hub.getGeneros()));
            if (hub.getDiretor() != null) {
                System.out.printf("   Diretor: %s\n", hub.getDiretor());
            }
        }
        System.out.println();
    }
    
    private void analisarIsolados() {
        List<Vertice> isolados = grafo.getFilmesSemConexoes();
        
        if (!isolados.isEmpty()) {
            System.out.println("FILMES ISOLADOS");
            System.out.println("---------------");
            System.out.println("Total de filmes sem conexões: " + isolados.size());
            
            if (isolados.size() <= 5) {
                System.out.println("Filmes:");
                for (Vertice filme : isolados) {
                    System.out.println("  - " + filme.getNome());
                }
            } else {
                System.out.println("Primeiros 5 filmes:");
                for (int i = 0; i < 5; i++) {
                    System.out.println("  - " + isolados.get(i).getNome());
                }
            }
            System.out.println();
        }
    }
    
    public void salvarRelatorioArquivo(String nomeArquivo) {
        try (PrintStream fileOut = new PrintStream(new FileOutputStream(nomeArquivo))) {
            PrintStream originalOut = System.out;
            System.setOut(fileOut);
            
            gerarRelatorioCompleto();
            
            System.setOut(originalOut);
            
            System.out.println("Relatório salvo em: " + nomeArquivo);
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao salvar relatório: " + e.getMessage());
        }
    }
    
    public void exibirCaminhoEntreFilmes(String filme1, String filme2) {
        Vertice v1 = grafo.getVerticePorNome(filme1);
        Vertice v2 = grafo.getVerticePorNome(filme2);
        
        if (v1 == null || v2 == null) {
            System.out.println("Um ou ambos os filmes não foram encontrados.");
            return;
        }
        
        List<Vertice> caminho = algoritmos.encontrarCaminho(v1, v2);
        
        if (caminho.isEmpty()) {
            System.out.println("Não há conexão entre " + filme1 + " e " + filme2);
        } else {
            System.out.println("\nCAMINHO ENTRE FILMES");
            System.out.println("--------------------");
            System.out.println("De: " + filme1);
            System.out.println("Para: " + filme2);
            System.out.println("Distância: " + (caminho.size() - 1) + " passos\n");
            
            for (int i = 0; i < caminho.size(); i++) {
                System.out.println((i + 1) + ". " + caminho.get(i).getNome());
                if (i < caminho.size() - 1) {
                    System.out.println("   ↓");
                }
            }
        }
    }
}