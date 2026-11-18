package com.github.unhappychoice.circleci.v2.request

data class PatchClaimsRequest(
    val audience: List<String>? = null,
    val ttl: String? = null
)
