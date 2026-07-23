package dev.watchbox.data.catalog

import dev.watchbox.core.model.Episode
import dev.watchbox.core.model.LicenseInfo
import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackSource
import dev.watchbox.core.model.SubtitleTrack

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

    override suspend fun listByType(type: String, page: Int): PaginatedResult =
        PaginatedResult("", emptyList(), 1, 1)

    override suspend fun genres(): List<CategoryItem> = emptyList()

    override suspend fun countries(): List<CategoryItem> = emptyList()

    override suspend fun listByGenre(slug: String, page: Int): PaginatedResult =
        PaginatedResult("", emptyList(), 1, 1)

    override suspend fun listByCountry(slug: String, page: Int): PaginatedResult =
        PaginatedResult("", emptyList(), 1, 1)

    private fun OPhimItemDto.toMovie(): Movie? {
        if (slug.isBlank() || name.isBlank()) return null
        return Movie(
            id = slug,
            title = name,
            originalTitle = originName.ifBlank { null },
            description = originName.ifBlank { name },
            artworkUrl = resolveOPhimImageUrl(posterUrl),
            backdropUrl = resolveOPhimImageUrl(thumbUrl.ifBlank { posterUrl }),
            year = year,
            runtimeMinutes = parseRuntime(time),
            sourceName = SOURCE_NAME,
            license = ophimLicense,
            lang = lang.ifBlank { null },
            quality = quality.ifBlank { null },
            episodeCurrent = episodeCurrent.ifBlank { null },
        )
    }

    private fun mapDetails(movie: OPhimMovieDto): MovieDetails? {
        if (movie.slug.isBlank() || movie.name.isBlank()) return null

        val description = stripHtml(movie.content).ifBlank {
            movie.originName.ifBlank { movie.name }
        }

        val allEpisodes = movie.episodes.firstOrNull()?.serverData
            ?.filter { it.linkM3u8.isNotBlank() }
            ?.map { ep ->
                Episode(
                    name = ep.name,
                    slug = ep.slug,
                    streamUrl = ep.linkM3u8,
                )
            }.orEmpty()

        val playbackSources = allEpisodes.take(1).map { ep ->
            PlaybackSource(
                url = ep.streamUrl,
                mimeType = "application/x-mpegURL",
                qualityLabel = movie.quality.ifBlank { null },
            )
        }

        val subtitleTracks = buildSubtitleInfo(movie.lang)

        return MovieDetails(
            movie = Movie(
                id = movie.slug,
                title = movie.name,
                originalTitle = movie.originName.ifBlank { null },
                description = description,
                artworkUrl = resolveOPhimImageUrl(movie.posterUrl),
                backdropUrl = resolveOPhimImageUrl(movie.thumbUrl.ifBlank { movie.posterUrl }),
                year = movie.year,
                runtimeMinutes = null,
                sourceName = SOURCE_NAME,
                license = ophimLicense,
                lang = movie.lang.ifBlank { null },
                quality = movie.quality.ifBlank { null },
            ),
            playbackSources = playbackSources,
            subtitleTracks = subtitleTracks,
            actors = movie.actor.filter { it.isNotBlank() },
            episodes = allEpisodes,
        )
    }

    private fun buildSubtitleInfo(lang: String): List<SubtitleTrack> {
        return when {
            lang.contains("Vietsub", ignoreCase = true) -> listOf(
                SubtitleTrack(url = "", language = "vi", label = "Vietsub (embedded)"),
            )
            lang.contains("Thuyết Minh", ignoreCase = true) -> listOf(
                SubtitleTrack(url = "", language = "vi", label = "Thuyết Minh (dubbed)"),
            )
            lang.contains("Lồng Tiếng", ignoreCase = true) -> listOf(
                SubtitleTrack(url = "", language = "vi", label = "Lồng Tiếng (dubbed)"),
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

        fun resolveOPhimImageUrl(url: String): String {
            if (url.isBlank()) return ""
            val fullUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                val path = url.removePrefix("/")
                "$IMAGE_CDN_PREFIX$path"
            }
            return dev.watchbox.core.util.resolveImageUrl(fullUrl)
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
