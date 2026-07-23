package dev.watchbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import dev.watchbox.ui.theme.Coral500
import dev.watchbox.ui.theme.Navy800

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    details: MovieDetails?,
    isLoading: Boolean,
    isFavorite: Boolean,
    progress: PlaybackProgress?,
    availableServers: List<dev.watchbox.ui.SourcedDetails>,
    activeServerIndex: Int,
    onPlay: () -> Unit,
    onEpisodeClick: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onSwitchServer: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading || details == null -> LoadingShimmer(modifier = modifier)
        else -> {
            val movie = details.movie
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Backdrop with gradient overlay and back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                ) {
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
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.8f),
                                    ),
                                ),
                            ),
                    )
                    // Back button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "\u2190",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    // Title
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                    )
                    // Source badge
                    val sourceColor = when {
                        movie.sourceName.contains("PhimAPI", ignoreCase = true) -> Color(0xFFFF6B5E)
                        movie.sourceName.contains("OPhim", ignoreCase = true) -> Color(0xFF3B82F6)
                        movie.sourceName.contains("NguonC", ignoreCase = true) -> Color(0xFF10B981)
                        else -> Color(0xFF6B7280)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Source: ${movie.sourceName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(sourceColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    movie.originalTitle?.let { original ->
                        if (original != movie.title) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = original,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }

                    // Metadata chips
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        movie.year?.let {
                            MetadataBadge(text = it.toString())
                        }
                        movie.runtimeMinutes?.let {
                            MetadataBadge(text = "$it min")
                        }
                        movie.quality?.let {
                            MetadataBadge(text = it, backgroundColor = Color(0xFFE65100))
                        }
                        movie.lang?.let { lang ->
                            val label = when {
                                lang.contains("Vietsub", ignoreCase = true) -> "Vietsub"
                                lang.contains("Thuy\u1EBFt Minh", ignoreCase = true) -> "TM"
                                lang.contains("L\u1ED3ng Ti\u1EBFng", ignoreCase = true) -> "LT"
                                else -> lang
                            }
                            MetadataBadge(text = label, backgroundColor = Color(0xFF1976D2))
                        }
                    }

                    // Categories
                    if (movie.categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            movie.categories.forEach { category ->
                                AssistChip(onClick = {}, label = { Text(category) })
                            }
                        }
                    }

                    // Country
                    movie.country?.let { country ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Country:",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                            Text(
                                country,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }

                    // Action buttons
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val playLabel = if (progress != null) "Continue Watching" else "Watch Now"
                        Button(onClick = onPlay) { Text(playLabel) }
                        Button(onClick = onToggleFavorite) {
                            Text(if (isFavorite) "Remove Favorite" else "Favorite")
                        }
                    }

                    // Description
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                    )

                    // Server switch chips
                    if (availableServers.size > 1) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Servers",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            availableServers.forEachIndexed { index, sourced ->
                                val chipColor = when (sourced.source) {
                                    dev.watchbox.ui.SourceFilter.PHIMAPI -> Color(0xFFFF6B5E)
                                    dev.watchbox.ui.SourceFilter.OPHIM -> Color(0xFF3B82F6)
                                    dev.watchbox.ui.SourceFilter.NGUONC -> Color(0xFF10B981)
                                    else -> Coral500
                                }
                                val isActive = index == activeServerIndex
                                AssistChip(
                                    onClick = { onSwitchServer(index) },
                                    label = {
                                        Text(
                                            sourced.source.label + " Server",
                                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
                                        )
                                    },
                                    modifier = if (isActive) Modifier.background(chipColor, RoundedCornerShape(8.dp)) else Modifier,
                                )
                            }
                        }
                    }

                    // Episodes
                    if (details.episodes.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Episodes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
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

                    // Cast
                    if (details.actors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Cast",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
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

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MetadataBadge(
    text: String,
    backgroundColor: Color = Navy800,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
