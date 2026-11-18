package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class PipelineDefinitionRepository(@SerializedName("external_id") val externalId: String)
