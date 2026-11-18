package com.github.unhappychoice.circleci.v2.request

data class CreateEnvVarRequest(
    val value: String
) {
    override fun toString() = "CreateEnvVarRequest(value=[REDACTED])"
}
