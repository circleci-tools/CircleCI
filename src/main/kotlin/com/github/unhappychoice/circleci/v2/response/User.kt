package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val login: String,
    val name: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)
