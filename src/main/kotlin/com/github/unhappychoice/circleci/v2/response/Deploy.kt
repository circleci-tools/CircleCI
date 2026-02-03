package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Component(
    val id: String,
    val name: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ComponentListResponse(
    val items: List<Component>,
    @SerializedName("next_page_token") val nextPageToken: String?
)

data class ComponentVersion(
    val id: String,
    @SerializedName("component_id") val componentId: String,
    val version: String,
    @SerializedName("created_at") val createdAt: String
)

data class ComponentVersionListResponse(
    val items: List<ComponentVersion>,
    @SerializedName("next_page_token") val nextPageToken: String?
)

data class DeployEnvironment(
    val id: String,
    val name: String,
    @SerializedName("organization_id") val organizationId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class DeployEnvironmentListResponse(
    val items: List<DeployEnvironment>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
