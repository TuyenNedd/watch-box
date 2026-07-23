package dev.watchbox.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import dev.watchbox.tv.R
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.ui.WatchBoxUiState
import dev.watchbox.tv.ui.components.EmptyState
import dev.watchbox.tv.ui.components.MovieCard

@Composable
fun LibraryScreen(
    uiState: WatchBoxUiState,
    allMovies: List<Movie>,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.favorites),
        stringResource(R.string.continue_watching),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
    ) {
        Text(
            text = stringResource(R.string.nav_library),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = index == selectedTab,
                    onFocus = { selectedTab = index },
                    onClick = { selectedTab = index },
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
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
                0 -> stringResource(R.string.no_favorites)
                else -> stringResource(R.string.no_continue_watching)
            }
            EmptyState(message = emptyMessage)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = movies,
                    key = { it.id },
                ) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) },
                    )
                }
            }
        }
    }
}
