package dev.watchbox.tv.data.catalog

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InternetArchiveCatalogSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var source: CatalogSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        source = InternetArchiveCatalogSource(
            InternetArchiveClient(OkHttpClient(), server.url("/")),
            InternetArchiveMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adapter connects search and metadata DTOs to domain models`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"response":{"docs":[{"identifier":"open-film","title":"Open Film","licenseurl":"https://creativecommons.org/licenses/by/4.0/"}]}}""",
            ),
        )
        server.enqueue(
            MockResponse().setBody(
                """{"metadata":{"identifier":"open-film","title":"Open Film","licenseurl":"https://creativecommons.org/licenses/by/4.0/"},"files":[{"name":"open film.mp4","format":"h.264"}]}""",
            ),
        )

        assertEquals(listOf("open-film"), source.search("open").map { it.id })
        val details = source.details("open-film")
        assertEquals("open-film", details?.movie?.id)
        assertTrue(details!!.playbackSources.single().url.endsWith("open%20film.mp4"))
    }
}
