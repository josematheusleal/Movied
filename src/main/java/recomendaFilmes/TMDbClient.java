package recomendaFilmes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TMDbClient {
    private static final String API_KEY = "e9638467d7bf43bdcce5ade03964fc75";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final OkHttpClient client = new OkHttpClient();

    public JsonObject searchMovie(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&language=pt-BR&query=" + encodedQuery;
        
        System.out.println("Buscando filme: " + query);
        
        List<JsonObject> results = getResults(url);
        
        if (results.isEmpty()) {
            System.out.println("Tentando busca sem idioma específico...");
            url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encodedQuery;
            results = getResults(url);
        }
        
        if (!results.isEmpty()) {
            JsonObject filme = results.get(0);
            System.out.println("Filme encontrado: " + filme.get("title").getAsString());
            if (results.size() > 1) {
                System.out.println("Outros resultados encontrados:");
                for (int i = 1; i < Math.min(5, results.size()); i++) {
                    System.out.println("  - " + results.get(i).get("title").getAsString());
                }
            }
            return filme;
        }
        
        return null;
    }

    public JsonObject getMovieDetails(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=pt-BR&append_to_response=credits";
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

    public String getMovieProductionCompany(JsonObject movieDetails) {
        JsonArray companies = movieDetails.getAsJsonArray("production_companies");
        if (companies != null && companies.size() > 0) {
            return companies.get(0).getAsJsonObject().get("name").getAsString();
        }
        return null;
    }
    
    public List<JsonObject> getPopularMovies(int totalPages) throws IOException {
        List<JsonObject> allMovies = new ArrayList<>();
        System.out.print("Carregando filmes populares");
        for (int i = 1; i <= totalPages; i++) {
            String url = BASE_URL + "/movie/popular?api_key=" + API_KEY + "&language=pt-BR&page=" + i;
            List<JsonObject> pageResults = getResults(url);
            allMovies.addAll(pageResults);
            if (i % 20 == 0) {
                System.out.print(".");
            }
        }
        System.out.println(" Total: " + allMovies.size() + " filmes");
        return allMovies;
    }

    private List<JsonObject> getResults(String url) throws IOException {
        try {
            JsonObject json = getJson(url);
            JsonArray arr = json.getAsJsonArray("results");
            List<JsonObject> list = new ArrayList<>();
            if (arr != null) {
                for (JsonElement e : arr) {
                    list.add(e.getAsJsonObject());
                }
            }
            return list;
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Erro ao buscar dados: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private JsonObject getJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                String responseStr = body.string();
                if (response.code() != 200) {
                    System.err.println("Erro HTTP: " + response.code());
                    System.err.println("Resposta: " + responseStr);
                }
                return JsonParser.parseString(responseStr).getAsJsonObject();
            }
            throw new IOException("Resposta da API vazia.");
        }
    }
}