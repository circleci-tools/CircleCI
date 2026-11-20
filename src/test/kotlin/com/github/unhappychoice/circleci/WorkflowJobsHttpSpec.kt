package com.github.unhappychoice.circleci

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek

object WorkflowJobsHttpSpec : Spek({
    test("getWorkflowJobs preserves dependency conditions and accepts jobs without requires") {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setHeader("Content-Type", "application/json").setBody(
                    """
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
            assertEquals("/api/v2/workflow/workflow-id/job", server.takeRequest().path)
        }
    }
})
