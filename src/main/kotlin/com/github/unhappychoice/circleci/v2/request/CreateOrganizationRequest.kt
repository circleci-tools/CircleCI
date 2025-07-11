package com.github.unhappychoice.circleci.v2.request

import com.squareup.moshi.Json

data class CreateOrganizationRequest(
    val name: String,
    @Json(name = "vcs_type") val vcsType: String
)
