package dev.watchbox.tv.data.local

import android.content.SharedPreferences
import dev.watchbox.tv.core.model.PlaybackProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class PreferencesLibraryStore(
    private val preferences: SharedPreferences,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : LibraryStore {
    private val mutableFavoriteIds = MutableStateFlow(loadFavorites())
    private val mutableProgress = MutableStateFlow(loadProgress())

    override val favoriteIds: StateFlow<Set<String>> = mutableFavoriteIds.asStateFlow()
    override val progress: StateFlow<Map<String, PlaybackProgress>> = mutableProgress.asStateFlow()

    override fun setFavorite(movieId: String, favorite: Boolean): Boolean {
        if (movieId.isBlank()) return false
        val updated = mutableFavoriteIds.value.toMutableSet().apply {
            if (favorite) add(movieId) else remove(movieId)
        }.toSet()
        mutableFavoriteIds.value = updated
        preferences.edit().putStringSet(FAVORITES_KEY, updated).apply()
        return favorite
    }

    override fun toggleFavorite(movieId: String): Boolean =
        setFavorite(movieId, movieId !in mutableFavoriteIds.value)

    override fun saveProgress(movieId: String, progress: PlaybackProgress, durationMs: Long?) {
        if (movieId.isBlank() || progress.positionMs < 0 || progress.updatedAtEpochMs < 0) return
        if (durationMs != null && durationMs < 0) return
        if (progress.shouldClear(durationMs)) {
            clearProgress(movieId)
            return
        }
        val updated = mutableProgress.value.toMutableMap().apply { put(movieId, progress) }.toMap()
        updateProgress(updated)
    }

    override fun clearProgress(movieId: String) {
        if (movieId !in mutableProgress.value) return
        val updated = mutableProgress.value.toMutableMap().apply { remove(movieId) }.toMap()
        updateProgress(updated)
    }

    private fun updateProgress(updated: Map<String, PlaybackProgress>) {
        mutableProgress.value = updated
        preferences.edit().putString(PROGRESS_KEY, json.encodeToString(updated)).apply()
    }

    private fun loadFavorites(): Set<String> =
        preferences.getStringSet(FAVORITES_KEY, emptySet()).orEmpty().filter { it.isNotBlank() }.toSet()

    private fun loadProgress(): Map<String, PlaybackProgress> {
        val raw = preferences.getString(PROGRESS_KEY, null) ?: return emptyMap()
        val entries = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull()
            ?: return emptyMap()
        return entries.mapNotNull { (id, element) ->
            val value = runCatching {
                json.decodeFromJsonElement<PlaybackProgress>(element)
            }.getOrNull() ?: return@mapNotNull null
            if (id.isBlank() || value.positionMs < 0 || value.updatedAtEpochMs < 0) {
                null
            } else {
                id to value
            }
        }.toMap()
    }

    private companion object {
        const val FAVORITES_KEY = "favorites"
        const val PROGRESS_KEY = "progress"
    }
}
