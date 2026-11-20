package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

/**
 * @property enableSsh Enables SSH access for the triggering user on the rerun jobs.
 * Requires selected job IDs in [jobs] and is mutually exclusive with `from_failed` ([fromFailed]).
 */
data class RerunWorkflowRequest(
    val jobs: List<String>? = null,
    @SerializedName("from_failed") val fromFailed: Boolean? = null,
    @SerializedName("sparse_tree") val sparseTree: Boolean? = null,
    @SerializedName("enable_ssh") val enableSsh: Boolean? = null
)
