package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class JobTimeseriesResponse(
    val items: List<JobTimeseriesItem>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class JobTimeseriesItem(
        val name: String,
        @SerializedName("min_started_at") val minStartedAt: String,
        @SerializedName("max_ended_at") val maxEndedAt: String,
        val timestamp: String,
        val metrics: JobMetrics
    )

    data class JobMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("failed_runs") val failedRuns: Long,
        @SerializedName("successful_runs") val successfulRuns: Long,
        val throughput: Float,
        @SerializedName("median_credits_used") val medianCreditsUsed: Long,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long,
        @SerializedName("duration_metrics") val durationMetrics: DurationMetrics
    )

    data class DurationMetrics(
        val min: Long?,
        val median: Long?,
        val max: Long?,
        val p95: Long?,
        val total: Long?
    )
}
