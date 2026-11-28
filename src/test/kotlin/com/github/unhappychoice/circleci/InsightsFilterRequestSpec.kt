package com.github.unhappychoice.circleci

import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.http.GET

object InsightsFilterRequestSpec : Spek({
    val orgSlug = "gh/example"
    val projectSlug = "$orgSlug/project"

    fun MockWebServer.client() = CircleCIAPIClient("test-token", url("/api/").toString()).clientV2()

    test("keeps the first-page JVM method and Kotlin default argument bridge while sharing the route") {
        val client = CircleCIAPIClientV2::class.java
        val parameters = Array(6) { String::class.java }
        val firstPage = client.getMethod("getJobTimeseries", *parameters)
        val paginated = client.getMethod("getJobTimeseries", *parameters, String::class.java)
        assertTrue(firstPage.isDefault)
        assertNull(firstPage.getAnnotation(GET::class.java))
        assertEquals(
            "insights/time-series/{project-slug}/workflows/{workflow-name}/jobs",
            paginated.getAnnotation(GET::class.java)?.value
        )
        val bridgeParameters = listOf(client) + parameters + listOf(Int::class.javaPrimitiveType!!, Any::class.java)
        client.getMethod("getJobTimeseries\$default", *bridgeParameters.toTypedArray())
    }

    listOf(emptyList(), listOf("next-page")).forEach { paginationArguments ->
        val arity = 6 + paginationArguments.size
        test("fetches job time series through the $arity-argument JVM compatibility bridge") {
            val clientType = CircleCIAPIClientV2::class.java
            val compatibilityClass = clientType.declaredClasses.single { it.simpleName == "DefaultImpls" }
            val parameters = listOf(clientType) + List(arity) { String::class.java } +
                listOf(Int::class.javaPrimitiveType, Any::class.java)
            val bridge = compatibilityClass.getMethod("getJobTimeseries\$default", *parameters.toTypedArray())
            val omittedFiltersMask = 0b111100

            MockWebServer().use { server ->
                server.start()
                server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))
                val arguments = listOf(server.client(), projectSlug, "ci", null, null, null, null) +
                    paginationArguments + listOf(omittedFiltersMask, null)

                (bridge.invoke(null, *arguments.toTypedArray()) as Observable<*>).blockingSingle()

                val request = server.takeRequest().url
                assertEquals(paginationArguments, request.queryParameterValues("page-token"))
            }
        }
    }

    test("fetches the next job time series page with the same filters") {
        MockWebServer().use { server ->
            server.start()
            val client = server.client()
            val branch = "release/v1"
            val granularity = "daily"
            val startDate = "2025-01-01T00:00:00Z"
            val endDate = "2025-01-03T00:00:00Z"
            val token = "next+/=&page"
            server.enqueue(MockResponse(body = """{"items":[{"name":"build"}],"next_page_token":"$token"}"""))
            server.enqueue(MockResponse(body = """{"items":[{"name":"test"}],"next_page_token":null}"""))

            val first = client.getJobTimeseries(projectSlug, "ci", branch, granularity, startDate, endDate).blockingSingle()
            assertEquals("build", first.items.single().name)
            assertEquals(token, first.nextPageToken)
            val second = client.getJobTimeseries(
                projectSlug, "ci", branch, granularity, startDate, endDate, pageToken = first.nextPageToken
            ).blockingSingle()
            assertEquals("test", second.items.single().name)
            assertEquals(null, second.nextPageToken)

            val firstRequest = server.takeRequest().url
            val secondRequest = server.takeRequest().url
            assertEquals("/api/v2/insights/time-series/gh%2Fexample%2Fproject/workflows/ci/jobs", firstRequest.encodedPath)
            assertEquals(listOf(branch), firstRequest.queryParameterValues("branch"))
            assertEquals(listOf(granularity), firstRequest.queryParameterValues("granularity"))
            assertEquals(listOf(startDate), firstRequest.queryParameterValues("start-date"))
            assertEquals(listOf(endDate), firstRequest.queryParameterValues("end-date"))
            assertEquals(emptyList<String>(), firstRequest.queryParameterValues("page-token"))
            assertEquals(firstRequest.newBuilder().addQueryParameter("page-token", token).build(), secondRequest)
        }
    }

    test("omits job time series queries when optional arguments are unspecified") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))

            server.client().getJobTimeseries(projectSlug, "ci").blockingSingle()

            val request = server.takeRequest().url
            assertEquals(emptySet<String>(), request.queryParameterNames)
        }
    }

    listOf("next-page", null).forEach { pageToken ->
        test("sends job time series queries with default filters and page token $pageToken") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(MockResponse(body = """{"items":[],"next_page_token":null}"""))

                server.client().getJobTimeseries(projectSlug, "ci", pageToken = pageToken).blockingSingle()

                val request = server.takeRequest().url
                val expectedQueries = if (pageToken == null) emptySet() else setOf("page-token")
                assertEquals(expectedQueries, request.queryParameterNames)
                assertEquals(pageToken, request.queryParameter("page-token"))
            }
        }
    }

    listOf(2, 1).forEach { count ->
        test("sends $count query entries for each Insights filter") {
            MockWebServer().use { server ->
                server.start()
                val client = server.client()
                val projects = listOf("frontend", "backend").take(count)
                val branches = listOf("main", "release/v1").take(count)
                val workflows = listOf("build", "deploy").take(count)
                repeat(2) { server.enqueue(MockResponse(body = "{}")) }

                client.getOrgSummaryData(orgSlug, projectNames = projects).blockingFirst()
                client.getProjectWorkflowsPageData(projectSlug, branches = branches, workflowNames = workflows)
                    .blockingFirst()

                val orgRequest = server.takeRequest().url
                val projectRequest = server.takeRequest().url
                assertEquals(projects, orgRequest.queryParameterValues("project-names"))
                assertEquals(branches, projectRequest.queryParameterValues("branches"))
                assertEquals(workflows, projectRequest.queryParameterValues("workflow-names"))
            }
        }
    }

    test("omits query entries for unspecified Insights filters") {
        MockWebServer().use { server ->
            server.start()
            val client = server.client()
            repeat(2) { server.enqueue(MockResponse(body = "{}")) }

            client.getOrgSummaryData(orgSlug).blockingFirst()
            client.getProjectWorkflowsPageData(projectSlug).blockingFirst()

            val orgRequest = server.takeRequest().url
            val projectRequest = server.takeRequest().url
            assertEquals(emptyList<String>(), orgRequest.queryParameterValues("project-names"))
            assertEquals(emptyList<String>(), projectRequest.queryParameterValues("branches"))
            assertEquals(emptyList<String>(), projectRequest.queryParameterValues("workflow-names"))
        }
    }
})
