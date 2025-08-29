package recomendaFilmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FiltroRecomendacao {
    private final Grafo grafo;
    
    public FiltroRecomendacao(Grafo grafo) {
        this.grafo = grafo;
    }
    
    public List<Vertice> filtrarPorGenero(List<Vertice> filmes, String genero) {
        return filmes.stream()
                .filter(filme -> filme.getGeneros().contains(genero))
                .collect(Collectors.toList());
    }
    
    public List<Vertice> filtrarPorDiretor(List<Vertice> filmes, String diretor) {
        return filmes.stream()
                .filter(filme -> Objects.equals(filme.getDiretor(), diretor))
                .collect(Collectors.toList());
    }
    
    public List<Vertice> filtrarPorAtor(List<Vertice> filmes, String ator) {
        return filmes.stream()
                .filter(filme -> Objects.equals(filme.getAtorPrincipal(), ator))
                .collect(Collectors.toList());
    }
    
    public List<Vertice> filtrarPorProdutora(List<Vertice> filmes, String produtora) {
        return filmes.stream()
                .filter(filme -> Objects.equals(filme.getProdutora(), produtora))
                .collect(Collectors.toList());
    }
    
    public List<Vertice> filtrarPorSimilaridadeMinima(Vertice filmeBase, double similaridadeMinima) {
        return grafo.getArestas().stream()
                .filter(a -> a.getOrigem().equals(filmeBase) && a.getPeso() >= similaridadeMinima)
                .sorted((a1, a2) -> Double.compare(a2.getPeso(), a1.getPeso()))
                .map(Aresta::getDestino)
                .collect(Collectors.toList());
    }
    
    public List<Vertice> recomendarComFiltros(Vertice filmeBase, int quantidade, FiltroConfig config) {
        List<Vertice> candidatos = grafo.recomendarFilmes(filmeBase, quantidade * 3);
        
        if (config.generoDesejado != null) {
            candidatos = filtrarPorGenero(candidatos, config.generoDesejado);
        }
        
        if (config.diretorDesejado != null) {
            candidatos = filtrarPorDiretor(candidatos, config.diretorDesejado);
        }
        
        if (config.atorDesejado != null) {
            candidatos = filtrarPorAtor(candidatos, config.atorDesejado);
        }
        
        if (config.similaridadeMinima > 0) {
            candidatos = filtrarPorSimilaridadeMinima(filmeBase, config.similaridadeMinima);
        }
        
        return candidatos.stream().limit(quantidade).collect(Collectors.toList());
    }
    
    public Map<String, List<Vertice>> agruparPorGenero(List<Vertice> filmes) {
        Map<String, List<Vertice>> grupos = new HashMap<>();
        
        for (Vertice filme : filmes) {
            for (String genero : filme.getGeneros()) {
                grupos.computeIfAbsent(genero, k -> new ArrayList<>()).add(filme);
            }
        }
        
        return grupos;
    }
    
    public Map<String, List<Vertice>> agruparPorDiretor(List<Vertice> filmes) {
        return filmes.stream()
                .filter(f -> f.getDiretor() != null)
                .collect(Collectors.groupingBy(Vertice::getDiretor));
    }
    
    public List<Vertice> obterFilmesDiversos(Vertice filmeBase, int quantidade) {
        Set<String> generosUsados = new HashSet<>();
        Set<String> diretoresUsados = new HashSet<>();
        List<Vertice> diversos = new ArrayList<>();
        
        List<Vertice> candidatos = grafo.recomendarFilmes(filmeBase, quantidade * 5);
        
        for (Vertice candidato : candidatos) {
            boolean novoGenero = candidato.getGeneros().stream()
                    .anyMatch(g -> !generosUsados.contains(g));
            boolean novoDiretor = candidato.getDiretor() != null && 
                    !diretoresUsados.contains(candidato.getDiretor());
            
            if (novoGenero || novoDiretor) {
                diversos.add(candidato);
                generosUsados.addAll(candidato.getGeneros());
                if (candidato.getDiretor() != null) {
                    diretoresUsados.add(candidato.getDiretor());
                }
                
                if (diversos.size() >= quantidade) {
                    break;
                }
            }
        }
        
        return diversos;
    }
    
    public static class FiltroConfig {
        public String generoDesejado;
        public String diretorDesejado;
        public String atorDesejado;
        public double similaridadeMinima;
        
        public FiltroConfig() {
            this.similaridadeMinima = 0.0;
        }
    }
}