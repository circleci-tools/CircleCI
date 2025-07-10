package com.github.unhappychoice.circleci.request

data class AddSshKeyRequest(
  val hostname: String,
  val private_key: String
)
