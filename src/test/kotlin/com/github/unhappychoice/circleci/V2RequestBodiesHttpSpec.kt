package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateProjectRequest
import com.github.unhappychoice.circleci.v2.request.TriggerPipelineRunRequest
import com.github.unhappychoice.circleci.v2.request.UpdateScheduleParameters
import com.github.unhappychoice.circleci.v2.request.UpdateWebhookRequest
import com.google.gson.JsonParser
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object V2RequestBodiesHttpSpec : Spek({
    test("triggerPipelineRun sends a pipeline definition and omits unspecified fields") {
        val definitionId = "2338d0ae-5541-4bbf-88a2-55e9f7281f80"
        assertRequest(
            "POST", "/api/v2/project/gh/example/project/pipeline/run", """{"definition_id":"$definitionId"}""",
            response = MockResponse(code = 201, body = """{"id":"pipeline-id"}""")
        ) { it.triggerPipelineRun("gh", "example", "project", TriggerPipelineRunRequest(definitionId = definitionId)) }
    }

    test("createProject sends the project name") {
        assertRequest(
            "POST", "/api/v2/project/gh/example/project", """{"name":"project"}""",
            response = MockResponse(code = 201, body = """{"id":"project-id"}""")
        ) { it.createProject("gh", "example", "project", CreateProjectRequest(name = "project")) }
    }

    test("updateSchedule sends an empty JSON object when all fields are omitted") {
        assertRequest("PATCH", "/api/v2/schedule/schedule-id", "{}") {
            it.updateSchedule("schedule-id", UpdateScheduleParameters())
        }
    }

    test("updateWebhook sends the supplied fields and omits unspecified fields") {
        assertRequest("PUT", "/api/v2/webhook/webhook-id", """{"name":"Build events","verify-tls":false}""") {
            it.updateWebhook("webhook-id", UpdateWebhookRequest(name = "Build events", verifyTls = false))
        }
    }

    test("updateWebhook sends an empty JSON object when all fields are omitted") {
        assertRequest("PUT", "/api/v2/webhook/webhook-id", "{}") {
            it.updateWebhook("webhook-id", UpdateWebhookRequest())
        }
    }
})

private fun assertRequest(
    method: String,
    path: String,
    expectedBody: String,
    response: MockResponse = MockResponse(body = """{"id":"example-id"}"""),
    call: (CircleCIAPIClientV2) -> Observable<*>
) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(response)
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        call(client).test().assertComplete().assertNoErrors().assertValueCount(1)

        val request = server.takeRequest()
        assertEquals(method, request.method)
        assertEquals(path, request.url.encodedPath)
        assertEquals(JsonParser.parseString(expectedBody), JsonParser.parseString(requireNotNull(request.body).utf8()))
    }
}
