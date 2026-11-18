package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateUsageExportRequest(
    @SerializedName("start") val startDate: String,
    @SerializedName("end") val endDate: String
)
