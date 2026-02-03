package com.github.unhappychoice.circleci.v2.request

data class CreatePipelineDefinitionRequest(
    val name: String,
    val definition: String
)