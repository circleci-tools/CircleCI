package com.github.unhappychoice.circleci

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsFilterRequestSpec : Spek({
    val orgSlug = "gh/example"
    val projectSlug = "$orgSlug/project"

    fun MockWebServer.client() = CircleCIAPIClient("test-token", url("/api/").toString()).clientV2()

    listOf(2, 1).forEach { count ->
        test("sends $count query entries for each Insights filter") {
            MockWebServer().use { server ->
                val client = server.client()
                val projects = listOf("frontend", "backend").take(count)
                val branches = listOf("main", "release/v1").take(count)
                val workflows = listOf("build", "deploy").take(count)
                repeat(2) { server.enqueue(MockResponse().setBody("{}")) }

                client.getOrgSummaryData(orgSlug, projectNames = projects).blockingFirst()
                client.getProjectWorkflowsPageData(projectSlug, branches = branches, workflowNames = workflows)
                    .blockingFirst()

                val orgRequest = requireNotNull(server.takeRequest().requestUrl)
                val projectRequest = requireNotNull(server.takeRequest().requestUrl)
                assertEquals(projects, orgRequest.queryParameterValues("project-names"))
                assertEquals(branches, projectRequest.queryParameterValues("branches"))
                assertEquals(workflows, projectRequest.queryParameterValues("workflow-names"))
            }
        }
    }

    test("omits query entries for unspecified Insights filters") {
        MockWebServer().use { server ->
            val client = server.client()
            repeat(2) { server.enqueue(MockResponse().setBody("{}")) }

            client.getOrgSummaryData(orgSlug).blockingFirst()
            client.getProjectWorkflowsPageData(projectSlug).blockingFirst()

            val orgRequest = requireNotNull(server.takeRequest().requestUrl)
            val projectRequest = requireNotNull(server.takeRequest().requestUrl)
            assertEquals(emptyList<String>(), orgRequest.queryParameterValues("project-names"))
            assertEquals(emptyList<String>(), projectRequest.queryParameterValues("branches"))
            assertEquals(emptyList<String>(), projectRequest.queryParameterValues("workflow-names"))
        }
    }
})
