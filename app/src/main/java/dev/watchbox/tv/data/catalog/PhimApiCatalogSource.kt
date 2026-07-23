package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.Episode
import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackSource
import dev.watchbox.tv.core.model.SubtitleTrack

class PhimApiCatalogSource(
    private val client: PhimApiClient,
) : CatalogSource {

    override suspend fun featured(): List<Movie> {
        val response = client.listNewMovies(page = 1)
        if (!response.status) return emptyList()
        return response.items.mapNotNull { it.toMovie() }
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
        if (!response.status) return null
        val movieDto = response.movie ?: return null
        return mapDetails(movieDto, response.episodes)
    }

    private fun PhimApiItemDto.toMovie(): Movie? {
        if (slug.isBlank() || name.isBlank()) return null
        return Movie(
            id = slug,
            title = name,
            originalTitle = originName.ifBlank { null },
            description = originName.ifBlank { name },
            artworkUrl = posterUrl,
            backdropUrl = thumbUrl.ifBlank { posterUrl },
            year = year,
            runtimeMinutes = parseRuntime(time),
            sourceName = SOURCE_NAME,
            license = phimApiLicense,
            lang = lang.ifBlank { null },
            quality = quality.ifBlank { null },
            episodeCurrent = episodeCurrent.ifBlank { null },
            categories = category.map { it.name }.filter { it.isNotBlank() },
            country = country.firstOrNull()?.name,
        )
    }

    private fun mapDetails(
        movie: PhimApiMovieDto,
        episodeServers: List<PhimApiEpisodeServerDto>,
    ): MovieDetails? {
        if (movie.slug.isBlank() || movie.name.isBlank()) return null

        val description = stripHtml(movie.content).ifBlank {
            movie.originName.ifBlank { movie.name }
        }

        val allEpisodes = episodeServers.firstOrNull()?.serverData
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

        val categories = movie.category.map { it.name }.filter { it.isNotBlank() }
        val country = movie.country.firstOrNull()?.name

        return MovieDetails(
            movie = Movie(
                id = movie.slug,
                title = movie.name,
                originalTitle = movie.originName.ifBlank { null },
                description = description,
                artworkUrl = movie.posterUrl,
                backdropUrl = movie.thumbUrl.ifBlank { movie.posterUrl },
                year = movie.year,
                runtimeMinutes = null,
                sourceName = SOURCE_NAME,
                license = phimApiLicense,
                lang = movie.lang.ifBlank { null },
                quality = movie.quality.ifBlank { null },
                categories = categories,
                country = country,
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
        private const val SOURCE_NAME = "PhimApi"

        private val phimApiLicense = LicenseInfo(
            name = "Streaming",
            url = "https://phimapi.com",
            sourceUrl = "https://phimapi.com",
        )

        private val htmlTagRegex = Regex("<[^>]*>")
        private val runtimeRegex = Regex("(\\d+)\\s*[Pp]hút")

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
