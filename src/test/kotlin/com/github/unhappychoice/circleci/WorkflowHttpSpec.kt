package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek

object WorkflowHttpSpec : Spek({
    test("getWorkflowById decodes workflows with and without automatic rerun metadata") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            server.enqueue(MockResponse(body = workflowJson("rerun", includeRerunMetadata = true)))
            server.enqueue(MockResponse(body = workflowJson("original")))

            val rerun = client.getWorkflowById("rerun").blockingSingle()
            assertEquals(2147483648L, rerun.pipelineNumber)
            assertEquals(1L, rerun.autoRerunNumber)
            assertEquals(5L, rerun.maxAutoReruns)
            assertEquals("/api/v2/workflow/rerun", server.takeRequest().target)

            val original = client.getWorkflowById("original").blockingSingle()
            assertEquals("original", original.id)
            assertNull(original.autoRerunNumber)
            assertNull(original.maxAutoReruns)
            assertEquals("/api/v2/workflow/original", server.takeRequest().target)
        }
    }

    test("getPipelineWorkflows decodes workflows with and without automatic rerun metadata") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            val rerunJson = workflowJson("rerun", includeRerunMetadata = true)
            val originalJson = workflowJson("original")
            server.enqueue(MockResponse(body = """{"items":[$rerunJson,$originalJson],"next_page_token":null}"""))

            val workflows = client.getPipelineWorkflows("pipeline-id", null).blockingSingle().items
            assertEquals(listOf("rerun", "original"), workflows.map { it.id })
            assertEquals(1L, workflows[0].autoRerunNumber)
            assertEquals(5L, workflows[0].maxAutoReruns)
            assertNull(workflows[1].autoRerunNumber)
            assertNull(workflows[1].maxAutoReruns)
            assertEquals("/api/v2/pipeline/pipeline-id/workflow", server.takeRequest().target)
        }
    }
})

private fun workflowJson(id: String, includeRerunMetadata: Boolean = false): String {
    val rerunMetadata = if (includeRerunMetadata) """"auto_rerun_number": 1, "max_auto_reruns": 5,""" else ""
    return """
        {
          $rerunMetadata
          "id": "$id",
          "name": "build",
          "project_slug": "gh/example/project",
          "status": "success",
          "created_at": "2026-01-01T00:00:00Z",
          "stopped_at": "2026-01-01T00:01:00Z",
          "pipeline_id": "pipeline-id",
          "pipeline_number": 2147483648,
          "started_by": "user-id"
        }
    """.trimIndent()
}
