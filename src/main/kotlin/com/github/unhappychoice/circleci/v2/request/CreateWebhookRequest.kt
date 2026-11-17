package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateWebhookRequest(
    val name: String,
    val url: String,
    @SerializedName("signing-secret") val signingSecret: String,
    @SerializedName("scope") val scope: WebhookScopeRequest,
    val events: List<String>,
    @SerializedName("verify-tls") val verifyTls: Boolean = true
)

data class WebhookScopeRequest(
    val type: String,
    val id: String
)
