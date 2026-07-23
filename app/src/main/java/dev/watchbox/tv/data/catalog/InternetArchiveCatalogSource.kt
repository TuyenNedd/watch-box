package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails

class InternetArchiveCatalogSource(
    private val client: InternetArchiveClient,
    private val mapper: InternetArchiveMapper,
) : CatalogSource {
    override suspend fun featured(): List<Movie> = mapper.mapSearch(client.search(""))

    override suspend fun search(query: String): List<Movie> =
        if (query.isBlank()) emptyList() else mapper.mapSearch(client.search(query))

    override suspend fun details(id: String): MovieDetails? =
        if (id.isBlank()) null else mapper.mapDetails(client.metadata(id))
}
