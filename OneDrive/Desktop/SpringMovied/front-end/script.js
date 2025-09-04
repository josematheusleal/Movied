const searchButton = document.getElementById('search-button');
const movieInput = document.getElementById('movie-input');
const resultsList = document.getElementById('results-list');
const loadingIndicator = document.getElementById('loading');
const suggestionsBox = document.getElementById('suggestions');

let typingTimeout;
const TMDB_API_KEY = "baf716a6b18b1abc236b34dc1429f5d5"; // sua chave

// ---------- AUTOCOMPLETE ----------
movieInput.addEventListener('input', () => {
  const query = movieInput.value.trim();
  clearTimeout(typingTimeout);
  if (query.length < 2) {
    suggestionsBox.classList.add('hidden');
    return;
  }
  typingTimeout = setTimeout(() => {
    fetch(`https://api.themoviedb.org/3/search/movie?api_key=${TMDB_API_KEY}&language=pt-BR&query=${encodeURIComponent(query)}`)
      .then(r => r.json())
      .then(data => renderSuggestions(data.results))
      .catch(err => {
        console.error('Erro TMDb autocomplete', err);
        suggestionsBox.classList.add('hidden');
      });
  }, 300);
});

function renderSuggestions(movies) {
  if (!movies || movies.length === 0) {
    suggestionsBox.classList.add('hidden');
    return;
  }
  suggestionsBox.innerHTML = '';
  movies.slice(0, 8).forEach(m => {
    const item = document.createElement('div');
    item.className = 'suggestion-item';
    const poster = m.poster_path ? `https://image.tmdb.org/t/p/w92${m.poster_path}` : 'https://via.placeholder.com/50x75?text=?';
    const year = m.release_date ? m.release_date.substring(0,4) : 'N/A';
    item.innerHTML = `<img src="${poster}" alt="${m.title}"><div class="suggestion-info"><div class="suggestion-title">${escapeHtml(m.title)}</div><div class="suggestion-year">${year}</div></div>`;
    item.addEventListener('click', () => {
      movieInput.value = m.title;
      suggestionsBox.classList.add('hidden');
    });
    suggestionsBox.appendChild(item);
  });
  suggestionsBox.classList.remove('hidden');
}

// ---------- BUSCAR RECOMENDAÇÕES ----------
searchButton.addEventListener('click', async () => {
  const movieName = movieInput.value.trim();
  if (!movieName) { alert('Por favor, digite o nome de um filme.'); return; }

  resultsList.innerHTML = '';
  loadingIndicator.classList.remove('hidden');
  suggestionsBox.classList.add('hidden');

  try {
    const resp = await fetch(`http://localhost:8080/recommendations?movieName=${encodeURIComponent(movieName)}`);
    if (!resp.ok) throw new Error('HTTP ' + resp.status);
    let data = await resp.json();
    loadingIndicator.classList.add('hidden');

    if (!Array.isArray(data) || data.length === 0) {
      resultsList.innerHTML = '<li class="no-results">Nenhuma recomendação encontrada.</li>';
      return;
    }

    // Normaliza (multiplica por 100 se campos vierem 0..1), arredonda e ordena
    function normalizeDto(dto) {
      const keys = ['actor_contribution','director_contribution','production_contribution','keywords_contribution','score'];
      keys.forEach(k => dto[k] = Number(dto[k] ?? 0));
      const maxVal = Math.max(...keys.map(k => dto[k]));
      if (maxVal <= 1) keys.forEach(k => dto[k] = dto[k] * 100);
      keys.forEach(k => dto[k] = Math.round((Number(dto[k] || 0)) * 10) / 10);
      return dto;
    }

    data = data.map(normalizeDto).sort((a,b) => (Number(b.score)||0) - (Number(a.score)||0)).slice(0, 10);

    // Renderiza
    data.forEach(dto => createMovieCard(dto));

  } catch (err) {
    loadingIndicator.classList.add('hidden');
    console.error('Erro ao buscar recomendações:', err);
    resultsList.innerHTML = '<li class="no-results">Erro ao conectar com o servidor. Verifique o backend.</li>';
  }
});

// ---------- Função que cria o card com breakdown ----------
function createMovieCard(movie) {
  const listItem = document.createElement('li');
  listItem.classList.add('movie-card');

  const posterUrl = movie.poster_path ? `https://image.tmdb.org/t/p/w300${movie.poster_path}` : 'https://via.placeholder.com/300x450/222/fff?text=Sem+Imagem';
  const rating = movie.vote_average ? Number(movie.vote_average).toFixed(1) : 'N/A';

  const actorPct = Number(movie.actor_contribution || 0);
  const directorPct = Number(movie.director_contribution || 0);
  const productionPct = Number(movie.production_contribution || 0);
  const keywordsPct = Number(movie.keywords_contribution || 0);
  const totalScore = Number(movie.score || 0);

  const actorText = movie.same_main_actor ? 'Mesmo ator principal' : 'Ator principal diferente';
  const directorText = movie.same_director ? 'Mesmo diretor' : 'Diretor diferente';
  const productionText = movie.same_production_company ? 'Mesma produtora' : 'Produtora diferente';

  listItem.innerHTML = `
<div class="movie-poster" style="background-image: url('${posterUrl}')"></div>
    <div class="movie-info">
      <div class="movie-title">${escapeHtml(movie.title)}</div>
      <div class="movie-subline">
        <div class="movie-rating"><i class="fas fa-star"></i> ${rating}</div>
        <div class="movie-score-badge">Score: <strong>${totalScore}</strong>/100</div>
      </div>

      <div class="similarity">
        <div class="badges">
          <span class="badge ${movie.same_main_actor ? 'match' : 'no-match'}">${actorText}</span>
          <span class="badge ${movie.same_director ? 'match' : 'no-match'}">${directorText}</span>
          <span class="badge ${movie.same_production_company ? 'match' : 'no-match'}">${productionText}</span>
        </div>

        <div class="criteria">
          <div class="criterion">
            <div class="criterion-label">Palavras-chave <span class="criterion-value">(${movie.shared_keywords_count}/${movie.total_keywords_count})</span></div>
          </div>
        </div>
      </div>
    </div>
  `;

  resultsList.appendChild(listItem);
}

// ---------- Helpers ----------
function clampPct(v) {
  const n = Number(v) || 0;
  if (n < 0) return 0;
  if (n > 100) return 100;
  return Math.round(n * 10) / 10;
}

function escapeHtml(text) {
  if (!text && text !== 0) return '';
  return String(text)
    .replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;')
    .replaceAll('"','&quot;').replaceAll("'",'&#39;');
}

// fechar sugestões ao clicar fora
document.addEventListener('click', (e) => {
  if (!movieInput.contains(e.target) && !suggestionsBox.contains(e.target)) {
    suggestionsBox.classList.add('hidden');
  }
});
