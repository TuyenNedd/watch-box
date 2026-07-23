package dev.watchbox.data.catalog

import dev.watchbox.core.model.Movie
import dev.watchbox.core.model.MovieDetails

interface CatalogSource {
    suspend fun featured(): List<Movie>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails?
}
