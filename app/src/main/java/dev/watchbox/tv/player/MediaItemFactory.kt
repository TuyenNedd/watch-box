package dev.watchbox.tv.player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import dev.watchbox.tv.core.model.PlaybackSource
import dev.watchbox.tv.core.model.SubtitleTrack
import java.net.URI

object MediaItemFactory {

    fun build(
        source: PlaybackSource,
        subtitleTracks: List<SubtitleTrack> = emptyList(),
        title: String? = null,
    ): MediaItem? {
        if (!source.isPlayable) return null
        if (!isHttpsUrl(source.url)) return null

        val builder = MediaItem.Builder()
            .setUri(source.url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .build(),
            )

        val subtitleConfigs = subtitleTracks
            .filter { isHttpsUrl(it.url) }
            .map { track ->
                MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(track.url))
                    .setMimeType(track.mimeType ?: MimeTypes.TEXT_VTT)
                    .setLanguage(track.language)
                    .setLabel(track.label)
                    .setSelectionFlags(
                        if (track.language == "vi") C.SELECTION_FLAG_DEFAULT else 0,
                    )
                    .build()
            }

        if (subtitleConfigs.isNotEmpty()) {
            builder.setSubtitleConfigurations(subtitleConfigs)
        }

        return builder.build()
    }

    private fun isHttpsUrl(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
