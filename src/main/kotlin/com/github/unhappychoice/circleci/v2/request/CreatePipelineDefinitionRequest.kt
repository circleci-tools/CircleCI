package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreatePipelineDefinitionRequest(
    val name: String,
    @SerializedName("config_source") val configSource: ConfigSource,
    @SerializedName("checkout_source") val checkoutSource: CheckoutSource,
    val description: String? = null
) {
    data class ConfigSource(
        val provider: String,
        @SerializedName("file_path") val filePath: String,
        val repo: PipelineDefinitionRepository? = null
    )

    data class CheckoutSource(val provider: String, val repo: PipelineDefinitionRepository)
}
