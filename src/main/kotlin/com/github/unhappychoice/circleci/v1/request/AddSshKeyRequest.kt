package com.github.unhappychoice.circleci.v1.request

data class AddSshKeyRequest(
  val hostname: String,
  val private_key: String
) {
  override fun toString() = "AddSshKeyRequest(hostname=$hostname, private_key=[REDACTED])"
}
