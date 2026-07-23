package dev.watchbox.tv.player

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.watchbox.tv.R
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackProgress
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    movieId: String,
    details: MovieDetails?,
    savedProgress: PlaybackProgress?,
    onSaveProgress: (positionMs: Long, durationMs: Long?) -> Unit,
    onClearProgress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val source = details?.playbackSources?.firstOrNull { it.isPlayable }
    val mediaItem = remember(source, details) {
        source?.let {
            MediaItemFactory.build(
                source = it,
                subtitleTracks = details?.subtitleTracks.orEmpty(),
                title = details?.movie?.title,
            )
        }
    }

    if (mediaItem == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.player_error),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onBack() }) {
                    Text(text = stringResource(R.string.back))
                }
            }
        }
        return
    }

    val player = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    // Keep screen on
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Set media and restore position
    LaunchedEffect(mediaItem) {
        player.setMediaItem(mediaItem)
        player.prepare()
        savedProgress?.let { progress ->
            player.seekTo(progress.positionMs)
        }
        player.play()
    }

    // Save progress every 10 seconds
    LaunchedEffect(player) {
        while (true) {
            delay(10_000L)
            if (player.isPlaying) {
                val position = player.currentPosition
                val duration = player.duration.takeIf { it > 0 }
                onSaveProgress(position, duration)
            }
        }
    }

    // Error listener
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                errorMessage = "Unable to play video. Please try again."
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    val duration = player.duration.takeIf { it > 0 }
                    if (duration != null) {
                        onClearProgress()
                    }
                }
            }
        }
        player.addListener(listener)
        onDispose {
            // Save progress on stop
            val position = player.currentPosition
            val duration = player.duration.takeIf { it > 0 }
            if (position > 0) {
                onSaveProgress(position, duration)
            }
            // Check near-completion
            if (duration != null && duration > 0) {
                val progress = PlaybackProgress(position, System.currentTimeMillis())
                if (progress.shouldClear(duration)) {
                    onClearProgress()
                }
            }
            player.removeListener(listener)
            player.release()
        }
    }

    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Error overlay
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onBack() }) {
                        Text(text = stringResource(R.string.back))
                    }
                }
            }
        }
    }
}
