package com.github.unhappychoice.circleci.v2.request

import com.squareup.moshi.Json

data class CreateURLOrbAllowListEntryRequest(
    val name: String,
    val prefix: String,
    val auth: String
)