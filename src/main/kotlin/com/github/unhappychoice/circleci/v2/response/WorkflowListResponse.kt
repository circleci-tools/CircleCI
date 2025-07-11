package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class WorkflowListResponse(
    val items: List<Workflow>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)