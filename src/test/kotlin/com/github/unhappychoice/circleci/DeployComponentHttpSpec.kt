package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Component
import com.github.unhappychoice.circleci.v2.response.ComponentLabel
import com.github.unhappychoice.circleci.v2.response.ComponentListResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.spekframework.spek2.Spek

object DeployComponentHttpSpec : Spek({
    val component = Component(
        id = "123e4127-e89b-12d3-a456-426123417400",
        name = "service-a",
        projectId = "39723015-b399-4601-9ff6-bd1bfbed8fa8",
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-02T00:00:00Z"
    )
    val componentFields = """
        "id": "${component.id}",
        "name": "${component.name}",
        "project_id": "${component.projectId}",
        "created_at": "${component.createdAt}",
        "updated_at": "${component.updatedAt}"
    """.trimIndent()
    val componentWithMetadata = component.copy(
        labels = listOf(ComponentLabel("env", "prod"), ComponentLabel("region", "us-east-1")),
        releaseCount = 0
    )
    val componentWithMetadataJson = """
        {
          $componentFields,
          "labels": [{"key": "env", "value": "prod"}, {"key": "region", "value": "us-east-1"}],
          "release_count": 0
        }
    """.trimIndent()

    test("listComponents decodes labels, a zero release count, and omitted fields") {
        val body = """{"items":[$componentWithMetadataJson,{$componentFields}],"next_page_token":"next"}"""
        withComponentResponse(body) { client ->
            client.listComponents("b9291e0d-a11e-41fb-8517-c545388b5953", 25).test()
                .assertResult(ComponentListResponse(listOf(componentWithMetadata, component), "next"))
        }
    }

    test("getComponent decodes labels and a zero release count") {
        withComponentResponse(componentWithMetadataJson) { client ->
            client.getComponent(component.id).test().assertResult(componentWithMetadata)
        }
    }

    test("getComponent decodes responses with omitted labels and release count") {
        withComponentResponse("{$componentFields}") { client ->
            client.getComponent(component.id).test().assertResult(component)
        }
    }
})

private fun withComponentResponse(body: String, verify: (CircleCIAPIClientV2) -> Unit) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(MockResponse(headers = headersOf("Content-Type", "application/json"), body = body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        verify(client)
    }
}
