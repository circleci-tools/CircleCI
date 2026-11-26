package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class AddEnvironmentVariableRequest(
    @Redacted val value: String
) {
    override fun toString() = redactedToString(this)
}
