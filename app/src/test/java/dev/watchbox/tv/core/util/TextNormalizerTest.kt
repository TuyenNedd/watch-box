package dev.watchbox.tv.core.util

import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun `normalizes Vietnamese accents`() {
        assertEquals("dien anh viet", normalizeForSearch("Điện Ảnh Việt"))
    }

    @Test
    fun `lowercases trims punctuation and collapses whitespace`() {
        assertEquals("big buck bunny", normalizeForSearch("  BIG,   Buck--Bunny!  "))
    }

    @Test
    fun `matches original title when Vietnamese title differs`() {
        val movie = movie(title = "Giấc mơ của voi", originalTitle = "Elephant's Dream")

        assertTrue(movie.matchesSearch("elephants dream"))
        assertFalse(movie.matchesSearch("sintel"))
    }

    private fun movie(title: String, originalTitle: String) = Movie(
        id = "movie",
        title = title,
        originalTitle = originalTitle,
        description = "Phim mở",
        artworkUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
        year = 2006,
        runtimeMinutes = 11,
        sourceName = "Blender Open Movies",
        license = LicenseInfo(
            name = "CC BY 2.5",
            url = "https://creativecommons.org/licenses/by/2.5/",
            sourceUrl = "https://orange.blender.org/",
        ),
    )
}
