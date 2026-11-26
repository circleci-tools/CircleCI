package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class PipelineValues(
    @Redacted val values: Map<String, Any>
) {
    override fun toString() = redactedToString(this)
}
