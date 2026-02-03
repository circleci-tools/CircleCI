package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class WorkflowRun(
    val id: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("stopped_at") val stoppedAt: String?,
    @SerializedName("duration") val duration: Int,
    @SerializedName("credits_used") val creditsUsed: Int,
    @SerializedName("is_rerun") val isRerun: Boolean
)
