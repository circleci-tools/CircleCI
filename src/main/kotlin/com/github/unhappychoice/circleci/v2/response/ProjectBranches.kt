package com.github.unhappychoice.circleci.v2.response

data class ProjectBranches(
    val org_id: String,
    val project_id: String,
    val branches: List<String>
)