// Conteúdo para Main.java

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
            sc.close();
            return;
        }

        // 2. Criar vértice para o filme base
        int idBase = filmeBaseJson.get("id").getAsInt();
        JsonObject detalhesBase = tmdb.getMovieDetails(idBase);

        List<String> generosBase = tmdb.getMovieGenres(detalhesBase);
        String atorBase = tmdb.getMovieMainActor(detalhesBase);
        String diretorBase = tmdb.getMovieDirector(detalhesBase);
        String produtoraBase = tmdb.getMovieProductionCompany(detalhesBase);

        Vertice filmeBase = new Vertice(
                filmeBaseJson.get("title").getAsString(),
                idBase,
                generosBase,
                atorBase,
                diretorBase,
                produtoraBase
        );
        grafo.adicionarVertice(filmeBase);

        // 3. Adicionar filmes populares ao grafo
        System.out.println("Buscando filmes semelhantes. Aguarde...");
List<JsonObject> populares = tmdb.getPopularMovies(100); 
for (JsonObject filme : populares) {
    int id = filme.get("id").getAsInt();
    if (id == idBase) continue;

    // Apenas UMA chamada de API por filme para buscar TUDO (detalhes + créditos)
    JsonObject detalhesComCreditos = tmdb.getMovieDetails(id);
    
    // Extrai os dados do objeto JSON já buscado
    List<String> generos = tmdb.getMovieGenres(detalhesComCreditos);
    String ator = tmdb.getMovieMainActor(detalhesComCreditos);
    String diretor = tmdb.getMovieDirector(detalhesComCreditos);
    String produtora = tmdb.getMovieProductionCompany(detalhesComCreditos);

    Vertice vertice = new Vertice(
            filme.get("title").getAsString(),
            id,
            generos,
            ator,
            diretor,
            produtora
    );
    grafo.adicionarVertice(vertice);
}
        // 4. Gerar relações de similaridade
        grafo.gerarArestasSimilaridade();

        // 5. Obter recomendações
        System.out.println("\nRecomendações para '" + filmeBase.getNome() + "':");
        List<Vertice> recomendacoes = grafo.recomendarFilmes(filmeBase, 3);
        
        if (recomendacoes.isEmpty()) {
            System.out.println("Nenhuma recomendação similar encontrada com base nos critérios.");
        } else {
            recomendacoes.forEach(v -> System.out.println("- " + v.getNome()));
        }

        sc.close();
    }
}