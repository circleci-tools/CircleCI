package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class OrgSummaryData(
    @SerializedName("org_data") val orgData: OrgData,
    @SerializedName("org_project_data") val orgProjectData: List<OrgProjectData>,
    @SerializedName("all_projects") val allProjects: List<String>?
) {
    data class OrgData(
        val metrics: SummaryMetrics,
        val trends: SummaryTrends
    )

    data class OrgProjectData(
        @SerializedName("project_name") val projectName: String,
        val metrics: ProjectMetrics,
        val trends: ProjectTrends
    )

    data class ProjectMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("total_duration_secs") val totalDurationSecs: Long,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long,
        @SerializedName("success_rate") val successRate: Float
    )

    data class ProjectTrends(
        @SerializedName("total_runs") val totalRuns: Float,
        @SerializedName("total_duration_secs") val totalDurationSecs: Float,
        @SerializedName("total_credits_used") val totalCreditsUsed: Float,
        @SerializedName("success_rate") val successRate: Float
    )
}
