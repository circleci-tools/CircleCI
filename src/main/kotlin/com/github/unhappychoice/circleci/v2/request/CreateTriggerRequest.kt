package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class CreateTriggerRequest(
    @SerializedName("event_source")
    val eventSource: EventSource,
    @SerializedName("event_preset")
    val eventPreset: String? = null,
    @SerializedName("checkout_ref")
    val checkoutRef: String? = null,
    @SerializedName("config_ref")
    val configRef: String? = null,
    @SerializedName("event_name")
    val eventName: String? = null,
    val disabled: Boolean? = null,
    @Redacted val parameters: Map<String, Any>? = null
) {
    override fun toString() = redactedToString(this)

    data class EventSource(
        val provider: String,
        val repo: Repo? = null,
        val webhook: Webhook? = null,
        val schedule: Schedule? = null
    ) {
        data class Repo(
            @SerializedName("external_id")
            val externalId: String
        )

        data class Webhook(val sender: String? = null)

        data class Schedule(
            @SerializedName("cron_expression")
            val cronExpression: String? = null,
            @SerializedName("attribution_actor")
            val attributionActor: String? = null
        )
    }
}
