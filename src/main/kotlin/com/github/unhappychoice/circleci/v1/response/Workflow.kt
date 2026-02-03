package com.github.unhappychoice.circleci.v1.response

data class Workflow(
    val jobId: String?,
    val jobName: String?,
    val workflowId: String?,
    val workflowName: String?,
    val workspaceId: String?
)
