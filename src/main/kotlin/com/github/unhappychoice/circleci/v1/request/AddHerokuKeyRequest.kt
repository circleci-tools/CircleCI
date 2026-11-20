package com.github.unhappychoice.circleci.v1.request

data class AddHerokuKeyRequest(
  val apikey: String
) {
  override fun toString() = "AddHerokuKeyRequest(apikey=[REDACTED])"
}
