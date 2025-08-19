<<<<<<< HEAD
// Conteúdo para TMDbClient.java

=======
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
package recomendaFilmes;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class TMDbClient {
    private static final String API_KEY = "baf716a6b18b1abc236b34dc1429f5d5";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public JsonObject searchMovie(String query) throws IOException {
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&language=pt-BR&query=" + query;
        return getFirstResult(url);
    }

<<<<<<< HEAD
    /**
     * OTIMIZADO: Agora busca detalhes e créditos em uma única chamada de API.
     */
    public JsonObject getMovieDetails(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=pt-BR&append_to_response=credits";
=======
    public JsonObject getMovieDetails(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=pt-BR";
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
        return getJson(url);
    }

    public List<String> getMovieGenres(JsonObject movie) {
        List<String> genres = new ArrayList<>();
        JsonArray arr = movie.getAsJsonArray("genres");
        if (arr != null) {
            for (JsonElement e : arr) {
                genres.add(e.getAsJsonObject().get("name").getAsString());
            }
        }
        return genres;
    }

<<<<<<< HEAD
   /**
     * OTIMIZADO: Extrai o ator principal do objeto de detalhes que já contém os créditos.
     * Não faz mais uma nova chamada de API.
     */
    public String getMovieMainActor(JsonObject movieDetailsWithCredits) {
        JsonObject credits = movieDetailsWithCredits.getAsJsonObject("credits");
        if (credits != null) {
            JsonArray cast = credits.getAsJsonArray("cast");
            if (cast != null && cast.size() > 0) {
                return cast.get(0).getAsJsonObject().get("name").getAsString();
            }
        }
        return null;
    }

    /**
     * OTIMIZADO: Extrai o diretor do objeto de detalhes que já contém os créditos.
     * Não faz mais uma nova chamada de API.
     */
    public String getMovieDirector(JsonObject movieDetailsWithCredits) {
        JsonObject credits = movieDetailsWithCredits.getAsJsonObject("credits");
        if (credits != null) {
            JsonArray crew = credits.getAsJsonArray("crew");
            if (crew != null) {
                for (JsonElement e : crew) {
                    JsonObject member = e.getAsJsonObject();
                    if (member.get("job").getAsString().equals("Director")) {
                        return member.get("name").getAsString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * NOVO: Extrai o nome da primeira produtora da lista.
     */
    public String getMovieProductionCompany(JsonObject movieDetails) {
        JsonArray companies = movieDetails.getAsJsonArray("production_companies");
        if (companies != null && companies.size() > 0) {
            return companies.get(0).getAsJsonObject().get("name").getAsString();
        }
        return null;
    }
    /**
     * NOVO: Busca filmes populares com paginação.
     */
public List<JsonObject> getPopularMovies(int totalPages) throws IOException {
    List<JsonObject> allMovies = new ArrayList<>();
    for (int i = 1; i <= totalPages; i++) {
        String url = BASE_URL + "/movie/popular?api_key=" + API_KEY + "&language=pt-BR&page=" + i;
        allMovies.addAll(getResults(url));
    }
    return allMovies;
}
=======
    public List<String> getMovieActors(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "/credits?api_key=" + API_KEY + "&language=pt-BR";
        JsonObject json = getJson(url);
        List<String> actors = new ArrayList<>();
        JsonArray cast = json.getAsJsonArray("cast");
        for (int i = 0; i < Math.min(5, cast.size()); i++) {
            actors.add(cast.get(i).getAsJsonObject().get("name").getAsString());
        }
        return actors;
    }

    public List<JsonObject> getPopularMovies() throws IOException {
        String url = BASE_URL + "/movie/popular?api_key=" + API_KEY + "&language=pt-BR&page=1";
        return getResults(url);
    }

>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
    private JsonObject getFirstResult(String url) throws IOException {
        List<JsonObject> results = getResults(url);
        return results.isEmpty() ? null : results.get(0);
    }

    private List<JsonObject> getResults(String url) throws IOException {
        JsonObject json = getJson(url);
        JsonArray arr = json.getAsJsonArray("results");
        List<JsonObject> list = new ArrayList<>();
<<<<<<< HEAD
        if (arr != null) {
            for (JsonElement e : arr) {
                list.add(e.getAsJsonObject());
            }
=======
        for (JsonElement e : arr) {
            list.add(e.getAsJsonObject());
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
        }
        return list;
    }

    private JsonObject getJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
<<<<<<< HEAD
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string()).getAsJsonObject();
            }
            throw new IOException("Resposta da API vazia.");
        }
    }
}
=======
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }
}
>>>>>>> 715c6b8eabfb78040cfc56944cdaf16208dfa403
