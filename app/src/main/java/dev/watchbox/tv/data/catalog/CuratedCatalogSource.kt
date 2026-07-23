package dev.watchbox.tv.data.catalog

import dev.watchbox.tv.core.model.LicenseInfo
import dev.watchbox.tv.core.model.Movie
import dev.watchbox.tv.core.model.MovieDetails
import dev.watchbox.tv.core.model.PlaybackSource
import dev.watchbox.tv.core.util.matchesSearch

class CuratedCatalogSource : CatalogSource {
    private val detailsById = curatedDetails.associateBy { it.movie.id }

    override suspend fun featured(): List<Movie> = curatedDetails.map { it.movie }

    override suspend fun search(query: String): List<Movie> =
        curatedDetails.map { it.movie }.filter { it.matchesSearch(query) }

    override suspend fun details(id: String): MovieDetails? = detailsById[id]

    private companion object {
        const val MEDIA_ROOT = "https://archive.org/download"
        const val ARTWORK_ROOT = "https://archive.org/services/img"

        val ccBy30 = LicenseInfo(
            name = "CC BY 3.0",
            url = "https://creativecommons.org/licenses/by/3.0/",
            sourceUrl = "https://www.blender.org/about/projects/",
        )
        val ccBy25 = LicenseInfo(
            name = "CC BY 2.5",
            url = "https://creativecommons.org/licenses/by/2.5/",
            sourceUrl = "https://orange.blender.org/",
        )

        val curatedDetails = listOf(
            detail(
                id = "big-buck-bunny",
                title = "Thỏ Bự Tinh Nghịch",
                originalTitle = "Big Buck Bunny",
                description = "Chú thỏ hiền lành dùng trí thông minh để đối phó với ba kẻ chuyên bắt nạt trong khu rừng.",
                year = 2008,
                runtimeMinutes = 10,
                archiveId = "BigBuckBunny_328",
                mediaPath = "BigBuckBunny_328/BigBuckBunny_512kb.mp4",
                license = ccBy30.copy(sourceUrl = "https://peach.blender.org/"),
            ),
            detail(
                id = "sintel",
                title = "Chiến Binh Sintel",
                originalTitle = "Sintel",
                description = "Một nữ chiến binh trẻ vượt qua hành trình khắc nghiệt để tìm lại người bạn rồng đã mất.",
                year = 2010,
                runtimeMinutes = 15,
                archiveId = "Sintel",
                mediaPath = "Sintel/sintel-2048-stereo_512kb.mp4",
                license = ccBy30.copy(sourceUrl = "https://durian.blender.org/"),
            ),
            detail(
                id = "tears-of-steel",
                title = "Nước Mắt Thép",
                originalTitle = "Tears of Steel",
                description = "Một nhóm chiến binh và nhà khoa học tìm cách cứu Amsterdam khỏi đội quân rô-bốt hủy diệt.",
                year = 2012,
                runtimeMinutes = 12,
                archiveId = "Tears-of-Steel",
                mediaPath = "Tears-of-Steel/tears_of_steel_720p.mp4",
                license = ccBy30.copy(sourceUrl = "https://mango.blender.org/"),
            ),
            detail(
                id = "elephants-dream",
                title = "Giấc Mơ Của Voi",
                originalTitle = "Elephant's Dream",
                description = "Hai nhân vật khám phá một cỗ máy khổng lồ, kỳ lạ trong phim hoạt hình mở đầu tiên của Blender.",
                year = 2006,
                runtimeMinutes = 11,
                archiveId = "ElephantsDream",
                mediaPath = "ElephantsDream/ed_1024.mp4",
                license = ccBy25,
            ),
        )

        fun detail(
            id: String,
            title: String,
            originalTitle: String,
            description: String,
            year: Int,
            runtimeMinutes: Int,
            archiveId: String,
            mediaPath: String,
            license: LicenseInfo,
        ): MovieDetails {
            val artwork = "$ARTWORK_ROOT/$archiveId"
            return MovieDetails(
                movie = Movie(
                    id = id,
                    title = title,
                    originalTitle = originalTitle,
                    description = description,
                    artworkUrl = artwork,
                    backdropUrl = artwork,
                    year = year,
                    runtimeMinutes = runtimeMinutes,
                    sourceName = "Blender Open Movies",
                    license = license,
                ),
                playbackSources = listOf(
                    PlaybackSource(
                        url = "$MEDIA_ROOT/$mediaPath",
                        mimeType = "video/mp4",
                        qualityLabel = "HD",
                    ),
                ),
                // Tracks are intentionally absent unless an official, stable Vietnamese file is verified.
                subtitleTracks = emptyList(),
            )
        }
    }
}
