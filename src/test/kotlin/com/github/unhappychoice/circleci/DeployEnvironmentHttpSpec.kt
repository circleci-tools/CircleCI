package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.DeployEnvironment
import com.github.unhappychoice.circleci.v2.response.DeployEnvironmentLabel
import com.github.unhappychoice.circleci.v2.response.DeployEnvironmentListResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.spekframework.spek2.Spek

object DeployEnvironmentHttpSpec : Spek({
    val environment = DeployEnvironment(
        id = "123e4127-e89b-12d3-a456-426123417400",
        name = "production",
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-02T00:00:00Z",
        description = "Production deployment targets",
        labels = listOf(DeployEnvironmentLabel("env", "prod"), DeployEnvironmentLabel("region", "us-east-1"))
    )
    val environmentJson = """
        {
          "id": "123e4127-e89b-12d3-a456-426123417400",
          "name": "production",
          "created_at": "2026-01-01T00:00:00Z",
          "updated_at": "2026-01-02T00:00:00Z",
          "description": "Production deployment targets",
          "labels": [{"key": "env", "value": "prod"}, {"key": "region", "value": "us-east-1"}]
        }
    """.trimIndent()

    test("listEnvironments decodes descriptions and labels without an organization ID") {
        withEnvironmentResponse("""{"items":[$environmentJson],"next_page_token":"next"}""") { client ->
            client.listEnvironments("b9291e0d-a11e-41fb-8517-c545388b5953", 25).test()
                .assertResult(DeployEnvironmentListResponse(listOf(environment), "next"))
        }
    }

    test("getEnvironment decodes the description and labels without an organization ID") {
        withEnvironmentResponse(environmentJson) { client ->
            client.getEnvironment(environment.id).test().assertResult(environment)
        }
    }
})

private fun withEnvironmentResponse(body: String, verify: (CircleCIAPIClientV2) -> Unit) {
    MockWebServer().use { server ->
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        verify(client)
    }
}
