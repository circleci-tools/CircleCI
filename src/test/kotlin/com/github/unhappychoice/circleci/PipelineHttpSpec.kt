package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.TriggerNewPipelineRequest
import com.github.unhappychoice.circleci.v2.response.Pipeline
import com.google.gson.JsonParser
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.util.concurrent.TimeUnit.SECONDS

object PipelineHttpSpec : Spek({
    test("listPipelines forwards the organization, mine filter, and next page token") {
        MockWebServer().use { server ->
            server.start()
            val firstPageJson = """
                {
                  "items": [{
                    "id": "pipeline-id",
                    "errors": [],
                    "project_slug": "gh/example/project",
                    "number": 42,
                    "state": "created",
                    "created_at": "2020-01-01T00:00:00Z",
                    "trigger": {
                      "type": "webhook",
                      "received_at": "2020-01-01T00:00:00Z",
                      "actor": {"login": "example"}
                    }
                  }],
                  "next_page_token": "page + token"
                }
            """.trimIndent()
            server.enqueue(MockResponse(body = firstPageJson))
            server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val firstPage = client.listPipelines(orgSlug = "gh/example", mine = true).blockingSingle()
            assertEquals("/api/v2/pipeline?org-slug=gh%2Fexample&mine=true", server.takeRequest(1, SECONDS)?.target)
            assertEquals("pipeline-id", firstPage.items.single().id)
            assertEquals("page + token", firstPage.nextPageToken)

            val nextPage = client.listPipelines("gh/example", firstPage.nextPageToken, mine = true).blockingSingle()
            val nextTarget = "/api/v2/pipeline?org-slug=gh%2Fexample&page-token=page%20%2B%20token&mine=true"
            assertEquals(nextTarget, server.takeRequest(1, SECONDS)?.target)
            assertTrue(nextPage.items.isEmpty())
            assertNull(nextPage.nextPageToken)
        }
    }

    test("listPipelines exposes the HTTP status and error body when access is forbidden") {
        MockWebServer().use { server ->
            server.start()
            val response = """{"message":"Forbidden"}"""
            server.enqueue(MockResponse(code = 403, body = response))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            client.listPipelines().test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 403 && error.response()?.errorBody()?.string() == response
            }
            assertEquals("/api/v2/pipeline", server.takeRequest(1, SECONDS)?.target)
        }
    }

    test("getPipelineById decodes a null revision on a present VCS object") {
        val vcs = getPipelineVcs("null")

        assertNull(vcs.revision)
        assertTrue(Pipeline.Vcs::revision.returnType.isMarkedNullable)
    }

    test("getPipelineById preserves a VCS revision string") {
        val vcs = getPipelineVcs("\"abc123\"")

        assertEquals("abc123", vcs.revision)
    }

    test("getPipelineById preserves numeric trigger parameters when triggering another pipeline") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(pipelineResponse(extraFields = """"trigger_parameters":{"replicas":2,"marker":9007199254740993},"""))
            server.enqueue(MockResponse(code = 201, body = """{"number":43,"state":"pending","id":"new-pipeline-id","created_at":"2020-01-01T00:00:00Z"}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val pipeline = client.getPipelineById("pipeline-id").blockingSingle()
            val parameters = requireNotNull(pipeline.triggerParameters)
            assertEquals("2", (parameters["replicas"] as Number).toString())
            assertEquals("9007199254740993", (parameters["marker"] as Number).toString())
            assertEquals("/api/v2/pipeline/pipeline-id", server.takeRequest(1, SECONDS)?.target)

            client.triggerNewPipeline(pipeline.projectSlug, TriggerNewPipelineRequest(parameters = parameters)).blockingSingle()

            val request = requireNotNull(server.takeRequest(1, SECONDS))
            assertEquals("POST", request.method)
            val sent = JsonParser.parseString(requireNotNull(request.body).utf8()).asJsonObject.getAsJsonObject("parameters")
            assertEquals("2", sent["replicas"].toString())
            assertEquals("9007199254740993", sent["marker"].toString())
        }
    }

    mapOf("omitted" to "", "explicitly null" to """"trigger_parameters":null,""").forEach { (description, field) ->
        test("getPipelineById decodes $description trigger parameters as null") {
            val pipeline = getPipeline(extraFields = field) { it.getPipelineById("pipeline-id").blockingSingle() }
            assertNull(pipeline.triggerParameters)
        }
    }

    val lookups: Map<String, (CircleCIAPIClientV2) -> Pipeline> = mapOf(
        "getPipelineById" to { it.getPipelineById("pipeline-id").blockingSingle() },
        "getPipelineByNumber" to { it.getPipelineByNumber("gh/unhappychoice/circleci", 42).blockingSingle() }
    )
    lookups.forEach { (name, lookup) ->
        test("$name preserves warnings with and without a URL") {
            val pipeline = getPipeline(extraFields = warningsField, lookup = lookup)

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
    extraFields: String = "",
    lookup: (CircleCIAPIClientV2) -> Pipeline
): Pipeline =
    MockWebServer().use { server ->
        server.start()
        server.enqueue(pipelineResponse(revisionJson, extraFields))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        lookup(client)
    }

private fun pipelineResponse(revisionJson: String = "null", extraFields: String = ""): MockResponse =
    MockResponse(
        headers = headersOf("Content-Type", "application/json"),
        body = """
        {
          "id": "pipeline-id",
          "errors": [],
          $extraFields
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
