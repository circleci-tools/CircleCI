package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest
import com.github.unhappychoice.circleci.v3.response.RunResponse
import com.google.gson.JsonParser
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientV3HttpSpec : Spek({
    test("rejects an invalid bearer token without revealing it") {
        val error = assertThrows(IllegalArgumentException::class.java) {
            CircleCIAPIClient("secret-test-token\n").clientV3()
        }
        assertEquals("CircleCI API token contains invalid header characters", error.message)
    }

    test("triggers a run with only a known pipeline ID") {
        MockWebServer().use { server ->
            server.enqueue(createdRun())
            val response = server.client().triggerRun(TriggerRunRequest(pipelineId)).blockingSingle()

            assertEquals(runId, response.data.id)
            val request = server.takeRequest(5, SECONDS)!!
            assertRequest(request, "POST", "/api/v3/runs")
            assertEquals(
                JsonParser.parseString("""{"data":{"attributes":{},"references":{"pipeline":{"id":"$pipelineId"}}}}"""),
                JsonParser.parseString(request.body.readUtf8())
            )
        }
    }

    test("serializes config, checkout, parameters, and project scope") {
        MockWebServer().use { server ->
            server.enqueue(createdRun())
            val request = TriggerRunRequest(
                pipelineId,
                config = TriggerRunRequest.Ref(branch = "main"),
                checkout = TriggerRunRequest.Ref(tag = "v1.0.0"),
                parameters = mapOf("deploy" to true, "retries" to 2, "environment" to "staging")
            )
            server.client().triggerRun(request, projectId).blockingSingle()

            val recorded = server.takeRequest(5, SECONDS)!!
            assertRequest(recorded, "POST", "/api/v3/runs?filter%5Bproject_id%5D=$projectId")
            val expected = """
                {"data":{"attributes":{"config":{"branch":"main"},"checkout":{"tag":"v1.0.0"},
                "parameters":{"deploy":true,"retries":2,"environment":"staging"}},
                "references":{"pipeline":{"id":"$pipelineId"}}}}
            """.trimIndent()
            assertEquals(JsonParser.parseString(expected), JsonParser.parseString(recorded.body.readUtf8()))
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

private fun assertRequest(request: RecordedRequest, method: String, path: String) {
    assertEquals(method, request.method)
    assertEquals(path, request.path)
    assertEquals("Bearer test-token", request.getHeader("Authorization"))
    assertNull(request.getHeader("Circle-Token"))
}
