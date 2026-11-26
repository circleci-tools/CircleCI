package com.github.unhappychoice.circleci.v1.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class AddHerokuKeyRequest(
  @Redacted val apikey: String
) {
  override fun toString() = redactedToString(this)
}
