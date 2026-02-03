package com.github.unhappychoice.circleci.v2.response

data class JobTimeseriesResponse(
    val items: List<JobTimeseriesItem>,
    val next_page_token: String?
) {
    data class JobTimeseriesItem(
        val name: String,
        val min_started_at: String,
        val max_ended_at: String,
        val timestamp: String,
        val metrics: JobMetrics
    )

    data class JobMetrics(
        val total_runs: Long,
        val failed_runs: Long,
        val successful_runs: Long,
        val throughput: Float,
        val median_credits_used: Long,
        val total_credits_used: Long,
        val duration_metrics: DurationMetrics
    )

    data class DurationMetrics(
        val min: Long?,
        val median: Long?,
        val max: Long?,
        val p95: Long?,
        val total: Long?
    )
}