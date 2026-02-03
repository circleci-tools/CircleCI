package com.github.unhappychoice.circleci.v2.response

import com.squareup.moshi.Json

data class Collaboration(
    val id: String,
    @Json(name = "vcs-type") val vcsType: String,
    val name: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    val slug: String
)
