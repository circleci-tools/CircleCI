package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class JobTimeseriesResponse(
    val items: List<JobTimeseriesItem>,
    @SerializedName("next_page_token") val next_page_token: String?
) {
    val nextPageToken: String? get() = next_page_token

    data class JobTimeseriesItem(
        val name: String,
        @SerializedName("min_started_at") val min_started_at: String,
        @SerializedName("max_ended_at") val max_ended_at: String,
        val timestamp: String,
        val metrics: JobMetrics
    ) {
        val minStartedAt: String get() = min_started_at
        val maxEndedAt: String get() = max_ended_at
    }

    data class JobMetrics(
        @SerializedName("total_runs") val total_runs: Long,
        @SerializedName("failed_runs") val failed_runs: Long,
        @SerializedName("successful_runs") val successful_runs: Long,
        val throughput: Float,
        @SerializedName("median_credits_used") val median_credits_used: Long,
        @SerializedName("total_credits_used") val total_credits_used: Long,
        @SerializedName("duration_metrics") val duration_metrics: DurationMetrics
    ) {
        val totalRuns: Long get() = total_runs
        val failedRuns: Long get() = failed_runs
        val successfulRuns: Long get() = successful_runs
        val medianCreditsUsed: Long get() = median_credits_used
        val totalCreditsUsed: Long get() = total_credits_used
        val durationMetrics: DurationMetrics get() = duration_metrics
    }

    data class DurationMetrics(
        val min: Long?,
        val median: Long?,
        val max: Long?,
        val p95: Long?,
        val total: Long?
    )
}
