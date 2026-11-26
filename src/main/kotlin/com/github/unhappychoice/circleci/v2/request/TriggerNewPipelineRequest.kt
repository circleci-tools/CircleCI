package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class TriggerNewPipelineRequest(
    val branch: String? = null,
    val tag: String? = null,
    @SerializedName("parameters") @Redacted val parameters: Map<String, Any>? = null
) {
    override fun toString() = redactedToString(this)
}
