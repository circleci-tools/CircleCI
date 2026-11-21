package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class UsageExportJob(
    @SerializedName("usage_export_job_id") val id: String,
    val state: String,
    val start: String,
    val end: String,
    @SerializedName("download_urls") val downloadUrls: List<String>
) {
    override fun toString() =
        "UsageExportJob(id=$id, state=$state, start=$start, end=$end, downloadUrls=[REDACTED])"
}
