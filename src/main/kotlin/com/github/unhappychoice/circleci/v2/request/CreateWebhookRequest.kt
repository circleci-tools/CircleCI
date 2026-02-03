package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateWebhookRequest(
    val name: String,
    val url: String,
    @SerializedName("signing_secret") val signingSecret: String,
    @SerializedName("scope") val scope: WebhookScopeRequest,
    val events: List<String>
)

data class WebhookScopeRequest(
    val type: String,
    val id: String
)
