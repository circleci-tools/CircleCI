package com.github.unhappychoice.circleci.v2.response

data class PipelineValues(
    val values: Map<String, Any>
) {
    override fun toString(): String = "PipelineValues(values=<redacted>)"
}
