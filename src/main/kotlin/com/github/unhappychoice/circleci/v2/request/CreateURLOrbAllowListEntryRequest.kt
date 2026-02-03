package com.github.unhappychoice.circleci.v2.request

data class CreateURLOrbAllowListEntryRequest(
    val name: String,
    val prefix: String,
    val auth: String
)