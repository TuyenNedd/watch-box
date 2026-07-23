package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackSource
import dev.watchbox.tv.core.model.SubtitleTrack

class OPhimCatalogSource(
    private val client: OPhimClient,
) : CatalogSource {

    override suspend fun featured(): List<Movie> {
        val response = client.listNewMovies(page = 1)
        if (response.status != "success") return emptyList()
        return response.data?.items?.mapNotNull { it.toMovie() }.orEmpty()
    }

    override suspend fun search(query: String): List<Movie> {
        if (query.isBlank()) return emptyList()
        val response = client.search(keyword = query)
        if (response.status != "success") return emptyList()
        return response.data?.items?.mapNotNull { it.toMovie() }.orEmpty()
    }

    override suspend fun details(id: String): MovieDetails? {
        if (id.isBlank()) return null
        val response = client.movieDetail(slug = id)
        if (response.status != "success") return null
        val detailData = response.data ?: return null
        val movieDto = detailData.item ?: return null
        return mapDetails(movieDto)
    }

    private fun OPhimItemDto.toMovie(): Movie? {
        if (slug.isBlank() || name.isBlank()) return null
        return Movie(
            id = slug,
            title = name,
            originalTitle = originName.ifBlank { null },
            description = originName.ifBlank { name },
            artworkUrl = resolveImageUrl(posterUrl),
            backdropUrl = resolveImageUrl(thumbUrl.ifBlank { posterUrl }),
            year = year,
            runtimeMinutes = parseRuntime(time),
            sourceName = SOURCE_NAME,
            license = ophimLicense,
        )
    }

    private fun mapDetails(movie: OPhimMovieDto): MovieDetails? {
        if (movie.slug.isBlank() || movie.name.isBlank()) return null

        val description = stripHtml(movie.content).ifBlank {
            movie.originName.ifBlank { movie.name }
        }

        val playbackSources = movie.episodes.firstOrNull()?.serverData
            ?.filter { it.linkM3u8.isNotBlank() }
            ?.map { episode ->
                PlaybackSource(
                    url = episode.linkM3u8,
                    mimeType = "application/x-mpegURL",
                    qualityLabel = movie.quality.ifBlank { null },
                )
            }.orEmpty()

        val subtitleTracks = buildSubtitleInfo(movie.lang)

        return MovieDetails(
            movie = Movie(
                id = movie.slug,
                title = movie.name,
                originalTitle = movie.originName.ifBlank { null },
                description = description,
                artworkUrl = resolveImageUrl(movie.posterUrl),
                backdropUrl = resolveImageUrl(movie.thumbUrl.ifBlank { movie.posterUrl }),
                year = movie.year,
                runtimeMinutes = null,
                sourceName = SOURCE_NAME,
                license = ophimLicense,
            ),
            playbackSources = playbackSources,
            subtitleTracks = subtitleTracks,
        )
    }

    private fun buildSubtitleInfo(lang: String): List<SubtitleTrack> {
        return when {
            lang.contains("Vietsub", ignoreCase = true) -> listOf(
                SubtitleTrack(
                    url = "",
                    language = "vi",
                    label = "Vietsub (embedded)",
                    mimeType = null,
                ),
            )
            lang.contains("Thuyết Minh", ignoreCase = true) -> listOf(
                SubtitleTrack(
                    url = "",
                    language = "vi",
                    label = "Thuyết Minh (dubbed)",
                    mimeType = null,
                ),
            )
            lang.contains("Lồng Tiếng", ignoreCase = true) -> listOf(
                SubtitleTrack(
                    url = "",
                    language = "vi",
                    label = "Lồng Tiếng (dubbed)",
                    mimeType = null,
                ),
            )
            else -> emptyList()
        }
    }

    companion object {
        private const val SOURCE_NAME = "OPhim"
        private const val IMAGE_CDN_PREFIX = "https://img.ophim.live/uploads/movies/"

        private val ophimLicense = LicenseInfo(
            name = "Streaming",
            url = "https://ophim1.com",
            sourceUrl = "https://ophim1.com",
        )

        private val htmlTagRegex = Regex("<[^>]*>")
        private val runtimeRegex = Regex("(\\d+)\\s*[Pp]hút")

        fun resolveImageUrl(url: String): String {
            if (url.isBlank()) return ""
            // Already absolute URL
            if (url.startsWith("http://") || url.startsWith("https://")) return url
            // Relative URL – prepend CDN prefix
            val path = url.removePrefix("/")
            return "$IMAGE_CDN_PREFIX$path"
        }

        fun stripHtml(html: String): String =
            html.replace(htmlTagRegex, "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()

        fun parseRuntime(time: String): Int? {
            if (time.isBlank()) return null
            val match = runtimeRegex.find(time) ?: return null
            return match.groupValues[1].toIntOrNull()
        }
    }
}
