package dev.watchbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.watchbox.ui.WatchBoxUiState
import dev.watchbox.ui.components.EmptyState
import dev.watchbox.ui.components.ErrorState
import dev.watchbox.ui.components.LoadingShimmer
import dev.watchbox.ui.components.MovieCard
import dev.watchbox.ui.components.SearchField

@Composable
fun SearchScreen(
    uiState: WatchBoxUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SearchField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                onQueryChanged(newQuery)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        when {
            uiState.isSearching -> LoadingShimmer()
            uiState.searchError != null -> ErrorState(
                message = uiState.searchError,
                onRetry = onRetry,
            )
            query.length >= 2 && uiState.searchResults.isEmpty() -> EmptyState(
                message = "No results",
            )
            uiState.searchResults.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(items = uiState.searchResults, key = { it.id }) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                    }
                }
            }
        }
    }
}
