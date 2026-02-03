package com.github.unhappychoice.circleci.v2.response

data class JobDetails(
    val name: String,
    val metrics: List<Metric>,
    val trends: List<Trend>
) {
    data class Metric(
        val name: String,
        val value: Double
    )

    data class Trend(
        val name: String,
        val value: Double
    )
}
