package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class TriggerNewPipelineRequest(
    val branch: String? = null,
    val tag: String? = null,
    @SerializedName("parameters") val parameters: Map<String, Any>? = null
)
