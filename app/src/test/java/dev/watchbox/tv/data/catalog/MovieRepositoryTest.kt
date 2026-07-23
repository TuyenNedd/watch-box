package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryTest {
    @Test
    fun `featured isolates source failures and deduplicates in stable order`() = runTest {
        val local = FakeSource(featuredMovies = listOf(movie("one"), movie("shared")))
        val failed = FakeSource(failure = IllegalStateException("offline"))
        val remote = FakeSource(featuredMovies = listOf(movie("shared"), movie("two")))
        val repository = MovieRepository(listOf(local, failed, remote))

        assertEquals(listOf("one", "shared", "two"), repository.featured().map { it.id })
    }

    @Test
    fun `search is accent insensitive and remote errors preserve local hits`() = runTest {
        val local = CuratedCatalogSource()
        val failed = FakeSource(failure = IllegalStateException("offline"))
        val repository = MovieRepository(listOf(local, failed))

        assertEquals(listOf("sintel"), repository.search("sintel").map { it.id })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search updates emit local hits before delayed remote enrichment`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val local = FakeSource(searchMovies = listOf(movie("local")))
        val remote = object : CatalogSource {
            override suspend fun featured(): List<Movie> = emptyList()
            override suspend fun search(query: String): List<Movie> {
                gate.await()
                return listOf(movie("remote"))
            }
            override suspend fun details(id: String): MovieDetails? = null
        }
        val repository = MovieRepository(listOf(local, remote))
        val emissions = mutableListOf<List<Movie>>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.searchUpdates("film").toList(emissions)
        }

        runCurrent()
        assertEquals(listOf("local"), emissions.single().map { it.id })

        gate.complete(Unit)
        job.join()
        assertEquals(listOf("local", "remote"), emissions.last().map { it.id })
    }

    @Test(expected = CancellationException::class)
    fun `repository never swallows cancellation`() = runTest {
        val cancelled = FakeSource(failure = CancellationException("stale"))
        MovieRepository(listOf(cancelled)).search("query")
    }

    @Test
    fun `repository state preserves local data and exposes remote failure`() = runTest {
        val repository = MovieRepository(
            listOf(
                FakeSource(searchMovies = listOf(movie("local"))),
                FakeSource(failure = IllegalStateException("offline")),
            ),
        )

        val final = repository.searchStates("film").toList().last()

        assertEquals(listOf("local"), final.data.map { it.id })
        assertEquals(1, final.failures.size)
        assertEquals("offline", final.failures.single().cause.message)
    }

    @Test
    fun `detail result distinguishes not found from source failure`() = runTest {
        val failed = MovieRepository(listOf(FakeSource(failure = IllegalStateException("offline"))))
        val empty = MovieRepository(listOf(FakeSource()))

        assertEquals(1, failed.detailsResult("missing").failures.size)
        assertTrue(empty.detailsResult("missing").failures.isEmpty())
    }

    @Test
    fun `details falls through failures and missing sources`() = runTest {
        val expected = MovieDetails(movie("target"))
        val repository = MovieRepository(
            listOf(
                FakeSource(failure = IllegalStateException("offline")),
                FakeSource(),
                FakeSource(detailsById = mapOf("target" to expected)),
            ),
        )

        val details = repository.details("target")
        assertNotNull(details)
        assertEquals("target", details!!.movie.id)
    }

    private class FakeSource(
        private val featuredMovies: List<Movie> = emptyList(),
        private val searchMovies: List<Movie> = featuredMovies,
        private val detailsById: Map<String, MovieDetails> = emptyMap(),
        private val failure: Throwable? = null,
    ) : CatalogSource {
        override suspend fun featured(): List<Movie> {
            failure?.let { throw it }
            return featuredMovies
        }

        override suspend fun search(query: String): List<Movie> {
            failure?.let { throw it }
            return searchMovies
        }

        override suspend fun details(id: String): MovieDetails? {
            failure?.let { throw it }
            return detailsById[id]
        }
    }

    private fun movie(id: String) = Movie(
        id = id,
        title = id,
        originalTitle = id,
        description = "Description",
        artworkUrl = "https://example.com/$id.jpg",
        backdropUrl = "https://example.com/$id-wide.jpg",
        year = null,
        runtimeMinutes = null,
        sourceName = "Test",
        license = LicenseInfo("CC BY 4.0", "https://creativecommons.org/licenses/by/4.0/", "https://example.com"),
    )
}
