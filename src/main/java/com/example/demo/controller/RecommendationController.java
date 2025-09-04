package com.example.demo.controller; // Verifique se o pacote está correto

import com.example.demo.RecommendationGraphService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recomendaFilmes.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// NOVO DTO DETALHADO
record MovieRecommendationDTO(
    int id,
    String title,
    String poster_path,
    double vote_average,
    boolean same_main_actor,
    boolean same_director,
    boolean same_production_company,
    int shared_keywords_count,
    int total_keywords_count,
    double keyword_fraction,
    double actor_contribution,
    double director_contribution,
    double production_contribution,
    double keywords_contribution,
    double genre_contribution, 
    List<String> shared_genres,
    double score
) {}

@RestController
public class RecommendationController {

    @Autowired
    private RecommendationGraphService graphService; // Serviço com o grafo pré-calculado

    private final TMDbClient tmdb = new TMDbClient();

    @CrossOrigin
    @GetMapping("/recommendations")
    public List<MovieRecommendationDTO> getRecommendations(@RequestParam String movieName) throws IOException {
        System.out.println("Recebida requisição para: " + movieName);
        Grafo grafo = graphService.getGrafo();

        // ROTA RÁPIDA: Tenta encontrar no grafo pré-calculado
        if (grafo != null) {
            Optional<Vertice> filmeBaseOpt = grafo.getVerticePorNome(movieName);
            if (filmeBaseOpt.isPresent()) {
                System.out.println("Filme encontrado no grafo pré-calculado. Retornando recomendações rápidas.");
                Vertice filmeBase = filmeBaseOpt.get();
                List<Vertice> recomendacoes = grafo.recomendarFilmes(filmeBase, 10);
                
                // Converte para o DTO detalhado usando o novo método 'calcularBreakdown'
                return recomendacoes.stream()
                    .map(vertice -> convertVerticeToDetailedDTO(filmeBase, vertice, grafo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }
        }

        // ROTA DE FALLBACK: Se não encontrou, executa a busca online
        System.out.println("Filme não encontrado no grafo. Iniciando busca online em tempo real...");
        return performOnlineRecommendation(movieName);
    }

    private List<MovieRecommendationDTO> performOnlineRecommendation(String movieName) throws IOException {
        Grafo grafoTemporario = new Grafo();
        JsonObject filmeBaseJson = tmdb.searchMovie(movieName);
        if (filmeBaseJson == null) return Collections.emptyList();

        int idBase = filmeBaseJson.get("id").getAsInt();
        JsonObject detalhesBase = tmdb.getMovieDetails(idBase);
        Vertice filmeBase = criarVertice(tmdb, detalhesBase);
        grafoTemporario.adicionarVertice(filmeBase);
        
        List<JsonObject> filmesDescobertos = tmdb.discoverMoviesByGenres(tmdb.getMovieGenreIds(detalhesBase), 50);
        for (JsonObject filme : filmesDescobertos) {
            int id = filme.get("id").getAsInt();
            if (id == idBase) continue;
            JsonObject detalhes = tmdb.getMovieDetails(id);
            grafoTemporario.adicionarVertice(criarVertice(tmdb, detalhes));
        }

        grafoTemporario.gerarArestasSimilaridade();
        List<Vertice> recomendacoes = grafoTemporario.recomendarFilmes(filmeBase, 10);

        // Converte para o DTO detalhado, usando o grafo temporário para o breakdown
        return recomendacoes.stream()
            .map(vertice -> convertVerticeToDetailedDTO(filmeBase, vertice, grafoTemporario))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private MovieRecommendationDTO convertVerticeToDetailedDTO(Vertice filmeBase, Vertice verticeRecomendado, Grafo grafo) {
        try {
            JsonObject detalhes = tmdb.getMovieDetails(verticeRecomendado.getIdTmdb());
            String posterPath = detalhes.has("poster_path") && !detalhes.get("poster_path").isJsonNull()
                                ? detalhes.get("poster_path").getAsString() : null;
            double voteAvg = detalhes.has("vote_average") ? detalhes.get("vote_average").getAsDouble() : 0.0;

            // Pega os detalhes do cálculo
            Grafo.SimilarityBreakdown bd = grafo.calcularBreakdown(filmeBase, verticeRecomendado);

            List<String> sharedGenres = filmeBase.getGeneros().stream()
                .filter(g -> verticeRecomendado.getGeneros().contains(g))
                .collect(Collectors.toList());

            return new MovieRecommendationDTO(
                verticeRecomendado.getIdTmdb(),
                verticeRecomendado.getNome(),
                posterPath,
                voteAvg,
                bd.sameActor(),
                bd.sameDirector(),
                bd.sameProducer(),
                bd.sharedKeywordsCount(),
                bd.totalKeywordsUnion(),
                bd.keywordFraction(),
                Math.round(bd.actorContribution() * 1000.0) / 10.0, // Converte para % com 1 casa decimal
                Math.round(bd.directorContribution() * 1000.0) / 10.0,
                Math.round(bd.producerContribution() * 1000.0) / 10.0,
                Math.round(bd.keywordsContribution() * 1000.0) / 10.0,
                Math.round(bd.genreContribution() * 1000.0) / 10.0,
                sharedGenres,
                Math.round(bd.weight() * 1000.0) / 10.0
            );
        } catch (IOException e) {
            System.err.println("Erro ao buscar detalhes para DTO do filme: " + verticeRecomendado.getNome());
            return null;
        }
    }

    private Vertice criarVertice(TMDbClient tmdb, JsonObject detalhes) {
        int id = detalhes.get("id").getAsInt();
        String titulo = detalhes.has("title") ? detalhes.get("title").getAsString() : "Título Desconhecido";
        List<String> generos = tmdb.getMovieGenres(detalhes);
        String ator = tmdb.getMovieMainActor(detalhes);
        String diretor = tmdb.getMovieDirector(detalhes);
        String produtora = tmdb.getMovieProductionCompany(detalhes);
        List<String> keywords = tmdb.getMovieKeywords(detalhes);
        return new Vertice(titulo, id, generos, ator, diretor, produtora, keywords);
    }
}