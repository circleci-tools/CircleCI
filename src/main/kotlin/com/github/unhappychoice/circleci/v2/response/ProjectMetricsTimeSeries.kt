package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectMetricsTimeSeries(
    val metrics: List<Metric>,
    val trends: List<Trend>
)

data class Metric(
    val name: String,
    val value: Double
)

data class Trend(
    val name: String,
    val value: Double
)
