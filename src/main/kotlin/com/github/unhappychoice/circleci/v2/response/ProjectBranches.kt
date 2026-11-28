package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectBranches(
    @SerializedName("org_id") val org_id: String?,
    @SerializedName("project_id") val project_id: String?,
    val branches: List<String>
) {
    val orgId: String? get() = org_id
    val projectId: String? get() = project_id
}
