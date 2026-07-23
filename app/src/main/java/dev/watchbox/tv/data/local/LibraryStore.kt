package dev.watchbox.tv.data.local

import dev.watchbox.tv.core.model.PlaybackProgress
import kotlinx.coroutines.flow.StateFlow

interface LibraryStore {
    val favoriteIds: StateFlow<Set<String>>
    val progress: StateFlow<Map<String, PlaybackProgress>>

    fun setFavorite(movieId: String, favorite: Boolean): Boolean
    fun toggleFavorite(movieId: String): Boolean
    fun saveProgress(movieId: String, progress: PlaybackProgress, durationMs: Long?)
    fun clearProgress(movieId: String)
}
