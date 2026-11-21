package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class RestrictionCreated(
    val id: String,
    @SerializedName("restriction_type") val restrictionType: String,
    @SerializedName("restriction_value") val restrictionValue: String,
    val name: String?
)
