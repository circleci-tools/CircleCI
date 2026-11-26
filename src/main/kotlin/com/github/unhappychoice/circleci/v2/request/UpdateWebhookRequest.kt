package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class UpdateWebhookRequest(
    val name: String? = null,
    @Redacted val url: String? = null,
    @SerializedName("signing-secret") @Redacted val signingSecret: String? = null,
    val events: List<String>? = null,
    @SerializedName("verify-tls") val verifyTls: Boolean? = null,
    @SerializedName("is-enabled") val isEnabled: Boolean? = null
) {
    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        name: String? = null,
        url: String? = null,
        signingSecret: String? = null,
        events: List<String>? = null,
        verifyTls: Boolean? = null
    ) : this(name, url, signingSecret, events, verifyTls, isEnabled = null)

    @Deprecated("Retained for JVM binary compatibility", level = DeprecationLevel.HIDDEN)
    fun copy(
        name: String? = this.name,
        url: String? = this.url,
        signingSecret: String? = this.signingSecret,
        events: List<String>? = this.events,
        verifyTls: Boolean? = this.verifyTls
    ): UpdateWebhookRequest = copy(
        name, url, signingSecret, events, verifyTls,
        isEnabled = this.isEnabled
    )

    override fun toString() = redactedToString(this)
}
