package dev.watchbox.tv.data.catalog

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InternetArchiveMapperTest {
    private val mapper = InternetArchiveMapper()

    @Test
    fun `maps only licensed well formed search records with HTTPS artwork`() {
        val response = ArchiveSearchResponseDto(
            response = ArchiveSearchPageDto(
                docs = listOf(
                    document("licensed-film", "Licensed Film", "https://creativecommons.org/licenses/by/4.0/"),
                    document("unlicensed-film", "No License", null),
                    document("", "Malformed", "https://creativecommons.org/licenses/by/4.0/"),
                ),
            ),
        )

        val result = mapper.mapSearch(response)

        assertEquals(listOf("licensed-film"), result.map { it.id })
        assertEquals("https://archive.org/services/img/licensed-film", result.single().artworkUrl)
        assertTrue(result.single().license.sourceUrl.startsWith("https://archive.org/details/"))
    }

    @Test
    fun `H264 MP4 outranks OGV torrent and thumbnail`() {
        val response = metadata(
            files = listOf(
                file("film.ogv", "Ogg Video"),
                file("film_archive.torrent", "Archive BitTorrent"),
                file("thumb.jpg", "JPEG Thumb"),
                file("film 720p.mp4", "h.264 MPEG4"),
            ),
        )

        val details = mapper.mapDetails(response)
        assertNotNull(details)

        assertEquals(1, details!!.playbackSources.size)
        assertEquals(
            "https://archive.org/download/licensed-film/film%20720p.mp4",
            details.playbackSources.single().url,
        )
    }

    @Test
    fun `encodes identifier and file as URL path segments`() {
        val response = metadata(
            identifier = "film id",
            files = listOf(file("folder/name 1080p.mp4", "MPEG4")),
        )

        val details = mapper.mapDetails(response)
        assertNotNull(details)
        val source = details!!.playbackSources.single()

        assertEquals(
            "https://archive.org/download/film%20id/folder%2Fname%201080p.mp4",
            source.url,
        )
    }

    @Test
    fun `unsupported or absent video yields no playable source`() {
        val unsupported = mapper.mapDetails(metadata(files = listOf(file("film.avi", "Cinepack"))))
        val absent = mapper.mapDetails(metadata(files = emptyList()))
        assertNotNull(unsupported)
        assertNotNull(absent)

        assertTrue(unsupported!!.playbackSources.isEmpty())
        assertTrue(absent!!.playbackSources.isEmpty())
    }

    @Test
    fun `unknown HTTPS license is rejected while known HTTP Creative Commons is normalized`() {
        val unknown = metadata(licenseUrl = "https://example.com/copyright")
        val deceptive = metadata(licenseUrl = "https://creativecommons.org.evil.test/licenses/by/4.0/")
        val invalidLicensePath = metadata(licenseUrl = "https://creativecommons.org/licenses/not-a-license/9.9/")
        val invalidPublicDomainPath = metadata(licenseUrl = "https://creativecommons.org/publicdomain/copyright/1.0/")
        val knownHttp = metadata(licenseUrl = "http://creativecommons.org/licenses/by/4.0/")

        assertEquals(null, mapper.mapDetails(unknown))
        assertEquals(null, mapper.mapDetails(deceptive))
        assertEquals(null, mapper.mapDetails(invalidLicensePath))
        assertEquals(null, mapper.mapDetails(invalidPublicDomainPath))
        assertEquals(
            "https://creativecommons.org/licenses/by/4.0/",
            mapper.mapDetails(knownHttp)?.movie?.license?.url,
        )
    }

    @Test
    fun `unlicensed metadata is rejected`() {
        assertEquals(null, mapper.mapDetails(metadata(licenseUrl = null)))
    }

    private fun document(id: String, title: String, licenseUrl: String?) = ArchiveSearchDocumentDto(
        identifier = JsonPrimitive(id),
        title = JsonPrimitive(title),
        description = JsonPrimitive("Mô tả phim"),
        year = JsonPrimitive(2020),
        licenseUrl = licenseUrl?.let(::JsonPrimitive),
    )

    private fun metadata(
        identifier: String = "licensed-film",
        licenseUrl: String? = "https://creativecommons.org/licenses/by/4.0/",
        files: List<ArchiveFileDto> = emptyList(),
    ) = ArchiveMetadataResponseDto(
        metadata = ArchiveMetadataDto(
            identifier = JsonPrimitive(identifier),
            title = JsonPrimitive("Licensed Film"),
            description = JsonPrimitive("Một phim được cấp phép mở"),
            date = JsonPrimitive("2020-01-01"),
            licenseUrl = licenseUrl?.let(::JsonPrimitive),
        ),
        files = files,
    )

    private fun file(name: String, format: String) = ArchiveFileDto(
        name = JsonPrimitive(name),
        format = JsonPrimitive(format),
    )
}
