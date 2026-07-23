package dev.watchbox.player

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long? = null,
    val error: String? = null,
    val isBuffering: Boolean = false,
)
