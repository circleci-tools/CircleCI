package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class TriggerPipelineRunRequest(
    @SerializedName("definition_id") val definitionId: String? = null,
    val config: Ref? = null,
    val checkout: Ref? = null,
    @Redacted val parameters: Map<String, Any>? = null
) {
    override fun toString() = redactedToString(this)

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
