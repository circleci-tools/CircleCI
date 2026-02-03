package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class MakeDecisionRequest(
    val input: String,
    val metadata: Map<String, Any>?
)
