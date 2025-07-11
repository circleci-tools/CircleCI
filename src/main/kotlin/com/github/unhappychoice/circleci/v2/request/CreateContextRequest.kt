package com.github.unhappychoice.circleci.v2.request

data class CreateContextRequest(
    val name: String,
    val owner: Owner
) {
    data class Owner(
        val id: String? = null,
        val slug: String? = null,
        val type: String? = null
    )
}