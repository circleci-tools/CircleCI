package com.github.unhappychoice.circleci.v1.request

data class CreateCheckoutKeyRequest(
    val type: String,
    val preferred: Boolean
)