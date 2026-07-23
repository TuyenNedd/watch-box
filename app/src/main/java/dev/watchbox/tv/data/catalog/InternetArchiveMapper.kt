package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackSource
import dev.watchbox.tv.core.model.SubtitleTrack
import java.net.URI
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl

class InternetArchiveMapper {
    fun mapSearch(response: ArchiveSearchResponseDto): List<Movie> =
        response.response.docs.mapNotNull(::mapDocument)

    fun mapDetails(response: ArchiveMetadataResponseDto): MovieDetails? {
        val metadata = response.metadata
        val id = metadata.identifier.text()?.takeIf { it.isNotBlank() } ?: return null
        val title = metadata.title.text()?.takeIf { it.isNotBlank() } ?: return null
        val licenseUrl = metadata.licenseUrl.text()?.let(::normalizeOpenLicense) ?: return null
        val movie = movie(
            id = id,
            title = title,
            description = metadata.description.text().orEmpty().ifBlank { "Open movie from Internet Archive." },
            yearValue = metadata.date.text(),
            licenseUrl = licenseUrl,
        )
        val selectedFile = response.files
            .mapNotNull { file -> rankedVideo(file)?.let { it to file } }
            .maxByOrNull { it.first }
            ?.second
        val playbackSources = selectedFile?.let { file ->
            val name = file.name.text() ?: return@let emptyList()
            listOf(
                PlaybackSource(
                    url = downloadUrl(id, name),
                    mimeType = mimeType(name),
                    qualityLabel = qualityLabel(name, file.format.text()),
                ),
            )
        }.orEmpty()
        val subtitleTracks = response.files.mapNotNull { file ->
            val name = file.name.text() ?: return@mapNotNull null
            val lower = name.lowercase()
            if (!(lower.endsWith(".srt") || lower.endsWith(".vtt"))) return@mapNotNull null
            if (!("_vi." in lower || ".vi." in lower || "vietnamese" in lower)) return@mapNotNull null
            SubtitleTrack(
                url = downloadUrl(id, name),
                language = "vi",
                label = "Vietnamese",
                mimeType = if (lower.endsWith(".vtt")) "text/vtt" else "application/x-subrip",
            )
        }
        return MovieDetails(movie, playbackSources, subtitleTracks)
    }

    private fun mapDocument(document: ArchiveSearchDocumentDto): Movie? {
        val id = document.identifier.text()?.takeIf { it.isNotBlank() } ?: return null
        val title = document.title.text()?.takeIf { it.isNotBlank() } ?: return null
        val licenseUrl = document.licenseUrl.text()?.let(::normalizeOpenLicense) ?: return null
        return movie(
            id = id,
            title = title,
            description = document.description.text().orEmpty().ifBlank { "Open movie from Internet Archive." },
            yearValue = document.year.text(),
            licenseUrl = licenseUrl,
        )
    }

    private fun movie(
        id: String,
        title: String,
        description: String,
        yearValue: String?,
        licenseUrl: String,
    ): Movie {
        val artwork = "https://archive.org/services/img/${encodeSegment(id)}"
        return Movie(
            id = id,
            title = title,
            originalTitle = title,
            description = description,
            artworkUrl = artwork,
            backdropUrl = artwork,
            year = yearValue?.take(4)?.toIntOrNull(),
            runtimeMinutes = null,
            sourceName = "Internet Archive",
            license = LicenseInfo(
                name = licenseName(licenseUrl),
                url = licenseUrl,
                sourceUrl = "https://archive.org/details/${encodeSegment(id)}",
            ),
        )
    }

    private fun rankedVideo(file: ArchiveFileDto): Int? {
        val name = file.name.text()?.lowercase() ?: return null
        val format = file.format.text()?.lowercase().orEmpty()
        if (listOf("torrent", "thumb", "metadata", "sqlite", "spectrogram").any { it in name || it in format }) {
            return null
        }
        return when {
            name.endsWith(".mp4") && ("h.264" in format || "h264" in format) -> 500
            name.endsWith(".mp4") -> 450
            name.endsWith(".m4v") -> 400
            name.endsWith(".m3u8") -> 350
            name.endsWith(".webm") -> 300
            else -> null
        }
    }

    private fun downloadUrl(identifier: String, name: String): String =
        "https://archive.org/download/".toHttpUrl().newBuilder()
            .addPathSegment(identifier)
            .addPathSegment(name)
            .build()
            .toString()

    private fun encodeSegment(value: String): String =
        "https://archive.org/".toHttpUrl().newBuilder().addPathSegment(value).build().pathSegments.last()

    private fun mimeType(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "mp4", "m4v" -> "video/mp4"
        "webm" -> "video/webm"
        "m3u8" -> "application/x-mpegURL"
        else -> "application/octet-stream"
    }

    private fun qualityLabel(name: String, format: String?): String? = when {
        "2160" in name || "4k" in name.lowercase() -> "4K"
        "1080" in name -> "1080p"
        "720" in name -> "720p"
        !format.isNullOrBlank() -> format
        else -> null
    }

    private fun licenseName(url: String): String {
        val lower = url.lowercase()
        val variant = when {
            "/by-sa/" in lower -> "CC BY-SA"
            "/by-nd/" in lower -> "CC BY-ND"
            "/by-nc-sa/" in lower -> "CC BY-NC-SA"
            "/by-nc-nd/" in lower -> "CC BY-NC-ND"
            "/by-nc/" in lower -> "CC BY-NC"
            "/by/" in lower -> "CC BY"
            "publicdomain" in lower || "zero" in lower -> "Public Domain"
            else -> "Open License"
        }
        val version = Regex("/(\\d+\\.\\d+)/?").find(lower)?.groupValues?.get(1)
        return if (version == null || variant == "Public Domain") variant else "$variant $version"
    }

    private fun normalizeOpenLicense(value: String): String? = runCatching {
        val uri = URI(value)
        if (uri.scheme != "http" && uri.scheme != "https") return@runCatching null
        if (uri.userInfo != null || uri.port != -1 || uri.query != null || uri.fragment != null) {
            return@runCatching null
        }
        val host = uri.host?.lowercase()?.removePrefix("www.")
        if (host != "creativecommons.org") return@runCatching null
        val path = uri.path.orEmpty().lowercase().trimEnd('/') + "/"
        val licensePath = Regex(
            "^/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/(1\\.0|2\\.0|2\\.5|3\\.0|4\\.0)(/[a-z]{2,3})?/$",
        )
        val publicDomainPath = Regex("^/publicdomain/(zero|mark)/1\\.0/$")
        if (!licensePath.matches(path) && !publicDomainPath.matches(path)) return@runCatching null
        URI("https", null, "creativecommons.org", -1, path, null, null).toString()
    }.getOrNull()

    private fun JsonElement?.text(): String? = when (this) {
        is JsonPrimitive -> content
        is JsonArray -> firstNotNullOfOrNull { it.text() }
        else -> null
    }
}
