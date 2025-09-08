Movied API - Sistema de Recomendação de Filmes
Este projeto é uma API REST desenvolvida em Java com Spring Boot que oferece recomendações de filmes com base em um grafo de similaridade. O sistema constrói uma rede de filmes onde as conexões representam o quão parecidos eles são, permitindo sugestões personalizadas e precisas. Aqui está como ele opera:

Funcionamento do Sistema:

1. Coleta de Dados na Inicialização
- Ao iniciar, a API conecta-se automaticamente ao TMDb (The Movie Database)
- Busca 300 páginas de filmes populares (cerca de 6.000 filmes). Essa quantidade pode ser alterada no método "construirGrafoCompleto", da classe "RecommendationGraphService"
- Coleta metadados de cada filme: gêneros, diretor, ator principal, produtora e palavras-chave

2. Construção do Grafo
- Após uma pausa de 10~15min (dependendo da quantidade de páginas escolhidas para a construção do grafo). Após a contrução é exibida uma mensagem de confirmação no terminal "Grafo concluído com sucesso!".
- Cada filme vira um vértice no grafo
- O cálculo de similaridade entre filmes é executado em segundo plano
- As conexões entre filmes (arestas) recebem "pesos" baseados na similaridade

3. Geração de Recomendações
- Para Usar: Abrir o arquivo "index.html" na pasta "front-end" após o terminal mostrar a mensagem de conclusão do grafo.
- Retorna os filmes com maiores pesos de conexão (mais similares)
- O número de recomendações é configurável

Tecnologia Principal:
- Java + Spring Boot com grafo construído em memória
- A inicialização é lenta (minutos) pois processa milhares de filmes
- As recomendações são geradas instantaneamente após o grafo estar pronto

Uso:
- Acesse via endpoint: GET /api/recommendations?movieName=NomeDoFilme&count=5
- Retorna lista de filmes similares ao título consultado
