package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class Context(
    val id: String,
    val name: String,
    @SerializedName("created_at") val createdAt: OffsetDateTime
)

data class ContextListResponse(
    val items: List<Context>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
