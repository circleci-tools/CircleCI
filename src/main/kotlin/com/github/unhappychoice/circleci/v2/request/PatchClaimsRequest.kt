package com.github.unhappychoice.circleci.v2.request

data class PatchClaimsRequest(
    val claims: Map<String, String>
)