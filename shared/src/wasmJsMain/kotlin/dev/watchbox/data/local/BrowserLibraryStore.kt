package dev.watchbox.data.local

import dev.watchbox.core.model.PlaybackProgress
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BrowserLibraryStore(
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
        saveFavorites(updated)
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
        window.localStorage.setItem(PROGRESS_KEY, json.encodeToString(updated))
    }

    private fun saveFavorites(favorites: Set<String>) {
        window.localStorage.setItem(FAVORITES_KEY, json.encodeToString(favorites))
    }

    private fun loadFavorites(): Set<String> {
        val raw = window.localStorage.getItem(FAVORITES_KEY) ?: return emptySet()
        return runCatching { json.decodeFromString<Set<String>>(raw) }.getOrDefault(emptySet())
    }

    private fun loadProgress(): Map<String, PlaybackProgress> {
        val raw = window.localStorage.getItem(PROGRESS_KEY) ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<String, PlaybackProgress>>(raw) }.getOrDefault(emptyMap())
    }

    private companion object {
        const val FAVORITES_KEY = "watchbox_favorites"
        const val PROGRESS_KEY = "watchbox_progress"
    }
}
