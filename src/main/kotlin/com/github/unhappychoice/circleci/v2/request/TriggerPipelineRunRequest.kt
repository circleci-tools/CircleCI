package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class TriggerPipelineRunRequest(
    @SerializedName("definition_id") val definitionId: String? = null,
    val config: Ref? = null,
    val checkout: Ref? = null,
    val parameters: Map<String, Any>? = null
) {
    data class Ref(val branch: String? = null, val tag: String? = null)
}
