package com.github.unhappychoice.circleci.v2.response

data class OrgSummaryData(
    val org_data: OrgData,
    val org_project_data: List<OrgProjectData>,
    val all_projects: List<String>?
) {
    data class OrgData(
        val metrics: Metrics,
        val trends: Trends
    )

    data class OrgProjectData(
        val project_name: String,
        val metrics: Metrics,
        val trends: Trends
    )

    data class Metrics(
        val total_runs: Long,
        val total_duration_secs: Long,
        val total_credits_used: Long,
        val success_rate: Float,
        val throughput: Float
    )

    data class Trends(
        val total_runs: Float,
        val total_duration_secs: Float,
        val total_credits_used: Float,
        val success_rate: Float,
        val throughput: Float
    )
}