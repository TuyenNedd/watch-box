package dev.watchbox.tv

import android.app.Application
import android.content.Context
import dev.watchbox.tv.data.catalog.CatalogSource
import dev.watchbox.tv.data.catalog.CuratedCatalogSource
import dev.watchbox.tv.data.catalog.InternetArchiveCatalogSource
import dev.watchbox.tv.data.catalog.InternetArchiveClient
import dev.watchbox.tv.data.catalog.InternetArchiveMapper
import dev.watchbox.tv.data.catalog.MovieRepository
import dev.watchbox.tv.data.catalog.OPhimCatalogSource
import dev.watchbox.tv.data.catalog.OPhimClient
import dev.watchbox.tv.data.catalog.PhimApiCatalogSource
import dev.watchbox.tv.data.catalog.PhimApiClient
import dev.watchbox.tv.data.local.LibraryStore
import dev.watchbox.tv.data.local.PreferencesLibraryStore
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class WatchBoxApplication : Application() {

    lateinit var repository: MovieRepository
        private set

    lateinit var libraryStore: LibraryStore
        private set

    override fun onCreate() {
        super.onCreate()

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val phimApiClient = PhimApiClient(httpClient)
        val oPhimClient = OPhimClient(httpClient)
        val archiveClient = InternetArchiveClient(httpClient)
        val mapper = InternetArchiveMapper()

        val sources: List<CatalogSource> = listOf(
            PhimApiCatalogSource(phimApiClient),
            OPhimCatalogSource(oPhimClient),
            CuratedCatalogSource(),
            InternetArchiveCatalogSource(archiveClient, mapper),
        )

        repository = MovieRepository(sources)

        val preferences = getSharedPreferences("watchbox_library", Context.MODE_PRIVATE)
        libraryStore = PreferencesLibraryStore(preferences)
    }
}
