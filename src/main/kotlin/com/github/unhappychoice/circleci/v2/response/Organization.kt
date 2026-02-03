package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Organization(
    val id: String,
    val name: String,
    val slug: String,
    @SerializedName("vcs_type")
    val vcsType: String
)
