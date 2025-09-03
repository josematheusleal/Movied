// Pega os elementos do HTML
const searchButton = document.getElementById('search-button');
const movieInput = document.getElementById('movie-input');
const resultsList = document.getElementById('results-list');
const loadingIndicator = document.getElementById('loading');
const suggestionsBox = document.getElementById('suggestions');

let typingTimeout;
const TMDB_API_KEY = "baf716a6b18b1abc236b34dc1429f5d5"; // coloque sua chave TMDb aqui

// ---------------- AUTOCOMPLETE ---------------- //
movieInput.addEventListener('input', () => {
    const query = movieInput.value.trim();
    clearTimeout(typingTimeout);

    if (query.length < 2) {
        suggestionsBox.classList.add('hidden');
        return;
    }

    // delay para evitar chamar a API a cada caractere digitado
    typingTimeout = setTimeout(() => {
        fetch(`https://api.themoviedb.org/3/search/movie?api_key=${TMDB_API_KEY}&query=${encodeURIComponent(query)}`)
            .then(res => res.json())
            .then(data => {
                renderSuggestions(data.results);
            })
            .catch(err => console.error("Erro na busca TMDb:", err));
    }, 300);
});

function renderSuggestions(movies) {
    if (!movies || movies.length === 0) {
        suggestionsBox.classList.add('hidden');
        return;
    }

    suggestionsBox.innerHTML = '';
    movies.slice(0, 8).forEach(movie => {
        const item = document.createElement('div');
        item.classList.add('suggestion-item');

        const posterUrl = movie.poster_path 
            ? `https://image.tmdb.org/t/p/w92${movie.poster_path}` 
            : 'https://via.placeholder.com/40x60?text=?';

        item.innerHTML = `
            <img src="${posterUrl}" alt="${movie.title}">
            <span>${movie.title}</span>
        `;

        // Clique na sugestão -> preencher input
        item.addEventListener('click', () => {
            movieInput.value = movie.title;
            suggestionsBox.classList.add('hidden');
        });

        suggestionsBox.appendChild(item);
    });

    suggestionsBox.classList.remove('hidden');
}

// ---------------- BUSCAR RECOMENDAÇÕES NA SUA API ---------------- //
searchButton.addEventListener('click', () => {
    const movieName = movieInput.value.trim();
    if (movieName === "") {
        alert("Por favor, digite o nome de um filme.");
        return;
    }

    // Limpa resultados antigos e mostra o indicador de "carregando"
    resultsList.innerHTML = '';
    loadingIndicator.classList.remove('hidden');

    // Faz a chamada (requisição) para a sua API Java
    fetch(`http://localhost:8080/recommendations?movieName=${encodeURIComponent(movieName)}`)
        .then(response => response.json()) // Converte JSON em objeto
        .then(data => {
            // Esconde o "carregando"
            loadingIndicator.classList.add('hidden');
            
            if (data.length === 0) {
                 resultsList.innerHTML = '<li>Nenhuma recomendação encontrada.</li>';
            } else {
                // Para cada recomendação recebida, cria um item na lista
                data.forEach(movieTitle => {
                    const listItem = document.createElement('li');
                    listItem.textContent = movieTitle;
                    resultsList.appendChild(listItem);
                });
            }
        })
        .catch(error => {
            // Lida com erros de conexão
            loadingIndicator.classList.add('hidden');
            resultsList.innerHTML = '<li>Erro ao conectar com o servidor.</li>';
            console.error('Erro:', error);
        });
});
