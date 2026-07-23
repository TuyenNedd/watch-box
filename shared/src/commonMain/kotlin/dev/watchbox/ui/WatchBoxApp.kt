package dev.watchbox.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.watchbox.player.PlatformPlayerView
import dev.watchbox.ui.screens.BrowseMenuScreen
import dev.watchbox.ui.screens.BrowseScreen
import dev.watchbox.ui.screens.CountriesScreen
import dev.watchbox.ui.screens.DetailsScreen
import dev.watchbox.ui.screens.GenresScreen
import dev.watchbox.ui.screens.HomeScreen
import dev.watchbox.ui.screens.LibraryScreen
import dev.watchbox.ui.screens.SearchScreen
import dev.watchbox.ui.theme.Coral500
import dev.watchbox.ui.theme.Navy900
import dev.watchbox.ui.theme.WatchBoxTheme

enum class Screen {
    HOME, BROWSE, SEARCH, LIBRARY, DETAILS, PLAYER,
    BROWSE_TYPE, BROWSE_GENRE, BROWSE_COUNTRY,
    GENRES_LIST, COUNTRIES_LIST
}

@Composable
fun WatchBoxApp(
    viewModel: WatchBoxViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedMovieId by remember { mutableStateOf("") }
    var selectedEpisodeIndex by remember { mutableStateOf(0) }
    var browseType by remember { mutableStateOf("") }
    var browseGenreSlug by remember { mutableStateOf("") }
    var browseCountrySlug by remember { mutableStateOf("") }

    WatchBoxTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (currentScreen) {
                Screen.PLAYER -> {
                    val details = uiState.selectedDetails
                    if (details != null) {
                        val playDetails = if (selectedEpisodeIndex > 0 && selectedEpisodeIndex < details.episodes.size) {
                            val ep = details.episodes[selectedEpisodeIndex]
                            details.copy(
                                playbackSources = listOf(
                                    dev.watchbox.core.model.PlaybackSource(
                                        url = ep.streamUrl,
                                        mimeType = "application/x-mpegURL",
                                    )
                                )
                            )
                        } else details

                        PlatformPlayerView(
                            details = playDetails,
                            savedProgress = uiState.progress[selectedMovieId],
                            onSaveProgress = { pos, dur -> viewModel.saveProgress(selectedMovieId, pos, dur) },
                            onClearProgress = { viewModel.clearProgress(selectedMovieId) },
                            onBack = { currentScreen = Screen.DETAILS },
                        )
                    }
                }
                Screen.DETAILS -> {
                    val isFavorite = selectedMovieId in uiState.favorites
                    DetailsScreen(
                        details = uiState.selectedDetails,
                        isLoading = uiState.isLoadingDetails,
                        isFavorite = isFavorite,
                        progress = uiState.progress[selectedMovieId],
                        onPlay = {
                            selectedEpisodeIndex = 0
                            currentScreen = Screen.PLAYER
                        },
                        onEpisodeClick = { index ->
                            selectedEpisodeIndex = index
                            currentScreen = Screen.PLAYER
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(selectedMovieId) },
                        onBack = { currentScreen = Screen.HOME },
                    )
                }
                Screen.BROWSE_TYPE -> {
                    BrowseScreen(
                        browseState = uiState.browseState,
                        onMovieClick = { id ->
                            selectedMovieId = id
                            viewModel.loadDetails(id)
                            currentScreen = Screen.DETAILS
                        },
                        onPreviousPage = {
                            val prev = uiState.browseState.currentPage - 1
                            if (prev >= 1) viewModel.loadByType(browseType, prev)
                        },
                        onNextPage = {
                            val next = uiState.browseState.currentPage + 1
                            if (next <= uiState.browseState.totalPages) viewModel.loadByType(browseType, next)
                        },
                        onBack = { currentScreen = Screen.BROWSE },
                    )
                }
                Screen.BROWSE_GENRE -> {
                    BrowseScreen(
                        browseState = uiState.browseState,
                        onMovieClick = { id ->
                            selectedMovieId = id
                            viewModel.loadDetails(id)
                            currentScreen = Screen.DETAILS
                        },
                        onPreviousPage = {
                            val prev = uiState.browseState.currentPage - 1
                            if (prev >= 1) viewModel.loadByGenre(browseGenreSlug, prev)
                        },
                        onNextPage = {
                            val next = uiState.browseState.currentPage + 1
                            if (next <= uiState.browseState.totalPages) viewModel.loadByGenre(browseGenreSlug, next)
                        },
                        onBack = { currentScreen = Screen.GENRES_LIST },
                    )
                }
                Screen.BROWSE_COUNTRY -> {
                    BrowseScreen(
                        browseState = uiState.browseState,
                        onMovieClick = { id ->
                            selectedMovieId = id
                            viewModel.loadDetails(id)
                            currentScreen = Screen.DETAILS
                        },
                        onPreviousPage = {
                            val prev = uiState.browseState.currentPage - 1
                            if (prev >= 1) viewModel.loadByCountry(browseCountrySlug, prev)
                        },
                        onNextPage = {
                            val next = uiState.browseState.currentPage + 1
                            if (next <= uiState.browseState.totalPages) viewModel.loadByCountry(browseCountrySlug, next)
                        },
                        onBack = { currentScreen = Screen.COUNTRIES_LIST },
                    )
                }
                Screen.GENRES_LIST -> {
                    GenresScreen(
                        genres = uiState.genres,
                        isLoading = uiState.isLoadingGenres,
                        onGenreClick = { slug ->
                            browseGenreSlug = slug
                            viewModel.loadByGenre(slug)
                            currentScreen = Screen.BROWSE_GENRE
                        },
                        onBack = { currentScreen = Screen.BROWSE },
                    )
                }
                Screen.COUNTRIES_LIST -> {
                    CountriesScreen(
                        countries = uiState.countries,
                        isLoading = uiState.isLoadingCountries,
                        onCountryClick = { slug ->
                            browseCountrySlug = slug
                            viewModel.loadByCountry(slug)
                            currentScreen = Screen.BROWSE_COUNTRY
                        },
                        onBack = { currentScreen = Screen.BROWSE },
                    )
                }
                else -> {
                    Scaffold(
                        bottomBar = {
                            WatchBoxBottomBar(
                                currentScreen = currentScreen,
                                onNavigate = { currentScreen = it },
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                Screen.HOME -> HomeScreen(
                                    uiState = uiState,
                                    onMovieClick = { id ->
                                        selectedMovieId = id
                                        viewModel.loadDetails(id)
                                        currentScreen = Screen.DETAILS
                                    },
                                    onPlayClick = { id ->
                                        selectedMovieId = id
                                        viewModel.loadDetails(id)
                                        selectedEpisodeIndex = 0
                                        currentScreen = Screen.PLAYER
                                    },
                                    onRetry = { viewModel.retry() },
                                    onSeeAll = { type ->
                                        browseType = type
                                        viewModel.loadByType(type)
                                        currentScreen = Screen.BROWSE_TYPE
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Screen.BROWSE -> BrowseMenuScreen(
                                    onTypeClick = { type ->
                                        browseType = type
                                        viewModel.loadByType(type)
                                        currentScreen = Screen.BROWSE_TYPE
                                    },
                                    onGenresClick = {
                                        viewModel.loadGenres()
                                        currentScreen = Screen.GENRES_LIST
                                    },
                                    onCountriesClick = {
                                        viewModel.loadCountries()
                                        currentScreen = Screen.COUNTRIES_LIST
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Screen.SEARCH -> SearchScreen(
                                    uiState = uiState,
                                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                    onMovieClick = { id ->
                                        selectedMovieId = id
                                        viewModel.loadDetails(id)
                                        currentScreen = Screen.DETAILS
                                    },
                                    onRetry = { viewModel.retry() },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Screen.LIBRARY -> {
                                    val allMovies = uiState.shelves.flatMap { it.movies }.distinctBy { it.id }
                                    LibraryScreen(
                                        uiState = uiState,
                                        allMovies = allMovies,
                                        onMovieClick = { id ->
                                            selectedMovieId = id
                                            viewModel.loadDetails(id)
                                            currentScreen = Screen.DETAILS
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchBoxBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Navy900,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            label = { Text("Home") },
            icon = {},
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = Coral500,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Navy900,
            ),
        )
        NavigationBarItem(
            selected = currentScreen == Screen.BROWSE,
            onClick = { onNavigate(Screen.BROWSE) },
            label = { Text("Browse") },
            icon = {},
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = Coral500,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Navy900,
            ),
        )
        NavigationBarItem(
            selected = currentScreen == Screen.SEARCH,
            onClick = { onNavigate(Screen.SEARCH) },
            label = { Text("Search") },
            icon = {},
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = Coral500,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Navy900,
            ),
        )
        NavigationBarItem(
            selected = currentScreen == Screen.LIBRARY,
            onClick = { onNavigate(Screen.LIBRARY) },
            label = { Text("Library") },
            icon = {},
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = Coral500,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Navy900,
            ),
        )
    }
}
