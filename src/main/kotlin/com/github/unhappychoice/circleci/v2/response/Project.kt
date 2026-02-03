package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Project(
    val slug: String,
    val name: String,
    val id: String,
    @SerializedName("organization_name")
    val organizationName: String,
    @SerializedName("organization_slug")
    val organizationSlug: String,
    @SerializedName("organization_id")
    val organizationId: String,
    @SerializedName("vcs_info")
    val vcsInfo: VcsInfo
) {
    data class VcsInfo(
        @SerializedName("vcs_url")
        val vcsUrl: String,
        val provider: String,
        @SerializedName("default_branch")
        val defaultBranch: String
    )
}

data class ProjectEnvironmentVariable(
    val name: String,
    val value: String
)

data class ProjectEnvironmentVariableListResponse(
    val items: List<ProjectEnvironmentVariable>,
    @SerializedName("next_page_token") val nextPageToken: String?
)