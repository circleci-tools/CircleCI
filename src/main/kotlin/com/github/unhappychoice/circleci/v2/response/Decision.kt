package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Decision(
    val status: String,
    @SerializedName("enabled_rules") val enabledRules: List<String>? = null,
    @SerializedName("hard_failures") val hardFailures: List<RuleFailure>? = null,
    @SerializedName("soft_failures") val softFailures: List<RuleFailure>? = null,
    val reason: String? = null
) {
    data class RuleFailure(val rule: String, val reason: String)
}
