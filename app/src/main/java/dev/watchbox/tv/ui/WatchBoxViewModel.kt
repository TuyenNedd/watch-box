package dev.watchbox.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackProgress
import dev.watchbox.tv.core.util.matchesSearch
import dev.watchbox.tv.data.catalog.MovieRepository
import dev.watchbox.tv.data.local.LibraryStore
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
class WatchBoxViewModel(
    private val repository: MovieRepository,
    private val libraryStore: LibraryStore,
) : ViewModel() {

    private data class InternalState(
        val featured: List<Movie> = emptyList(),
        val archive: List<Movie> = emptyList(),
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
        val shelves = buildShelves(state.featured, state.archive, favIds, progressMap)
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WatchBoxUiState())

    init {
        loadCatalog()
        observeSearch()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun loadDetails(movieId: String) {
        viewModelScope.launch {
            internal.update { it.copy(isLoadingDetails = true, selectedDetails = null) }
            try {
                val result = repository.detailsResult(movieId)
                internal.update { it.copy(selectedDetails = result.data, isLoadingDetails = false) }
            } catch (_: Exception) {
                internal.update {
                    it.copy(
                        isLoadingDetails = false,
                        error = "Không thể tải thông tin phim. Vui lòng thử lại.",
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
            updatedAtEpochMs = System.currentTimeMillis(),
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

    fun clearError() {
        internal.update { it.copy(error = null, searchError = null) }
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            internal.update { it.copy(isLoading = true, error = null) }
            try {
                val featured = repository.featured()
                internal.update { it.copy(featured = featured, isLoading = false) }
            } catch (_: Exception) {
                internal.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể kết nối máy chủ. Kiểm tra mạng và thử lại.",
                    )
                }
                return@launch
            }
            // Load archive async
            try {
                val archive = repository.search("")
                internal.update { it.copy(archive = archive) }
            } catch (_: Exception) {
                // Archive failure is non-critical
            }
        }
    }

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
                searchJob = viewModelScope.launch {
                    internal.update { it.copy(isSearching = true, searchError = null) }
                    try {
                        // Local search first (accent-insensitive)
                        val allMovies = internal.value.featured + internal.value.archive
                        val local = allMovies.filter { it.matchesSearch(query) }
                        internal.update { it.copy(searchResults = local) }
                        // Remote search
                        val remote = repository.search(query)
                        val merged = LinkedHashMap<String, Movie>()
                        local.forEach { merged.putIfAbsent(it.id, it) }
                        remote.forEach { merged.putIfAbsent(it.id, it) }
                        internal.update { it.copy(searchResults = merged.values.toList(), isSearching = false) }
                    } catch (_: Exception) {
                        internal.update {
                            it.copy(
                                isSearching = false,
                                searchError = "Tìm kiếm thất bại. Vui lòng thử lại.",
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun buildShelves(
        featured: List<Movie>,
        archive: List<Movie>,
        favoriteIds: Set<String>,
        progressMap: Map<String, PlaybackProgress>,
    ): List<MovieShelf> {
        val shelves = mutableListOf<MovieShelf>()
        if (featured.isNotEmpty()) {
            shelves += MovieShelf("Phim mở nổi bật", featured)
        }
        if (archive.isNotEmpty()) {
            shelves += MovieShelf("Kho phim cộng đồng", archive)
        }
        val continueWatching = (featured + archive)
            .filter { it.id in progressMap }
            .distinctBy { it.id }
        if (continueWatching.isNotEmpty()) {
            shelves += MovieShelf("Xem tiếp", continueWatching)
        }
        return shelves
    }

    class Factory(
        private val repository: MovieRepository,
        private val libraryStore: LibraryStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WatchBoxViewModel(repository, libraryStore) as T
        }
    }
}
