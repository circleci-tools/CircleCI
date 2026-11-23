package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class UpdateTriggerRequest(
    @SerializedName("event_preset")
    val eventPreset: String? = null,
    @SerializedName("checkout_ref")
    val checkoutRef: String? = null,
    @SerializedName("config_ref")
    val configRef: String? = null,
    @SerializedName("event_name")
    val eventName: String? = null,
    val disabled: Boolean? = null,
    @SerializedName("event_source")
    val eventSource: EventSource? = null,
    val parameters: Map<String, Any>? = null
) {
    override fun toString() =
        "UpdateTriggerRequest(eventPreset=$eventPreset, checkoutRef=$checkoutRef, configRef=$configRef, " +
            "eventName=$eventName, disabled=$disabled, eventSource=$eventSource, parameters=[REDACTED])"

    data class EventSource(
        val webhook: CreateTriggerRequest.EventSource.Webhook? = null,
        val schedule: CreateTriggerRequest.EventSource.Schedule? = null
    )
}
