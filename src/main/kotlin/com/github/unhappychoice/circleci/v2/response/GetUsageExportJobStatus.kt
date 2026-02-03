package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class GetUsageExportJobStatus(
    val id: String,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("download_url")
    val downloadUrl: String?
)