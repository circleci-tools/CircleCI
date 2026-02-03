package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class WorkflowMetricsResponse(
    val items: List<WorkflowMetric>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class WorkflowMetric(
        val name: String,
        val metrics: WorkflowMetrics
    )

    data class WorkflowMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        @SerializedName("mttr") val mttr: Long,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("success_rate") val successRate: Float,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics,
        @SerializedName("total_recoveries") val totalRecoveries: Long,
        val throughput: Float
    )

    data class DurationMetrics(
        val min: Long,
        val mean: Long,
        val median: Long,
        val p95: Long,
        val max: Long,
        @SerializedName("standard_deviation") val standardDeviation: Float
    )
}

data class WorkflowRunsResponse(
    val items: List<WorkflowRunItem>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class WorkflowRunItem(
        val id: String,
        @SerializedName("branch") val branch: String?,
        @SerializedName("duration") val duration: Long,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("stopped_at") val stoppedAt: String?,
        @SerializedName("credits_used") val creditsUsed: Long,
        val status: String
    )
}

data class JobMetricsResponse(
    val items: List<JobMetric>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class JobMetric(
        val name: String,
        val metrics: JobMetrics
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

    data class DurationMetrics(
        val min: Long,
        val mean: Long,
        val median: Long,
        val p95: Long,
        val max: Long,
        @SerializedName("standard_deviation") val standardDeviation: Float
    )
}

data class WorkflowSummary(
    val metrics: Metrics,
    val trends: Trends
) {
    data class Metrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        @SerializedName("success_rate") val successRate: Float,
        val throughput: Float,
        @SerializedName("mttr") val mttr: Long,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long
    )

    data class Trends(
        @SerializedName("total_runs") val totalRuns: Float,
        @SerializedName("failed_runs") val failedRuns: Float,
        @SerializedName("successful_runs") val successfulRuns: Float,
        @SerializedName("success_rate") val successRate: Float,
        val throughput: Float,
        @SerializedName("mttr") val mttr: Float,
        @SerializedName("total_credits_used") val totalCreditsUsed: Float
    )

    data class DurationMetrics(
        val min: Long,
        val mean: Long,
        val median: Long,
        val p95: Long,
        val max: Long,
        @SerializedName("standard_deviation") val standardDeviation: Float
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
        @SerializedName("class_name") val className: String,
        val file: String?,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("total_runs") val totalRuns: Long
    )

    data class SlowTest(
        @SerializedName("test_name") val testName: String,
        @SerializedName("class_name") val className: String,
        val file: String?,
        @SerializedName("p95_duration") val p95Duration: Float
    )

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
