package dev.watchbox.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SuggestionChip
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import dev.watchbox.tv.R
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackProgress
import dev.watchbox.tv.ui.components.LoadingShimmer

@OptIn(ExperimentalLayoutApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsScreen(
    details: MovieDetails?,
    isLoading: Boolean,
    isFavorite: Boolean,
    progress: PlaybackProgress?,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading || details == null -> LoadingShimmer(modifier = modifier)
        else -> {
            val movie = details.movie
            Box(modifier = modifier.fillMaxSize()) {
                // Backdrop
                AsyncImage(
                    model = movie.backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.9f),
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                ) {
                    // Poster
                    AsyncImage(
                        model = movie.artworkUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    // Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                        )
                        movie.originalTitle?.let { original ->
                            if (original != movie.title) {
                                Text(
                                    text = original,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Metadata chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            movie.year?.let {
                                SuggestionChip(onClick = {}) { Text(it.toString()) }
                            }
                            movie.runtimeMinutes?.let {
                                SuggestionChip(onClick = {}) { Text("${it} min") }
                            }
                            movie.quality?.let {
                                SuggestionChip(onClick = {}) {
                                    Text(stringResource(R.string.quality_label) + ": $it")
                                }
                            }
                            movie.lang?.let {
                                SuggestionChip(onClick = {}) { Text(it) }
                            }
                        }
                        // Categories as chips
                        if (movie.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.category_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                movie.categories.forEach { category ->
                                    SuggestionChip(onClick = {}) { Text(category) }
                                }
                            }
                        }
                        // Country chip
                        movie.country?.let { country ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.country_label) + ":",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                                SuggestionChip(onClick = {}) { Text(country) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = movie.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val playLabel = if (progress != null) {
                                stringResource(R.string.continue_watching)
                            } else {
                                stringResource(R.string.watch_now)
                            }
                            Button(onClick = { onPlay() }) {
                                Text(text = playLabel)
                            }
                            Button(onClick = { onToggleFavorite() }) {
                                val favText = if (isFavorite) {
                                    stringResource(R.string.remove_favorite)
                                } else {
                                    stringResource(R.string.add_favorite)
                                }
                                Text(text = favText)
                            }
                        }
                        // Episode list if series
                        if (details.episodes.size > 1) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.episodes_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(details.episodes) { episode ->
                                    SuggestionChip(onClick = {}) {
                                        Text(episode.name)
                                    }
                                }
                            }
                        }
                        // Actor list
                        if (details.actors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.cast_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                details.actors.forEach { actor ->
                                    SuggestionChip(onClick = {}) { Text(actor) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
