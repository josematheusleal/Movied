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

    public JsonObject getMovieDetails(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=pt-BR";
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

    private JsonObject getFirstResult(String url) throws IOException {
        List<JsonObject> results = getResults(url);
        return results.isEmpty() ? null : results.get(0);
    }

    private List<JsonObject> getResults(String url) throws IOException {
        JsonObject json = getJson(url);
        JsonArray arr = json.getAsJsonArray("results");
        List<JsonObject> list = new ArrayList<>();
        for (JsonElement e : arr) {
            list.add(e.getAsJsonObject());
        }
        return list;
    }

    private JsonObject getJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }
}
