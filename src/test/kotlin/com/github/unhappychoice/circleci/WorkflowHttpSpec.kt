package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek
import retrofit2.HttpException

object WorkflowHttpSpec : Spek({
    test("getWorkflowById decodes workflows with and without automatic rerun metadata") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            server.enqueue(MockResponse(body = workflowJson("rerun", includeRerunMetadata = true)))
            server.enqueue(MockResponse(body = workflowJson("original")))

            val rerun = client.getWorkflowById("rerun").blockingSingle()
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

    test("cancelWorkflow posts to the workflow cancel endpoint and decodes the response") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            server.enqueue(MockResponse(body = """{"message":"Workflow canceled."}"""))

            assertEquals("Workflow canceled.", client.cancelWorkflow("workflow-id").blockingSingle().message)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v2/workflow/workflow-id/cancel", request.target)
        }
    }

    test("approveJob posts to the approval request endpoint and decodes the response") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            server.enqueue(MockResponse(body = """{"message":"Job approved."}"""))

            val response = client.approveJob("workflow-id", "approval-request-id").blockingSingle()
            assertEquals("Job approved.", response.message)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v2/workflow/workflow-id/approve/approval-request-id", request.target)
        }
    }

    test("approveJob propagates the status and body when CircleCI rejects approval") {
        MockWebServer().use { server ->
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
            val body = """{"message":"Approval request not found."}"""
            server.enqueue(MockResponse(code = 404, body = body))

            client.approveJob("workflow-id", "missing-approval-request").test()
                .assertNoValues().assertNotComplete().assertError { error ->
                    error is HttpException && error.code() == 404 && error.response()?.errorBody()?.string() == body
                }
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
          "pipeline_number": 42,
          "started_by": "user-id"
        }
    """.trimIndent()
}
