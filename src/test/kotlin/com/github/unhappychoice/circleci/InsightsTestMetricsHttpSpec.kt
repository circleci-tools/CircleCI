package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsTestMetricsHttpSpec : Spek({
    test("decodes class names for the most failed and slowest tests") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse(body = """
                {
                  "average_test_count": 2,
                  "most_failed_tests": [{
                    "classname": "CalculatorTest",
                    "test_name": "addsNumbers",
                    "file": "CalculatorTest.kt",
                    "failed_runs": 1,
                    "total_runs": 2,
                    "p95_duration": 0.1,
                    "flaky": true,
                    "source": "junit",
                    "job_name": "test"
                  }],
                  "most_failed_tests_extra": 0,
                  "slowest_tests": [{
                    "classname": "ReportTest",
                    "test_name": "generatesReport",
                    "file": "ReportTest.kt",
                    "failed_runs": 0,
                    "total_runs": 2,
                    "p95_duration": 2.5,
                    "flaky": false,
                    "source": "junit",
                    "job_name": "test"
                  }],
                  "slowest_tests_extra": 0,
                  "total_test_runs": 4,
                  "test_runs": []
                }
            """.trimIndent()))
            val response = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
                .getProjectWorkflowTestMetrics("gh/example/project", "ci")
                .blockingSingle()

            assertAll(
                { assertEquals("CalculatorTest", response.mostFailedTests.single().className) },
                { assertEquals("ReportTest", response.slowestTests.single().className) }
            )
        }
    }
})
