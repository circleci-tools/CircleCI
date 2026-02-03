package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class DecisionLog(
    val id: String,
    @SerializedName("created_at") val createdAt: String?,
    val decision: String,
    val metadata: Map<String, Any>?,
    val policies: Map<String, String>?,
    @SerializedName("time_taken_ms") val timeTakenMs: Long?
)

data class DecisionLogListResponse(
    val items: List<DecisionLog>,
    @SerializedName("next_page_token") val nextPageToken: String?
)