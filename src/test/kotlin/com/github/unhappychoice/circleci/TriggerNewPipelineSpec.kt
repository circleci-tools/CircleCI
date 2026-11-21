package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.TriggerNewPipelineRequest
import com.github.unhappychoice.circleci.v2.response.TriggerNewPipelineResponse
import com.google.gson.JsonParser
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object TriggerNewPipelineSpec : Spek({
    val projectSlug = "gh/unhappychoice/circleci"
    val response = TriggerNewPipelineResponse(42, "pending", "pipeline-id", "2026-01-01T00:00:00Z")
    val responseJson = """{"number":42,"state":"pending","id":"pipeline-id","created_at":"2026-01-01T00:00:00Z"}"""

    fun triggerAndReadBody(trigger: (CircleCIAPIClientV2) -> Observable<TriggerNewPipelineResponse>): String =
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(code = 201, body = responseJson))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            trigger(client).test().assertResult(response)

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals(listOf("api", "v2", "project", projectSlug, "pipeline"), request.url.pathSegments)
            requireNotNull(request.body).utf8()
        }

    test("sends an empty JSON object when options are omitted") {
        assertEquals("{}", triggerAndReadBody { it.triggerNewPipeline(projectSlug) })
    }

    test("sends the specified branch and parameters") {
        val options = TriggerNewPipelineRequest(branch = "main", parameters = mapOf("deploy" to true, "count" to 2))
        val body = triggerAndReadBody { it.triggerNewPipeline(projectSlug, options) }
        val expected = """{"branch":"main","parameters":{"deploy":true,"count":2}}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("sends the specified tag") {
        val options = TriggerNewPipelineRequest(tag = "v1.0.0")
        val body = triggerAndReadBody { it.triggerNewPipeline(projectSlug, options) }
        assertEquals(JsonParser.parseString("""{"tag":"v1.0.0"}"""), JsonParser.parseString(body))
    }
})
