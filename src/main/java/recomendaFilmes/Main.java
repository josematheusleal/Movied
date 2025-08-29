package recomendaFilmes;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class Main {
    private static Grafo grafo;
    private static TMDbClient tmdb;
    private static Scanner sc;
    private static GeradorRelatorio relatorio;
    private static AlgoritmosGrafo algoritmos;
    private static boolean grafoCarregado = false;

    public static void main(String[] args) throws IOException {
        sc = new Scanner(System.in);
        tmdb = new TMDbClient();
        grafo = new Grafo();
        
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║          MOVIED - RECOMENDADOR        ║");
        System.out.println("║            Sistema de Grafos          ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        exibirMenu();
        sc.close();
    }
    
    private static void exibirMenu() throws IOException {
        while (true) {
            System.out.println("\n============ MENU PRINCIPAL ============");
            System.out.println("1. Carregar filme base e criar grafo");
            System.out.println("2. Obter recomendações");
            System.out.println("3. Gerar relatório completo");
            System.out.println("4. Analisar caminho entre filmes");
            System.out.println("5. Explorar filme (BFS)");
            System.out.println("6. Detectar hubs do grafo");
            System.out.println("7. Salvar relatório em arquivo");
            System.out.println("0. Sair");
            System.out.println("========================================");
            System.out.print("Escolha uma opção: ");
            
            int opcao = sc.nextInt();
            sc.nextLine();
            
            switch (opcao) {
                case 1 -> carregarFilmeBase();
                case 2 -> obterRecomendacoes();
                case 3 -> gerarRelatorio();
                case 4 -> analisarCaminho();
                case 5 -> explorarFilme();
                case 6 -> detectarHubs();
                case 7 -> salvarRelatorio();
                case 0 -> {
                    System.out.println("Encerrando o sistema...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }
    
    private static void carregarFilmeBase() throws IOException {
        System.out.print("\nDigite o nome de um filme: ");
        String filmeBaseNome = sc.nextLine();
        JsonObject filmeBaseJson = tmdb.searchMovie(filmeBaseNome);

        if (filmeBaseJson == null) {
            System.out.println("Filme não encontrado.");
            return;
        }

        grafo = new Grafo();
        
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

        System.out.println("\n✓ Filme base carregado: " + filmeBase.getNome());
        System.out.println("Buscando filmes semelhantes. Aguarde...");
        
        List<JsonObject> populares = tmdb.getPopularMovies(100);
        int filmesAdicionados = 0;
        
        for (JsonObject filme : populares) {
            int id = filme.get("id").getAsInt();
            if (id == idBase) continue;

            JsonObject detalhesComCreditos = tmdb.getMovieDetails(id);
            
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
            filmesAdicionados++;
            
            if (filmesAdicionados % 20 == 0) {
                System.out.print(".");
            }
        }

        System.out.println("\n✓ " + filmesAdicionados + " filmes adicionados ao grafo");
        System.out.println("Gerando conexões de similaridade...");
        grafo.gerarArestasSimilaridade();
        
        algoritmos = new AlgoritmosGrafo(grafo);
        relatorio = new GeradorRelatorio(grafo);
        grafoCarregado = true;
        
        System.out.println("✓ Grafo criado com sucesso!");
        System.out.println("Total de conexões: " + grafo.getArestas().size());
    }
    
    private static void obterRecomendacoes() {
        if (!verificarGrafoCarregado()) return;
        
        System.out.print("\nDigite o nome do filme para obter recomendações: ");
        String nomeFilme = sc.nextLine();
        Vertice filme = grafo.getVerticePorNome(nomeFilme);
        
        if (filme == null) {
            System.out.println("Filme não encontrado no grafo.");
            return;
        }
        
        System.out.print("Quantas recomendações deseja? ");
        int quantidade = sc.nextInt();
        sc.nextLine();
        
        List<Vertice> recomendacoes = grafo.recomendarFilmes(filme, quantidade);
        
        if (recomendacoes.isEmpty()) {
            System.out.println("Nenhuma recomendação encontrada para este filme.");
        } else {
            System.out.println("\n=== RECOMENDAÇÕES PARA '" + filme.getNome() + "' ===");
            for (int i = 0; i < recomendacoes.size(); i++) {
                Vertice rec = recomendacoes.get(i);
                System.out.println((i + 1) + ". " + rec.getNome());
                System.out.println("   Gêneros: " + String.join(", ", rec.getGeneros()));
            }
        }
    }
    
    private static void gerarRelatorio() {
        if (!verificarGrafoCarregado()) return;
        relatorio.gerarRelatorioCompleto();
    }
    
    private static void analisarCaminho() {
        if (!verificarGrafoCarregado()) return;
        
        System.out.print("\nDigite o nome do primeiro filme: ");
        String filme1 = sc.nextLine();
        System.out.print("Digite o nome do segundo filme: ");
        String filme2 = sc.nextLine();
        
        relatorio.exibirCaminhoEntreFilmes(filme1, filme2);
    }
    
    private static void explorarFilme() {
        if (!verificarGrafoCarregado()) return;
        
        System.out.print("\nDigite o nome do filme para explorar: ");
        String nomeFilme = sc.nextLine();
        Vertice filme = grafo.getVerticePorNome(nomeFilme);
        
        if (filme == null) {
            System.out.println("Filme não encontrado no grafo.");
            return;
        }
        
        System.out.println("\nExplorando vizinhança de '" + filme.getNome() + "':");
        List<Vertice> explorados = algoritmos.buscaEmLargura(filme);
        
        System.out.println("Filmes alcançáveis: " + explorados.size());
        System.out.println("\nPrimeiros 10 filmes na exploração:");
        for (int i = 0; i < Math.min(10, explorados.size()); i++) {
            System.out.println((i + 1) + ". " + explorados.get(i).getNome());
        }
    }
    
    private static void detectarHubs() {
        if (!verificarGrafoCarregado()) return;
        
        System.out.println("\n=== HUBS DO GRAFO ===");
        System.out.println("(Filmes com maior número de conexões)\n");
        
        List<Vertice> hubs = algoritmos.detectarHubs(5);
        for (int i = 0; i < hubs.size(); i++) {
            Vertice hub = hubs.get(i);
            int conexoes = grafo.getVizinhos(hub).size();
            System.out.println((i + 1) + ". " + hub.getNome() + " - " + conexoes + " conexões");
        }
    }
    
    private static void salvarRelatorio() {
        if (!verificarGrafoCarregado()) return;
        
        System.out.print("\nDigite o nome do arquivo (ex: relatorio.txt): ");
        String nomeArquivo = sc.nextLine();
        relatorio.salvarRelatorioArquivo(nomeArquivo);
    }
    
    private static boolean verificarGrafoCarregado() {
        if (!grafoCarregado) {
            System.out.println("\nÉ necessário carregar um filme base primeiro (Opção 1)");
            return false;
        }
        return true;
    }
}