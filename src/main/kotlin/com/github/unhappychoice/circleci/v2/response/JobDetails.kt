package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class JobDetails(
    val name: String,
    val metrics: List<Metric>,
    val trends: List<Trend>
)
