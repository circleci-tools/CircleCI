package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class Trigger(
    val id: String,
    @SerializedName("event_source")
    val eventSource: EventSource,
    @SerializedName("event_name")
    val eventName: String? = null,
    @SerializedName("event_preset")
    val eventPreset: String? = null,
    @SerializedName("checkout_ref")
    val checkoutRef: String? = null,
    @SerializedName("config_ref")
    val configRef: String? = null,
    val disabled: Boolean? = null,
    @Redacted val parameters: Map<String, Any>? = null
) {
    override fun toString() = redactedToString(this)

    data class EventSource(
        val provider: String,
        val repo: Repository? = null,
        val webhook: Webhook? = null,
        val schedule: Schedule? = null
    )

    data class Repository(
        @SerializedName("full_name")
        val fullName: String?,
        @SerializedName("external_id")
        val externalId: String?
    )

    data class Webhook(@Redacted val url: String?, val sender: String?) {
        override fun toString() = redactedToString(this)
    }

    data class Schedule(
        @SerializedName("cron_expression")
        val cronExpression: String?,
        @SerializedName("attribution_actor")
        val attributionActor: AttributionActor?
    )

    data class AttributionActor(val id: String?)
}
