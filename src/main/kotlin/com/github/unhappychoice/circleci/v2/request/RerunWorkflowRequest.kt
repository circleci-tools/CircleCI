package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class RerunWorkflowRequest(
    val jobs: List<String>? = null,
    @SerializedName("from_failed") val fromFailed: Boolean? = null,
    @SerializedName("sparse_tree") val sparseTree: Boolean? = null
)
