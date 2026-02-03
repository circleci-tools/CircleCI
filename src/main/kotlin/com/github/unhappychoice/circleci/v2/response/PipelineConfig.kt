package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class PipelineConfig(
    val source: String,
    val compiled: String,
    @SerializedName("setup-config") val setupConfig: String?,
    @SerializedName("compiled-setup-config") val compiledSetupConfig: String?
)