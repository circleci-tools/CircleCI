package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest
import com.github.unhappychoice.circleci.v3.response.RunListResponse
import com.github.unhappychoice.circleci.v3.response.RunResponse
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object CircleCIAPIClientV3HttpSpec : Spek({
    test("rejects an invalid bearer token without revealing it") {
        val error = assertThrows(IllegalArgumentException::class.java) {
            CircleCIAPIClient("secret-test-token\n").clientV3()
        }
        assertEquals("CircleCI API token contains invalid header characters", error.message)
    }

    mapOf(
        "{}" to TriggerRunRequest(pipelineId),
        """{"config":{"branch":"main"}}""" to TriggerRunRequest(pipelineId, config = TriggerRunRequest.Ref(branch = "main")),
        """{"checkout":{"tag":"v1"}}""" to TriggerRunRequest(pipelineId, checkout = TriggerRunRequest.Ref(tag = "v1"))
    ).forEach { (attributes, request) ->
        test("triggers a run with attributes $attributes") {
            MockWebServer().use { server ->
                server.enqueue(createdRun())
                val response = server.client().triggerRun(request).blockingSingle()

                assertEquals(runId, response.data.id)
                val recorded = server.takeRequest(5, SECONDS)!!
                assertRequest(recorded, "POST", "/api/v3/runs")
                val expected = """{"data":{"attributes":$attributes,"references":{"pipeline":{"id":"$pipelineId"}}}}"""
                assertEquals(JsonParser.parseString(expected), JsonParser.parseString(recorded.body.readUtf8()))
            }
        }
    }

    test("rejects refs with both branch and tag or neither") {
        assertThrows(IllegalArgumentException::class.java) { TriggerRunRequest.Ref(branch = "main", tag = "v1") }
        assertThrows(IllegalArgumentException::class.java) { TriggerRunRequest.Ref() }
    }

    test("rejects mixed config and checkout reference types") {
        val branch = TriggerRunRequest.Ref(branch = "main")
        val tag = TriggerRunRequest.Ref(tag = "v1")
        assertThrows(IllegalArgumentException::class.java) { TriggerRunRequest(pipelineId, config = branch, checkout = tag) }
        assertThrows(IllegalArgumentException::class.java) { TriggerRunRequest(pipelineId, config = tag, checkout = branch) }
    }

    listOf("branch" to "main", "tag" to "v1.0.0").forEach { (field, value) ->
        test("serializes config, checkout, parameters, and project scope for $field and $value") {
            val selection = TriggerRunRequest.Ref(
                branch = value.takeIf { field == "branch" },
                tag = value.takeIf { field == "tag" }
            )
            MockWebServer().use { server ->
                server.enqueue(createdRun())
                val request = TriggerRunRequest(
                    pipelineId,
                    config = selection,
                    checkout = selection,
                    parameters = mapOf("deploy" to true, "retries" to 2, "environment" to "staging")
                )
                server.client().triggerRun(request, projectId).blockingSingle()

                val recorded = server.takeRequest(5, SECONDS)!!
                assertRequest(recorded, "POST", "/api/v3/runs?filter%5Bproject_id%5D=$projectId")
                val expected = """
                    {"data":{"attributes":{"config":{"$field":"$value"},"checkout":{"$field":"$value"},
                    "parameters":{"deploy":true,"retries":2,"environment":"staging"}},
                    "references":{"pipeline":{"id":"$pipelineId"}}}}
                """.trimIndent()
                assertEquals(JsonParser.parseString(expected), JsonParser.parseString(recorded.body.readUtf8()))
            }
        }
    }

    test("decodes the phase and current outcome for a run") {
        MockWebServer().use { server ->
            server.enqueue(run("ended", "\"succeeded\""))
            val response = server.client().getRun(runId).blockingSingle()

            val expected = RunResponse(RunResponse.Data(runId, RunResponse.Attributes("ended", "succeeded")))
            assertEquals(expected, response)
            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", "/api/v3/runs/$runId")
        }
    }

    test("decodes a queued run with a null outcome") {
        MockWebServer().use { server ->
            server.enqueue(run("queued", "null"))
            val response = server.client().getRun(runId).blockingSingle()

            assertEquals("queued", response.data.attributes.phase)
            assertNull(response.data.attributes.currentOutcome)
        }
    }

    test("getRun exposes the HTTP status and body when rate limited") {
        MockWebServer().use { server ->
            val body = """{"message":"Rate limit exceeded"}"""
            server.enqueue(MockResponse().setResponseCode(429).setBody(body))

            server.client().getRun(runId).test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 429 && error.response()?.errorBody()?.string() == body
            }
        }
    }

    test("getRun rejects a success response with non-object run attributes") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"data":{"id":"$runId","attributes":[]}}"""))

            server.client().getRun(runId).test().assertFailure(JsonSyntaxException::class.java)
        }
    }

    test("waits for a run to end through the v3 HTTP client") {
        MockWebServer().use { server ->
            server.enqueue(run("started", "\"failed\""))
            server.enqueue(run("ended", "\"succeeded\""))
            val expected = RunResponse(RunResponse.Data(runId, RunResponse.Attributes("ended", "succeeded")))

            server.client().waitForRun(runId, 1.milliseconds, 5.seconds)
                .test()
                .awaitDone(5, SECONDS)
                .assertResult(expected)
            repeat(2) {
                assertRequest(server.takeRequest(5, SECONDS)!!, "GET", "/api/v3/runs/$runId")
            }
            assertEquals(2, server.requestCount)
        }
    }

    test("lists project runs and follows the opaque next cursor") {
        MockWebServer().use { server ->
            val cursor = "a+/=%2F&? #"
            server.enqueue(runPage("""{"branch":"main","revision":"abc123"}""", """{"next":"$cursor","prev":null}"""))
            server.enqueue(MockResponse().setBody("""{"data":[],"page":{"next":null,"prev":"back"}}"""))
            val client = server.client()
            val first = client.listProjectRuns(projectId).blockingSingle()
            val summary = first.data.single()
            assertEquals(runId, summary.id)
            assertEquals(
                RunListResponse.Attributes("queued", null, "2026-11-18T00:00:00Z", 7),
                summary.attributes
            )
            assertEquals(projectId, summary.references.project.id)
            assertEquals("main", summary.branch)
            assertEquals("abc123", summary.revision)
            assertEquals(cursor, first.page.next)
            assertNull(first.page.prev)

            val path = "/api/v3/runs?filter%5Bproject_id%5D=$projectId"
            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", path)
            val second = client.listProjectRuns(projectId, pageSize = 10, cursor = first.page.next).blockingSingle()
            val request = server.takeRequest(5, SECONDS)!!
            assertRequest(request, "GET", "$path&page%5Blimit%5D=10&page%5Bcursor%5D=a%2B%2F%3D%252F%26%3F%20%23")
            assertEquals(cursor, request.requestUrl!!.queryParameter("page[cursor]"))
            assertEquals(emptyList<Any>(), second.data)
            assertNull(second.page.next)
            assertEquals("back", second.page.prev)
        }
    }

    test("lists the authenticated user's runs without default pagination parameters") {
        MockWebServer().use { server ->
            server.enqueue(runPage("null", "{}"))
            val response = server.client().listMyRuns().blockingSingle()

            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", "/api/v3/runs?filter%5Buser_id%5D=me")
            assertNull(response.data.single().branch)
            assertNull(response.data.single().revision)
            assertNull(response.page.next)
        }
    }

    test("follows the next cursor while retaining the authenticated user filter") {
        MockWebServer().use { server ->
            server.enqueue(runPage("null", """{"next":"next+/="}"""))
            server.enqueue(MockResponse().setBody("""{"data":[],"page":{}}"""))
            val client = server.client()
            val first = client.listMyRuns(pageSize = 5).blockingSingle()
            val path = "/api/v3/runs?filter%5Buser_id%5D=me&page%5Blimit%5D=5"
            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", path)

            val second = client.listMyRuns(pageSize = 5, cursor = first.page.next).blockingSingle()
            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", "$path&page%5Bcursor%5D=next%2B%2F%3D")
            assertEquals(emptyList<Any>(), second.data)
            assertNull(second.page.next)
        }
    }

    test("delivers v3 responses asynchronously when created with the async factory") {
        MockWebServer().use { server ->
            server.enqueue(run("ended", "\"succeeded\""))
            val subscribingThread = Thread.currentThread()
            val client = CircleCIAPIClient.async("test-token", server.url("/api/").toString()).clientV3()
            val expected = RunResponse(RunResponse.Data(runId, RunResponse.Attributes("ended", "succeeded")))

            client.getRun(runId)
                .doOnNext { assertNotSame(subscribingThread, Thread.currentThread()) }
                .test()
                .awaitDone(5, SECONDS)
                .assertResult(expected)
            assertRequest(server.takeRequest(5, SECONDS)!!, "GET", "/api/v3/runs/$runId")
        }
    }
})

private const val pipelineId = "11111111-1111-4111-8111-111111111111"
private const val projectId = "22222222-2222-4222-8222-222222222222"
private const val runId = "33333333-3333-4333-8333-333333333333"

private fun MockWebServer.client() = CircleCIAPIClient("test-token", url("/api/").toString()).clientV3()

private fun createdRun() = MockResponse().setResponseCode(201).setBody(
    """{"data":{"id":"$runId","attributes":{"created_at":"2026-11-18T00:00:00Z"}}}"""
)

private fun run(phase: String, outcome: String) = MockResponse().setBody(
    """{"data":{"id":"$runId","attributes":{"phase":"$phase","current_outcome":$outcome,"number":1}}}"""
)

private fun runPage(vcs: String, page: String) = MockResponse().setBody(
    """
    {
      "data": [{
        "id": "$runId",
        "attributes": {"phase":"queued","current_outcome":null,"created_at":"2026-11-18T00:00:00Z","number":7},
        "references": {
          "event":{"attributes":{"vcs":$vcs}},
          "project":{"id":"$projectId"},
          "pipeline":{"id":"$pipelineId","attributes":{}},
          "trigger":{"id":"44444444-4444-4444-8444-444444444444","attributes":{"event_source":{}}}
        }
      }],
      "page": $page
    }
    """.trimIndent()
)

private fun assertRequest(request: RecordedRequest, method: String, path: String) {
    assertEquals(method, request.method)
    assertEquals(path, request.path)
    assertEquals("Bearer test-token", request.getHeader("Authorization"))
    assertNull(request.getHeader("Circle-Token"))
}
