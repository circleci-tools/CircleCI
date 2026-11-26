package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.DurationMetrics
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

private const val WINDOW_START = "2025-01-01T00:00:00Z"
private const val WINDOW_END = "2025-01-08T00:00:00Z"
private const val PROJECT_ID = "11111111-1111-1111-1111-111111111111"

object InsightsMetricsHttpSpec : Spek({
    val projectSlug = "gh/example/project"

    listOf(null, 0L).forEach { value ->
        val deviation = value?.toFloat()

        test("decodes the workflow aggregation window and list statistics with value $value") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(metricsResponse(value, workflow = true))
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

                val item = client.getProjectWorkflowMetrics(projectSlug).blockingSingle().items.single()
                val metrics = item.metrics

                assertEquals(WINDOW_START, item.windowStart)
                assertEquals(WINDOW_END, item.windowEnd)
                assertEquals(PROJECT_ID, item.projectId)
                assertEquals(
                    DurationMetrics(value, value, value, value, value, deviation),
                    metrics.durationMetrics
                )
                assertEquals(value, metrics.mttr)
                assertEquals(value, metrics.totalCreditsUsed)
                assertEquals(value, metrics.totalRecoveries)
            }
        }

        test("decodes the job aggregation window and list duration statistics with value $value") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(metricsResponse(value, workflow = false))
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

                val item = client.getProjectWorkflowJobMetrics(projectSlug, "build").blockingSingle().items.single()

                assertEquals(WINDOW_START, item.windowStart)
                assertEquals(WINDOW_END, item.windowEnd)
                assertEquals(
                    DurationMetrics(value, value, value, value, value, deviation),
                    item.metrics.durationMetrics
                )
            }
        }
    }
})

private fun metricsResponse(value: Long?, workflow: Boolean): MockResponse {
    val recoveryMetrics = if (workflow) """"mttr": $value, "total_recoveries": $value,""" else ""
    val credits = if (workflow) value else 0L
    val projectId = if (workflow) """"project_id": "$PROJECT_ID",""" else ""
    return MockResponse.Builder().setHeader("Content-Type", "application/json").body(
        """
        {
          "items": [{
            "name": "build",
            $projectId
            "window_start": "$WINDOW_START",
            "window_end": "$WINDOW_END",
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
