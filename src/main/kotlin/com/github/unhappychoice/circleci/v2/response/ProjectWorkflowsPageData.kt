package com.github.unhappychoice.circleci.v2.response

data class ProjectWorkflowsPageData(
    val org_id: String,
    val project_id: String,
    val project_data: ProjectData,
    val project_workflow_data: List<ProjectWorkflowData>,
    val project_workflow_branch_data: List<ProjectWorkflowBranchData>,
    val all_branches: List<String>,
    val all_workflows: List<String>
) {
    data class ProjectData(
        val metrics: Metrics,
        val trends: Trends
    )

    data class ProjectWorkflowData(
        val workflow_name: String,
        val metrics: Metrics,
        val trends: Trends
    )

    data class ProjectWorkflowBranchData(
        val workflow_name: String,
        val branch: String,
        val metrics: Metrics,
        val trends: Trends
    )

    data class Metrics(
        val total_runs: Long,
        val total_duration_secs: Long,
        val total_credits_used: Long,
        val success_rate: Float,
        val throughput: Float,
        val p95_duration_secs: Float? = null
    )

    data class Trends(
        val total_runs: Float,
        val total_duration_secs: Float,
        val total_credits_used: Float,
        val success_rate: Float,
        val throughput: Float,
        val p95_duration_secs: Float? = null
    )
}