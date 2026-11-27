package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectWorkflowsPageData(
    @SerializedName("org_id") val orgId: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("project_data") val projectData: ProjectData,
    @SerializedName("project_workflow_data") val projectWorkflowData: List<ProjectWorkflowData>,
    @SerializedName("project_workflow_branch_data") val projectWorkflowBranchData: List<ProjectWorkflowBranchData>,
    @SerializedName("all_branches") val allBranches: List<String>,
    @SerializedName("all_workflows") val allWorkflows: List<String>
) {
    data class ProjectData(
        val metrics: SummaryMetrics,
        val trends: SummaryTrends
    )

    data class ProjectWorkflowData(
        @SerializedName("workflow_name") val workflowName: String,
        val metrics: WorkflowMetrics,
        val trends: WorkflowTrends
    )

    data class ProjectWorkflowBranchData(
        @SerializedName("workflow_name") val workflowName: String,
        val branch: String,
        val metrics: WorkflowMetrics,
        val trends: WorkflowTrends
    )

    data class WorkflowMetrics(
        @SerializedName("total_runs") val totalRuns: Long,
        @SerializedName("total_credits_used") val totalCreditsUsed: Long,
        @SerializedName("success_rate") val successRate: Float,
        @SerializedName("p95_duration_secs") val p95DurationSecs: Float
    )

    data class WorkflowTrends(
        @SerializedName("total_runs") val totalRuns: Float,
        @SerializedName("total_credits_used") val totalCreditsUsed: Float,
        @SerializedName("success_rate") val successRate: Float,
        @SerializedName("p95_duration_secs") val p95DurationSecs: Float
    )
}
