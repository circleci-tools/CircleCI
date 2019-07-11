package com.github.unhappychoice.circleci.response

data class Workflow(
    val jobId: String?,
    val jobName: String?,
    val workflowId: String?,
    val workflowName: String?,
    val workspaceId: String?
)
