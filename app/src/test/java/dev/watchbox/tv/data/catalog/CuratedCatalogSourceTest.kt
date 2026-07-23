package dev.watchbox.tv.data.catalog

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedCatalogSourceTest {
    private val source = CuratedCatalogSource()

    @Test
    fun `contains the four required Blender open movies`() = runTest {
        val movies = source.featured()

        assertEquals(
            setOf("Big Buck Bunny", "Sintel", "Tears of Steel", "Elephant's Dream"),
            movies.mapNotNull { it.originalTitle }.toSet(),
        )
    }

    @Test
    fun `all curated movies have explicit license and HTTPS assets`() = runTest {
        source.featured().forEach { movie ->
            assertTrue(movie.description.isNotBlank())
            assertTrue(movie.artworkUrl.startsWith("https://"))
            assertTrue(movie.backdropUrl.startsWith("https://"))
            assertTrue(movie.license.name.startsWith("CC BY"))
            assertTrue(movie.license.url.startsWith("https://creativecommons.org/"))
            assertTrue(movie.license.sourceUrl.startsWith("https://"))

            val details = source.details(movie.id)
            assertNotNull(details)
            assertTrue(details!!.playbackSources.any { it.isPlayable })
            assertTrue(details.playbackSources.all { it.url.startsWith("https://archive.org/download/") })
            assertTrue(details.subtitleTracks.all { it.url.startsWith("https://") })
        }
    }

    @Test
    fun `search is accent insensitive across titles`() = runTest {
        assertEquals(listOf("sintel"), source.search("sintel").map { it.id })
        assertEquals(listOf("elephants-dream"), source.search("elephant's dream").map { it.id })
    }

    @Test
    fun `unknown details return null`() = runTest {
        assertEquals(null, source.details("missing"))
    }
}
