package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Webhook(
    val id: String,
    val name: String,
    val url: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("signing_secret") val signingSecret: String,
    @SerializedName("scope") val scope: WebhookScope,
    @SerializedName("events") val events: List<String>
)

data class WebhookScope(
    val type: String,
    val id: String
)
