package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek

object InsightsWorkflowRunsHttpSpec : Spek({
    test("preserves nullable workflow run fields, zero duration, and approval flags") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(body = """
                {
                  "items": [
                    {
                      "id": "1",
                      "branch": "main",
                      "duration": null,
                      "created_at": "2024-01-01T00:00:00Z",
                      "stopped_at": null,
                      "credits_used": 0,
                      "status": null,
                      "is_approval": true
                    },
                    {
                      "id": "2",
                      "branch": "main",
                      "duration": 0,
                      "created_at": "2024-01-01T00:00:00Z",
                      "stopped_at": "2024-01-01T00:00:00Z",
                      "credits_used": 0,
                      "status": "success",
                      "is_approval": false
                    },
                    {
                      "id": "3",
                      "branch": "main",
                      "duration": 10,
                      "created_at": "2024-01-01T00:00:00Z",
                      "stopped_at": "2024-01-01T00:00:10Z",
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
            val (runWithNullFields, runWithZeroDuration, runWithoutApprovalFlag) = response.items

            assertAll(
                { assertNull(runWithNullFields.duration) },
                { assertNull(runWithNullFields.status) },
                { assertEquals(0L, runWithZeroDuration.duration) },
                { assertEquals("success", runWithZeroDuration.status) },
                { assertEquals(true, runWithNullFields.isApproval) },
                { assertEquals(false, runWithZeroDuration.isApproval) },
                { assertNull(runWithoutApprovalFlag.isApproval) }
            )
        }
    }
})
