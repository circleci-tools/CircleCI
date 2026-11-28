package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.ProjectEnvironmentVariable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek
import java.util.concurrent.TimeUnit.SECONDS

object ProjectEnvironmentVariablesHttpSpec : Spek({
    test("redacts the value in a single project environment variable response") {
        MockWebServer().use { server ->
            server.start()
            val value = "dummy-unmasked-secret"
            server.enqueue(MockResponse(body = """{"name":"FIRST","value":"$value"}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val variable = client.getProjectEnvironmentVariable("gh/example/project", "FIRST").blockingSingle()

            assertEquals(value, variable.value)
            assertEquals("ProjectEnvironmentVariable(name=FIRST, value=[REDACTED], createdAt=null)", variable.toString())
        }
    }

    test("decodes optional creation timestamps across project environment variable pages") {
        MockWebServer().use { server ->
            server.start()
            val projectSlug = "gh/example/project"
            val path = "/api/v2/project/gh%2Fexample%2Fproject/envvar"
            val createdAt = "2015-09-21T17:29:21.042Z"
            server.enqueue(
                MockResponse(body = """{"items":[{"name":"FIRST","value":"xxxx1234","created-at":"$createdAt"}],"next_page_token":"page + token"}""")
            )
            server.enqueue(
                MockResponse(body = """{"items":[{"name":"SECOND","value":"xxxx5678"}],"next_page_token":null}""")
            )
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val firstPage = client.getProjectEnvironmentVariables(projectSlug).blockingSingle()
            assertEquals(path, server.takeRequest(1, SECONDS)?.target)
            assertEquals(listOf(ProjectEnvironmentVariable("FIRST", "xxxx1234", createdAt)), firstPage.items)
            assertEquals("xxxx1234", firstPage.items.single().value)
            assertFalse(firstPage.toString().contains("1234"))
            assertEquals("page + token", firstPage.nextPageToken)

            val secondPage = client.getProjectEnvironmentVariables(projectSlug, firstPage.nextPageToken).blockingSingle()
            assertEquals("$path?page-token=page%20%2B%20token", server.takeRequest(1, SECONDS)?.target)
            assertEquals(listOf(ProjectEnvironmentVariable("SECOND", "xxxx5678")), secondPage.items)
            assertNull(secondPage.items.single().createdAt)
            assertNull(secondPage.nextPageToken)
        }
    }
})
