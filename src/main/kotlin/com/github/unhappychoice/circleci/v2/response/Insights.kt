package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class DurationMetrics(
    val min: Long?,
    val mean: Long?,
    val median: Long?,
    val p95: Long?,
    val max: Long?,
    @SerializedName("standard_deviation") val standardDeviation: Float?
)

data class SummaryMetrics(
    @SerializedName("total_runs") val total_runs: Long,
    @SerializedName("total_duration_secs") val total_duration_secs: Long,
    @SerializedName("total_credits_used") val total_credits_used: Long,
    @SerializedName("success_rate") val success_rate: Float,
    val throughput: Float
) {
    val totalRuns: Long get() = total_runs
    val totalDurationSecs: Long get() = total_duration_secs
    val totalCreditsUsed: Long get() = total_credits_used
    val successRate: Float get() = success_rate
}

data class SummaryTrends(
    @SerializedName("total_runs") val total_runs: Float,
    @SerializedName("total_duration_secs") val total_duration_secs: Float,
    @SerializedName("total_credits_used") val total_credits_used: Float,
    @SerializedName("success_rate") val success_rate: Float,
    val throughput: Float
) {
    val totalRuns: Float get() = total_runs
    val totalDurationSecs: Float get() = total_duration_secs
    val totalCreditsUsed: Float get() = total_credits_used
    val successRate: Float get() = success_rate
}

data class WorkflowMetricsResponse(
    val items: List<WorkflowMetric>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class WorkflowMetric(
        val name: String,
        val metrics: WorkflowMetrics,
        @SerializedName("project_id") val projectId: String? = null,
        @SerializedName("window_start") val windowStart: String? = null,
        @SerializedName("window_end") val windowEnd: String? = null
    )

    data class WorkflowMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        @SerializedName("mttr") val mttr: Long?,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long?,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("success_rate") val successRate: Float,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics,
        @SerializedName("total_recoveries") val totalRecoveries: Long?,
        val throughput: Float
    )
}

data class WorkflowRunsResponse(
    val items: List<WorkflowRunItem>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class WorkflowRunItem(
        val id: String,
        @SerializedName("branch") val branch: String?,
        @SerializedName("duration") val duration: Long?,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("stopped_at") val stoppedAt: String?,
        @SerializedName("credits_used") val creditsUsed: Long,
        val status: String?,
        @SerializedName("is_approval") val isApproval: Boolean? = null
    )
}

data class JobMetricsResponse(
    val items: List<JobMetric>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class JobMetric(
        val name: String,
        val metrics: JobMetrics,
        @SerializedName("window_start") val windowStart: String? = null,
        @SerializedName("window_end") val windowEnd: String? = null
    )

    data class JobMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long,
        @SerializedName("success_rate") val successRate: Float,
        val throughput: Float
    )
}

data class WorkflowSummary(
    val metrics: Metrics,
    val trends: Trends,
    @SerializedName("workflow_names") val workflowNames: List<String>? = null
) {
    data class Metrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        @SerializedName("success_rate") val successRate: Float,
        val throughput: Float,
        @SerializedName("mttr") val mttr: Long?,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long?,
        @SerializedName("completed_runs") val completedRuns: Long? = null,
        @SerializedName("window_start") val windowStart: String? = null,
        @SerializedName("window_end") val windowEnd: String? = null
    )

    data class Trends(
        @SerializedName("total_runs") val totalRuns: Float,
        @SerializedName("failed_runs") val failedRuns: Float,
        @SerializedName("successful_runs") val successfulRuns: Float,
        @SerializedName("success_rate") val successRate: Float,
        val throughput: Float,
        @SerializedName("mttr") val mttr: Float,
        @SerializedName("total_credits_used") val totalCreditsUsed: Float,
        @SerializedName("p95_duration_secs") val p95DurationSecs: Float? = null,
        @SerializedName("median_duration_secs") val medianDurationSecs: Float? = null
    )
}

data class TestMetricsResponse(
    @SerializedName("average_test_count") val averageTestCount: Long,
    @SerializedName("most_failed_tests") val mostFailedTests: List<FailedTest>,
    @SerializedName("most_failed_tests_extra") val mostFailedTestsExtra: Int,
    @SerializedName("slowest_tests") val slowestTests: List<SlowTest>,
    @SerializedName("slowest_tests_extra") val slowestTestsExtra: Int,
    @SerializedName("total_test_runs") val totalTestRuns: Long,
    @SerializedName("test_runs") val testRuns: List<TestRun>
) {
    data class FailedTest(
        @SerializedName("test_name") val testName: String,
        @SerializedName("classname") val className: String?,
        val file: String?,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("job_name") val jobName: String? = null,
        @SerializedName("p95_duration") val p95Duration: Double? = null,
        val flaky: Boolean = false,
        val source: String? = null
    ) {
        // Retain the original JVM constructor and copy overloads, including default arguments.
        constructor(
            testName: String,
            className: String?,
            file: String?,
            failedRuns: Long,
            totalRuns: Long,
            jobName: String? = null
        ) : this(testName, className, file, failedRuns, totalRuns, jobName, p95Duration = null)

        fun copy(
            testName: String = this.testName,
            className: String? = this.className,
            file: String? = this.file,
            failedRuns: Long = this.failedRuns,
            totalRuns: Long = this.totalRuns,
            jobName: String? = this.jobName
        ): FailedTest = FailedTest(testName, className, file, failedRuns, totalRuns, jobName, p95Duration, flaky, source)
    }

    data class SlowTest(
        @SerializedName("test_name") val testName: String,
        @SerializedName("classname") val className: String?,
        val file: String?,
        @SerializedName("p95_duration") val p95Duration: Float?,
        @SerializedName("job_name") val jobName: String? = null,
        @SerializedName("failed_runs") val failedRuns: Long = 0,
        @SerializedName("total_runs") val totalRuns: Long = 0,
        val flaky: Boolean = false,
        val source: String? = null
    ) {
        // Retain the original JVM constructor and copy overloads, including default arguments.
        constructor(
            testName: String,
            className: String?,
            file: String?,
            p95Duration: Float?,
            jobName: String? = null
        ) : this(testName, className, file, p95Duration, jobName, failedRuns = 0)

        fun copy(
            testName: String = this.testName,
            className: String? = this.className,
            file: String? = this.file,
            p95Duration: Float? = this.p95Duration,
            jobName: String? = this.jobName
        ): SlowTest = SlowTest(testName, className, file, p95Duration, jobName, failedRuns, totalRuns, flaky, source)
    }

    data class TestRun(
        @SerializedName("pipeline_number") val pipelineNumber: Long,
        @SerializedName("workflow_id") val workflowId: String?,
        @SerializedName("success_rate") val successRate: Float,
        @SerializedName("test_counts") val testCounts: TestCounts
    )

    data class TestCounts(
        val error: Long,
        val failure: Long,
        val skipped: Long,
        val success: Long,
        val total: Long
    )
}
