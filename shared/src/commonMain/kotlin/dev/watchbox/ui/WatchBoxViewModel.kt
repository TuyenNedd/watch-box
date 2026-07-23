package dev.watchbox.ui

import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackProgress
import dev.watchbox.core.util.matchesSearch
import dev.watchbox.data.catalog.MovieRepository
import dev.watchbox.data.local.LibraryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieShelf(
    val title: String,
    val movies: List<Movie>,
)

data class WatchBoxUiState(
    val shelves: List<MovieShelf> = emptyList(),
    val searchResults: List<Movie> = emptyList(),
    val selectedDetails: MovieDetails? = null,
    val favorites: Set<String> = emptySet(),
    val progress: Map<String, PlaybackProgress> = emptyMap(),
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val error: String? = null,
    val searchError: String? = null,
)

class WatchBoxViewModel(
    private val repository: MovieRepository,
    private val libraryStore: LibraryStore,
    private val scope: CoroutineScope,
) {
    private data class InternalState(
        val featured: List<Movie> = emptyList(),
        val backup: List<Movie> = emptyList(),
        val searchResults: List<Movie> = emptyList(),
        val selectedDetails: MovieDetails? = null,
        val isLoading: Boolean = true,
        val isSearching: Boolean = false,
        val isLoadingDetails: Boolean = false,
        val error: String? = null,
        val searchError: String? = null,
    )

    private val internal = MutableStateFlow(InternalState())
    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    val uiState: StateFlow<WatchBoxUiState> = combine(
        internal,
        libraryStore.favoriteIds,
        libraryStore.progress,
    ) { state, favIds, progressMap ->
        val shelves = buildShelves(state.featured, state.backup, favIds, progressMap)
        WatchBoxUiState(
            shelves = shelves,
            searchResults = state.searchResults,
            selectedDetails = state.selectedDetails,
            favorites = favIds,
            progress = progressMap,
            isLoading = state.isLoading,
            isSearching = state.isSearching,
            isLoadingDetails = state.isLoadingDetails,
            error = state.error,
            searchError = state.searchError,
        )
    }.stateIn(scope, SharingStarted.Eagerly, WatchBoxUiState())

    init {
        loadCatalog()
        observeSearch()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun loadDetails(movieId: String) {
        scope.launch {
            internal.update { it.copy(isLoadingDetails = true, selectedDetails = null) }
            try {
                val result = repository.detailsResult(movieId)
                internal.update { it.copy(selectedDetails = result.data, isLoadingDetails = false) }
            } catch (_: Exception) {
                internal.update {
                    it.copy(
                        isLoadingDetails = false,
                        error = "Unable to load movie details. Please try again.",
                    )
                }
            }
        }
    }

    fun toggleFavorite(movieId: String) {
        libraryStore.toggleFavorite(movieId)
    }

    fun saveProgress(movieId: String, positionMs: Long, durationMs: Long?) {
        val progress = PlaybackProgress(
            positionMs = positionMs,
            updatedAtEpochMs = currentTimeMs(),
        )
        libraryStore.saveProgress(movieId, progress, durationMs)
    }

    fun clearProgress(movieId: String) {
        libraryStore.clearProgress(movieId)
    }

    fun retry() {
        internal.update { it.copy(error = null) }
        loadCatalog()
    }

    private fun loadCatalog() {
        scope.launch {
            internal.update { it.copy(isLoading = true, error = null) }
            try {
                val featured = repository.featured()
                internal.update { it.copy(featured = featured, isLoading = false) }
            } catch (_: Exception) {
                internal.update {
                    it.copy(
                        isLoading = false,
                        error = "Unable to connect. Check your network and try again.",
                    )
                }
                return@launch
            }
            try {
                val backup = repository.search("")
                internal.update { it.copy(backup = backup) }
            } catch (_: Exception) {
                // non-critical
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    private fun observeSearch() {
        searchQuery
            .debounce(300L)
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length < 2) {
                    internal.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
                    return@onEach
                }
                searchJob?.cancel()
                searchJob = scope.launch {
                    internal.update { it.copy(isSearching = true, searchError = null) }
                    try {
                        val allMovies = internal.value.featured + internal.value.backup
                        val local = allMovies.filter { it.matchesSearch(query) }
                        internal.update { it.copy(searchResults = local) }
                        val remote = repository.search(query)
                        val merged = LinkedHashMap<String, Movie>()
                        local.forEach { if (it.id !in merged) merged[it.id] = it }
                        remote.forEach { if (it.id !in merged) merged[it.id] = it }
                        internal.update { it.copy(searchResults = merged.values.toList(), isSearching = false) }
                    } catch (_: Exception) {
                        internal.update {
                            it.copy(isSearching = false, searchError = "Search failed. Please try again.")
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    private fun buildShelves(
        featured: List<Movie>,
        backup: List<Movie>,
        favoriteIds: Set<String>,
        progressMap: Map<String, PlaybackProgress>,
    ): List<MovieShelf> {
        val shelves = mutableListOf<MovieShelf>()
        if (featured.isNotEmpty()) {
            shelves += MovieShelf("New Movies", featured)
        }
        val featuredIds = featured.map { it.id }.toSet()
        val deduplicatedBackup = backup.filter { it.id !in featuredIds }
        if (deduplicatedBackup.isNotEmpty()) {
            shelves += MovieShelf("Featured", deduplicatedBackup)
        }
        val continueWatching = (featured + deduplicatedBackup)
            .filter { it.id in progressMap }
            .distinctBy { it.id }
        if (continueWatching.isNotEmpty()) {
            shelves += MovieShelf("Continue Watching", continueWatching)
        }
        return shelves
    }
}

internal expect fun currentTimeMs(): Long
