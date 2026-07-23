package dev.watchbox.data.catalog

import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails

data class CategoryItem(val id: String, val name: String, val slug: String)

data class PaginatedResult(
    val title: String,
    val movies: List<Movie>,
    val currentPage: Int,
    val totalPages: Int,
)

interface CatalogSource {
    suspend fun featured(): List<Movie>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails?
    suspend fun listByType(type: String, page: Int): PaginatedResult
    suspend fun genres(): List<CategoryItem>
    suspend fun countries(): List<CategoryItem>
    suspend fun listByGenre(slug: String, page: Int): PaginatedResult
    suspend fun listByCountry(slug: String, page: Int): PaginatedResult
}
