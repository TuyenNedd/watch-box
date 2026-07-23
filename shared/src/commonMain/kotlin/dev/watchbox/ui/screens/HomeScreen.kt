package dev.watchbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.watchbox.ui.SourceFilter
import dev.watchbox.ui.WatchBoxUiState
import dev.watchbox.ui.components.EmptyState
import dev.watchbox.ui.components.ErrorState
import dev.watchbox.ui.components.FeaturedHero
import dev.watchbox.ui.components.LoadingShimmer
import dev.watchbox.ui.components.MovieShelfWithSeeAll

@Composable
fun HomeScreen(
    uiState: WatchBoxUiState,
    onMovieClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onRetry: () -> Unit,
    onSeeAll: (type: String) -> Unit,
    onSourceSelected: (SourceFilter) -> Unit,
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
                // Source selector chips
                item(key = "source-selector") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SourceFilter.entries.forEach { source ->
                            FilterChip(
                                selected = uiState.selectedSource == source,
                                onClick = { onSourceSelected(source) },
                                label = { Text(source.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF6B5E),
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }
                if (featuredMovie != null) {
                    item(key = "hero") {
                        FeaturedHero(
                            movie = featuredMovie,
                            onPlay = { onPlayClick(featuredMovie.id) },
                            onDetails = { onMovieClick(featuredMovie.id) },
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                items(items = uiState.shelves, key = { it.title }) { shelf ->
                    val seeAllType = when (shelf.title) {
                        "New Movies" -> "phim-moi-cap-nhat"
                        "Featured" -> "phim-le"
                        else -> null
                    }
                    MovieShelfWithSeeAll(
                        title = shelf.title,
                        movies = shelf.movies,
                        onMovieClick = onMovieClick,
                        onSeeAll = seeAllType?.let { type -> { onSeeAll(type) } },
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
