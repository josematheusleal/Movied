package recomendaFilmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AlgoritmosGrafo {
    private final Grafo grafo;
    
    public AlgoritmosGrafo(Grafo grafo) {
        this.grafo = grafo;
    }
    
    public List<Vertice> buscaEmLargura(Vertice inicio) {
        List<Vertice> visitados = new ArrayList<>();
        Queue<Vertice> fila = new LinkedList<>();
        Set<Vertice> marcados = new HashSet<>();
        
        fila.add(inicio);
        marcados.add(inicio);
        
        while (!fila.isEmpty()) {
            Vertice atual = fila.poll();
            visitados.add(atual);
            
            List<Vertice> vizinhos = grafo.getVizinhos(atual);
            for (Vertice vizinho : vizinhos) {
                if (!marcados.contains(vizinho)) {
                    marcados.add(vizinho);
                    fila.add(vizinho);
                }
            }
        }
        
        return visitados;
    }
    
    public List<Vertice> buscaEmProfundidade(Vertice inicio) {
        List<Vertice> visitados = new ArrayList<>();
        Set<Vertice> marcados = new HashSet<>();
        dfsRecursivo(inicio, marcados, visitados);
        return visitados;
    }
    
    private void dfsRecursivo(Vertice atual, Set<Vertice> marcados, List<Vertice> visitados) {
        marcados.add(atual);
        visitados.add(atual);
        
        List<Vertice> vizinhos = grafo.getVizinhos(atual);
        for (Vertice vizinho : vizinhos) {
            if (!marcados.contains(vizinho)) {
                dfsRecursivo(vizinho, marcados, visitados);
            }
        }
    }
    
    public List<Vertice> encontrarCaminho(Vertice origem, Vertice destino) {
        Map<Vertice, Vertice> predecessores = new HashMap<>();
        Queue<Vertice> fila = new LinkedList<>();
        Set<Vertice> visitados = new HashSet<>();
        
        fila.add(origem);
        visitados.add(origem);
        predecessores.put(origem, null);
        
        while (!fila.isEmpty()) {
            Vertice atual = fila.poll();
            
            if (atual.equals(destino)) {
                return reconstruirCaminho(predecessores, destino);
            }
            
            for (Vertice vizinho : grafo.getVizinhos(atual)) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    predecessores.put(vizinho, atual);
                    fila.add(vizinho);
                }
            }
        }
        
        return new ArrayList<>();
    }
    
    private List<Vertice> reconstruirCaminho(Map<Vertice, Vertice> predecessores, Vertice destino) {
        List<Vertice> caminho = new ArrayList<>();
        Vertice atual = destino;
        
        while (atual != null) {
            caminho.add(0, atual);
            atual = predecessores.get(atual);
        }
        
        return caminho;
    }
    
    public List<Set<Vertice>> encontrarComponentesConectados() {
        List<Set<Vertice>> componentes = new ArrayList<>();
        Set<Vertice> visitados = new HashSet<>();
        
        for (Vertice v : grafo.getVertices()) {
            if (!visitados.contains(v)) {
                Set<Vertice> componente = new HashSet<>();
                explorarComponente(v, visitados, componente);
                componentes.add(componente);
            }
        }
        
        return componentes;
    }
    
    private void explorarComponente(Vertice atual, Set<Vertice> visitados, Set<Vertice> componente) {
        visitados.add(atual);
        componente.add(atual);
        
        for (Vertice vizinho : grafo.getVizinhos(atual)) {
            if (!visitados.contains(vizinho)) {
                explorarComponente(vizinho, visitados, componente);
            }
        }
    }
    
    public Map<Vertice, Double> calcularCentralidadeGrau() {
        Map<Vertice, Double> centralidade = new HashMap<>();
        
        for (Vertice v : grafo.getVertices()) {
            int grau = grafo.getVizinhos(v).size();
            centralidade.put(v, (double) grau);
        }
        
        return centralidade;
    }
    
    public List<Vertice> detectarHubs(int topN) {
        Map<Vertice, Double> centralidade = calcularCentralidadeGrau();
        
        return centralidade.entrySet().stream()
                .sorted(Map.Entry.<Vertice, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(ArrayList::new, (list, entry) -> list.add(entry), ArrayList::addAll);
    }
}