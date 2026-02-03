package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class PipelineDefinition(
    val id: String,
    @SerializedName("project_id")
    val projectId: String,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val definition: String
)