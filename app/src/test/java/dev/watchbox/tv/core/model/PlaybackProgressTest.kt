package dev.watchbox.tv.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackProgressTest {
    @Test
    fun `playback source is playable only over HTTPS`() {
        assertTrue(PlaybackSource("https://example.com/movie.mp4", "video/mp4").isPlayable)
        assertFalse(PlaybackSource("http://example.com/movie.mp4", "video/mp4").isPlayable)
        assertFalse(PlaybackSource("not a url", "video/mp4").isPlayable)
    }

    @Test
    fun `clears progress at ninety five percent`() {
        assertTrue(PlaybackProgress(positionMs = 95_000).shouldClear(durationMs = 100_000))
    }

    @Test
    fun `clears progress when less than sixty seconds remain`() {
        assertTrue(PlaybackProgress(positionMs = 61_000).shouldClear(durationMs = 120_000))
    }

    @Test
    fun `keeps useful progress`() {
        assertFalse(PlaybackProgress(positionMs = 60_000).shouldClear(durationMs = 180_000))
    }

    @Test
    fun `keeps progress when duration is unknown or invalid`() {
        val progress = PlaybackProgress(positionMs = 60_000)
        assertFalse(progress.shouldClear(durationMs = null))
        assertFalse(progress.shouldClear(durationMs = 0))
    }
}
