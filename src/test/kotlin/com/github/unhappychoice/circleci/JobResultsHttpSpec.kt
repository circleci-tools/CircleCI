package com.github.unhappychoice.circleci

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek
import java.util.concurrent.TimeUnit.SECONDS

object JobResultsHttpSpec : Spek({
    val projectSlug = "gh/example/project"
    val jobNumber = "123"
    val jobPath = "/api/v2/project/gh%2Fexample%2Fproject/123"
    val token = "page + token"
    val encodedQuery = "page-token=page%20%2B%20token"

    test("fetches the next job artifacts page using the response token") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(
                """{"items":[{"path":"first.txt","node_index":0,"url":"https://example.com/first.txt"}],"next_page_token":"$token"}"""
            ))
            server.enqueue(MockResponse().setBody(
                """{"items":[{"path":"second.txt","node_index":1,"url":"https://example.com/second.txt"}],"next_page_token":null}"""
            ))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            val path = "$jobPath/artifacts"

            val firstPage = client.getJobArtifacts(projectSlug, jobNumber).blockingSingle()
            assertEquals(path, server.takeRequest(1, SECONDS)?.path)
            assertEquals("first.txt", firstPage.items.single().path)
            assertEquals(token, firstPage.nextPageToken)

            val secondPage = client.getJobArtifacts(projectSlug, jobNumber, firstPage.nextPageToken).blockingSingle()
            assertEquals("$path?$encodedQuery", server.takeRequest(1, SECONDS)?.path)
            assertEquals("second.txt", secondPage.items.single().path)
            assertNull(secondPage.nextPageToken)
        }
    }

    test("fetches the next job tests page using the response token") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(testPage("firstTest", "\"$token\"")))
            server.enqueue(MockResponse().setBody(testPage("secondTest", "null")))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            val path = "$jobPath/tests"

            val firstPage = client.getTests(projectSlug, jobNumber).blockingSingle()
            assertEquals(path, server.takeRequest(1, SECONDS)?.path)
            assertEquals("firstTest", firstPage.items.single().name)
            assertEquals(token, firstPage.nextPageToken)

            val secondPage = client.getTests(projectSlug, jobNumber, firstPage.nextPageToken).blockingSingle()
            assertEquals("$path?$encodedQuery", server.takeRequest(1, SECONDS)?.path)
            assertEquals("secondTest", secondPage.items.single().name)
            assertNull(secondPage.nextPageToken)
        }
    }
})

private fun testPage(name: String, tokenJson: String) = """
    {
      "items": [{
        "name": "$name", "classname": "ExampleTest", "file": "ExampleTest.kt",
        "source": "junit", "run_time": 0.1, "result": "success", "message": null
      }],
      "next_page_token": $tokenJson
    }
""".trimIndent()
