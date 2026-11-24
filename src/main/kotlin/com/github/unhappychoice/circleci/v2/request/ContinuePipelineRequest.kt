package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class ContinuePipelineRequest(
    @SerializedName("continuation-key") val continuationKey: String,
    val configuration: String,
    val parameters: Map<String, Any>?
) {
    override fun toString() =
        "ContinuePipelineRequest(continuationKey=[REDACTED], configuration=[REDACTED], parameters=[REDACTED])"
}
