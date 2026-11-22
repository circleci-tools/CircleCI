package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Component(
    val id: String,
    val name: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val labels: List<ComponentLabel>? = null,
    @SerializedName("release_count") val releaseCount: Int? = null
)

data class ComponentLabel(
    val key: String?,
    val value: String?
)

data class ComponentListResponse(
    val items: List<Component>,
    @SerializedName("next_page_token") val nextPageToken: String?
)

data class ComponentVersion(
    val name: String,
    @SerializedName("environment_id") val environmentId: String,
    val namespace: String,
    @SerializedName("is_live") val isLive: Boolean,
    @SerializedName("pipeline_id") val pipelineId: String,
    @SerializedName("workflow_id") val workflowId: String,
    @SerializedName("job_id") val jobId: String,
    @SerializedName("job_number") val jobNumber: Int,
    @SerializedName("last_deployed_at") val lastDeployedAt: String
) {
    val version: String get() = name
}

data class ComponentVersionListResponse(
    val items: List<ComponentVersion>,
    @SerializedName("next_page_token") val nextPageToken: String?
)

data class DeployEnvironment(
    val id: String,
    val name: String,
    @SerializedName("organization_id") val organizationId: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val description: String? = null,
    val labels: List<DeployEnvironmentLabel>? = null
)

data class DeployEnvironmentLabel(
    val key: String?,
    val value: String?
)

data class DeployEnvironmentListResponse(
    val items: List<DeployEnvironment>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
