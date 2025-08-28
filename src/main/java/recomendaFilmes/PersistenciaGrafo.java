package recomendaFilmes;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PersistenciaGrafo {
    private static final String VERSAO = "1.0";
    private final Gson gson;
    
    public PersistenciaGrafo() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }
    
    public void salvarGrafo(Grafo grafo, String nomeArquivo) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("versao", VERSAO);
        root.addProperty("dataExportacao", new Date().toString());
        
        JsonArray verticesArray = new JsonArray();
        for (Vertice v : grafo.getVertices()) {
            JsonObject verticeJson = new JsonObject();
            verticeJson.addProperty("nome", v.getNome());
            verticeJson.addProperty("idTmdb", v.getIdTmdb());
            verticeJson.addProperty("atorPrincipal", v.getAtorPrincipal());
            verticeJson.addProperty("diretor", v.getDiretor());
            verticeJson.addProperty("produtora", v.getProdutora());
            
            JsonArray generosArray = new JsonArray();
            for (String genero : v.getGeneros()) {
                generosArray.add(genero);
            }
            verticeJson.add("generos", generosArray);
            
            verticesArray.add(verticeJson);
        }
        root.add("vertices", verticesArray);
        
        JsonArray arestasArray = new JsonArray();
        Set<String> arestasProcessadas = new HashSet<>();
        
        for (Aresta a : grafo.getArestas()) {
            String chave = a.getOrigem().getNome() + "|" + a.getDestino().getNome();
            String chaveInversa = a.getDestino().getNome() + "|" + a.getOrigem().getNome();
            
            if (!arestasProcessadas.contains(chave) && !arestasProcessadas.contains(chaveInversa)) {
                JsonObject arestaJson = new JsonObject();
                arestaJson.addProperty("origem", a.getOrigem().getNome());
                arestaJson.addProperty("destino", a.getDestino().getNome());
                arestaJson.addProperty("peso", a.getPeso());
                arestasArray.add(arestaJson);
                arestasProcessadas.add(chave);
            }
        }
        root.add("arestas", arestasArray);
        
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            gson.toJson(root, writer);
        }
        
        System.out.println("✓ Grafo salvo com sucesso em: " + nomeArquivo);
        System.out.println("  - " + grafo.getVertices().size() + " vértices");
        System.out.println("  - " + arestasProcessadas.size() + " arestas únicas");
    }
    
    public Grafo carregarGrafo(String nomeArquivo) throws IOException {
        Grafo grafo = new Grafo();
        
        try (FileReader reader = new FileReader(nomeArquivo)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            String versao = root.get("versao").getAsString();
            if (!versao.equals(VERSAO)) {
                System.out.println("⚠ Aviso: Versão do arquivo diferente da atual");
            }
            
            JsonArray verticesArray = root.getAsJsonArray("vertices");
            for (JsonElement elem : verticesArray) {
                JsonObject vJson = elem.getAsJsonObject();
                
                List<String> generos = new ArrayList<>();
                JsonArray generosArray = vJson.getAsJsonArray("generos");
                for (JsonElement genero : generosArray) {
                    generos.add(genero.getAsString());
                }
                
                Vertice vertice = new Vertice(
                    vJson.get("nome").getAsString(),
                    vJson.get("idTmdb").getAsInt(),
                    generos,
                    getStringOrNull(vJson, "atorPrincipal"),
                    getStringOrNull(vJson, "diretor"),
                    getStringOrNull(vJson, "produtora")
                );
                
                grafo.adicionarVertice(vertice);
            }
            
            JsonArray arestasArray = root.getAsJsonArray("arestas");
            for (JsonElement elem : arestasArray) {
                JsonObject aJson = elem.getAsJsonObject();
                
                String nomeOrigem = aJson.get("origem").getAsString();
                String nomeDestino = aJson.get("destino").getAsString();
                double peso = aJson.get("peso").getAsDouble();
                
                Vertice origem = grafo.getVerticePorNome(nomeOrigem);
                Vertice destino = grafo.getVerticePorNome(nomeDestino);
                
                if (origem != null && destino != null) {
                    grafo.adicionarAresta(origem, destino, peso);
                    grafo.adicionarAresta(destino, origem, peso);
                }
            }
            
            System.out.println("✓ Grafo carregado com sucesso de: " + nomeArquivo);
            System.out.println("  - " + grafo.getVertices().size() + " vértices");
            System.out.println("  - " + grafo.getArestas().size() + " arestas");
        }
        
        return grafo;
    }
    
    private String getStringOrNull(JsonObject obj, String key) {
        JsonElement elem = obj.get(key);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsString();
        }
        return null;
    }
    
    public void exportarParaCSV(Grafo grafo, String prefixoArquivo) throws IOException {
        exportarVerticesCSV(grafo, prefixoArquivo + "_vertices.csv");
        exportarArestasCSV(grafo, prefixoArquivo + "_arestas.csv");
        System.out.println("✓ Dados exportados para CSV");
    }
    
    private void exportarVerticesCSV(Grafo grafo, String nomeArquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("Nome,ID_TMDb,Generos,Ator_Principal,Diretor,Produtora");
            
            for (Vertice v : grafo.getVertices()) {
                writer.printf("%s,%d,\"%s\",%s,%s,%s\n",
                    v.getNome(),
                    v.getIdTmdb(),
                    String.join(";", v.getGeneros()),
                    v.getAtorPrincipal() != null ? v.getAtorPrincipal() : "",
                    v.getDiretor() != null ? v.getDiretor() : "",
                    v.getProdutora() != null ? v.getProdutora() : ""
                );
            }
        }
    }
    
    private void exportarArestasCSV(Grafo grafo, String nomeArquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("Origem,Destino,Peso");
            
            Set<String> processadas = new HashSet<>();
            for (Aresta a : grafo.getArestas()) {
                String chave = a.getOrigem().getNome() + "|" + a.getDestino().getNome();
                String chaveInversa = a.getDestino().getNome() + "|" + a.getOrigem().getNome();
                
                if (!processadas.contains(chave) && !processadas.contains(chaveInversa)) {
                    writer.printf("%s,%s,%.4f\n",
                        a.getOrigem().getNome(),
                        a.getDestino().getNome(),
                        a.getPeso()
                    );
                    processadas.add(chave);
                }
            }
        }
    }
}