package dev.watchbox.data.catalog

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
