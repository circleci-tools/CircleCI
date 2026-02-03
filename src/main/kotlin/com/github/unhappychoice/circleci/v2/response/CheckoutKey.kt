package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class CheckoutKey(
    @SerializedName("public-key")
    val publicKey: String,
    val type: String,
    val fingerprint: String,
    val preferred: Boolean,
    @SerializedName("created-at")
    val createdAt: OffsetDateTime
)
