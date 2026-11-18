package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.request.CreateContextRestrictionRequest
import com.github.unhappychoice.circleci.v2.request.CreateTriggerRequest
import com.github.unhappychoice.circleci.v2.request.CreateTriggerRequest.EventSource
import com.google.gson.JsonParser
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientV2HttpSpec : Spek({
    test("creates a V2 client with the default base URL") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
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
    group("creates a pipeline trigger") {
        mapOf(
            "github_app" to "all-pushes",
            "github_server" to "all-pushes",
            "github_oauth" to "only-build-prs"
        ).forEach { (provider, preset) ->
            test("sends the repository for $provider") {
                val source = EventSource(provider, repo = EventSource.Repo("repo-id"))
                assertTriggerRequest(
                    CreateTriggerRequest(source, eventPreset = preset),
                    """{"event_source":{"provider":"$provider","repo":{"external_id":"repo-id"}},"event_preset":"$preset"}"""
                )
            }
        }

        test("sends webhook settings") {
            val source = EventSource("webhook", webhook = EventSource.Webhook("monitor"))
            assertTriggerRequest(
                CreateTriggerRequest(source, eventName = "alert", checkoutRef = "main", configRef = "main"),
                """{
                    "event_source":{"provider":"webhook","webhook":{"sender":"monitor"}},
                    "event_name":"alert","checkout_ref":"main","config_ref":"main"
                }"""
            )
        }

        test("sends schedule settings and pipeline parameters") {
            val source = EventSource("schedule", schedule = EventSource.Schedule("0 9 * * 1-5", "system"))
            assertTriggerRequest(
                CreateTriggerRequest(
                    source, eventName = "weekday-build", checkoutRef = "main", configRef = "main",
                    disabled = false, parameters = mapOf("deploy_env" to "staging")
                ),
                """{
                    "event_source":{"provider":"schedule","schedule":{"cron_expression":"0 9 * * 1-5","attribution_actor":"system"}},
                    "event_name":"weekday-build","checkout_ref":"main","config_ref":"main",
                    "disabled":false,"parameters":{"deploy_env":"staging"}
                }"""
            )
        }
    }
})

private fun assertTriggerRequest(request: CreateTriggerRequest, expected: String) {
    val path = "/projects/project-id/pipeline-definitions/definition-id/triggers"
    val body = withResponse("""{"id":"trigger-id"}""", "POST", path) { client ->
        assertEquals("trigger-id", client.createTrigger("project-id", "definition-id", request).blockingSingle().id)
    }
    assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
}

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
