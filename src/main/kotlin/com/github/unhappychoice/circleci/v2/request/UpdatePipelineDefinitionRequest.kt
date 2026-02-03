package com.github.unhappychoice.circleci.v2.request

data class UpdatePipelineDefinitionRequest(
    val name: String?,
    val definition: String?
)