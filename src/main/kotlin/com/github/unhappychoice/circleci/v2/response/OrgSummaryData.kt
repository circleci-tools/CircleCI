package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class OrgSummaryData(
    @SerializedName("org_data") val org_data: OrgData,
    @SerializedName("org_project_data") val org_project_data: List<OrgProjectData>,
    @SerializedName("all_projects") val all_projects: List<String>?
) {
    val orgData: OrgData get() = org_data
    val orgProjectData: List<OrgProjectData> get() = org_project_data
    val allProjects: List<String>? get() = all_projects

    data class OrgData(
        val metrics: SummaryMetrics,
        val trends: SummaryTrends
    )

    data class OrgProjectData(
        @SerializedName("project_name") val project_name: String,
        val metrics: ProjectMetrics,
        val trends: ProjectTrends
    ) {
        val projectName: String get() = project_name
    }

    data class ProjectMetrics(
        @SerializedName("total_runs") val total_runs: Long,
        @SerializedName("total_duration_secs") val total_duration_secs: Long,
        @SerializedName("total_credits_used") val total_credits_used: Long,
        @SerializedName("success_rate") val success_rate: Float
    ) {
        val totalRuns: Long get() = total_runs
        val totalDurationSecs: Long get() = total_duration_secs
        val totalCreditsUsed: Long get() = total_credits_used
        val successRate: Float get() = success_rate
    }

    data class ProjectTrends(
        @SerializedName("total_runs") val total_runs: Float,
        @SerializedName("total_duration_secs") val total_duration_secs: Float,
        @SerializedName("total_credits_used") val total_credits_used: Float,
        @SerializedName("success_rate") val success_rate: Float
    ) {
        val totalRuns: Float get() = total_runs
        val totalDurationSecs: Float get() = total_duration_secs
        val totalCreditsUsed: Float get() = total_credits_used
        val successRate: Float get() = success_rate
    }
}
