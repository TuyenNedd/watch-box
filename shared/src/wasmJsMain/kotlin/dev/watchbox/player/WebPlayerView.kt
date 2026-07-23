package dev.watchbox.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackProgress
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLVideoElement

@Composable
actual fun PlatformPlayerView(
    details: MovieDetails,
    savedProgress: PlaybackProgress?,
    onSaveProgress: (positionMs: Long, durationMs: Long?) -> Unit,
    onClearProgress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val source = details.playbackSources.firstOrNull { it.isPlayable }
    if (source == null) {
        PlayerErrorView(onBack = onBack, modifier = modifier)
        return
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Create video element reference
    val videoElementId = remember { "watchbox-video-${details.movie.id.hashCode()}" }

    LaunchedEffect(source.url, videoElementId) {
        // Wait for DOM element to be available
        delay(100)
        val video = document.getElementById(videoElementId) as? HTMLVideoElement ?: return@LaunchedEffect

        // Try to load HLS via native support or HLS.js
        val url = source.url
        if (url.contains(".m3u8")) {
            // Check native HLS support first (Safari)
            video.src = url
        }
        video.load()
        savedProgress?.let { p ->
            video.currentTime = (p.positionMs / 1000.0)
        }
        video.play()
    }

    // Save progress periodically
    LaunchedEffect(videoElementId) {
        while (true) {
            delay(10_000L)
            val video = document.getElementById(videoElementId) as? HTMLVideoElement ?: continue
            if (!video.paused) {
                val position = (video.currentTime * 1000).toLong()
                val duration = if (video.duration.isFinite()) (video.duration * 1000).toLong() else null
                onSaveProgress(position, duration)
            }
        }
    }

    DisposableEffect(videoElementId) {
        onDispose {
            val video = document.getElementById(videoElementId) as? HTMLVideoElement
            if (video != null) {
                val position = (video.currentTime * 1000).toLong()
                val duration = if (video.duration.isFinite()) (video.duration * 1000).toLong() else null
                if (position > 0) onSaveProgress(position, duration)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // We use a composable that embeds HTML video
        HtmlVideoPlayer(
            videoId = videoElementId,
            modifier = Modifier.fillMaxSize(),
        )

        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
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
                    Button(onClick = onBack) { Text("Back") }
                }
            }
        }

        // Back button overlay
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun HtmlVideoPlayer(videoId: String, modifier: Modifier = Modifier) {
    // Create video element in DOM
    DisposableEffect(videoId) {
        val container = document.getElementById("player-container") as? HTMLDivElement
            ?: (document.createElement("div") as HTMLDivElement).also {
                it.id = "player-container"
                it.style.apply {
                    position = "fixed"
                    top = "0"
                    left = "0"
                    width = "100%"
                    height = "100%"
                    zIndex = "1000"
                }
                document.body?.appendChild(it)
            }

        val video = document.createElement("video") as HTMLVideoElement
        video.id = videoId
        video.controls = true
        video.autoplay = true
        video.style.apply {
            width = "100%"
            height = "100%"
            setProperty("object-fit", "contain")
            setProperty("background", "black")
        }
        container.appendChild(video)

        onDispose {
            video.pause()
            video.src = ""
            container.removeChild(video)
            container.parentElement?.removeChild(container)
        }
    }

    // The actual Compose area is just a placeholder that takes up space
    Box(modifier = modifier)
}
