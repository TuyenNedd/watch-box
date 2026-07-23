package dev.watchbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.watchbox.core.model.Movie
import dev.watchbox.ui.WatchBoxUiState
import dev.watchbox.ui.components.EmptyState
import dev.watchbox.ui.components.MovieCard
import dev.watchbox.ui.theme.Coral500

@Composable
fun LibraryScreen(
    uiState: WatchBoxUiState,
    allMovies: List<Movie>,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Favorites", "Continue Watching")

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedTab = index },
                    colors = if (index == selectedTab) {
                        ButtonDefaults.buttonColors(containerColor = Coral500)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    },
                ) {
                    Text(title)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        val movies = when (selectedTab) {
            0 -> allMovies.filter { it.id in uiState.favorites }
            else -> allMovies.filter { it.id in uiState.progress }
        }

        if (movies.isEmpty()) {
            val emptyMessage = when (selectedTab) {
                0 -> "No favorite movies yet."
                else -> "No movies in progress."
            }
            EmptyState(message = emptyMessage)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = movies, key = { it.id }) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}
