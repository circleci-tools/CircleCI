package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateUsageExportRequest(
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String
)
