package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateContextRestrictionRequest(
    @SerializedName("restriction_type") val restrictionType: String,
    @SerializedName("restriction_value") val restrictionValue: String
)
