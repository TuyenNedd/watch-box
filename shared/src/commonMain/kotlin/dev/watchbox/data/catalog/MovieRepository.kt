package dev.watchbox.data.catalog

import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map

data class CatalogFailure(
    val sourceIndex: Int,
    val cause: Exception,
)

data class CatalogResult<T>(
    val data: T,
    val failures: List<CatalogFailure> = emptyList(),
)

class MovieRepository(
    val sources: List<CatalogSource>,
) {
    fun featuredStates(): Flow<CatalogResult<List<Movie>>> = mergeStates { it.featured() }

    fun featuredUpdates(): Flow<List<Movie>> =
        featuredStates().map { it.data }.distinctUntilChanged()

    suspend fun featured(): List<Movie> = featuredStates().lastOrNull()?.data.orEmpty()

    fun searchStates(query: String): Flow<CatalogResult<List<Movie>>> =
        if (query.isBlank()) flow { emit(CatalogResult(emptyList())) } else mergeStates { it.search(query) }

    fun searchUpdates(query: String): Flow<List<Movie>> =
        searchStates(query).map { it.data }.distinctUntilChanged()

    suspend fun search(query: String): List<Movie> = searchStates(query).lastOrNull()?.data.orEmpty()

    suspend fun detailsResult(id: String): CatalogResult<MovieDetails?> {
        if (id.isBlank()) return CatalogResult(null)
        val failures = mutableListOf<CatalogFailure>()
        sources.forEachIndexed { index, source ->
            val details = try {
                source.details(id)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failures += CatalogFailure(index, error)
                null
            }
            if (details != null) return CatalogResult(details, failures.toList())
        }
        return CatalogResult(null, failures.toList())
    }

    suspend fun details(id: String): MovieDetails? = detailsResult(id).data

    suspend fun listByType(type: String, page: Int): PaginatedResult {
        for (source in sources) {
            try {
                val result = source.listByType(type, page)
                if (result.movies.isNotEmpty()) return result
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) { /* try next */ }
        }
        return PaginatedResult("", emptyList(), 1, 1)
    }

    suspend fun genres(): List<CategoryItem> {
        for (source in sources) {
            try {
                val result = source.genres()
                if (result.isNotEmpty()) return result
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) { /* try next */ }
        }
        return emptyList()
    }

    suspend fun countries(): List<CategoryItem> {
        for (source in sources) {
            try {
                val result = source.countries()
                if (result.isNotEmpty()) return result
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) { /* try next */ }
        }
        return emptyList()
    }

    suspend fun listByGenre(slug: String, page: Int): PaginatedResult {
        for (source in sources) {
            try {
                val result = source.listByGenre(slug, page)
                if (result.movies.isNotEmpty()) return result
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) { /* try next */ }
        }
        return PaginatedResult("", emptyList(), 1, 1)
    }

    suspend fun listByCountry(slug: String, page: Int): PaginatedResult {
        for (source in sources) {
            try {
                val result = source.listByCountry(slug, page)
                if (result.movies.isNotEmpty()) return result
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) { /* try next */ }
        }
        return PaginatedResult("", emptyList(), 1, 1)
    }

    private fun mergeStates(
        load: suspend (CatalogSource) -> List<Movie>,
    ): Flow<CatalogResult<List<Movie>>> = flow {
        val unique = LinkedHashMap<String, Movie>()
        val failures = mutableListOf<CatalogFailure>()
        sources.forEachIndexed { index, source ->
            val movies = try {
                load(source)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failures += CatalogFailure(index, error)
                emptyList()
            }
            movies.forEach { movie -> if (movie.id !in unique) unique[movie.id] = movie }
            emit(CatalogResult(unique.values.toList(), failures.toList()))
        }
    }
}
