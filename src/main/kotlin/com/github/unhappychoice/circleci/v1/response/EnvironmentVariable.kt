package com.github.unhappychoice.circleci.v1.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class EnvironmentVariable(
    val name: String,
    @Redacted val value: String
) {
    override fun toString() = redactedToString(this)
}
