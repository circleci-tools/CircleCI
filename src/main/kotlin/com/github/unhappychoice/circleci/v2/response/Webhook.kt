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
    @SerializedName("events") val events: List<String>,
    @SerializedName("is_enabled") val isEnabled: Boolean? = null,
    @SerializedName("verify_tls") val verifyTls: Boolean? = null,
    @SerializedName("auto_disabled_at") val autoDisabledAt: String? = null
) {
    override fun toString() =
        "Webhook(id=$id, name=$name, url=$url, createdAt=$createdAt, updatedAt=$updatedAt, " +
            "signingSecret=[REDACTED], scope=$scope, events=$events, isEnabled=$isEnabled, " +
            "verifyTls=$verifyTls, autoDisabledAt=$autoDisabledAt)"
}

data class WebhookScope(
    val type: String,
    val id: String
)
