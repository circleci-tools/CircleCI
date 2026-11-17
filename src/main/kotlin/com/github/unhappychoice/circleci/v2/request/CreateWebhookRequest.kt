package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateWebhookRequest(
    val name: String,
    val url: String,
    @SerializedName("signing_secret") val signingSecret: String,
    @SerializedName("scope") val scope: WebhookScopeRequest,
    val events: List<String>
) {
    override fun toString() =
        "CreateWebhookRequest(name=$name, url=$url, signingSecret=[REDACTED], scope=$scope, events=$events)"
}

data class WebhookScopeRequest(
    val type: String,
    val id: String
)
