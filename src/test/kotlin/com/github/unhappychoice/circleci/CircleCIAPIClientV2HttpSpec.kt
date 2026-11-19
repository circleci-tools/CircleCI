package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.request.CreateContextRestrictionRequest
import com.github.unhappychoice.circleci.v2.request.CreatePipelineDefinitionRequest
import com.github.unhappychoice.circleci.v2.request.CreateUsageExportRequest
import com.github.unhappychoice.circleci.v2.request.PatchClaimsRequest
import com.github.unhappychoice.circleci.v2.request.PipelineDefinitionRepository
import com.github.unhappychoice.circleci.v2.request.RollbackProjectRequest
import com.github.unhappychoice.circleci.v2.request.UpdatePipelineDefinitionRequest
import com.github.unhappychoice.circleci.v2.response.FlakyTests.FlakyTest
import com.github.unhappychoice.circleci.v2.response.User
import com.google.gson.JsonParser
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientV2HttpSpec : Spek({
    test("creates a V2 client with the default base URL") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    test("decodes Insights flaky-test counts and nullable metadata") {
        val firstTest = """
            {
                "workflow-created-at": "2024-01-01T00:00:00Z",
                "workflow-id": "123e4567-e89b-12d3-a456-426614174000",
                "classname": "ExampleTest",
                "pipeline-number": 7,
                "workflow-name": "build-and-test",
                "test-name": "testWithSource",
                "job-name": "test",
                "job-number": 9,
                "times-flaked": 2147483648,
                "source": "junit",
                "file": "src/test/ExampleTest.kt"
            }
        """.trimIndent()
        val secondTest = firstTest.replace("\"testWithSource\"", "\"testWithoutSource\"")
            .replace("\"junit\"", "null").replace("\"src/test/ExampleTest.kt\"", "null")
            .replace("2147483648", "3")
        val response = """{"total-flaky-tests":2.0,"flaky-tests":[$firstTest,$secondTest]}"""
        withResponse(response, "GET", "/insights/gh%2Forg%2Frepo/flaky-tests") { client ->
            val result = client.getFlakyTests("gh/org/repo").blockingSingle()
            assertEquals(2.0, result.`total-flaky-tests`, 0.0)
            assertEquals(2, result.`flaky-tests`.size)
            val (withSource, withoutSource) = result.`flaky-tests`
            assertEquals(2147483648L, withSource.`times-flaked`)
            assertEquals("junit", withSource.source)
            assertEquals("src/test/ExampleTest.kt", withSource.file)
            assertNull(withSource.`pipeline-id`)
            assertNull(withSource.`test-result`)
            assertTrue(FlakyTest::`pipeline-id`.returnType.isMarkedNullable)
            assertTrue(FlakyTest::`test-result`.returnType.isMarkedNullable)
            assertEquals(3L, withoutSource.`times-flaked`)
            assertNull(withoutSource.source)
            assertNull(withoutSource.file)
        }
    }

    val rollbackRequest = RollbackProjectRequest("production", "frontend", "1.1.0", "1.0.0")
    val rollbackResponse = """{"id":"123e4567-e89b-12d3-a456-426614174000","rollback_type":"PIPELINE"}"""
    val rollbackPath = "/projects/project-id/rollback"
    val rollbackBody = """
        {
            "environment_name": "production",
            "component_name": "frontend",
            "current_version": "1.1.0",
            "target_version": "1.0.0"
        }
    """.trimIndent()

    test("rolls back a project with a reason") {
        val body = withResponse(rollbackResponse, "POST", rollbackPath, 202) { client ->
            client.rollbackProject("project-id", rollbackRequest.copy(reason = "Restore the previous release"))
                .blockingSingle()
        }
        val expected = JsonParser.parseString(rollbackBody).asJsonObject
        expected.addProperty("reason", "Restore the previous release")
        assertEquals(expected, JsonParser.parseString(body))
    }

    test("rolls back a project without a reason") {
        val body = withResponse(rollbackResponse, "POST", rollbackPath, 202) { client ->
            client.rollbackProject("project-id", rollbackRequest).blockingSingle()
        }
        assertEquals(JsonParser.parseString(rollbackBody), JsonParser.parseString(body))
    }

    test("decodes the authenticated user") {
        val response = """{"id":"user-id","login":"test-login","name":"Test User"}"""
        withResponse(response, "GET", "/me") { client ->
            client.getMe().test().assertResult(User("user-id", "test-login", "Test User"))
        }
    }

    test("returns the HTTP status and error body when authentication fails") {
        val response = """{"message":"Unauthorized"}"""
        withResponse(response, "GET", "/me", status = 401) { client ->
            client.getMe().test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 401 &&
                    error.response()?.errorBody()?.string() == response
            }
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

    test("creates a pipeline definition with both sources") {
        val request = CreatePipelineDefinitionRequest(
            "pipeline",
            CreatePipelineDefinitionRequest.ConfigSource(
                "github_app", ".circleci/pipeline.yml", PipelineDefinitionRepository("config-repo-id")
            ),
            CreatePipelineDefinitionRequest.CheckoutSource("github_app", PipelineDefinitionRepository("checkout-repo-id"))
        )
        val body = withResponse("""{"id":"definition-id"}""", "POST", "/projects/project-id/pipeline-definitions") { client ->
            client.createPipelineDefinition("project-id", request).blockingSingle()
        }
        val expected = """
            {
                "name":"pipeline",
                "config_source":{"provider":"github_app","file_path":".circleci/pipeline.yml","repo":{"external_id":"config-repo-id"}},
                "checkout_source":{"provider":"github_app","repo":{"external_id":"checkout-repo-id"}}
            }
        """
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("updates a pipeline definition checkout source independently") {
        val request = UpdatePipelineDefinitionRequest(
            checkoutSource = UpdatePipelineDefinitionRequest.CheckoutSource(repo = PipelineDefinitionRepository("new-repo-id"))
        )
        val path = "/projects/project-id/pipeline-definitions/definition-id"
        val body = withResponse("""{"id":"definition-id"}""", "PATCH", path) { client ->
            client.updatePipelineDefinition("project-id", "definition-id", request).blockingSingle()
        }
        val expected = """{"checkout_source":{"repo":{"external_id":"new-repo-id"}}}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("deletes a context restriction") {
        val path = "/context/context-id/restrictions/restriction-id"
        withResponse("""{"message":"Restriction deleted"}""", "DELETE", path) { client ->
            val result = client.deleteContextRestriction("context-id", "restriction-id").blockingSingle()
            assertEquals("Restriction deleted", result.message)
        }
    }

    test("serializes usage export dates as start and end") {
        val start = "2025-01-01T00:00:00Z"
        val end = "2025-02-01T00:00:00Z"
        val response = """{"usage_export_job_id":"e8235eed-f121-4ae3-9c72-2719d6572818"}"""
        val body = withResponse(response, "POST", "/organizations/org-id/usage_export_job") { client ->
            client.createUsageExport("org-id", CreateUsageExportRequest(start, end)).blockingSingle()
        }
        val json = JsonParser.parseString(body).asJsonObject
        assertEquals(start, json.get("start")?.asString)
        assertEquals(end, json.get("end")?.asString)
        assertFalse(json.has("start_date"))
        assertFalse(json.has("end_date"))
    }

    test("patches organization OIDC claims with a top-level audience array and TTL") {
        val response = """{"org_id":"org-id"}"""
        val path = "/org/org-id/oidc-custom-claims"
        val body = withResponse(response, "PATCH", path) { client ->
            val request = PatchClaimsRequest(audience = listOf("service-a", "service-b"), ttl = "1h")
            client.patchOrgClaims("org-id", request).blockingSingle()
        }
        val expected = """{"audience":["service-a","service-b"],"ttl":"1h"}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("patches only the selected project OIDC claim") {
        val response = """{"org_id":"org-id","project_id":"project-id"}"""
        val path = "/org/org-id/project/project-id/oidc-custom-claims"
        val body = withResponse(response, "PATCH", path) { client ->
            val request = PatchClaimsRequest(audience = listOf("service-a"))
            client.patchProjectClaims("org-id", "project-id", request).blockingSingle()
        }
        assertEquals(JsonParser.parseString("""{"audience":["service-a"]}"""), JsonParser.parseString(body))
    }

    test("deletes selected organization OIDC claims in a single query parameter") {
        val path = "/org/org-id/oidc-custom-claims?claims=audience%2Cttl"
        withResponse("""{"org_id":"org-id"}""", "DELETE", path) { client ->
            client.deleteOrgClaims("org-id", "audience,ttl").blockingSingle()
        }
    }

    test("deletes selected project OIDC claims in a single query parameter") {
        val path = "/org/org-id/project/project-id/oidc-custom-claims?claims=audience%2Cttl"
        withResponse("""{"org_id":"org-id","project_id":"project-id"}""", "DELETE", path) { client ->
            client.deleteProjectClaims("org-id", "project-id", "audience,ttl").blockingSingle()
        }
    }
})

private data class RecordedRequest(val method: String, val uri: URI, val headers: Headers, val body: String)

private fun withResponse(
    json: String, method: String, path: String, status: Int = 200, call: (CircleCIAPIClientV2) -> Unit
): String {
    val request = CompletableFuture<RecordedRequest>()
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/") { exchange ->
        exchange.use {
            val body = exchange.requestBody.bufferedReader(Charsets.UTF_8).use { it.readText() }
            request.complete(RecordedRequest(exchange.requestMethod, exchange.requestURI, exchange.requestHeaders, body))
            val response = json.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, response.size.toLong())
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
