package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class WebhookListResponse(
    val items: List<Webhook>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)