package dev.watchbox.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackProgress

@Composable
expect fun PlatformPlayerView(
    details: MovieDetails,
    savedProgress: PlaybackProgress?,
    onSaveProgress: (positionMs: Long, durationMs: Long?) -> Unit,
    onClearProgress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
)
