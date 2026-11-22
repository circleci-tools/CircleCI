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
    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        id: String,
        name: String,
        url: String,
        createdAt: String,
        updatedAt: String,
        signingSecret: String,
        scope: WebhookScope,
        events: List<String>
    ) : this(
        id, name, url, createdAt, updatedAt, signingSecret, scope, events,
        isEnabled = null, verifyTls = null, autoDisabledAt = null
    )

    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    fun copy(
        id: String = this.id,
        name: String = this.name,
        url: String = this.url,
        createdAt: String = this.createdAt,
        updatedAt: String = this.updatedAt,
        signingSecret: String = this.signingSecret,
        scope: WebhookScope = this.scope,
        events: List<String> = this.events
    ): Webhook = copy(
        id, name, url, createdAt, updatedAt, signingSecret, scope, events,
        isEnabled = this.isEnabled, verifyTls = this.verifyTls, autoDisabledAt = this.autoDisabledAt
    )

    override fun toString() =
        "Webhook(id=$id, name=$name, url=[REDACTED], createdAt=$createdAt, updatedAt=$updatedAt, " +
            "signingSecret=[REDACTED], scope=$scope, events=$events, isEnabled=$isEnabled, " +
            "verifyTls=$verifyTls, autoDisabledAt=$autoDisabledAt)"
}

data class WebhookScope(
    val type: String,
    val id: String
)
