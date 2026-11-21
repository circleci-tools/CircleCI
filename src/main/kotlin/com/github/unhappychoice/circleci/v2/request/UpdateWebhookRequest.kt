package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class UpdateWebhookRequest(
    val name: String? = null,
    val url: String? = null,
    @SerializedName("signing-secret") val signingSecret: String? = null,
    val events: List<String>? = null,
    @SerializedName("verify-tls") val verifyTls: Boolean? = null,
    @SerializedName("is-enabled") val isEnabled: Boolean? = null
) {
    override fun toString() =
        "UpdateWebhookRequest(name=$name, url=$url, signingSecret=[REDACTED], " +
            "events=$events, verifyTls=$verifyTls, isEnabled=$isEnabled)"
}
