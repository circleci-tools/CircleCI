package com.github.unhappychoice.circleci.v1.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class AddEnvironmentVariableRequest(
    val name: String,
    @Redacted val value: String
) {
    override fun toString() = redactedToString(this)
}
