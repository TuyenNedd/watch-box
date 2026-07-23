package dev.watchbox.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackProgress
import dev.watchbox.ui.components.LoadingShimmer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    details: MovieDetails?,
    isLoading: Boolean,
    isFavorite: Boolean,
    progress: PlaybackProgress?,
    onPlay: () -> Unit,
    onEpisodeClick: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading || details == null -> LoadingShimmer(modifier = modifier)
        else -> {
            val movie = details.movie
            Box(modifier = modifier.fillMaxSize()) {
                AsyncImage(
                    model = movie.backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.9f),
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                            ),
                        ),
                    ),
                )
                Row(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    AsyncImage(
                        model = movie.artworkUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(180.dp).aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
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
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            movie.year?.let { AssistChip(onClick = {}, label = { Text(it.toString()) }) }
                            movie.runtimeMinutes?.let { AssistChip(onClick = {}, label = { Text("$it min") }) }
                            movie.quality?.let { AssistChip(onClick = {}, label = { Text("Quality: $it") }) }
                            movie.lang?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                        }
                        if (movie.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Category", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                movie.categories.forEach { category ->
                                    AssistChip(onClick = {}, label = { Text(category) })
                                }
                            }
                        }
                        movie.country?.let { country ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Country:", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
                                AssistChip(onClick = {}, label = { Text(country) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = movie.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val playLabel = if (progress != null) "Continue Watching" else "Watch Now"
                            Button(onClick = onPlay) { Text(playLabel) }
                            Button(onClick = onToggleFavorite) {
                                Text(if (isFavorite) "Remove Favorite" else "Favorite")
                            }
                        }
                        if (details.episodes.size > 1) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Episodes", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(details.episodes.size) { index ->
                                    val episode = details.episodes[index]
                                    AssistChip(
                                        onClick = { onEpisodeClick(index) },
                                        label = { Text(episode.name) },
                                    )
                                }
                            }
                        }
                        if (details.actors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Cast", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                details.actors.forEach { actor ->
                                    AssistChip(onClick = {}, label = { Text(actor) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
