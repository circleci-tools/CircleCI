package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateUsageExportRequest @JvmOverloads constructor(
    @SerializedName("start") val startDate: String,
    @SerializedName("end") val endDate: String,
    @SerializedName("shared_org_ids") val sharedOrgIds: List<String>? = null
)
