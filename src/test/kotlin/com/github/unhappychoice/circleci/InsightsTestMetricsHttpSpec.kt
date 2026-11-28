package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsTestMetricsHttpSpec : Spek({
    test("preserves ranked test metrics and nullable values") {
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
                  }, {
                    "classname": null,
                    "test_name": "subtractsNumbers",
                    "file": "CalculatorTest.kt",
                    "failed_runs": 1,
                    "total_runs": 2,
                    "flaky": false,
                    "job_name": "test-linux"
                  }, {
                    "classname": null,
                    "test_name": "subtractsNumbers",
                    "file": "CalculatorTest.kt",
                    "failed_runs": 1,
                    "total_runs": 2,
                    "p95_duration": 0,
                    "flaky": false,
                    "source": null,
                    "job_name": "test-windows"
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
                  }, {
                    "classname": null,
                    "test_name": "exportsReport",
                    "file": "ReportTest.kt",
                    "failed_runs": 1,
                    "total_runs": 3,
                    "p95_duration": null,
                    "flaky": true,
                    "source": null,
                    "job_name": "test-linux"
                  }, {
                    "classname": null,
                    "test_name": "exportsReport",
                    "file": "ReportTest.kt",
                    "failed_runs": 0,
                    "total_runs": 1,
                    "p95_duration": 0,
                    "flaky": false,
                    "job_name": "test-windows"
                  }],
                  "slowest_tests_extra": 0,
                  "total_test_runs": 4,
                  "test_runs": []
                }
            """.trimIndent()))
            val response = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
                .getProjectWorkflowTestMetrics("gh/example/project", "ci")
                .blockingSingle()

            val failedTests = response.mostFailedTests
            val slowTests = response.slowestTests
            val expectedJobs = listOf("test", "test-linux", "test-windows")
            val failedCopy = failedTests.first().copy(jobName = "copy")
            val slowCopy = slowTests.first().copy(jobName = "copy")
            assertAll(
                { assertEquals(failedTests.first().copy(jobName = "copy", source = "junit"), failedCopy) },
                { assertEquals(slowTests.first().copy(jobName = "copy", source = "junit"), slowCopy) },
                { assertEquals(listOf("CalculatorTest", null, null), failedTests.map { it.className }) },
                { assertEquals(listOf("ReportTest", null, null), slowTests.map { it.className }) },
                { assertEquals(expectedJobs, failedTests.map { it.jobName }) },
                { assertEquals(expectedJobs, slowTests.map { it.jobName }) },
                { assertEquals(listOf(1L, 1L, 1L), failedTests.map { it.failedRuns }) },
                { assertEquals(listOf(0L, 1L, 0L), slowTests.map { it.failedRuns }) },
                { assertEquals(listOf(2L, 2L, 2L), failedTests.map { it.totalRuns }) },
                { assertEquals(listOf(2L, 3L, 1L), slowTests.map { it.totalRuns }) },
                { assertEquals(listOf(0.1, null, 0.0), failedTests.map { it.p95Duration }) },
                { assertEquals(listOf(2.5f, null, 0f), slowTests.map { it.p95Duration }) },
                { assertEquals(listOf(true, false, false), failedTests.map { it.flaky }) },
                { assertEquals(listOf(false, true, false), slowTests.map { it.flaky }) },
                { assertEquals(listOf("junit", null, null), failedTests.map { it.source }) },
                { assertEquals(listOf("junit", null, null), slowTests.map { it.source }) }
            )
        }
    }
})
