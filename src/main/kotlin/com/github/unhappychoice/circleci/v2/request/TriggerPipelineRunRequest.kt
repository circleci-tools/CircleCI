package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class TriggerPipelineRunRequest(
    @SerializedName("definition_id") val definitionId: String? = null,
    val config: Ref? = null,
    val checkout: Ref? = null,
    val parameters: Map<String, Any>? = null
) {
    data class Ref(val branch: String? = null, val tag: String? = null) {
        init {
            require((branch != null) xor (tag != null)) { "Specify exactly one of branch or tag" }
        }
    }

    init {
        require(config == null || checkout == null || (config.branch != null) == (checkout.branch != null)) {
            "Config and checkout must use the same reference type"
        }
    }
}
