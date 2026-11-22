package com.github.unhappychoice.circleci

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek

object InsightsWorkflowRunsHttpSpec : Spek({
    test("preserves null duration and status and explicit zero duration") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""
                {
                  "items": [
                    {
                      "id": "1",
                      "branch": "main",
                      "duration": null,
                      "created_at": "2024-01-01T00:00:00Z",
                      "stopped_at": null,
                      "credits_used": 0,
                      "status": null
                    },
                    {
                      "id": "2",
                      "branch": "main",
                      "duration": 0,
                      "created_at": "2024-01-01T00:00:00Z",
                      "stopped_at": "2024-01-01T00:00:00Z",
                      "credits_used": 0,
                      "status": "success"
                    }
                  ],
                  "next_page_token": null
                }
            """.trimIndent()))
            val response = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
                .getProjectWorkflowRuns("gh/example/project", "ci")
                .blockingSingle()
            val (runWithNullFields, runWithZeroDuration) = response.items

            assertAll(
                { assertNull(runWithNullFields.duration) },
                { assertNull(runWithNullFields.status) },
                { assertEquals(0L, runWithZeroDuration.duration) },
                { assertEquals("success", runWithZeroDuration.status) }
            )
        }
    }
})
