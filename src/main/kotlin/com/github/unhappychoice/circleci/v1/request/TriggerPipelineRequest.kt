package com.github.unhappychoice.circleci.v1.request

data class TriggerPipelineRequest(
    val revision: String? = null,
    val branch: String? = null,
    val tag: String? = null
)
