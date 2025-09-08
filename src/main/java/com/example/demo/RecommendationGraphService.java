package com.example.demo; 

import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct; 
import org.springframework.stereotype.Service;
import recomendaFilmes.Grafo;
import recomendaFilmes.TMDbClient;
import recomendaFilmes.Vertice;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service 
public class RecommendationGraphService {

    private final TMDbClient tmdbClient = new TMDbClient();

    private volatile Grafo grafoDeRecomendacoes;

    @PostConstruct
    public void iniciarConstrucaoInicial() {
        new Thread(this::construirGrafoCompleto).start();
    }

    public Grafo getGrafo() {
        return this.grafoDeRecomendacoes;
    }

    private void construirGrafoCompleto() {
        try {
            System.out.println("\nIniciando processo de construção do grafo...\n");
            Grafo novoGrafo = new Grafo();
           
            List<JsonObject> filmesPopulares = tmdbClient.discoverMoviesByGenres(Collections.emptyList(), 250);

            for (JsonObject filmeJson : filmesPopulares) {
                int id = filmeJson.get("id").getAsInt();
                JsonObject detalhes = tmdbClient.getMovieDetails(id);
                if (detalhes != null) {
                    novoGrafo.adicionarVertice(criarVertice(tmdbClient, detalhes));
                }
            }
            System.out.println("Grafo povoado com " + novoGrafo.getVertices().size() + " filmes.");

            System.out.println("Calculando todas as similaridades...");
            novoGrafo.gerarArestasSimilaridade();

            this.grafoDeRecomendacoes = novoGrafo;
            System.out.println("Construção do grafo concluída com sucesso!");

        } catch (IOException e) {
            System.err.println("Falha ao construir o grafo de recomendações: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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