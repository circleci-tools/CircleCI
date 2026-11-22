package com.github.unhappychoice.circleci.v1.response

import com.google.gson.annotations.SerializedName

data class CheckoutKey(
    val type: String,
    val preferred: Boolean,
    @SerializedName(value = "created_at", alternate = ["time"])
    val created_at: String,
    val fingerprint: String,
    val public_key: String
)
