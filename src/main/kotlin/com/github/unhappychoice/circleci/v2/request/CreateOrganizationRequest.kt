package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateOrganizationRequest(
    val name: String,
    @SerializedName("vcs_type") val vcsType: String
)
