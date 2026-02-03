package com.github.unhappychoice.circleci.v1.response

data class CheckoutKey(
    val type: String,
    val preferred: Boolean,
    val created_at: String,
    val fingerprint: String,
    val public_key: String
)
