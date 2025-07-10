package com.github.unhappychoice.circleci.response

data class CheckoutKey(
  val type: String,
  val preferred: Boolean,
  val created_at: String,
  val fingerprint: String,
  val public_key: String
)
