package dev.watchbox.data.catalog

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OPhimListResponseDto(
    val status: String = "",
    val data: OPhimListDataDto? = null,
)

@Serializable
data class OPhimListDataDto(
    val items: List<OPhimItemDto> = emptyList(),
)

@Serializable
data class OPhimItemDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("origin_name") val originName: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    val year: Int? = null,
    val lang: String = "",
    val quality: String = "",
    @SerialName("episode_current") val episodeCurrent: String = "",
    val time: String = "",
)

@Serializable
data class OPhimDetailResponseDto(
    val status: String = "",
    val data: OPhimDetailDataDto? = null,
)

@Serializable
data class OPhimDetailDataDto(
    val item: OPhimMovieDto? = null,
    @SerialName("APP_DOMAIN_CDN_IMAGE") val appDomainCdnImage: String = "",
)

@Serializable
data class OPhimMovieDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("origin_name") val originName: String = "",
    val content: String = "",
    val lang: String = "",
    val quality: String = "",
    val year: Int? = null,
    val actor: List<String> = emptyList(),
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    val episodes: List<OPhimEpisodeServerDto> = emptyList(),
)

@Serializable
data class OPhimEpisodeServerDto(
    @SerialName("server_name") val serverName: String = "",
    @SerialName("server_data") val serverData: List<OPhimEpisodeDto> = emptyList(),
)

@Serializable
data class OPhimEpisodeDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("link_m3u8") val linkM3u8: String = "",
)

@Serializable
data class OPhimSearchResponseDto(
    val status: String = "",
    val data: OPhimSearchDataDto? = null,
)

@Serializable
data class OPhimSearchDataDto(
    val items: List<OPhimItemDto> = emptyList(),
)

class OPhimClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://ophim1.com/v1/api",
    private val json: Json = defaultJson,
) {
    suspend fun listNewMovies(page: Int = 1): OPhimListResponseDto {
        val body = httpClient.get("$baseUrl/danh-sach/phim-moi-cap-nhat") {
            parameter("page", page.toString())
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun movieDetail(slug: String): OPhimDetailResponseDto {
        if (slug.isBlank()) return OPhimDetailResponseDto()
        val body = httpClient.get("$baseUrl/phim/$slug").bodyAsText()
        return json.decodeFromString(body)
    }

    suspend fun search(keyword: String, limit: Int = 20): OPhimSearchResponseDto {
        if (keyword.isBlank()) return OPhimSearchResponseDto()
        val body = httpClient.get("$baseUrl/tim-kiem") {
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
