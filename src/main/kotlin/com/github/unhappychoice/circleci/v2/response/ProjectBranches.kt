package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectBranches(
    @SerializedName("org_id") val orgId: String?,
    @SerializedName("project_id") val projectId: String?,
    val branches: List<String>
)
