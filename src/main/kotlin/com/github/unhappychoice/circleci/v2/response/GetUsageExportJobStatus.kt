package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class GetUsageExportJobStatus(
    @SerializedName("usage_export_job_id") val id: String,
    val state: String,
    @SerializedName("download_urls") val downloadUrls: List<String>,
    @SerializedName("error_reason") val errorReason: String? = null
) {
    override fun toString() =
        "GetUsageExportJobStatus(id=$id, state=$state, downloadUrls=[REDACTED], errorReason=$errorReason)"
}
