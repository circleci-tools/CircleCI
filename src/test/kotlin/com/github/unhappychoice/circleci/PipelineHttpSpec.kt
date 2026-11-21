package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Pipeline
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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

    val lookups: Map<String, (CircleCIAPIClientV2) -> Pipeline> = mapOf(
        "getPipelineById" to { it.getPipelineById("pipeline-id").blockingSingle() },
        "getPipelineByNumber" to { it.getPipelineByNumber("gh/unhappychoice/circleci", 42).blockingSingle() }
    )
    lookups.forEach { (name, lookup) ->
        test("$name preserves warnings with and without a URL") {
            val pipeline = getPipeline(warningsField = warningsField, lookup = lookup)

            assertEquals(emptyList<Pipeline.PipelineError>(), pipeline.errors)
            assertEquals(
                listOf(
                    Pipeline.PipelineWarning("config", "Check the config", "Config warning", "https://example.com/config"),
                    Pipeline.PipelineWarning("vcs", "Check the branch", "Branch warning")
                ),
                pipeline.warnings
            )
        }

        test("$name decodes a pipeline without a warnings field") {
            val pipeline = getPipeline(lookup = lookup)

            assertEquals("pipeline-id", pipeline.id)
            assertNull(pipeline.warnings)
        }
    }
})

private fun getPipelineVcs(revisionJson: String): Pipeline.Vcs =
    requireNotNull(getPipeline(revisionJson) { it.getPipelineById("pipeline-id").blockingSingle() }.vcs)

private fun getPipeline(
    revisionJson: String = "null",
    warningsField: String = "",
    lookup: (CircleCIAPIClientV2) -> Pipeline
): Pipeline =
    MockWebServer().use { server ->
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(
                """
                {
                  "id": "pipeline-id",
                  "errors": [],
                  $warningsField
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
        lookup(client)
    }

private val warningsField = """
    "warnings": [
      {
        "type": "config",
        "message": "Check the config",
        "description": "Config warning",
        "url": "https://example.com/config"
      },
      {
        "type": "vcs",
        "message": "Check the branch",
        "description": "Branch warning"
      }
    ],
""".trimIndent()
