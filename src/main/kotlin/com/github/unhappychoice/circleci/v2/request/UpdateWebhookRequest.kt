package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class UpdateWebhookRequest(
    val name: String?,
    val url: String?,
    @SerializedName("signing_secret") val signingSecret: String?,
    val events: List<String>?
)
