package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectListResponse(
    val items: List<Project>?,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)
