package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class UsageExportJob(
    @SerializedName("usage_export_job_id") val id: String,
    val state: String,
    val start: String,
    val end: String,
    @SerializedName("download_urls") @Redacted val downloadUrls: List<String>
) {
    override fun toString() = redactedToString(this)
}
