package com.example.demo; // Verifique se este é o pacote correto

import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct; // CORREÇÃO: Import correto para @PostConstruct
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import recomendaFilmes.Grafo;
import recomendaFilmes.TMDbClient;
import recomendaFilmes.Vertice;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
//import java.util.Optional;

@Service // Marca esta classe como um Serviço do Spring (um componente singleton)
public class RecommendationGraphService {

    private final TMDbClient tmdbClient = new TMDbClient();

    // 'volatile' garante que as atualizações do grafo sejam visíveis para todas as threads
    private volatile Grafo grafoDeRecomendacoes;

    /**
     * Este método é executado uma vez, logo após a aplicação iniciar.
     * Ele constrói o primeiro grafo para que o sistema não fique sem dados no início.
     */
    @PostConstruct
    public void iniciarConstrucaoInicial() {
        // Inicia a construção em uma nova thread para não bloquear a inicialização do servidor
        new Thread(this::construirGrafoCompleto).start();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void construirGrafoAgendado() {
        System.out.println("Iniciando reconstrução agendada do grafo de recomendações...");
        construirGrafoCompleto();
    }

    /**
     * Retorna o grafo atualmente disponível.
     */
    public Grafo getGrafo() {
        return this.grafoDeRecomendacoes;
    }

    /**
     * Contém toda a lógica de busca de dados e construção do grafo.
     */
    private void construirGrafoCompleto() {
        try {
            System.out.println("Iniciando processo de construção do grafo...");
            Grafo novoGrafo = new Grafo();
            
            // Busca um grande número de filmes (50 páginas = ~1000 filmes) para ter uma base sólida
            // Passar uma lista vazia para discoverMoviesByGenres funciona como um "get populares".
            List<JsonObject> filmesPopulares = tmdbClient.discoverMoviesByGenres(Collections.emptyList(), 200);

            // Para cada filme, busca detalhes e cria um vértice
            for (JsonObject filmeJson : filmesPopulares) {
                int id = filmeJson.get("id").getAsInt();
                JsonObject detalhes = tmdbClient.getMovieDetails(id);
                if (detalhes != null) {
                    novoGrafo.adicionarVertice(criarVertice(tmdbClient, detalhes));
                }
            }
            System.out.println("Grafo povoado com " + novoGrafo.getVertices().size() + " filmes.");

            // Gera todas as arestas de similaridade
            System.out.println("Calculando todas as similaridades...");
            novoGrafo.gerarArestasSimilaridade();

            // Substitui o grafo antigo pelo novo de forma atômica
            this.grafoDeRecomendacoes = novoGrafo;
            System.out.println("Construção do grafo concluída com sucesso!");

        } catch (IOException e) {
            System.err.println("Falha ao construir o grafo de recomendações: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método helper para criar um Vertice (mesmo de antes)
    private Vertice criarVertice(TMDbClient tmdb, JsonObject detalhes) {
        int id = detalhes.get("id").getAsInt();
        String titulo = detalhes.get("title").getAsString();
        List<String> generos = tmdb.getMovieGenres(detalhes);
        String ator = tmdb.getMovieMainActor(detalhes);
        String diretor = tmdb.getMovieDirector(detalhes);
        String produtora = tmdb.getMovieProductionCompany(detalhes);
        List<String> keywords = tmdb.getMovieKeywords(detalhes);
        return new Vertice(titulo, id, generos, ator, diretor, produtora, keywords);
    }
}