package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class PipelineDefinitionRepo(
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("external_id")
    val externalId: String?
)
