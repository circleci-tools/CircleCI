package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class TriggerPipelineRunRequest(
    @SerializedName("definition_id") val definitionId: String?,
    val config: String?,
    @SerializedName("checkout_keys") val checkoutKeys: List<String>?,
    @SerializedName("trigger_parameters") val triggerParameters: Map<String, Any>?
)
