package com.example.demo.controller;

import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recomendaFilmes.*; 

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


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
    double score                  
) {}

@RestController
public class RecommendationController {

    private final TMDbClient tmdb = new TMDbClient();

    @CrossOrigin
    @GetMapping("/recommendations")
    public List<MovieRecommendationDTO> getRecommendations(@RequestParam String movieName) {
        try {
            Grafo grafo = new Grafo();

           
            JsonObject filmeBaseJson = tmdb.searchMovie(movieName);
            if (filmeBaseJson == null) return Collections.emptyList();

            int idBase = filmeBaseJson.get("id").getAsInt();
            JsonObject detalhesBase = tmdb.getMovieDetails(idBase);

            Vertice filmeBase = criarVertice(tmdb, detalhesBase);
            grafo.adicionarVertice(filmeBase);

            // 2) povoar grafo (mesma lógica sua)
            List<JsonObject> filmesDescobertos = tmdb.discoverMoviesByGenres(tmdb.getMovieGenreIds(detalhesBase), 25);

            Set<Integer> idsProcessados = new HashSet<>();
            idsProcessados.add(idBase);

            for (JsonObject filme : filmesDescobertos) {
                int id = filme.get("id").getAsInt();
                if (idsProcessados.contains(id)) continue;
                JsonObject detalhes = tmdb.getMovieDetails(id);
                grafo.adicionarVertice(criarVertice(tmdb, detalhes));
                idsProcessados.add(id);
            }

            
            grafo.gerarArestasSimilaridade();

            // 4) recomendações (usa sua função existente)
            List<Vertice> recomendacoes = grafo.recomendarFilmes(filmeBase, 10);

           
            return recomendacoes.stream()
                    .map(vertice -> {
                        try {
                            JsonObject detalhes = tmdb.getMovieDetails(vertice.getIdTmdb());
                            String posterPath = detalhes.has("poster_path") && !detalhes.get("poster_path").isJsonNull()
                                    ? detalhes.get("poster_path").getAsString() : null;
                            double voteAvg = detalhes.has("vote_average") ? detalhes.get("vote_average").getAsDouble() : 0.0;

                            
                            Grafo.SimilarityBreakdown bd = grafo.calcularBreakdown(filmeBase, vertice);

                            double actorContributionPct = bd.actorContribution() * 100.0;      // ex: 
                            double directorContributionPct = bd.directorContribution() * 100.0; // ex: 
                            double productionContributionPct = bd.producerContribution() * 100.0;
                            double keywordsContributionPct = bd.keywordsContribution() * 100.0;
                            double scorePct = bd.weight() * 100.0;

                            return new MovieRecommendationDTO(
                                    vertice.getIdTmdb(),
                                    vertice.getNome(),
                                    posterPath,
                                    voteAvg,
                                    bd.sameActor(),
                                    bd.sameDirector(),
                                    bd.sameProducer(),
                                    bd.sharedKeywordsCount(),
                                    bd.totalKeywordsUnion(),
                                    bd.keywordFraction(),
                                    Math.round(actorContributionPct * 10.0) / 10.0,
                                    Math.round(directorContributionPct * 10.0) / 10.0,
                                    Math.round(productionContributionPct * 10.0) / 10.0,
                                    Math.round(keywordsContributionPct * 10.0) / 10.0,
                                    Math.round(scorePct * 10.0) / 10.0
                            );
                        } catch (IOException e) {
                            System.err.println("Erro ao buscar detalhes para: " + vertice.getNome());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Vertice criarVertice(TMDbClient tmdb, JsonObject detalhes) {
        int id = detalhes.get("id").getAsInt();
        String titulo = detalhes.has("title") ? detalhes.get("title").getAsString() : ("movie-" + id);
        List<String> generos = tmdb.getMovieGenres(detalhes);
        String ator = tmdb.getMovieMainActor(detalhes);
        String diretor = tmdb.getMovieDirector(detalhes);
        String produtora = tmdb.getMovieProductionCompany(detalhes);
        List<String> keywords = tmdb.getMovieKeywords(detalhes);
        return new Vertice(titulo, id, generos, ator, diretor, produtora, keywords);
    }
}
