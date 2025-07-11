package com.github.unhappychoice.circleci.v2.response

data class JSONDuration(
    val value: String // This should be a string matching the pattern "^([0-9]+(ms|s|m|h|d|w)){1,7}$"
)
