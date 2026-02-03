package com.github.unhappychoice.circleci.v1.request

data class DeleteSshKeyRequest(
    val fingerprint: String,
    val hostname: String
)
