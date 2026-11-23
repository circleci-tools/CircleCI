package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.PipelineConfig
import com.google.gson.JsonSyntaxException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.util.concurrent.TimeUnit.SECONDS

object PipelineConfigHttpSpec : Spek({
    test("getPipelineConfigById requests and decodes the source and compiled configuration") {
        val body = """
            {
              "source": "version: 2.1\n",
              "compiled": "version: 2\n",
              "setup-config": "setup: true\n",
              "compiled-setup-config": "version: 2\nsetup: true\n"
            }
        """.trimIndent()

        withConfigResponse(MockResponse(body = body)) { client, server ->
            val config = client.getPipelineConfigById("pipeline-id").blockingSingle()
            assertEquals(
                PipelineConfig("version: 2.1\n", "version: 2\n", "setup: true\n", "version: 2\nsetup: true\n"),
                config
            )
            val request = server.takeRequest(1, SECONDS)
            assertEquals("GET", request?.method)
            assertEquals("/api/v2/pipeline/pipeline-id/config", request?.target)
        }
    }

    test("getPipelineConfigById fails when the response has an invalid source field") {
        val body = """{"source":{},"compiled":"version: 2"}"""

        withConfigResponse(MockResponse(body = body)) { client, _ ->
            client.getPipelineConfigById("pipeline-id").test()
                .assertFailure(JsonSyntaxException::class.java)
        }
    }

    test("getPipelineConfigById exposes the HTTP status and body for a missing pipeline") {
        val body = """{"message":"Pipeline not found"}"""

        withConfigResponse(MockResponse(code = 404, body = body)) { client, _ ->
            client.getPipelineConfigById("pipeline-id").test()
                .assertNoValues()
                .assertNotComplete()
                .assertError { error ->
                    error is HttpException && error.code() == 404 && error.response()?.errorBody()?.string() == body
                }
        }
    }
})

private fun withConfigResponse(response: MockResponse, test: (CircleCIAPIClientV2, MockWebServer) -> Unit) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(response)
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        test(client, server)
    }
}
