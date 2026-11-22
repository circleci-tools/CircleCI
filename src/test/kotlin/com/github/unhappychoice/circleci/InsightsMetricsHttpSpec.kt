package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.JobMetricsResponse
import com.github.unhappychoice.circleci.v2.response.WorkflowMetricsResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsMetricsHttpSpec : Spek({
    val projectSlug = "gh/example/project"

    listOf(null, 0L).forEach { value ->
        val deviation = value?.toFloat()

        test("decodes workflow list statistics with value $value") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(metricsResponse(value, workflow = true))
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

                val metrics = client.getProjectWorkflowMetrics(projectSlug).blockingSingle().items.single().metrics

                assertEquals(
                    WorkflowMetricsResponse.DurationMetrics(value, value, value, value, value, deviation),
                    metrics.durationMetrics
                )
                assertEquals(value, metrics.mttr)
                assertEquals(value, metrics.totalCreditsUsed)
                assertEquals(value, metrics.totalRecoveries)
            }
        }

        test("decodes workflow job list duration statistics with value $value") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(metricsResponse(value, workflow = false))
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

                val response = client.getProjectWorkflowJobMetrics(projectSlug, "build").blockingSingle()

                assertEquals(
                    JobMetricsResponse.DurationMetrics(value, value, value, value, value, deviation),
                    response.items.single().metrics.durationMetrics
                )
            }
        }
    }
})

private fun metricsResponse(value: Long?, workflow: Boolean): MockResponse {
    val recoveryMetrics = if (workflow) """"mttr": $value, "total_recoveries": $value,""" else ""
    val credits = if (workflow) value else 0L
    return MockResponse.Builder().addHeader("Content-Type", "application/json").body(
        """
        {
          "items": [{
            "name": "build",
            "metrics": {
              "total_runs": 1,
              "successful_runs": 1,
              "failed_runs": 0,
              "success_rate": 1.0,
              "throughput": 1.0,
              $recoveryMetrics
              "total_credits_used": $credits,
              "duration_metrics": {
                "min": $value,
                "mean": $value,
                "median": $value,
                "p95": $value,
                "max": $value,
                "standard_deviation": ${value?.toFloat()}
              }
            }
          }],
          "next_page_token": null
        }
        """.trimIndent()
    ).build()
}
