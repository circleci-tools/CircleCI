package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Collaboration(
    val id: String,
    @SerializedName("vcs-type") val vcsType: String,
    val name: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val slug: String
)
