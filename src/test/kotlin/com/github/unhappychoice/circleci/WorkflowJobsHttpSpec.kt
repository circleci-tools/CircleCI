package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.util.concurrent.TimeUnit.SECONDS

object WorkflowJobsHttpSpec : Spek({
    test("getWorkflowJobs preserves dependency conditions and accepts jobs without requires") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(
                MockResponse(
                    headers = headersOf("Content-Type", "application/json"),
                    body = """
                    {
                      "items": [
                        {
                          "id": "deploy", "name": "deploy", "type": "build", "status": "blocked",
                          "started_at": "2026-01-01T00:00:00Z",
                          "dependencies": ["build"],
                          "requires": {"build": ["success"]}
                        },
                        {
                          "id": "cleanup", "name": "cleanup", "type": "build", "status": "blocked",
                          "started_at": "2026-01-01T00:00:00Z",
                          "dependencies": ["build"],
                          "requires": {"build": ["failed", "canceled"]}
                        },
                        {
                          "id": "legacy", "name": "legacy", "type": "build", "status": "blocked",
                          "started_at": "2026-01-01T00:00:00Z",
                          "dependencies": ["build"]
                        }
                      ],
                      "next_page_token": null
                    }
                    """.trimIndent()
                )
            )
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            val jobs = client.getWorkflowJobs("workflow-id").blockingSingle().items

            assertEquals(3, jobs.size)
            assertEquals(List(3) { listOf("build") }, jobs.map { it.dependencies })
            assertEquals(mapOf("build" to listOf("success")), jobs[0].requires)
            assertEquals(mapOf("build" to listOf("failed", "canceled")), jobs[1].requires)
            assertNull(jobs[2].requires)
            assertEquals("/api/v2/workflow/workflow-id/job", server.takeRequest().target)
        }
    }

    test("fetches the next workflow job page using the returned token") {
        MockWebServer().use { server ->
            server.start()
            val path = "/api/v2/workflow/workflow-id/job"
            server.enqueue(MockResponse(body = """{"items":[{"id":"build"}],"next_page_token":"page + token"}"""))
            server.enqueue(MockResponse(body = """{"items":[{"id":"test"}],"next_page_token":null}"""))
            server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val firstPage = client.getWorkflowJobs("workflow-id").blockingSingle()
            assertEquals(path, server.takeRequest(1, SECONDS)?.target)
            assertEquals("build", firstPage.items.single().id)
            assertEquals("page + token", firstPage.nextPageToken)

            val secondPage = client.getWorkflowJobs("workflow-id", firstPage.nextPageToken).blockingSingle()
            assertEquals("$path?page-token=page%20%2B%20token", server.takeRequest(1, SECONDS)?.target)
            assertEquals("test", secondPage.items.single().id)
            assertNull(secondPage.nextPageToken)

            assertTrue(client.getWorkflowJobs("workflow-id", null).blockingSingle().items.isEmpty())
            assertEquals(path, server.takeRequest(1, SECONDS)?.target)
        }
    }
})
