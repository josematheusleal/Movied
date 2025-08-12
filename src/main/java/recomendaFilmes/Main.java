package recomendaFilmes;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        TMDbClient tmdb = new TMDbClient();
        Grafo grafo = new Grafo();

        // 1. Buscar filme base
        System.out.print("Digite o nome de um filme: ");
        String filmeBaseNome = sc.nextLine();
        JsonObject filmeBaseJson = tmdb.searchMovie(filmeBaseNome);
        
        if (filmeBaseJson == null) {
            System.out.println("Filme não encontrado.");
            return;
        }

        // 2. Criar vértice para o filme base
        int idBase = filmeBaseJson.get("id").getAsInt();
        JsonObject detalhesBase = tmdb.getMovieDetails(idBase);
        List<String> generosBase = tmdb.getMovieGenres(detalhesBase);
        List<String> atoresBase = tmdb.getMovieActors(idBase);
        
        Vertice filmeBase = new Vertice(
            filmeBaseJson.get("title").getAsString(),
            idBase,
            generosBase,
            atoresBase
        );
        grafo.adicionarVertice(filmeBase);

        // 3. Adicionar filmes populares ao grafo
        List<JsonObject> populares = tmdb.getPopularMovies();
        for (JsonObject filme : populares) {
            int id = filme.get("id").getAsInt();
            if (id == idBase) continue;

            JsonObject detalhes = tmdb.getMovieDetails(id);
            Vertice vertice = new Vertice(
                filme.get("title").getAsString(),
                id,
                tmdb.getMovieGenres(detalhes),
                tmdb.getMovieActors(id)
            );
            grafo.adicionarVertice(vertice);
        }

        // 4. Gerar relações de similaridade
        grafo.gerarArestasSimilaridade();

        // 5. Obter recomendações
        System.out.println("\nRecomendações para '" + filmeBase.getNome() + "':");
        List<Vertice> recomendacoes = grafo.recomendarFilmes(filmeBase, 5);
        recomendacoes.forEach(v -> System.out.println("- " + v.getNome()));
    sc. close();
    }
}