package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class ContinuePipelineRequest(
    @SerializedName("continuation-key") @Redacted val continuationKey: String,
    @Redacted val configuration: String,
    @Redacted val parameters: Map<String, Any>?
) {
    override fun toString() = redactedToString(this)
}
