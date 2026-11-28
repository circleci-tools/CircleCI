package com.github.unhappychoice.circleci.v2.request

data class MakeDecisionRequest(
    val input: String,
    val metadata: Map<String, Any>?
)
