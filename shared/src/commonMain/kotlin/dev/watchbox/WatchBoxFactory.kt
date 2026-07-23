package dev.watchbox

import dev.watchbox.data.catalog.CatalogSource
import dev.watchbox.data.catalog.MovieRepository
import dev.watchbox.data.catalog.OPhimCatalogSource
import dev.watchbox.data.catalog.OPhimClient
import dev.watchbox.data.catalog.PhimApiCatalogSource
import dev.watchbox.data.catalog.PhimApiClient
import dev.watchbox.data.catalog.createHttpClient
import dev.watchbox.data.local.LibraryStore
import dev.watchbox.ui.WatchBoxViewModel
import kotlinx.coroutines.CoroutineScope

object WatchBoxFactory {
    fun createViewModel(libraryStore: LibraryStore, scope: CoroutineScope): WatchBoxViewModel {
        val httpClient = createHttpClient()
        val phimApiClient = PhimApiClient(httpClient)
        val oPhimClient = OPhimClient(httpClient)

        val sources: List<CatalogSource> = listOf(
            PhimApiCatalogSource(phimApiClient),
            OPhimCatalogSource(oPhimClient),
        )
        val repository = MovieRepository(sources)
        return WatchBoxViewModel(repository, libraryStore, scope)
    }
}
