package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateCheckoutKeyRequest
import com.github.unhappychoice.circleci.v2.request.CreateEnvVarRequest
import com.google.gson.JsonParser
import io.reactivex.rxjava3.core.Observable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek

object ProjectCreationRequestsHttpSpec : Spek({
    val projectSlug = "gh/unhappychoice/circleci"

    fun assertPost(
        endpoint: String,
        responseJson: String,
        expectedBody: String,
        create: (CircleCIAPIClientV2) -> Observable<*>
    ) {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(201).setBody(responseJson))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            create(client).test().assertComplete().assertNoErrors().assertValueCount(1)

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals(listOf("api", "v2", "project", projectSlug, endpoint), request.requestUrl?.pathSegments)
            assertEquals(JsonParser.parseString(expectedBody), JsonParser.parseString(request.body.readUtf8()))
        }
    }

    test("sends the checkout key type") {
        assertPost(
            endpoint = "checkout-key",
            responseJson = """
                {"public-key":"example-key","type":"deploy-key","fingerprint":"aa:bb",
                 "preferred":false,"created-at":"2026-01-01T00:00:00Z"}
            """.trimIndent(),
            expectedBody = """{"type":"deploy-key"}"""
        ) { it.createProjectCheckoutKey(projectSlug, CreateCheckoutKeyRequest(type = "deploy-key")) }
    }

    test("sends the project environment variable name and value") {
        assertPost(
            endpoint = "envvar",
            responseJson = """{"name":"EXAMPLE","value":"xxxx1234"}""",
            expectedBody = """{"name":"EXAMPLE","value":"dummy-1234"}"""
        ) { it.createProjectEnvironmentVariable(projectSlug, CreateEnvVarRequest(name = "EXAMPLE", value = "dummy-1234")) }
    }
})
