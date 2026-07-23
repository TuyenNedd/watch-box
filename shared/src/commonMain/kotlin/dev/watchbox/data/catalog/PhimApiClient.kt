package dev.watchbox.data.catalog

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PhimApiListResponseDto(
    val status: Boolean = false,
    val items: List<PhimApiItemDto> = emptyList(),
)

@Serializable
data class PhimApiItemDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("origin_name") val originName: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    val year: Int? = null,
    val lang: String = "",
    val quality: String = "",
    @SerialName("episode_current") val episodeCurrent: String = "",
    val time: String = "",
    val category: List<PhimApiCategoryDto> = emptyList(),
    val country: List<PhimApiCountryDto> = emptyList(),
)

@Serializable
data class PhimApiDetailResponseDto(
    val status: Boolean = false,
    val movie: PhimApiMovieDto? = null,
    val episodes: List<PhimApiEpisodeServerDto> = emptyList(),
)

@Serializable
data class PhimApiMovieDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("origin_name") val originName: String = "",
    val content: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    val lang: String = "",
    val quality: String = "",
    val year: Int? = null,
    val actor: List<String> = emptyList(),
    val category: List<PhimApiCategoryDto> = emptyList(),
    val country: List<PhimApiCountryDto> = emptyList(),
)

@Serializable
data class PhimApiCategoryDto(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class PhimApiCountryDto(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class PhimApiEpisodeServerDto(
    @SerialName("server_name") val serverName: String = "",
    @SerialName("server_data") val serverData: List<PhimApiEpisodeDto> = emptyList(),
)

@Serializable
data class PhimApiEpisodeDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("link_m3u8") val linkM3u8: String = "",
)

@Serializable
data class PhimApiSearchResponseDto(
    val status: String = "",
    val data: PhimApiSearchDataDto? = null,
)

@Serializable
data class PhimApiSearchDataDto(
    val items: List<PhimApiItemDto> = emptyList(),
)

class PhimApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://phimapi.com",
    private val json: Json = defaultJson,
) {
    suspend fun listNewMovies(page: Int = 1): PhimApiListResponseDto {
        val body = httpClient.get("$baseUrl/danh-sach/phim-moi-cap-nhat") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun movieDetail(slug: String): PhimApiDetailResponseDto {
        if (slug.isBlank()) return PhimApiDetailResponseDto()
        val body = httpClient.get("$baseUrl/phim/$slug").bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun search(keyword: String, limit: Int = 20): PhimApiSearchResponseDto {
        if (keyword.isBlank()) return PhimApiSearchResponseDto()
        val body = httpClient.get("$baseUrl/v1/api/tim-kiem") {
            parameter("keyword", keyword)
            parameter("limit", limit.toString())
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
