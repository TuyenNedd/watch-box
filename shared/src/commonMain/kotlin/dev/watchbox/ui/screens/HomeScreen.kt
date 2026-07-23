package dev.watchbox.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.watchbox.ui.WatchBoxUiState
import dev.watchbox.ui.components.EmptyState
import dev.watchbox.ui.components.ErrorState
import dev.watchbox.ui.components.FeaturedHero
import dev.watchbox.ui.components.LoadingShimmer
import dev.watchbox.ui.components.MovieShelf

@Composable
fun HomeScreen(
    uiState: WatchBoxUiState,
    onMovieClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> LoadingShimmer(modifier = modifier)
        uiState.error != null -> ErrorState(
            message = uiState.error,
            onRetry = onRetry,
            modifier = modifier,
        )
        uiState.shelves.isEmpty() -> EmptyState(
            message = "No movies found.",
            modifier = modifier,
        )
        else -> {
            val featuredMovie = uiState.shelves.firstOrNull()?.movies?.firstOrNull()
            LazyColumn(modifier = modifier.fillMaxSize()) {
                if (featuredMovie != null) {
                    item(key = "hero") {
                        FeaturedHero(
                            movie = featuredMovie,
                            onPlay = { onPlayClick(featuredMovie.id) },
                            onDetails = { onMovieClick(featuredMovie.id) },
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                items(items = uiState.shelves, key = { it.title }) { shelf ->
                    MovieShelf(
                        title = shelf.title,
                        movies = shelf.movies,
                        onMovieClick = onMovieClick,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
