package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails

interface CatalogSource {
    suspend fun featured(): List<Movie>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails?
}
