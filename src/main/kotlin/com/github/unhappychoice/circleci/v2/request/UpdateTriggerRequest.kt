package com.github.unhappychoice.circleci.v2.request

data class UpdateTriggerRequest(
    val name: String?,
    val description: String?,
    val type: String?,
    val parameters: Map<String, Any>?
)