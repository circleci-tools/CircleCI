package com.github.unhappychoice.circleci.response

data class SSHKey(
  val fingerprint: String,
  val public_key: String
)
