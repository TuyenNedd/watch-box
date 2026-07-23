package dev.watchbox.data.catalog

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 30_000
    }
}
