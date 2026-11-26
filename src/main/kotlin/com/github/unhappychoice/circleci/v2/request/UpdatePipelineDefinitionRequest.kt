package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class UpdatePipelineDefinitionRequest(
    val name: String? = null,
    @SerializedName("config_source") val configSource: ConfigSource? = null,
    @SerializedName("checkout_source") val checkoutSource: CheckoutSource? = null,
    val description: String? = null
) {
    data class ConfigSource(@SerializedName("file_path") val filePath: String? = null)

    data class CheckoutSource(val provider: String? = null, val repo: PipelineDefinitionRepository? = null)
}
