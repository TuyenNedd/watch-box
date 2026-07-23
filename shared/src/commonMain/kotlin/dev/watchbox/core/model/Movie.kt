package dev.watchbox.core.model

import kotlinx.serialization.Serializable

data class Movie(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val description: String,
    val artworkUrl: String,
    val backdropUrl: String,
    val year: Int? = null,
    val runtimeMinutes: Int? = null,
    val sourceName: String,
    val license: LicenseInfo,
    val lang: String? = null,
    val quality: String? = null,
    val episodeCurrent: String? = null,
    val categories: List<String> = emptyList(),
    val country: String? = null,
)

data class Episode(
    val name: String,
    val slug: String,
    val streamUrl: String,
)

data class MovieDetails(
    val movie: Movie,
    val playbackSources: List<PlaybackSource> = emptyList(),
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val actors: List<String> = emptyList(),
    val episodes: List<Episode> = emptyList(),
)

data class PlaybackSource(
    val url: String,
    val mimeType: String? = null,
    val qualityLabel: String? = null,
) {
    val isPlayable: Boolean
        get() = url.startsWith("https://") && url.length > 10
}

data class SubtitleTrack(
    val url: String,
    val language: String,
    val label: String,
    val mimeType: String? = null,
)

data class LicenseInfo(
    val name: String,
    val url: String,
    val sourceUrl: String,
)

@Serializable
data class PlaybackProgress(
    val positionMs: Long,
    val updatedAtEpochMs: Long = 0,
) {
    fun shouldClear(durationMs: Long?): Boolean {
        if (positionMs < 0 || durationMs == null || durationMs <= 0) return false
        val boundedPosition = positionMs.coerceAtMost(durationMs)
        val watchedFraction = boundedPosition.toDouble() / durationMs.toDouble()
        val remainingMs = durationMs - boundedPosition
        return watchedFraction >= COMPLETION_FRACTION || remainingMs < COMPLETION_REMAINING_MS
    }

    companion object {
        const val COMPLETION_FRACTION = 0.95
        const val COMPLETION_REMAINING_MS = 60_000L
    }
}
