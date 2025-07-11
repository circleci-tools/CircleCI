package com.github.unhappychoice.circleci.v1.response

data class SSHKey(
  val fingerprint: String,
  val public_key: String
)
