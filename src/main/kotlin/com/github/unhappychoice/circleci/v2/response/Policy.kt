package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Policy(
    val name: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("created_by") val createdBy: String? = null
)
