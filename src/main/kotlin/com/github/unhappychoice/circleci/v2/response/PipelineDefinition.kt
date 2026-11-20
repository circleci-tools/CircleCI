package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class PipelineDefinition(
    val id: String? = null,
    @SerializedName("project_id")
    val projectId: String? = null,
    val name: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val definition: String? = null,
    val description: String? = null,
    @SerializedName("config_source")
    val configSource: ConfigSource? = null,
    @SerializedName("checkout_source")
    val checkoutSource: CheckoutSource? = null
) {
    data class ConfigSource(
        val provider: String? = null,
        val repo: Repository? = null,
        @SerializedName("file_path")
        val filePath: String? = null
    )

    data class CheckoutSource(
        val provider: String? = null,
        val repo: Repository? = null
    )

    data class Repository(
        @SerializedName("full_name")
        val fullName: String? = null,
        @SerializedName("external_id")
        val externalId: String? = null
    )
}
