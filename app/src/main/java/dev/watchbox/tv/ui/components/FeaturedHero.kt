package dev.watchbox.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import dev.watchbox.tv.R
import dev.watchbox.tv.core.model.Movie

@Composable
fun FeaturedHero(
    movie: Movie,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
    ) {
        // Backdrop
        AsyncImage(
            model = movie.backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                        ),
                        startY = 200f,
                    ),
                ),
        )
        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 48.dp, end = 48.dp)
                .fillMaxWidth(0.5f),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Metadata
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                movie.year?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                movie.runtimeMinutes?.let {
                    Text(
                        text = "${it} phút",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onPlay() }) {
                    Text(text = stringResource(R.string.watch_now))
                }
                Button(onClick = { onDetails() }) {
                    Text(text = stringResource(R.string.details))
                }
            }
        }
    }
}
