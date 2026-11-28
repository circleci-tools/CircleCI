package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.HttpException

@Suppress("DEPRECATION")
object CoroutineExtensionsSpec : Spek({
    test("awaits a V2 Observable's single item") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(body = """{"id":"user-id","login":"octocat","name":"The Octocat"}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val user = runBlocking { client.getMe().await() }

            assertEquals("octocat", user.login)
            assertEquals("GET", server.takeRequest().method)
        }
    }

    test("propagates a V3 HTTP error through the suspend wrapper") {
        MockWebServer().use { server ->
            server.start()
            val body = """{"message":"Rate limit exceeded"}"""
            server.enqueue(MockResponse(code = 429, body = body))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()

            val error = assertThrows(HttpException::class.java) {
                runBlocking { client.getRun("run-id").await() }
            }

            assertEquals(429, error.code())
            assertEquals(body, error.response()?.errorBody()?.string())
        }
    }

    test("awaits a V3 Observable's single item") {
        MockWebServer().use { server ->
            server.start()
            val body = """{"data":{"id":"run-id","attributes":{"phase":"ended","current_outcome":"succeeded"}}}"""
            server.enqueue(MockResponse(body = body))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()

            val run = runBlocking { client.getRun("run-id").await() }

            assertEquals("ended", run.data.attributes.phase)
            assertEquals("succeeded", run.data.attributes.currentOutcome)
        }
    }

    test("awaits a legacy V1 Completable's completion") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(code = 200))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString())
            val request = AddSshKeyRequest("example.com", "test-key")

            runBlocking { client.client().addSshKey("example-org", "example-project", request).await() }

            assertEquals("POST", server.takeRequest().method)
        }
    }

    test("Observable await leaves the caller dispatcher free and disposes on cancellation") {
        assertPendingRequestCanBeCancelled("GET") { client, onDispose ->
            client.clientV2().getMe().doOnDispose { onDispose() }.await()
        }
    }

    test("Completable await leaves the caller dispatcher free and disposes on cancellation") {
        assertPendingRequestCanBeCancelled("POST") { client, onDispose ->
            val request = AddSshKeyRequest("example.com", "test-key")
            client.client().addSshKey("example-org", "example-project", request).doOnDispose { onDispose() }.await()
        }
    }
})

private fun assertPendingRequestCanBeCancelled(
    method: String,
    awaitRequest: suspend (CircleCIAPIClient, () -> Unit) -> Unit,
) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString())
        val disposed = CompletableDeferred<Unit>()

        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { dispatcher ->
            runBlocking {
                val request = launch(dispatcher) { awaitRequest(client) { disposed.complete(Unit) } }
                assertEquals(method, server.takeRequest(5, SECONDS)?.method)
                withTimeout(5_000) {
                    withContext(dispatcher) { assertTrue(request.isActive) }
                    request.cancelAndJoin()
                    disposed.await()
                }
            }
        }
    }
}
