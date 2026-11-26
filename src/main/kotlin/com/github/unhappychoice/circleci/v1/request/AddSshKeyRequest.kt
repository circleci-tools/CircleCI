package com.github.unhappychoice.circleci.v1.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class AddSshKeyRequest(
  val hostname: String,
  @Redacted val private_key: String
) {
  override fun toString() = redactedToString(this)
}
