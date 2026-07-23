package dev.watchbox.tv.data.catalog

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
            runtimeMinutes = null,
            sourceName = SOURCE_NAME,
            license = phimApiLicense,
        )
    }

    private fun mapDetails(
        movie: PhimApiMovieDto,
        episodes: List<PhimApiEpisodeServerDto>,
    ): MovieDetails? {
        if (movie.slug.isBlank() || movie.name.isBlank()) return null

        val description = stripHtml(movie.content).ifBlank {
            movie.originName.ifBlank { movie.name }
        }

        val playbackSources = episodes.firstOrNull()?.serverData
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
                artworkUrl = movie.posterUrl,
                backdropUrl = movie.thumbUrl.ifBlank { movie.posterUrl },
                year = movie.year,
                runtimeMinutes = null,
                sourceName = SOURCE_NAME,
                license = phimApiLicense,
            ),
            playbackSources = playbackSources,
            subtitleTracks = subtitleTracks,
        )
    }

    private fun buildSubtitleInfo(lang: String): List<SubtitleTrack> {
        // PhimApi "lang" field indicates embedded subtitle type, not an external track URL.
        // We surface it as metadata so the UI can display the subtitle language info.
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

        fun stripHtml(html: String): String =
            html.replace(htmlTagRegex, "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()
    }
}
