package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class PipelineListResponse(
    val items: List<Pipeline>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)