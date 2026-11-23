package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.request.ContinuePipelineRequest
import com.github.unhappychoice.circleci.v2.request.CreateContextRestrictionRequest
import com.github.unhappychoice.circleci.v2.request.CreatePipelineDefinitionRequest
import com.github.unhappychoice.circleci.v2.request.CreateScheduleParameters
import com.github.unhappychoice.circleci.v2.request.CreateScheduleTimetable
import com.github.unhappychoice.circleci.v2.request.CreateTriggerRequest
import com.github.unhappychoice.circleci.v2.request.CreateTriggerRequest.EventSource
import com.github.unhappychoice.circleci.v2.request.CreateURLOrbAllowListEntryRequest
import com.github.unhappychoice.circleci.v2.request.CreateUsageExportRequest
import com.github.unhappychoice.circleci.v2.request.CreateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.PatchClaimsRequest
import com.github.unhappychoice.circleci.v2.request.PipelineDefinitionRepository
import com.github.unhappychoice.circleci.v2.request.RollbackProjectRequest
import com.github.unhappychoice.circleci.v2.request.ScheduleAttributionActor
import com.github.unhappychoice.circleci.v2.request.UpdatePipelineDefinitionRequest
import com.github.unhappychoice.circleci.v2.request.UpdateTriggerRequest
import com.github.unhappychoice.circleci.v2.request.UpdateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.WebhookScopeRequest
import com.github.unhappychoice.circleci.v2.response.Collaboration
import com.github.unhappychoice.circleci.v2.response.ComponentVersion
import com.github.unhappychoice.circleci.v2.response.ComponentVersionListResponse
import com.github.unhappychoice.circleci.v2.response.ContextRestriction
import com.github.unhappychoice.circleci.v2.response.FlakyTests.FlakyTest
import com.github.unhappychoice.circleci.v2.response.JobDetails
import com.github.unhappychoice.circleci.v2.response.PipelineDefinition
import com.github.unhappychoice.circleci.v2.response.PipelineListResponse
import com.github.unhappychoice.circleci.v2.response.RestrictionCreated
import com.github.unhappychoice.circleci.v2.response.TestsResponse
import com.github.unhappychoice.circleci.v2.response.Trigger
import com.github.unhappychoice.circleci.v2.response.URLOrbAllowListEntryAcknowledgment
import com.github.unhappychoice.circleci.v2.response.User
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.net.InetSocketAddress
import java.net.URI
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientV2HttpSpec : Spek({
    test("creates a V2 client with the default base URL") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    test("decodes collaborations with null and UUID organization IDs") {
        val organizationId = "123e4567-e89b-12d3-a456-426614174000"
        val response = """
            [
              {"id":null,"vcs-type":"github","name":"legacy","avatar_url":"https://example.com/legacy","slug":"gh/legacy"},
              {"id":"$organizationId","vcs-type":"github","name":"example","avatar_url":"https://example.com/org","slug":"gh/example"}
            ]
        """.trimIndent()
        withResponse(response, "GET", "/me/collaborations") { client ->
            val expected = listOf(
                Collaboration(null, "github", "legacy", "https://example.com/legacy", "gh/legacy"),
                Collaboration(organizationId, "github", "example", "https://example.com/org", "gh/example")
            )
            assertEquals(expected, client.getCollaborations().blockingSingle())
        }
    }

    test("lists project checkout keys with a SHA-256 fingerprint digest") {
        val path = "/project/gh%2Fmy-org%2Fmy-repo/checkout-key?digest=sha256"
        val response = """
            {"items":[{"public-key":"ssh-rsa ...","type":"deploy-key",
                "fingerprint":"SHA256:example","preferred":true,"created-at":"2025-01-02T03:04:05Z"}]}
        """.trimIndent()
        withResponse(response, "GET", path) { client ->
            val key = client.getProjectCheckoutKeys("gh/my-org/my-repo", digest = "sha256")
                .blockingSingle().items!!.single()
            assertEquals("SHA256:example", key.fingerprint)
            assertEquals(OffsetDateTime.parse("2025-01-02T03:04:05Z"), key.createdAt)
        }
    }

    test("lists project checkout keys without a digest query by default") {
        val path = "/project/gh%2Fmy-org%2Fmy-repo/checkout-key"
        withResponse("""{"items":[]}""", "GET", path) { client ->
            client.getProjectCheckoutKeys("gh/my-org/my-repo").blockingSingle()
        }
    }

    test("lists project checkout keys with an MD5 fingerprint digest") {
        val path = "/project/gh%2Fmy-org%2Fmy-repo/checkout-key?digest=md5"
        withResponse("""{"items":[]}""", "GET", path) { client ->
            client.getProjectCheckoutKeys("gh/my-org/my-repo", digest = "md5").blockingSingle()
        }
    }

    test("continues a pipeline with its configuration and parameters") {
        val request = ContinuePipelineRequest("test-key", "version: 2.1", mapOf("deploy" to true))
        val body = withResponse("""{"message":"Pipeline continued"}""", "POST", "/pipeline/continue") { client ->
            client.continuePipeline(request).blockingSingle()
        }
        val expected = """{"continuation-key":"test-key","configuration":"version: 2.1","parameters":{"deploy":true}}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("creates a schedule with its timetable and pipeline parameters") {
        val request = CreateScheduleParameters(
            name = "Nightly",
            timetable = CreateScheduleTimetable(1, listOf(2), daysOfWeek = listOf("MON", "FRI")),
            attributionActor = ScheduleAttributionActor.SYSTEM,
            parameters = mapOf("branch" to "main", "deploy" to true)
        )
        val path = "/project/gh%2Fexample%2Fproject/schedule"
        val body = withResponse("""{"id":"schedule-id"}""", "POST", path, 201) { client ->
            client.createSchedule("gh/example/project", request).blockingSingle()
        }
        val expected = """
            {
                "name":"Nightly",
                "timetable":{"per-hour":1,"hours-of-day":[2],"days-of-week":["MON","FRI"]},
                "attribution-actor":"system",
                "parameters":{"branch":"main","deploy":true}
            }
        """
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("creates a webhook with its scope, events, and TLS verification setting while omitting enablement by default") {
        val request = CreateWebhookRequest(
            name = "Build notifications",
            url = "https://example.com/webhook",
            signingSecret = "test-secret",
            scope = WebhookScopeRequest("project", "project-id"),
            events = listOf("workflow-completed"),
            verifyTls = false
        )
        val body = withResponse("""{"id":"webhook-id"}""", "POST", "/webhook", 201) { client ->
            client.createWebhook(request).blockingSingle()
        }
        val expected = """
            {
                "name":"Build notifications",
                "url":"https://example.com/webhook",
                "signing-secret":"test-secret",
                "scope":{"type":"project","id":"project-id"},
                "events":["workflow-completed"],
                "verify-tls":false
            }
        """
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("creates a disabled webhook and decodes its settings") {
        val request = CreateWebhookRequest(
            name = "Build notifications",
            url = "https://example.com/webhook",
            signingSecret = "test-secret",
            scope = WebhookScopeRequest("project", "project-id"),
            events = listOf("workflow-completed"),
            isEnabled = false
        )
        val response = """{"id":"webhook-id","is_enabled":false,"verify_tls":true,"auto_disabled_at":null}"""
        val body = withResponse(response, "POST", "/webhook", 201) { client ->
            val webhook = client.createWebhook(request).blockingSingle()
            assertEquals(false, webhook.isEnabled)
            assertEquals(true, webhook.verifyTls)
            assertNull(webhook.autoDisabledAt)
        }
        assertEquals(JsonParser.parseString("false"), JsonParser.parseString(body).asJsonObject["is-enabled"])
    }

    test("updates a webhook to disabled and decodes its auto-disable timestamp") {
        val timestamp = "2025-01-02T03:04:05Z"
        val response = """{"id":"webhook-id","is_enabled":false,"verify_tls":false,"auto_disabled_at":"$timestamp"}"""
        val body = withResponse(response, "PUT", "/webhook/webhook-id") { client ->
            val webhook = client.updateWebhook("webhook-id", UpdateWebhookRequest(isEnabled = false)).blockingSingle()
            assertEquals(false, webhook.isEnabled)
            assertEquals(false, webhook.verifyTls)
            assertEquals(timestamp, webhook.autoDisabledAt)
        }
        assertEquals(JsonParser.parseString("""{"is-enabled":false}"""), JsonParser.parseString(body))
    }

    test("updates a webhook without sending an omitted enablement setting") {
        val response = """{"id":"webhook-id","is_enabled":true,"verify_tls":true}"""
        val body = withResponse(response, "PUT", "/webhook/webhook-id") { client ->
            val webhook = client.updateWebhook("webhook-id", UpdateWebhookRequest(name = "Renamed")).blockingSingle()
            assertEquals(true, webhook.isEnabled)
            assertNull(webhook.autoDisabledAt)
        }
        assertEquals(JsonParser.parseString("""{"name":"Renamed"}"""), JsonParser.parseString(body))
    }

    test("decodes Insights flaky-test counts and nullable metadata") {
        val workflowId = "123e4567-e89b-12d3-a456-426614174000"
        val firstTest = """
            {
                "workflow-created-at": "2024-01-01T00:00:00Z",
                "workflow-id": "$workflowId",
                "classname": "ExampleTest",
                "pipeline-number": 7,
                "workflow-name": "build-and-test",
                "test-name": "testWithSource",
                "job-name": "test",
                "job-number": 9,
                "times-flaked": 2147483648,
                "source": "junit",
                "file": "src/test/ExampleTest.kt"
            }
        """.trimIndent()
        val secondTest = firstTest.replace("\"testWithSource\"", "\"testWithoutSource\"")
            .replace("\"$workflowId\"", "null")
            .replace("\"junit\"", "null").replace("\"src/test/ExampleTest.kt\"", "null")
            .replace("2147483648", "3")
        val response = """{"total-flaky-tests":2.0,"flaky-tests":[$firstTest,$secondTest]}"""
        withResponse(response, "GET", "/insights/gh%2Forg%2Frepo/flaky-tests") { client ->
            val result = client.getFlakyTests("gh/org/repo").blockingSingle()
            assertEquals(2.0, result.`total-flaky-tests`, 0.0)
            assertEquals(2, result.`flaky-tests`.size)
            val (withSource, withoutSource) = result.`flaky-tests`
            assertEquals(workflowId, withSource.`workflow-id`)
            assertEquals(2147483648L, withSource.`times-flaked`)
            assertEquals("junit", withSource.source)
            assertEquals("src/test/ExampleTest.kt", withSource.file)
            assertNull(withSource.`pipeline-id`)
            assertNull(withSource.`test-result`)
            assertTrue(FlakyTest::`pipeline-id`.returnType.isMarkedNullable)
            assertTrue(FlakyTest::`test-result`.returnType.isMarkedNullable)
            assertTrue(FlakyTest::`workflow-id`.returnType.isMarkedNullable)
            assertNull(withoutSource.`workflow-id`)
            assertEquals("testWithoutSource", withoutSource.`test-name`)
            assertEquals("build-and-test", withoutSource.`workflow-name`)
            assertEquals(3L, withoutSource.`times-flaked`)
            assertNull(withoutSource.source)
            assertNull(withoutSource.file)
        }
    }

    test("decodes deployed component versions using the CircleCI response schema") {
        val componentId = "b9291e0d-a11e-41fb-8517-c545388b5953"
        val response = """
            {
              "items": [{
                "name": "1.0.0",
                "environment_id": "123e4127-e89b-12d3-a456-426123417400",
                "namespace": "default",
                "is_live": true,
                "pipeline_id": "cc54a110-d03f-4916-96fc-ff36a1221ed7",
                "workflow_id": "5034460f-c7c4-4c43-9457-de07e2029e7b",
                "job_id": "f7e18b53-db27-4706-ad77-b16dc1e6fc53",
                "job_number": 123,
                "last_deployed_at": "2026-01-13T18:51:40Z"
              }],
              "next_page_token": null
            }
        """.trimIndent()
        val expected = ComponentVersion(
            name = "1.0.0",
            environmentId = "123e4127-e89b-12d3-a456-426123417400",
            namespace = "default",
            isLive = true,
            pipelineId = "cc54a110-d03f-4916-96fc-ff36a1221ed7",
            workflowId = "5034460f-c7c4-4c43-9457-de07e2029e7b",
            jobId = "f7e18b53-db27-4706-ad77-b16dc1e6fc53",
            jobNumber = 123,
            lastDeployedAt = "2026-01-13T18:51:40Z"
        )
        withResponse(response, "GET", "/deploy/components/$componentId/versions") { client ->
            val versions = client.listComponentVersions(componentId).blockingSingle()
            assertEquals(ComponentVersionListResponse(listOf(expected), null), versions)
            assertEquals(expected.name, versions.items.single().version)
        }
    }

    val projectSlug = "gh/CircleCI-Public/api-preview-docs"

    group("lists project workflow job metrics") {
        val path = "/insights/gh%2FCircleCI-Public%2Fapi-preview-docs/workflows/build-and-test/jobs"
        val response = """{"items":[],"next_page_token":null}"""

        test("sends the job-name filter") {
            withResponse(response, "GET", "$path?job-name=lint") { client ->
                client.getProjectWorkflowJobMetrics(projectSlug, "build-and-test", jobName = "lint").blockingSingle()
            }
        }

        test("sends the job-name filter with a page token") {
            withResponse(response, "GET", "$path?page-token=next&job-name=lint") { client ->
                client.getProjectWorkflowJobMetrics(
                    projectSlug, "build-and-test", pageToken = "next", jobName = "lint"
                ).blockingSingle()
            }
        }

        test("omits the job-name query when the filter is unspecified") {
            withResponse(response, "GET", path) { client ->
                client.getProjectWorkflowJobMetrics(projectSlug, "build-and-test").blockingSingle()
            }
        }

        test("sends the existing queries through the six-argument overload") {
            val query = "page-token=next&all-branches=true&branch=main&reporting-window=last-30-days"
            withResponse(response, "GET", "$path?$query") { client ->
                client.getProjectWorkflowJobMetrics(
                    projectSlug, "build-and-test", "next", true, "main", "last-30-days"
                ).blockingSingle()
            }
        }

        test("omits the job-name query when the filter is explicitly null") {
            withResponse(response, "GET", "$path?page-token=next") { client ->
                client.getProjectWorkflowJobMetrics(
                    projectSlug, "build-and-test", pageToken = "next", jobName = null
                ).blockingSingle()
            }
        }
    }

    test("lists project pipelines without branch or pagination query parameters") {
        val path = "/project/gh%2FCircleCI-Public%2Fapi-preview-docs/pipeline"
        withResponse("""{"items":[],"next_page_token":"next"}""", "GET", path) { client ->
            client.getProjectPipelines(projectSlug).test()
                .assertResult(PipelineListResponse(emptyList(), "next"))
        }
    }

    val jobPath = "/project/gh%2FCircleCI-Public%2Fapi-preview-docs/job/123"
    val jobTestsPath = "/project/gh%2FCircleCI-Public%2Fapi-preview-docs/123/tests"

    test("cancels a job by project slug and job number") {
        val response = """{"message":"Job canceled"}"""
        val body = withResponse(response, "POST", "$jobPath/cancel") { client ->
            assertEquals("Job canceled", client.cancelJob(projectSlug, "123").blockingSingle().message)
        }
        assertEquals("", body)
    }

    test("cancels a job by ID") {
        val response = """{"message":"Job canceled"}"""
        val body = withResponse(response, "POST", "/jobs/job-id/cancel") { client ->
            assertEquals("Job canceled", client.cancelJobByJobId("job-id").blockingSingle().message)
        }
        assertEquals("", body)
    }

    test("returns the HTTP status and error body when a job cannot be found for cancellation") {
        val response = """{"message":"Job not found"}"""
        withResponse(response, "POST", "$jobPath/cancel", status = 404) { client ->
            client.cancelJob(projectSlug, "123").test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 404 &&
                    error.response()?.errorBody()?.string() == response
            }
        }
    }

    test("decodes job test results and the next page token") {
        val response = """
            {
              "items": [{
                "message": "Expected 2 but got 1",
                "source": "junit",
                "run_time": 0.125,
                "file": "CalculatorTest.kt",
                "result": "failure",
                "name": "addsNumbers",
                "classname": "CalculatorTest"
              }],
              "next_page_token": "next-page"
            }
        """.trimIndent()
        val item = TestsResponse.Test(
            "Expected 2 but got 1", "junit", 0.125, "CalculatorTest.kt", "failure", "addsNumbers", "CalculatorTest"
        )
        withResponse(response, "GET", jobTestsPath) { client ->
            client.getTests(projectSlug, "123").test().assertResult(TestsResponse(listOf(item), "next-page"))
        }
    }

    test("rejects job test results with a non-array items field") {
        withResponse("""{"items":{}}""", "GET", jobTestsPath) { client ->
            client.getTests(projectSlug, "123").test().assertFailure(JsonSyntaxException::class.java)
        }
    }

    test("returns the HTTP status and error body when the job is missing") {
        val response = """{"message":"Job not found"}"""
        withResponse(response, "GET", jobTestsPath, status = 404) { client ->
            client.getTests(projectSlug, "123").test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 404 &&
                    error.response()?.errorBody()?.string() == response
            }
        }
    }

    val jobDetailsJson = """
        {
          "web_url": "https://app.circleci.com/jobs/123",
          "project": {
            "id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
            "slug": "$projectSlug",
            "name": "api-preview-docs",
            "external_url": "https://github.com/CircleCI-Public/api-preview-docs"
          },
          "parallel_runs": [{"index": 0, "status": "success"}],
          "started_at": "2020-01-13T18:51:40Z",
          "latest_workflow": {"id": "8469d90e-f7b9-4642-80da-8aa6244431ac", "name": "build-and-test"},
          "name": "build",
          "executor": {"type": "docker", "resource_class": "medium"},
          "parallelism": 1,
          "status": "success",
          "number": 123,
          "pipeline": {"id": "5034460f-c7c4-4c43-9457-de07e2029e7b"},
          "duration": 42000,
          "created_at": "2020-01-13T18:51:38Z",
          "messages": [{"type": "info", "message": "Job completed", "reason": "success"}],
          "contexts": [{"name": "build-context"}],
          "organization": {"name": "CircleCI-Public"},
          "queued_at": "2020-01-13T18:51:39Z",
          "stopped_at": "2020-01-13T18:52:22Z"
        }
    """.trimIndent()

    test("decodes job details using the CircleCI response schema") {
        withResponse(jobDetailsJson, "GET", jobPath) { client ->
            val details = client.getJobDetails(projectSlug, "123").blockingSingle()
            val expected = JobDetails(
                name = "build",
                number = 123,
                status = "success",
                duration = 42000,
                webUrl = "https://app.circleci.com/jobs/123",
                project = JobDetails.Project(
                    id = "497f6eca-6276-4993-bfeb-53cbbbba6f08",
                    slug = projectSlug,
                    name = "api-preview-docs",
                    externalUrl = "https://github.com/CircleCI-Public/api-preview-docs"
                ),
                executor = JobDetails.Executor(type = "docker", resourceClass = "medium"),
                parallelism = 1,
                parallelRuns = listOf(JobDetails.ParallelRun(0, "success")),
                latestWorkflow = JobDetails.LatestWorkflow("8469d90e-f7b9-4642-80da-8aa6244431ac", "build-and-test"),
                pipeline = JobDetails.Pipeline("5034460f-c7c4-4c43-9457-de07e2029e7b"),
                organization = JobDetails.Organization("CircleCI-Public"),
                contexts = listOf(JobDetails.Context("build-context")),
                messages = listOf(JobDetails.Message("info", "Job completed", "success")),
                createdAt = "2020-01-13T18:51:38Z",
                queuedAt = "2020-01-13T18:51:39Z",
                startedAt = "2020-01-13T18:51:40Z",
                stoppedAt = "2020-01-13T18:52:22Z"
            )
            assertEquals(expected, details)
        }
    }

    test("decodes job details with null and omitted fields") {
        val response = JsonParser.parseString(jobDetailsJson).asJsonObject.apply {
            add("duration", JsonNull.INSTANCE)
            add("status", JsonNull.INSTANCE)
            remove("stopped_at")
            getAsJsonObject("project").remove("id")
            getAsJsonArray("messages")[0].asJsonObject.remove("reason")
        }
        withResponse(response.toString(), "GET", jobPath) { client ->
            val details = client.getJobDetails(projectSlug, "123").blockingSingle()
            assertNull(details.duration)
            assertNull(details.status)
            assertNull(details.stoppedAt)
            assertNull(details.project.id)
            assertNull(details.messages.single().reason)
        }
    }

    val rollbackRequest = RollbackProjectRequest("production", "frontend", "1.1.0", "1.0.0")
    val rollbackResponse = """{"id":"123e4567-e89b-12d3-a456-426614174000","rollback_type":"PIPELINE"}"""
    val rollbackPath = "/projects/project-id/rollback"
    val rollbackBody = """
        {
            "environment_name": "production",
            "component_name": "frontend",
            "current_version": "1.1.0",
            "target_version": "1.0.0"
        }
    """.trimIndent()

    test("rolls back a project with a reason, namespace, and pipeline parameters") {
        val body = withResponse(rollbackResponse, "POST", rollbackPath, 202) { client ->
            val request = rollbackRequest.copy(
                reason = "Restore the previous release",
                namespace = "payments",
                parameters = mapOf("region" to "us-east-1", "dry-run" to true, "replicas" to 2)
            )
            client.rollbackProject("project-id", request).blockingSingle()
        }
        val expected = JsonParser.parseString(rollbackBody).asJsonObject
        expected.addProperty("reason", "Restore the previous release")
        expected.addProperty("namespace", "payments")
        expected.add("parameters", JsonParser.parseString("""{"region":"us-east-1","dry-run":true,"replicas":2}"""))
        assertEquals(expected, JsonParser.parseString(body))
    }

    test("omits optional fields when rolling back a project") {
        val body = withResponse(rollbackResponse, "POST", rollbackPath, 202) { client ->
            val response = client.rollbackProject("project-id", rollbackRequest).blockingSingle()
            assertEquals("123e4567-e89b-12d3-a456-426614174000", response.id)
            assertEquals("PIPELINE", response.rollbackType)
        }
        assertEquals(JsonParser.parseString(rollbackBody), JsonParser.parseString(body))
    }

    test("decodes the authenticated user") {
        val response = """{"id":"user-id","login":"test-login","name":"Test User"}"""
        withResponse(response, "GET", "/me") { client ->
            client.getMe().test().assertResult(User("user-id", "test-login", "Test User"))
        }
    }

    test("returns the HTTP status and error body when authentication fails") {
        val response = """{"message":"Unauthorized"}"""
        withResponse(response, "GET", "/me", status = 401) { client ->
            client.getMe().test().assertNoValues().assertNotComplete().assertError { error ->
                error is HttpException && error.code() == 401 &&
                    error.response()?.errorBody()?.string() == response
            }
        }
    }

    test("decodes the acknowledgment for creating a URL Orb allow-list entry") {
        val response = """{"id":"entry-id","message":"Created."}"""
        val path = "/organization/org-id/url-orb-allow-list"
        val request = CreateURLOrbAllowListEntryRequest("Example", "https://example.com/", "none")
        withResponse(response, "POST", path) { client ->
            client.createUrlOrbAllowListEntry("org-id", request).test()
                .assertResult(URLOrbAllowListEntryAcknowledgment("entry-id", "Created."))
        }
    }

    test("decodes the acknowledgment for removing a URL Orb allow-list entry") {
        val response = """{"id":"entry-id","message":"Removed."}"""
        val path = "/organization/org-id/url-orb-allow-list/entry-id"
        withResponse(response, "DELETE", path) { client ->
            client.removeUrlOrbAllowListEntry("org-id", "entry-id").test()
                .assertResult(URLOrbAllowListEntryAcknowledgment("entry-id", "Removed."))
        }
    }

    test("deletes a context") {
        withResponse("""{"message":"Context deleted"}""", "DELETE", "/context/context-id") { client ->
            assertEquals("Context deleted", client.deleteContext("context-id").blockingSingle().message)
        }
    }

    test("returns typed metadata when adding or updating a context environment variable") {
        val response = """
            {
              "variable": "VAR",
              "context_id": "context-id",
              "created_at": "2025-01-02T03:04:05.042Z",
              "updated_at": "2025-01-03T04:05:06.123456Z"
            }
        """.trimIndent()
        val body = withResponse(response, "PUT", "/context/context-id/environment-variable/VAR") { client ->
            val result = client.addEnvironmentVariableToContext(
                "context-id", "VAR", AddEnvironmentVariableRequest("test-value")
            ).blockingSingle()
            assertEquals("VAR", result.variable)
            assertEquals("context-id", result.contextId)
            assertEquals(OffsetDateTime.parse("2025-01-02T03:04:05.042Z"), result.createdAt)
            assertEquals(OffsetDateTime.parse("2025-01-03T04:05:06.123456Z"), result.updatedAt)
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
        val response = """
            {
              "items": [
                {
                  "id": "project-restriction-id",
                  "context_id": "context-id",
                  "restriction_type": "project",
                  "restriction_value": "project-id",
                  "name": "Example project"
                },
                {
                  "id": "expression-restriction-id",
                  "context_id": "context-id",
                  "restriction_type": "expression",
                  "restriction_value": "pipeline.git.branch == \"main\"",
                  "name": null
                },
                {
                  "id": "group-restriction-id",
                  "context_id": "context-id",
                  "restriction_type": "group",
                  "restriction_value": "group-id",
                  "name": "Example group"
                }
              ],
              "next_page_token": "next-page"
            }
        """.trimIndent()
        val path = "/context/context-id/restrictions?page-token=page%20%2B%20token"
        withResponse(response, "GET", path) { client ->
            val result = client.getContextRestrictions("context-id", "page + token").blockingSingle()
            assertEquals("next-page", result.nextPageToken)
            assertEquals(
                listOf(
                    ContextRestriction("project-restriction-id", "context-id", "project", "project-id", "Example project"),
                    ContextRestriction(
                        "expression-restriction-id", "context-id", "expression", "pipeline.git.branch == \"main\"", null
                    ),
                    ContextRestriction("group-restriction-id", "context-id", "group", "group-id", "Example group")
                ),
                result.items
            )
        }
    }

    group("creates context restrictions") {
        val path = "/context/context-id/restrictions"
        val id = "497f6eca-6276-4993-bfeb-53cbbbba6f08"

        test("creates a project restriction and decodes the project name") {
            val projectId = "405d8375-3514-403b-8c43-83ae74cfe0e9"
            val response = """
                {
                  "id": "$id",
                  "project_id": "$projectId",
                  "name": "project-name",
                  "restriction_type": "project",
                  "restriction_value": "$projectId"
                }
            """.trimIndent()
            val body = withResponse(response, "POST", path, 201) { client ->
                client.createContextRestriction("context-id", CreateContextRestrictionRequest("project", projectId))
                    .test().assertResult(RestrictionCreated(id, "project", projectId, "project-name"))
            }
            val expected = """{"restriction_type":"project","restriction_value":"$projectId"}"""
            assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
        }

        test("creates an expression restriction and decodes a null name") {
            val expression = "pipeline.git.branch == \"main\""
            val response = """
                {
                  "id": "$id",
                  "name": null,
                  "restriction_type": "expression",
                  "restriction_value": "pipeline.git.branch == \"main\""
                }
            """.trimIndent()
            val body = withResponse(response, "POST", path, 201) { client ->
                client.createContextRestriction("context-id", CreateContextRestrictionRequest("expression", expression))
                    .test().assertResult(RestrictionCreated(id, "expression", expression, null))
            }
            val expected = """{"restriction_type":"expression","restriction_value":"pipeline.git.branch == \"main\""}"""
            assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
        }
    }

    group("decodes pipeline definitions") {
        val path = "/projects/project-id/pipeline-definitions"
        val id = "39723015-b399-4601-9ff6-bd1bfbed8fa8"
        val response = """
            {
              "id": "$id",
              "name": "Build",
              "description": "Build the application",
              "created_at": "2025-01-01T00:00:00Z",
              "config_source": {
                "provider": "github_app",
                "repo": {"full_name": "example/config", "external_id": "config-id"},
                "file_path": ".circleci/build.yml"
              },
              "checkout_source": {
                "provider": "github_app",
                "repo": {"full_name": "example/app", "external_id": "app-id"}
              }
            }
        """.trimIndent()
        val expected = PipelineDefinition(
            id = id,
            name = "Build",
            description = "Build the application",
            createdAt = "2025-01-01T00:00:00Z",
            configSource = PipelineDefinition.ConfigSource(
                "github_app", PipelineDefinition.Repository("example/config", "config-id"), ".circleci/build.yml"
            ),
            checkoutSource = PipelineDefinition.CheckoutSource(
                "github_app", PipelineDefinition.Repository("example/app", "app-id")
            )
        )

        test("decodes the sources and configuration path from a single response") {
            withResponse(response, "GET", "$path/$id") { client ->
                assertEquals(expected, client.getPipelineDefinition("project-id", id).blockingSingle())
            }
        }

        test("decodes sources and absent optional fields in list items") {
            val partial = """{"config_source":{"file_path":".circleci/managed.yml"},"checkout_source":null}"""
            val page = """{"items":[$response,$partial],"next_page_token":"next-page"}"""
            withResponse(page, "GET", path) { client ->
                val result = client.listPipelineDefinitions("project-id", null).blockingSingle()
                val managedConfig = PipelineDefinition.ConfigSource(filePath = ".circleci/managed.yml")
                assertEquals(listOf(expected, PipelineDefinition(configSource = managedConfig)), result.items)
                assertEquals("next-page", result.nextPageToken)
            }
        }
    }

    test("creates a pipeline definition with both sources") {
        val request = CreatePipelineDefinitionRequest(
            "pipeline",
            CreatePipelineDefinitionRequest.ConfigSource(
                "github_app", ".circleci/pipeline.yml", PipelineDefinitionRepository("config-repo-id")
            ),
            CreatePipelineDefinitionRequest.CheckoutSource("github_app", PipelineDefinitionRepository("checkout-repo-id"))
        )
        val body = withResponse("""{"id":"definition-id"}""", "POST", "/projects/project-id/pipeline-definitions") { client ->
            client.createPipelineDefinition("project-id", request).blockingSingle()
        }
        val expected = """
            {
                "name":"pipeline",
                "config_source":{"provider":"github_app","file_path":".circleci/pipeline.yml","repo":{"external_id":"config-repo-id"}},
                "checkout_source":{"provider":"github_app","repo":{"external_id":"checkout-repo-id"}}
            }
        """
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("updates a pipeline definition checkout source independently") {
        val request = UpdatePipelineDefinitionRequest(
            checkoutSource = UpdatePipelineDefinitionRequest.CheckoutSource(repo = PipelineDefinitionRepository("new-repo-id"))
        )
        val path = "/projects/project-id/pipeline-definitions/definition-id"
        val body = withResponse("""{"id":"definition-id"}""", "PATCH", path) { client ->
            client.updatePipelineDefinition("project-id", "definition-id", request).blockingSingle()
        }
        val expected = """{"checkout_source":{"repo":{"external_id":"new-repo-id"}}}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
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

        test("omits unspecified webhook settings") {
            val source = EventSource("webhook", webhook = EventSource.Webhook())
            assertTriggerRequest(
                CreateTriggerRequest(source),
                """{"event_source":{"provider":"webhook","webhook":{}}}"""
            )
        }

        test("omits unspecified schedule settings") {
            val source = EventSource("schedule", schedule = EventSource.Schedule())
            assertTriggerRequest(
                CreateTriggerRequest(source),
                """{"event_source":{"provider":"schedule","schedule":{}}}"""
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

    group("updates a pipeline trigger") {
        test("sends only the disabled flag when disabling a trigger") {
            assertTriggerUpdate(UpdateTriggerRequest(disabled = true), """{"disabled":true}""")
        }

        test("sends schedule settings and pipeline parameters") {
            val source = UpdateTriggerRequest.EventSource(schedule = EventSource.Schedule("0 9 * * 1-5", "system"))
            assertTriggerUpdate(
                UpdateTriggerRequest(
                    eventSource = source, eventName = "weekday-build", checkoutRef = "main", configRef = "main",
                    parameters = mapOf("deploy_env" to "staging")
                ),
                """{
                    "event_source":{"schedule":{"cron_expression":"0 9 * * 1-5","attribution_actor":"system"}},
                    "event_name":"weekday-build","checkout_ref":"main","config_ref":"main",
                    "parameters":{"deploy_env":"staging"}
                }"""
            )
        }

        test("sends webhook sender settings") {
            val source = UpdateTriggerRequest.EventSource(webhook = EventSource.Webhook("monitor"))
            assertTriggerUpdate(
                UpdateTriggerRequest(eventSource = source, eventName = "alert"),
                """{"event_source":{"webhook":{"sender":"monitor"}},"event_name":"alert"}"""
            )
        }

        test("sends the event preset and explicit false when enabling a trigger") {
            assertTriggerUpdate(
                UpdateTriggerRequest(eventPreset = "all-pushes", disabled = false),
                """{"event_preset":"all-pushes","disabled":false}"""
            )
        }
    }

    test("lists all webhooks when the enabled filter is omitted") {
        val path = "/webhook?scope-id=project-id&scope-type=project"
        withResponse("""{"items":[]}""", "GET", path) { client ->
            client.getWebhooks("project-id", "project").blockingSingle()
        }
    }

    test("lists all webhooks when the enabled filter is null") {
        val path = "/webhook?scope-id=project-id&scope-type=project"
        withResponse("""{"items":[]}""", "GET", path) { client ->
            client.getWebhooks("project-id", "project", null).blockingSingle()
        }
    }

    listOf(true, false).forEach { isEnabled ->
        test("lists webhooks with is-enabled=$isEnabled") {
            val path = "/webhook?scope-id=project-id&scope-type=project&is-enabled=$isEnabled"
            withResponse("""{"items":[]}""", "GET", path) { client ->
                client.getWebhooks("project-id", "project", isEnabled).blockingSingle()
            }
        }
    }

    test("lists the next webhook page with the original filters and cursor") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(body = """{"items":[],"next_page_token":"cursor+2"}"""))
            server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

            val firstPage = client.getWebhooks("project-id", "project", false).blockingSingle()
            val secondPage = client.getWebhooks("project-id", "project", false, firstPage.nextPageToken).blockingSingle()
            assertNull(secondPage.nextPageToken)

            val firstRequest = requireNotNull(server.takeRequest(1, SECONDS)).url
            val secondRequest = requireNotNull(server.takeRequest(1, SECONDS)).url
            assertEquals("/api/v2/webhook", secondRequest.encodedPath)
            assertNull(firstRequest.queryParameter("page-token"))
            assertEquals(setOf("scope-id", "scope-type", "is-enabled", "page-token"), secondRequest.queryParameterNames)
            assertEquals(listOf("project-id"), secondRequest.queryParameterValues("scope-id"))
            assertEquals(listOf("project"), secondRequest.queryParameterValues("scope-type"))
            assertEquals(listOf("false"), secondRequest.queryParameterValues("is-enabled"))
            assertEquals(listOf("cursor+2"), secondRequest.queryParameterValues("page-token"))
        }
    }

    val usageExportId = "e8235eed-f121-4ae3-9c72-2719d6572818"
    val usageExportPath = "/organizations/org-id/usage_export_job"

    test("serializes only dates when shared organization IDs are omitted and decodes the created job") {
        val start = "2025-01-01T00:00:00Z"
        val end = "2025-02-01T00:00:00Z"
        val response = """
            {
              "usage_export_job_id": "$usageExportId",
              "state": "created",
              "start": "$start",
              "end": "$end",
              "download_urls": []
            }
        """.trimIndent()
        val body = withResponse(response, "POST", usageExportPath, status = 201) { client ->
            val job = client.createUsageExport("org-id", CreateUsageExportRequest(start, end)).blockingSingle()
            assertEquals(usageExportId, job.id)
            assertEquals("created", job.state)
            assertEquals(start, job.start)
            assertEquals(end, job.end)
            assertEquals(emptyList<String>(), job.downloadUrls)
        }
        val json = JsonParser.parseString(body).asJsonObject
        assertEquals(start, json.get("start")?.asString)
        assertEquals(end, json.get("end")?.asString)
        assertEquals(setOf("start", "end"), json.keySet())
    }

    test("serializes shared organization IDs in the usage export request") {
        val start = "2025-01-01T00:00:00Z"
        val end = "2025-02-01T00:00:00Z"
        val sharedOrgIds = listOf("b9291e0d-a11e-41fb-8517-c545388b5953", "48931ceb-8a7c-4962-918c-6e819b5f260e")
        val response = """
            {
              "usage_export_job_id": "$usageExportId",
              "state": "created",
              "start": "$start",
              "end": "$end",
              "download_urls": []
            }
        """.trimIndent()
        val body = withResponse(response, "POST", usageExportPath, status = 201) { client ->
            client.createUsageExport("org-id", CreateUsageExportRequest(start, end, sharedOrgIds)).blockingSingle()
        }
        val json = JsonParser.parseString(body).asJsonObject
        assertEquals(start, json.get("start")?.asString)
        assertEquals(end, json.get("end")?.asString)
        assertEquals(sharedOrgIds, json.getAsJsonArray("shared_org_ids").map { it.asString })
    }

    test("preserves ordered download URLs and redacts them from both usage export response strings") {
        val firstUrl = "https://example.com/usage-1.csv?signature=first"
        val secondUrl = "https://example.com/usage-2.csv?signature=second"
        val start = "2025-01-01T00:00:00Z"
        val end = "2025-02-01T00:00:00Z"
        val response = """
            {
              "usage_export_job_id": "$usageExportId",
              "state": "completed",
              "start": "$start",
              "end": "$end",
              "download_urls": ["$firstUrl", "$secondUrl"]
            }
        """.trimIndent()
        withResponse(response, "GET", "$usageExportPath/$usageExportId") { client ->
            val job = client.getUsageExport("org-id", usageExportId).blockingSingle()
            assertEquals(usageExportId, job.id)
            assertEquals("completed", job.state)
            assertEquals(listOf(firstUrl, secondUrl), job.downloadUrls)
            assertNull(job.errorReason)
            assertEquals(
                "GetUsageExportJobStatus(id=$usageExportId, state=completed, downloadUrls=[REDACTED], errorReason=null)",
                job.toString()
            )
        }
        withResponse(response, "POST", usageExportPath, status = 201) { client ->
            val job = client.createUsageExport("org-id", CreateUsageExportRequest(start, end)).blockingSingle()
            assertEquals(listOf(firstUrl, secondUrl), job.downloadUrls)
            assertEquals(
                "UsageExportJob(id=$usageExportId, state=completed, start=$start, end=$end, downloadUrls=[REDACTED])",
                job.toString()
            )
        }
    }

    test("decodes the error reason for a failed usage export") {
        val response = """
            {
              "usage_export_job_id": "$usageExportId",
              "state": "failed",
              "download_urls": [],
              "error_reason": "Export generation failed"
            }
        """.trimIndent()
        withResponse(response, "GET", "$usageExportPath/$usageExportId") { client ->
            val job = client.getUsageExport("org-id", usageExportId).blockingSingle()
            assertEquals("failed", job.state)
            assertEquals(emptyList<String>(), job.downloadUrls)
            assertEquals("Export generation failed", job.errorReason)
        }
    }

    listOf(
        "with a TTL update timestamp" to "2026-01-01T00:00:00Z",
        "without a TTL update timestamp" to null
    ).forEach { (scenario, timestamp) ->
        val timestampField = timestamp?.let { ""","ttl_updated_at":"$it"""" }.orEmpty()

        test("decodes organization OIDC claims $scenario") {
            val response = """{"org_id":"org-id","ttl":"1h"$timestampField}"""
            withResponse(response, "GET", "/org/org-id/oidc-custom-claims") { client ->
                val claims = client.getOrgClaims("org-id").blockingSingle()
                assertEquals("1h", claims.ttl)
                assertEquals(timestamp, claims.ttlUpdatedAt)
            }
        }

        test("decodes project OIDC claims $scenario") {
            val response = """{"org_id":"org-id","project_id":"project-id","ttl":"1h"$timestampField}"""
            val path = "/org/org-id/project/project-id/oidc-custom-claims"
            withResponse(response, "GET", path) { client ->
                val claims = client.getProjectClaims("org-id", "project-id").blockingSingle()
                assertEquals("1h", claims.ttl)
                assertEquals(timestamp, claims.ttlUpdatedAt)
            }
        }
    }

    test("patches organization OIDC claims with a top-level audience array and TTL") {
        val response = """{"org_id":"org-id"}"""
        val path = "/org/org-id/oidc-custom-claims"
        val body = withResponse(response, "PATCH", path) { client ->
            val request = PatchClaimsRequest(audience = listOf("service-a", "service-b"), ttl = "1h")
            client.patchOrgClaims("org-id", request).blockingSingle()
        }
        val expected = """{"audience":["service-a","service-b"],"ttl":"1h"}"""
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
    }

    test("decodes a webhook trigger response") {
        val response = """
            {
              "id": "trigger-id",
              "event_name": "monitor-alert",
              "event_source": {
                "provider": "webhook",
                "webhook": {"url": "https://example.com/webhook?secret=test-secret", "sender": "datadog"}
              },
              "checkout_ref": "main",
              "config_ref": "ci-config",
              "disabled": false
            }
        """.trimIndent()

        withResponse(response, "GET", "/projects/project-id/triggers/trigger-id") { client ->
            val trigger = client.getTrigger("project-id", "trigger-id").blockingSingle()
            assertEquals("trigger-id", trigger.id)
            assertEquals("monitor-alert", trigger.eventName)
            assertEquals("webhook", trigger.eventSource.provider)
            val webhook = Trigger.Webhook("https://example.com/webhook?secret=test-secret", "datadog")
            assertEquals(webhook, trigger.eventSource.webhook)
            assertFalse(trigger.toString().contains("test-secret"))
            assertEquals("main", trigger.checkoutRef)
            assertEquals("ci-config", trigger.configRef)
            assertEquals(false, trigger.disabled)
            assertNull(trigger.eventPreset)
            assertNull(trigger.parameters)
        }
    }

    test("decodes the repository and event preset in a VCS trigger response") {
        val response = """
            {
              "id": "trigger-id",
              "event_preset": "all-pushes",
              "event_source": {
                "provider": "github_app",
                "repo": {"full_name": "example/project", "external_id": "repo-id"}
              }
            }
        """.trimIndent()

        withResponse(response, "GET", "/projects/project-id/triggers/trigger-id") { client ->
            val trigger = client.getTrigger("project-id", "trigger-id").blockingSingle()
            assertEquals("all-pushes", trigger.eventPreset)
            val repository = Trigger.Repository("example/project", "repo-id")
            assertEquals(Trigger.EventSource("github_app", repo = repository), trigger.eventSource)
            assertEquals(repository.fullName, trigger.eventSource.repo?.fullName)
            assertEquals(repository.externalId, trigger.eventSource.repo?.externalId)
        }
    }

    test("decodes a schedule trigger in the list response") {
        val response = """
            {
              "items": [{
                "id": "schedule-id",
                "event_name": "weekday-build",
                "event_source": {
                  "provider": "schedule",
                  "schedule": {
                    "cron_expression": "0 9 * * 1-5",
                    "attribution_actor": {"id": "actor-id"}
                  }
                },
                "checkout_ref": "main",
                "config_ref": "main",
                "disabled": true,
                "parameters": {"deploy_env": "staging"}
              }]
            }
        """.trimIndent()

        val path = "/projects/project-id/pipeline-definitions/definition-id/triggers"
        withResponse(response, "GET", path) { client ->
            val trigger = client.listPipelineDefinitionTriggers("project-id", "definition-id").blockingSingle().items.single()
            assertEquals("schedule-id", trigger.id)
            assertEquals("weekday-build", trigger.eventName)
            assertEquals("schedule", trigger.eventSource.provider)
            val schedule = Trigger.Schedule("0 9 * * 1-5", Trigger.AttributionActor("actor-id"))
            assertEquals(schedule.cronExpression, trigger.eventSource.schedule?.cronExpression)
            assertEquals(schedule.attributionActor, trigger.eventSource.schedule?.attributionActor)
            assertEquals(true, trigger.disabled)
            assertEquals(mapOf("deploy_env" to "staging"), trigger.parameters)
        }
    }

    test("patches only the selected project OIDC claim") {
        val response = """{"org_id":"org-id","project_id":"project-id"}"""
        val path = "/org/org-id/project/project-id/oidc-custom-claims"
        val body = withResponse(response, "PATCH", path) { client ->
            val request = PatchClaimsRequest(audience = listOf("service-a"))
            client.patchProjectClaims("org-id", "project-id", request).blockingSingle()
        }
        assertEquals(JsonParser.parseString("""{"audience":["service-a"]}"""), JsonParser.parseString(body))
    }

    test("deletes selected organization OIDC claims in a single query parameter") {
        val path = "/org/org-id/oidc-custom-claims?claims=audience%2Cttl"
        withResponse("""{"org_id":"org-id"}""", "DELETE", path) { client ->
            client.deleteOrgClaims("org-id", "audience,ttl").blockingSingle()
        }
    }

    test("deletes selected project OIDC claims in a single query parameter") {
        val path = "/org/org-id/project/project-id/oidc-custom-claims?claims=audience%2Cttl"
        withResponse("""{"org_id":"org-id","project_id":"project-id"}""", "DELETE", path) { client ->
            client.deleteProjectClaims("org-id", "project-id", "audience,ttl").blockingSingle()
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

private fun assertTriggerUpdate(request: UpdateTriggerRequest, expected: String) {
    val path = "/projects/project-id/triggers/trigger-id"
    val body = withResponse("""{"id":"trigger-id"}""", "PATCH", path) { client ->
        assertEquals("trigger-id", client.updateTrigger("project-id", "trigger-id", request).blockingSingle().id)
    }
    assertEquals(JsonParser.parseString(expected), JsonParser.parseString(body))
}

private data class RecordedRequest(val method: String, val uri: URI, val headers: Headers, val body: String)

private fun withResponse(
    json: String, method: String, path: String, status: Int = 200, call: (CircleCIAPIClientV2) -> Unit
): String {
    val request = CompletableFuture<RecordedRequest>()
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/") { exchange ->
        exchange.use {
            val body = exchange.requestBody.bufferedReader(Charsets.UTF_8).use { it.readText() }
            request.complete(RecordedRequest(exchange.requestMethod, exchange.requestURI, exchange.requestHeaders, body))
            val response = json.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, response.size.toLong())
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
