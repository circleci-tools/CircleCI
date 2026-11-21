package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateWebhookRequest(
    val name: String,
    val url: String,
    @SerializedName("signing-secret") val signingSecret: String,
    @SerializedName("scope") val scope: WebhookScopeRequest,
    val events: List<String>,
    @SerializedName("verify-tls") val verifyTls: Boolean = true,
    @SerializedName("is-enabled") val isEnabled: Boolean? = null
) {
    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        name: String,
        url: String,
        signingSecret: String,
        scope: WebhookScopeRequest,
        events: List<String>,
        verifyTls: Boolean = true
    ) : this(name, url, signingSecret, scope, events, verifyTls, isEnabled = null)

    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    fun copy(
        name: String = this.name,
        url: String = this.url,
        signingSecret: String = this.signingSecret,
        scope: WebhookScopeRequest = this.scope,
        events: List<String> = this.events,
        verifyTls: Boolean = this.verifyTls
    ): CreateWebhookRequest = copy(
        name, url, signingSecret, scope, events, verifyTls,
        isEnabled = this.isEnabled
    )

    override fun toString() =
        "CreateWebhookRequest(name=$name, url=[REDACTED], signingSecret=[REDACTED], scope=$scope, " +
            "events=$events, verifyTls=$verifyTls, isEnabled=$isEnabled)"
}

data class WebhookScopeRequest(
    val type: String,
    val id: String
)
