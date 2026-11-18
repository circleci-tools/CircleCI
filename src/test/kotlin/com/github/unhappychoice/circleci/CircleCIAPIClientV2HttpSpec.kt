package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.request.CreateContextRestrictionRequest
import com.github.unhappychoice.circleci.v2.response.JobDetails
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientV2HttpSpec : Spek({
    test("creates a V2 client with the default base URL") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    val projectSlug = "gh/CircleCI-Public/api-preview-docs"
    val jobPath = "/project/gh%2FCircleCI-Public%2Fapi-preview-docs/job/123"
    val jobDetailsJson = """
        {
          "web_url": "https://app.circleci.com/jobs/123",
          "project": {
            "id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
            "slug": "$projectSlug",
            "name": "api-preview-docs",
            "external_url": "https://github.com/CircleCI-Public/api-preview-docs"
          },
          "parallel_runs": [{"index": 0, "status": "success"}],
          "started_at": "2020-01-13T18:51:40Z",
          "latest_workflow": {"id": "8469d90e-f7b9-4642-80da-8aa6244431ac", "name": "build-and-test"},
          "name": "build",
          "executor": {"type": "docker", "resource_class": "medium"},
          "parallelism": 1,
          "status": "success",
          "number": 123,
          "pipeline": {"id": "5034460f-c7c4-4c43-9457-de07e2029e7b"},
          "duration": 42000,
          "created_at": "2020-01-13T18:51:38Z",
          "messages": [{"type": "info", "message": "Job completed", "reason": "success"}],
          "contexts": [{"name": "build-context"}],
          "organization": {"name": "CircleCI-Public"},
          "queued_at": "2020-01-13T18:51:39Z",
          "stopped_at": "2020-01-13T18:52:22Z"
        }
    """.trimIndent()

    test("decodes job details using the CircleCI response schema") {
        withResponse(jobDetailsJson, "GET", jobPath) { client ->
            val details = client.getJobDetails(projectSlug, "123").blockingSingle()
            assertEquals(123L, details.number)
            assertEquals(42000L, details.duration)
            assertEquals(projectSlug, details.project.slug)
            assertEquals("medium", details.executor.resourceClass)
            assertEquals("https://app.circleci.com/jobs/123", details.webUrl)
            assertEquals("https://github.com/CircleCI-Public/api-preview-docs", details.project.externalUrl)
            assertEquals(JobDetails.ParallelRun(0, "success"), details.parallelRuns.single())
            assertEquals("build-and-test", details.latestWorkflow.name)
            assertEquals("2020-01-13T18:51:38Z", details.createdAt)
            assertEquals("2020-01-13T18:51:39Z", details.queuedAt)
            assertEquals("2020-01-13T18:51:40Z", details.startedAt)
            assertEquals("2020-01-13T18:52:22Z", details.stoppedAt)
        }
    }

    test("decodes job details with null and omitted fields") {
        val response = JsonParser.parseString(jobDetailsJson).asJsonObject.apply {
            add("duration", JsonNull.INSTANCE)
            add("status", JsonNull.INSTANCE)
            remove("stopped_at")
            getAsJsonObject("project").remove("id")
            getAsJsonArray("messages")[0].asJsonObject.remove("reason")
        }
        withResponse(response.toString(), "GET", jobPath) { client ->
            val details = client.getJobDetails(projectSlug, "123").blockingSingle()
            assertNull(details.duration)
            assertNull(details.status)
            assertNull(details.stoppedAt)
            assertNull(details.project.id)
            assertNull(details.messages.single().reason)
        }
    }

    test("deletes a context") {
        withResponse("""{"message":"Context deleted"}""", "DELETE", "/context/context-id") { client ->
            assertEquals("Context deleted", client.deleteContext("context-id").blockingSingle().message)
        }
    }

    test("adds a context environment variable") {
        val response = """{"variable":"VAR","context_id":"context-id","created_at":"2025-01-02T03:04:05Z"}"""
        val body = withResponse(response, "PUT", "/context/context-id/environment-variable/VAR") { client ->
            val result = client.addEnvironmentVariableToContext(
                "context-id", "VAR", AddEnvironmentVariableRequest("test-value")
            ).blockingSingle() as Map<*, *>
            assertEquals("VAR", result["variable"])
            assertEquals("context-id", result["context_id"])
            assertEquals("2025-01-02T03:04:05Z", result["created_at"])
        }
        assertEquals("test-value", JsonParser.parseString(body).asJsonObject["value"].asString)
    }

    test("deletes a context environment variable") {
        val path = "/context/context-id/environment-variable/VAR"
        withResponse("""{"message":"Variable deleted"}""", "DELETE", path) { client ->
            val result = client.deleteEnvironmentVariableFromContext("context-id", "VAR").blockingSingle()
            assertEquals("Variable deleted", result.message)
        }
    }

    test("lists context restrictions with an encoded page token") {
        val response = """{"items":[{"id":"restriction-id"}],"next_page_token":"next-page"}"""
        val path = "/context/context-id/restrictions?page-token=page%20%2B%20token"
        withResponse(response, "GET", path) { client ->
            val result = client.getContextRestrictions("context-id", "page + token").blockingSingle()
            assertEquals("next-page", result.nextPageToken)
            assertEquals("restriction-id", (result.items.single() as Map<*, *>)["id"])
        }
    }

    test("creates a context restriction") {
        val response = """{"id":"restriction-id","message":"Restriction created"}"""
        val body = withResponse(response, "POST", "/context/context-id/restrictions") { client ->
            val result = client.createContextRestriction(
                "context-id", CreateContextRestrictionRequest("project-id")
            ).blockingSingle()
            assertEquals("restriction-id", result.id)
            assertEquals("Restriction created", result.message)
        }
        assertEquals("project-id", JsonParser.parseString(body).asJsonObject["restriction"].asString)
    }

    test("deletes a context restriction") {
        val path = "/context/context-id/restrictions/restriction-id"
        withResponse("""{"message":"Restriction deleted"}""", "DELETE", path) { client ->
            val result = client.deleteContextRestriction("context-id", "restriction-id").blockingSingle()
            assertEquals("Restriction deleted", result.message)
        }
    }
})

private data class RecordedRequest(val method: String, val uri: URI, val headers: Headers, val body: String)

private fun withResponse(json: String, method: String, path: String, call: (CircleCIAPIClientV2) -> Unit): String {
    val request = CompletableFuture<RecordedRequest>()
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/") { exchange ->
        exchange.use {
            val body = exchange.requestBody.bufferedReader(Charsets.UTF_8).use { it.readText() }
            request.complete(RecordedRequest(exchange.requestMethod, exchange.requestURI, exchange.requestHeaders, body))
            val response = json.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
    }
    server.start()
    try {
        call(CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/"))
        val recorded = request.get(1, SECONDS)
        assertEquals(method, recorded.method)
        assertEquals(URI.create("/api/v2$path"), recorded.uri)
        assertEquals("test-token", recorded.headers.getFirst("Circle-Token"))
        assertEquals("application/json", recorded.headers.getFirst("Accept"))
        assertEquals("application/json", recorded.headers.getFirst("Content-Type")?.substringBefore(';'))
        return recorded.body
    } finally {
        server.stop(0)
    }
}
