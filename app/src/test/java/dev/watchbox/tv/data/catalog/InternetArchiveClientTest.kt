package dev.watchbox.tv.data.catalog

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InternetArchiveClientTest {
    private lateinit var server: MockWebServer
    private lateinit var httpClient: OkHttpClient
    private lateinit var client: InternetArchiveClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        httpClient = OkHttpClient()
        client = InternetArchiveClient(httpClient, server.url("/"))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search requests only licensed open source movies and parses defaults`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"response":{"docs":[{"identifier":"film","title":"Film","unknown":"ignored"}]}}""",
            ),
        )

        val response = client.search("sintel")
        val request = server.takeRequest()

        assertEquals("film", response.response.docs.single().identifier?.toString()?.trim('"'))
        val query = request.requestUrl!!.queryParameter("q").orEmpty()
        assertTrue(query.contains("mediatype:movies"))
        assertTrue(query.contains("collection:opensource_movies"))
        assertTrue(query.contains("licenseurl:*"))
        assertTrue(query.contains("sintel"))
    }

    @Test
    fun `malformed items are skipped without discarding valid siblings`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"response":{"docs":["broken",{"identifier":"valid","title":"Valid"}]}}""",
            ),
        )
        server.enqueue(
            MockResponse().setBody(
                """{"metadata":{"identifier":"valid","title":"Valid"},"files":[42,{"name":"valid.mp4","format":"MPEG4"}]}""",
            ),
        )

        val search = client.search("valid")
        val metadata = client.metadata("valid")

        assertEquals(1, search.response.docs.size)
        assertEquals(1, metadata.files.size)
    }

    @Test
    fun `HTTP errors remain observable to callers`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))

        var failure: Throwable? = null
        try {
            client.search("offline")
        } catch (error: Throwable) {
            failure = error
        }

        assertTrue(failure is IOException)
    }

    @Test
    fun `malformed whole JSON envelope remains observable`() = runTest {
        server.enqueue(MockResponse().setBody("not-json"))

        var failure: Throwable? = null
        try {
            client.metadata("film id/part")
        } catch (error: Throwable) {
            failure = error
        }

        val request = server.takeRequest()
        assertTrue(request.path!!.startsWith("/metadata/film%20id%2Fpart"))
        assertTrue(failure != null)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `cancelling coroutine cancels in-flight OkHttp call`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val job = launch { client.search("slow") }
        yield()
        assertTrue(server.takeRequest(1, TimeUnit.SECONDS) != null)
        withTimeout(1_000) {
            while (httpClient.dispatcher.runningCallsCount() == 0) delay(10)
        }

        job.cancelAndJoin()

        withTimeout(1_000) {
            while (httpClient.dispatcher.runningCallsCount() != 0) delay(10)
        }
        assertTrue(job.isCancelled)
    }
}
