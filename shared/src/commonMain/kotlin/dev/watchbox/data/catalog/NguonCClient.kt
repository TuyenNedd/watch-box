package dev.watchbox.data.catalog

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NguonCListResponseDto(
    val status: String = "",
    val paginate: NguonCPaginateDto? = null,
    val items: List<NguonCItemDto> = emptyList(),
)

@Serializable
data class NguonCPaginateDto(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("total_page") val totalPage: Int = 1,
    @SerialName("total_items") val totalItems: Int = 0,
    @SerialName("items_per_page") val itemsPerPage: Int = 10,
)

@Serializable
data class NguonCItemDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("original_name") val originalName: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    val description: String = "",
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
    @SerialName("current_episode") val currentEpisode: String = "",
    val language: String = "",
    val quality: String = "",
    val year: Int? = null,
    val category: List<NguonCCategoryDto> = emptyList(),
    val country: List<NguonCCountryDto> = emptyList(),
)

@Serializable
data class NguonCDetailResponseDto(
    val status: String = "",
    val movie: NguonCMovieDto? = null,
)

@Serializable
data class NguonCMovieDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("original_name") val originalName: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    val description: String = "",
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
    @SerialName("current_episode") val currentEpisode: String = "",
    val language: String = "",
    val quality: String = "",
    val year: Int? = null,
    val category: List<NguonCCategoryDto> = emptyList(),
    val country: List<NguonCCountryDto> = emptyList(),
    val episodes: List<NguonCEpisodeServerDto> = emptyList(),
)

@Serializable
data class NguonCCategoryDto(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class NguonCCountryDto(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class NguonCEpisodeServerDto(
    @SerialName("server_name") val serverName: String = "",
    val items: List<NguonCEpisodeDto> = emptyList(),
)

@Serializable
data class NguonCEpisodeDto(
    val name: String = "",
    val slug: String = "",
    val embed: String = "",
    val m3u8: String = "",
)

class NguonCClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://phim.nguonc.com/api",
    private val json: Json = defaultJson,
) {
    suspend fun listNewMovies(page: Int = 1): NguonCListResponseDto {
        val body = httpClient.get("$baseUrl/films/phim-moi-cap-nhat") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun listByCategory(slug: String, page: Int = 1): NguonCListResponseDto {
        if (slug.isBlank()) return NguonCListResponseDto()
        val body = httpClient.get("$baseUrl/films/danh-sach/$slug") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun movieDetail(slug: String): NguonCDetailResponseDto {
        if (slug.isBlank()) return NguonCDetailResponseDto()
        val body = httpClient.get("$baseUrl/film/$slug").bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun listByGenre(slug: String, page: Int = 1): NguonCListResponseDto {
        if (slug.isBlank()) return NguonCListResponseDto()
        val body = httpClient.get("$baseUrl/films/the-loai/$slug") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun listByCountry(slug: String, page: Int = 1): NguonCListResponseDto {
        if (slug.isBlank()) return NguonCListResponseDto()
        val body = httpClient.get("$baseUrl/films/quoc-gia/$slug") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun listByYear(year: Int, page: Int = 1): NguonCListResponseDto {
        val body = httpClient.get("$baseUrl/films/nam-phat-hanh/$year") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun search(keyword: String): NguonCListResponseDto {
        if (keyword.isBlank()) return NguonCListResponseDto()
        val body = httpClient.get("$baseUrl/films/search") {
            parameter("keyword", keyword)
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
