package dev.watchbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.watchbox.player.PlatformPlayerView
import dev.watchbox.ui.screens.DetailsScreen
import dev.watchbox.ui.screens.HomeScreen
import dev.watchbox.ui.screens.LibraryScreen
import dev.watchbox.ui.screens.SearchScreen
import dev.watchbox.ui.theme.Coral500
import dev.watchbox.ui.theme.Navy900
import dev.watchbox.ui.theme.WatchBoxTheme

enum class Screen {
    HOME, SEARCH, LIBRARY, DETAILS, PLAYER
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

    WatchBoxTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (currentScreen) {
                Screen.PLAYER -> {
                    val details = uiState.selectedDetails
                    if (details != null) {
                        // If a specific episode is selected, override playback sources
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
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Navigation rail
                        NavigationRail(
                            currentScreen = currentScreen,
                            onNavigate = { currentScreen = it },
                        )
                        // Content
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
                                modifier = Modifier.weight(1f),
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
                                modifier = Modifier.weight(1f),
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
                                    modifier = Modifier.weight(1f),
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

@Composable
private fun NavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight().width(72.dp).background(Navy900).padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavItem("Home", currentScreen == Screen.HOME) { onNavigate(Screen.HOME) }
        Spacer(modifier = Modifier.height(8.dp))
        NavItem("Search", currentScreen == Screen.SEARCH) { onNavigate(Screen.SEARCH) }
        Spacer(modifier = Modifier.height(8.dp))
        NavItem("Library", currentScreen == Screen.LIBRARY) { onNavigate(Screen.LIBRARY) }
    }
}

@Composable
private fun NavItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Coral500 else MaterialTheme.colorScheme.onSurface,
        )
    }
}
