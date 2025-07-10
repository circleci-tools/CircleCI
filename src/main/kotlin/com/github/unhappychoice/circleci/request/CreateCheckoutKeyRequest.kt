package com.github.unhappychoice.circleci.request

data class CreateCheckoutKeyRequest(
  val type: String,
  val preferred: Boolean
)
