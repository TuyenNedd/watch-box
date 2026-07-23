package dev.watchbox.ui

private fun dateNow(): Double = js("Date.now()")

internal actual fun currentTimeMs(): Long = dateNow().toLong()
