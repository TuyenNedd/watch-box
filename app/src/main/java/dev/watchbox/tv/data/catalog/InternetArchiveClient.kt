package dev.watchbox.tv.data.catalog

import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

@Serializable
data class ArchiveSearchResponseDto(
    val response: ArchiveSearchPageDto = ArchiveSearchPageDto(),
)

@Serializable
data class ArchiveSearchPageDto(
    val docs: List<ArchiveSearchDocumentDto> = emptyList(),
)

@Serializable
data class ArchiveSearchDocumentDto(
    val identifier: JsonElement? = null,
    val title: JsonElement? = null,
    val description: JsonElement? = null,
    val year: JsonElement? = null,
    @SerialName("licenseurl") val licenseUrl: JsonElement? = null,
    val creator: JsonElement? = null,
)

@Serializable
data class ArchiveMetadataResponseDto(
    val metadata: ArchiveMetadataDto = ArchiveMetadataDto(),
    val files: List<ArchiveFileDto> = emptyList(),
)

@Serializable
data class ArchiveMetadataDto(
    val identifier: JsonElement? = null,
    val title: JsonElement? = null,
    val description: JsonElement? = null,
    val date: JsonElement? = null,
    @SerialName("licenseurl") val licenseUrl: JsonElement? = null,
    val creator: JsonElement? = null,
)

@Serializable
data class ArchiveFileDto(
    val name: JsonElement? = null,
    val format: JsonElement? = null,
    val size: JsonElement? = null,
)

class InternetArchiveClient(
    private val httpClient: OkHttpClient,
    private val baseUrl: HttpUrl = "https://archive.org/".toHttpUrl(),
    private val json: Json = defaultJson,
) {
    suspend fun search(query: String): ArchiveSearchResponseDto {
        val archiveQuery = buildString {
            append("mediatype:movies AND collection:opensource_movies AND licenseurl:*")
            if (query.isNotBlank()) {
                append(" AND (title:(\"")
                append(query.replace("\"", " "))
                append("\") OR description:(\"")
                append(query.replace("\"", " "))
                append("\"))")
            }
        }
        val url = baseUrl.newBuilder()
            .addPathSegment("advancedsearch.php")
            .addQueryParameter("q", archiveQuery)
            .addQueryParameter("fl[]", "identifier,title,description,year,licenseurl,creator")
            .addQueryParameter("rows", "50")
            .addQueryParameter("page", "1")
            .addQueryParameter("output", "json")
            .build()
        return decodeSearch(getBody(url))
    }

    suspend fun metadata(identifier: String): ArchiveMetadataResponseDto {
        if (identifier.isBlank()) return ArchiveMetadataResponseDto()
        val url = baseUrl.newBuilder()
            .addPathSegment("metadata")
            .addPathSegment(identifier)
            .build()
        return decodeMetadata(getBody(url))
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
                                Result.failure(IOException("Internet Archive HTTP ${response.code}")),
                            )
                            return
                        }
                        val body = response.body?.string()
                        if (body == null) {
                            continuation.resumeWith(Result.failure(IOException("Internet Archive response has no body")))
                        } else {
                            continuation.resumeWith(Result.success(body))
                        }
                    }
                }
            },
        )
    }

    private fun decodeSearch(body: String): ArchiveSearchResponseDto {
        val root = json.parseToJsonElement(body) as? JsonObject
            ?: throw SerializationException("Internet Archive search envelope must be an object")
        val responseElement = root["response"] ?: return ArchiveSearchResponseDto()
        val response = responseElement as? JsonObject
            ?: throw SerializationException("Internet Archive response must be an object")
        val docsElement = response["docs"] ?: return ArchiveSearchResponseDto()
        val docs = docsElement as? JsonArray
            ?: throw SerializationException("Internet Archive docs must be an array")
        return ArchiveSearchResponseDto(
            ArchiveSearchPageDto(
                docs.mapNotNull { element ->
                    runCatching { json.decodeFromJsonElement<ArchiveSearchDocumentDto>(element) }.getOrNull()
                },
            ),
        )
    }

    private fun decodeMetadata(body: String): ArchiveMetadataResponseDto {
        val root = json.parseToJsonElement(body) as? JsonObject
            ?: throw SerializationException("Internet Archive metadata envelope must be an object")
        val metadata = root["metadata"]?.let { element ->
            runCatching { json.decodeFromJsonElement<ArchiveMetadataDto>(element) }
                .getOrElse { throw SerializationException("Internet Archive metadata is malformed", it) }
        } ?: ArchiveMetadataDto()
        val filesElement = root["files"]
        val files = when (filesElement) {
            null -> emptyList()
            is JsonArray -> filesElement.mapNotNull { element ->
                runCatching { json.decodeFromJsonElement<ArchiveFileDto>(element) }.getOrNull()
            }
            else -> throw SerializationException("Internet Archive files must be an array")
        }
        return ArchiveMetadataResponseDto(metadata, files)
    }

    companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
