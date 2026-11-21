package com.github.unhappychoice.circleci.v2.request

data class CreateEnvVarRequest(
    val name: String,
    val value: String
) {
    override fun toString() = "CreateEnvVarRequest(value=[REDACTED])"
}
