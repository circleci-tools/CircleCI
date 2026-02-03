package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Workflow(
    val id: String,
    val name: String,
    @SerializedName("project_slug") val projectSlug: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("stopped_at") val stoppedAt: String?,
    @SerializedName("pipeline_id") val pipelineId: String?,
    @SerializedName("pipeline_number") val pipelineNumber: Int?,
    @SerializedName("started_by") val startedBy: String,
    @SerializedName("canceled_by") val canceledBy: String?,
    @SerializedName("errored_by") val erroredBy: String?,
    val tag: String?
)

data class WorkflowId(
    @SerializedName("workflow_id") val workflowId: String
)