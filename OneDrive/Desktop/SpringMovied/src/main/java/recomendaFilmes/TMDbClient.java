package recomendaFilmes;

import com.google.gson.*;
import okhttp3.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TMDbClient {
    private static final String API_KEY = "baf716a6b18b1abc236b34dc1429f5d5"; 
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final OkHttpClient client = new OkHttpClient();

    public JsonObject searchMovie(String query) throws IOException {
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&language=pt-BR&query=" + query;
        return getFirstResult(url);
    }

   
    public JsonObject getMovieDetails(int movieId) throws IOException {
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + 
                     "&language=pt-BR&append_to_response=credits,keywords";
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
    
    public List<Integer> getMovieGenreIds(JsonObject movie) {
        List<Integer> genreIds = new ArrayList<>();
        JsonArray arr = movie.getAsJsonArray("genres");
        if (arr != null) {
            for (JsonElement e : arr) {
                genreIds.add(e.getAsJsonObject().get("id").getAsInt());
            }
        }
        return genreIds;
    }

    public String getMovieMainActor(JsonObject movieDetailsWithCredits) {
        if (!movieDetailsWithCredits.has("credits")) return null;
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
        if (!movieDetailsWithCredits.has("credits")) return null;
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

    public List<String> getMovieKeywords(JsonObject movieDetails) {
        List<String> keywords = new ArrayList<>();
        if (movieDetails.has("keywords")) {
            JsonObject keywordsObject = movieDetails.getAsJsonObject("keywords");
            JsonArray arr = keywordsObject.getAsJsonArray("keywords");
            if (arr != null) {
                for (JsonElement e : arr) {
                    keywords.add(e.getAsJsonObject().get("name").getAsString());
                }
            }
        }
        return keywords;
    }

    public List<JsonObject> discoverMoviesByGenres(List<Integer> genreIds, int totalPages) throws IOException {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        String genreIdsString = genreIds.stream()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining("|"));

        List<JsonObject> allMovies = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            String url = BASE_URL + "/discover/movie?api_key=" + API_KEY +
                         "&language=pt-BR&sort_by=popularity.desc&with_genres=" + genreIdsString +
                         "&page=" + i;
            allMovies.addAll(getResults(url));
        }
        return allMovies;
    }

    private JsonObject getFirstResult(String url) throws IOException {
        List<JsonObject> results = getResults(url);
        return results.isEmpty() ? null : results.get(0);
    }

    private List<JsonObject> getResults(String url) throws IOException {
        JsonObject json = getJson(url);
        JsonArray arr = json.getAsJsonArray("results");
        List<JsonObject> list = new ArrayList<>();
        if (arr != null) {
            for (JsonElement e : arr) {
                list.add(e.getAsJsonObject());
            }
        }
        return list;
    }

    private JsonObject getJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return JsonParser.parseString(response.body().string()).getAsJsonObject();
            }
            throw new IOException("Resposta da API vazia.");
        }
    }
}