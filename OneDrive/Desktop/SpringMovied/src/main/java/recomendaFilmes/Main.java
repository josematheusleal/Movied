package recomendaFilmes;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");
        Scanner sc = new Scanner(System.in, "UTF-8");

        TMDbClient tmdb = new TMDbClient();
        Grafo grafo = new Grafo();


        System.out.print("Digite o nome de um filme: ");
        String filmeBaseNome = sc.nextLine();
        JsonObject filmeBaseJson = tmdb.searchMovie(filmeBaseNome);

        if (filmeBaseJson == null) {
            System.out.println("Filme não encontrado.");
            sc.close();
            return;
        }

      
        int idBase = filmeBaseJson.get("id").getAsInt();
        JsonObject detalhesBase = tmdb.getMovieDetails(idBase);
        
        List<Integer> generosIdsBase = tmdb.getMovieGenreIds(detalhesBase);
        List<String> generosNomesBase = tmdb.getMovieGenres(detalhesBase);
        String atorBase = tmdb.getMovieMainActor(detalhesBase);
        String diretorBase = tmdb.getMovieDirector(detalhesBase);
        String produtoraBase = tmdb.getMovieProductionCompany(detalhesBase);
        List<String> keywordsBase = tmdb.getMovieKeywords(detalhesBase);

        Vertice filmeBase = new Vertice(
                filmeBaseJson.get("title").getAsString(),
                idBase,
                generosNomesBase,
                atorBase,
                diretorBase,
                produtoraBase,
                keywordsBase
        );
        grafo.adicionarVertice(filmeBase);
        System.out.println("Filme base: " + filmeBase.getNome());

      
        System.out.println("Buscando filmes com gêneros parecidos. Aguarde...");
        List<JsonObject> filmesDescobertos = tmdb.discoverMoviesByGenres(generosIdsBase, 80);
        
        Set<Integer> idsProcessados = new HashSet<>();
        idsProcessados.add(idBase);

        for (JsonObject filme : filmesDescobertos) {
            int id = filme.get("id").getAsInt();
            if (idsProcessados.contains(id)) continue;

            JsonObject detalhes = tmdb.getMovieDetails(id);
            List<String> generos = tmdb.getMovieGenres(detalhes);
            String ator = tmdb.getMovieMainActor(detalhes);
            String diretor = tmdb.getMovieDirector(detalhes);
            String produtora = tmdb.getMovieProductionCompany(detalhes);
            List<String> keywords = tmdb.getMovieKeywords(detalhes);

            Vertice vertice = new Vertice(
                    filme.get("title").getAsString(), id, generos, ator, diretor, produtora, keywords);
            grafo.adicionarVertice(vertice);
            idsProcessados.add(id);
        }

        System.out.println("Calculando similaridades...");
        grafo.gerarArestasSimilaridade();


        System.out.println("\n--- Recomendações para '" + filmeBase.getNome() + "' ---");
        List<Vertice> recomendacoes = grafo.recomendarFilmes(filmeBase, 10);
        
        if (recomendacoes.isEmpty()) {
            System.out.println("Nenhuma recomendação similar encontrada com base nos critérios.");
        } else {
            recomendacoes.forEach(v -> System.out.println("- " + v.getNome()));
        }

        sc.close();
    }
}