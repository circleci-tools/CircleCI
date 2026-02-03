package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class EnvironmentVariableListResponse(
    val items: List<EnvironmentVariable>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)