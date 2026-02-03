package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Job(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("stopped_at") val stoppedAt: String?,
    @SerializedName("approval_request_id") val approvalRequestId: String?,
    @SerializedName("dependencies") val dependencies: List<String>
)

data class JobListResponse(
    val items: List<Job>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
