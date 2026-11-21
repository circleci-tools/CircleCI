package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Pipeline
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek

object PipelineHttpSpec : Spek({
    test("getPipelineById decodes a null revision on a present VCS object") {
        val vcs = getPipelineVcs("null")

        assertNull(vcs.revision)
        assertTrue(Pipeline.Vcs::revision.returnType.isMarkedNullable)
    }

    test("getPipelineById preserves a VCS revision string") {
        val vcs = getPipelineVcs("\"abc123\"")

        assertEquals("abc123", vcs.revision)
    }
})

private fun getPipelineVcs(revisionJson: String): Pipeline.Vcs =
    MockWebServer().use { server ->
        server.start()
        server.enqueue(
            MockResponse(
                headers = headersOf("Content-Type", "application/json"),
                body = """
                {
                  "id": "pipeline-id",
                  "errors": [],
                  "project_slug": "gh/unhappychoice/circleci",
                  "number": 42,
                  "state": "created",
                  "created_at": "2020-01-01T00:00:00Z",
                  "trigger": {
                    "type": "webhook",
                    "received_at": "2020-01-01T00:00:00Z",
                    "actor": {"login": "example"}
                  },
                  "vcs": {
                    "provider_name": "GitHub",
                    "target_repository_url": "https://github.com/unhappychoice/circleci",
                    "origin_repository_url": "https://github.com/unhappychoice/circleci",
                    "revision": $revisionJson
                  }
                }
                """.trimIndent()
            )
        )
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        requireNotNull(client.getPipelineById("pipeline-id").blockingSingle().vcs)
    }
