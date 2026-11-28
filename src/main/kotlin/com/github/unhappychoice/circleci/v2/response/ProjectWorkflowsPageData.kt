package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectWorkflowsPageData(
    @SerializedName("org_id") val org_id: String,
    @SerializedName("project_id") val project_id: String,
    @SerializedName("project_data") val project_data: ProjectData,
    @SerializedName("project_workflow_data") val project_workflow_data: List<ProjectWorkflowData>,
    @SerializedName("project_workflow_branch_data") val project_workflow_branch_data: List<ProjectWorkflowBranchData>,
    @SerializedName("all_branches") val all_branches: List<String>,
    @SerializedName("all_workflows") val all_workflows: List<String>
) {
    val orgId: String get() = org_id
    val projectId: String get() = project_id
    val projectData: ProjectData get() = project_data
    val projectWorkflowData: List<ProjectWorkflowData> get() = project_workflow_data
    val projectWorkflowBranchData: List<ProjectWorkflowBranchData> get() = project_workflow_branch_data
    val allBranches: List<String> get() = all_branches
    val allWorkflows: List<String> get() = all_workflows

    data class ProjectData(
        val metrics: SummaryMetrics,
        val trends: SummaryTrends
    )

    data class ProjectWorkflowData(
        @SerializedName("workflow_name") val workflow_name: String,
        val metrics: WorkflowMetrics,
        val trends: WorkflowTrends
    ) {
        val workflowName: String get() = workflow_name
    }

    data class ProjectWorkflowBranchData(
        @SerializedName("workflow_name") val workflow_name: String,
        val branch: String,
        val metrics: WorkflowMetrics,
        val trends: WorkflowTrends
    ) {
        val workflowName: String get() = workflow_name
    }

    data class WorkflowMetrics(
        @SerializedName("total_runs") val total_runs: Long,
        @SerializedName("total_credits_used") val total_credits_used: Long,
        @SerializedName("success_rate") val success_rate: Float,
        @SerializedName("p95_duration_secs") val p95_duration_secs: Float
    ) {
        val totalRuns: Long get() = total_runs
        val totalCreditsUsed: Long get() = total_credits_used
        val successRate: Float get() = success_rate
        val p95DurationSecs: Float get() = p95_duration_secs
    }

    data class WorkflowTrends(
        @SerializedName("total_runs") val total_runs: Float,
        @SerializedName("total_credits_used") val total_credits_used: Float,
        @SerializedName("success_rate") val success_rate: Float,
        @SerializedName("p95_duration_secs") val p95_duration_secs: Float
    ) {
        val totalRuns: Float get() = total_runs
        val totalCreditsUsed: Float get() = total_credits_used
        val successRate: Float get() = success_rate
        val p95DurationSecs: Float get() = p95_duration_secs
    }
}
