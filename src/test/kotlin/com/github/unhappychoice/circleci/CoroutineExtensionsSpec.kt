package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
})
