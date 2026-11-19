package com.github.unhappychoice.circleci.v2.response

data class BundleDiff(
    val created: List<String> = emptyList(),
    val deleted: List<String> = emptyList(),
    val modified: List<String> = emptyList()
)
