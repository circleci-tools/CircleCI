package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Trigger(
    val id: String,
    @SerializedName("pipeline_definition_id")
    val pipelineDefinitionId: String,
    @SerializedName("project_id")
    val projectId: String,
    val name: String,
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val type: String,
    val parameters: Map<String, Any>
)