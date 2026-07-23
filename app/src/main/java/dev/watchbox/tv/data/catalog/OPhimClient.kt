package dev.watchbox.tv.data.catalog

import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

// --- DTOs for OPhim list endpoint (danh-sach/phim-moi-cap-nhat) ---

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

// --- DTOs for OPhim detail endpoint (/phim/{slug}) ---

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

// --- DTOs for OPhim search endpoint (tim-kiem) ---

@Serializable
data class OPhimSearchResponseDto(
    val status: String = "",
    val data: OPhimSearchDataDto? = null,
)

@Serializable
data class OPhimSearchDataDto(
    val items: List<OPhimItemDto> = emptyList(),
)

// --- Client ---

class OPhimClient(
    private val httpClient: OkHttpClient,
    private val baseUrl: HttpUrl = "https://ophim1.com/v1/api/".toHttpUrl(),
    private val json: Json = defaultJson,
) {
    suspend fun listNewMovies(page: Int = 1): OPhimListResponseDto {
        val url = baseUrl.newBuilder()
            .addPathSegment("danh-sach")
            .addPathSegment("phim-moi-cap-nhat")
            .addQueryParameter("page", page.toString())
            .build()
        val body = getBody(url)
        return json.decodeFromString<OPhimListResponseDto>(body)
    }

    suspend fun movieDetail(slug: String): OPhimDetailResponseDto {
        if (slug.isBlank()) return OPhimDetailResponseDto()
        val url = baseUrl.newBuilder()
            .addPathSegment("phim")
            .addPathSegment(slug)
            .build()
        val body = getBody(url)
        return json.decodeFromString<OPhimDetailResponseDto>(body)
    }

    suspend fun search(keyword: String, limit: Int = 20): OPhimSearchResponseDto {
        if (keyword.isBlank()) return OPhimSearchResponseDto()
        val url = baseUrl.newBuilder()
            .addPathSegment("tim-kiem")
            .addQueryParameter("keyword", keyword)
            .addQueryParameter("limit", limit.toString())
            .build()
        val body = getBody(url)
        return json.decodeFromString<OPhimSearchResponseDto>(body)
    }

    private suspend fun getBody(url: HttpUrl): String = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(url).get().build()
        val call = httpClient.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    if (continuation.isActive) continuation.resumeWith(Result.failure(error))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!continuation.isActive) return
                        if (!response.isSuccessful) {
                            continuation.resumeWith(
                                Result.failure(IOException("OPhim HTTP ${response.code}")),
                            )
                            return
                        }
                        val body = response.body?.string()
                        if (body == null) {
                            continuation.resumeWith(Result.failure(IOException("OPhim response has no body")))
                        } else {
                            continuation.resumeWith(Result.success(body))
                        }
                    }
                }
            },
        )
    }

    companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
