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

// --- DTOs for phim-moi-cap-nhat (list) endpoint ---

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

// --- DTOs for detail endpoint ---

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

// --- DTOs for search endpoint ---

@Serializable
data class PhimApiSearchResponseDto(
    val status: String = "",
    val data: PhimApiSearchDataDto? = null,
)

@Serializable
data class PhimApiSearchDataDto(
    val items: List<PhimApiItemDto> = emptyList(),
)

// --- Client ---

class PhimApiClient(
    private val httpClient: OkHttpClient,
    private val baseUrl: HttpUrl = "https://phimapi.com/".toHttpUrl(),
    private val json: Json = defaultJson,
) {
    suspend fun listNewMovies(page: Int = 1): PhimApiListResponseDto {
        val url = baseUrl.newBuilder()
            .addPathSegment("danh-sach")
            .addPathSegment("phim-moi-cap-nhat")
            .addQueryParameter("page", page.toString())
            .build()
        val body = getBody(url)
        return json.decodeFromString<PhimApiListResponseDto>(body)
    }

    suspend fun movieDetail(slug: String): PhimApiDetailResponseDto {
        if (slug.isBlank()) return PhimApiDetailResponseDto()
        val url = baseUrl.newBuilder()
            .addPathSegment("phim")
            .addPathSegment(slug)
            .build()
        val body = getBody(url)
        return json.decodeFromString<PhimApiDetailResponseDto>(body)
    }

    suspend fun search(keyword: String, limit: Int = 20): PhimApiSearchResponseDto {
        if (keyword.isBlank()) return PhimApiSearchResponseDto()
        val url = baseUrl.newBuilder()
            .addPathSegment("v1")
            .addPathSegment("api")
            .addPathSegment("tim-kiem")
            .addQueryParameter("keyword", keyword)
            .addQueryParameter("limit", limit.toString())
            .build()
        val body = getBody(url)
        return json.decodeFromString<PhimApiSearchResponseDto>(body)
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
                                Result.failure(IOException("PhimApi HTTP ${response.code}")),
                            )
                            return
                        }
                        val body = response.body?.string()
                        if (body == null) {
                            continuation.resumeWith(Result.failure(IOException("PhimApi response has no body")))
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
