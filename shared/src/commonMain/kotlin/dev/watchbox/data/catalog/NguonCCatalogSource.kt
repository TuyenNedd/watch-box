package dev.watchbox.data.catalog

import dev.watchbox.core.model.Episode
import dev.watchbox.core.model.LicenseInfo
import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails
import dev.watchbox.core.model.PlaybackSource
import dev.watchbox.core.model.SubtitleTrack
import dev.watchbox.core.util.resolveImageUrl

class NguonCCatalogSource(
    private val client: NguonCClient,
) : CatalogSource {

    override suspend fun featured(): List<Movie> {
        return try {
            val response = client.listNewMovies(page = 1)
            if (response.status != "success") return emptyList()
            response.items.mapNotNull { it.toMovie() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String): List<Movie> {
        if (query.isBlank()) return emptyList()
        return try {
            val response = client.search(keyword = query)
            if (response.status != "success") return emptyList()
            response.items.mapNotNull { it.toMovie() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun details(id: String): MovieDetails? {
        if (id.isBlank()) return null
        return try {
            val response = client.movieDetail(slug = id)
            if (response.status != "success") return null
            val movieDto = response.movie ?: return null
            mapDetails(movieDto)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun listByType(type: String, page: Int): PaginatedResult {
        return try {
            val response = client.listByCategory(slug = type, page = page)
            if (response.status != "success") return emptyPaginated()
            PaginatedResult(
                title = type,
                movies = response.items.mapNotNull { it.toMovie() },
                currentPage = response.paginate?.currentPage ?: 1,
                totalPages = response.paginate?.totalPage ?: 1,
            )
        } catch (_: Exception) {
            emptyPaginated()
        }
    }

    override suspend fun genres(): List<CategoryItem> {
        // NguonC does not have a dedicated genres list endpoint;
        // return empty so the repository falls through to other sources.
        return emptyList()
    }

    override suspend fun countries(): List<CategoryItem> {
        // NguonC does not have a dedicated countries list endpoint;
        // return empty so the repository falls through to other sources.
        return emptyList()
    }

    override suspend fun listByGenre(slug: String, page: Int): PaginatedResult {
        return try {
            val response = client.listByGenre(slug = slug, page = page)
            if (response.status != "success") return emptyPaginated()
            PaginatedResult(
                title = slug,
                movies = response.items.mapNotNull { it.toMovie() },
                currentPage = response.paginate?.currentPage ?: 1,
                totalPages = response.paginate?.totalPage ?: 1,
            )
        } catch (_: Exception) {
            emptyPaginated()
        }
    }

    override suspend fun listByCountry(slug: String, page: Int): PaginatedResult {
        return try {
            val response = client.listByCountry(slug = slug, page = page)
            if (response.status != "success") return emptyPaginated()
            PaginatedResult(
                title = slug,
                movies = response.items.mapNotNull { it.toMovie() },
                currentPage = response.paginate?.currentPage ?: 1,
                totalPages = response.paginate?.totalPage ?: 1,
            )
        } catch (_: Exception) {
            emptyPaginated()
        }
    }

    private fun NguonCItemDto.toMovie(): Movie? {
        if (slug.isBlank() || name.isBlank()) return null
        return Movie(
            id = slug,
            title = name,
            originalTitle = originalName.ifBlank { null },
            description = description.ifBlank { originalName.ifBlank { name } },
            artworkUrl = resolveImageUrl(posterUrl),
            backdropUrl = resolveImageUrl(thumbUrl.ifBlank { posterUrl }),
            year = year,
            runtimeMinutes = null,
            sourceName = SOURCE_NAME,
            license = nguonCLicense,
            lang = language.ifBlank { null },
            quality = quality.ifBlank { null },
            episodeCurrent = currentEpisode.ifBlank { null },
            categories = category.map { it.name }.filter { it.isNotBlank() },
            country = country.firstOrNull()?.name,
        )
    }

    private fun mapDetails(movie: NguonCMovieDto): MovieDetails? {
        if (movie.slug.isBlank() || movie.name.isBlank()) return null

        val description = stripHtml(movie.description).ifBlank {
            movie.originalName.ifBlank { movie.name }
        }

        val allEpisodes = movie.episodes.firstOrNull()?.items
            ?.filter { it.m3u8.isNotBlank() }
            ?.map { ep ->
                Episode(
                    name = ep.name,
                    slug = ep.slug,
                    streamUrl = ep.m3u8,
                )
            }.orEmpty()

        val playbackSources = allEpisodes.take(1).map { ep ->
            PlaybackSource(
                url = ep.streamUrl,
                mimeType = "application/x-mpegURL",
                qualityLabel = movie.quality.ifBlank { null },
            )
        }

        val subtitleTracks = buildSubtitleInfo(movie.language)
        val categories = movie.category.map { it.name }.filter { it.isNotBlank() }
        val country = movie.country.firstOrNull()?.name

        return MovieDetails(
            movie = Movie(
                id = movie.slug,
                title = movie.name,
                originalTitle = movie.originalName.ifBlank { null },
                description = description,
                artworkUrl = resolveImageUrl(movie.posterUrl),
                backdropUrl = resolveImageUrl(movie.thumbUrl.ifBlank { movie.posterUrl }),
                year = movie.year,
                runtimeMinutes = null,
                sourceName = SOURCE_NAME,
                license = nguonCLicense,
                lang = movie.language.ifBlank { null },
                quality = movie.quality.ifBlank { null },
                categories = categories,
                country = country,
            ),
            playbackSources = playbackSources,
            subtitleTracks = subtitleTracks,
            actors = emptyList(),
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
        private const val SOURCE_NAME = "NguonC"

        private val nguonCLicense = LicenseInfo(
            name = "Streaming",
            url = "https://phim.nguonc.com",
            sourceUrl = "https://phim.nguonc.com",
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

        private fun emptyPaginated() = PaginatedResult("", emptyList(), 1, 1)
    }
}
