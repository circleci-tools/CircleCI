package com.github.unhappychoice.circleci.v2.response

import com.squareup.moshi.Json

data class URLOrbAllowListEntry(
    val id: String,
    val name: String,
    val prefix: String,
    val auth: String
)