package com.github.unhappychoice.circleci.v1.request

data class AddEnvironmentVariableRequest(
    val name: String,
    val value: String
)
