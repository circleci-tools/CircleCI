package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.DurationMetrics
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object WorkflowSummaryResponseSpec : Spek({
    listOf(null to 0L, 0L to null).forEach { (firstMetric, secondMetric) ->
        test("decodes workflow summary fields with mttr $firstMetric and credits $secondMetric") {
            val standardDeviation = secondMetric?.toFloat()
            val windowStart = "2025-01-01T00:00:00Z"
            val windowEnd = "2025-01-03T00:00:00Z"

            MockWebServer().use { server ->
                server.start()
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
                server.enqueue(MockResponse(
                    headers = headersOf("Content-Type", "application/json"),
                    body = """
                    {
                      "metrics": {
                        "total_runs": 5,
                        "successful_runs": 4,
                        "failed_runs": 0,
                        "success_rate": 1.0,
                        "throughput": 2.5,
                        "mttr": $firstMetric,
                        "total_credits_used": $secondMetric,
                        "completed_runs": 4,
                        "window_start": "$windowStart",
                        "window_end": "$windowEnd",
                        "duration_metrics": {
                          "min": $firstMetric,
                          "mean": $secondMetric,
                          "median": $firstMetric,
                          "p95": $secondMetric,
                          "max": $firstMetric,
                          "standard_deviation": $standardDeviation
                        }
                      },
                      "trends": {
                        "total_runs": 0.5,
                        "failed_runs": 0.0,
                        "success_rate": 0.0,
                        "throughput": 0.5,
                        "mttr": 0.0,
                        "total_credits_used": 0.0,
                        "p95_duration_secs": 0.25,
                        "median_duration_secs": -0.5
                      },
                      "workflow_names": ["build", "deploy"]
                    }
                    """.trimIndent()
                ))

                val summary = client.getWorkflowSummary("gh/example/project", "build").blockingSingle()

                assertEquals(firstMetric, summary.metrics.mttr)
                assertEquals(secondMetric, summary.metrics.totalCreditsUsed)
                assertEquals(
                    DurationMetrics(
                        firstMetric, secondMetric, firstMetric, secondMetric, firstMetric, standardDeviation
                    ),
                    summary.metrics.durationMetrics
                )
                assertEquals(4L, summary.metrics.completedRuns)
                assertEquals(windowStart, summary.metrics.windowStart)
                assertEquals(windowEnd, summary.metrics.windowEnd)
                assertEquals(0.25f, summary.trends.p95DurationSecs)
                assertEquals(-0.5f, summary.trends.medianDurationSecs)
                assertEquals(listOf("build", "deploy"), summary.workflowNames)
            }
        }
    }
})
