package com.github.unhappychoice.circleci.v2.response

import com.squareup.moshi.Json

data class URLOrbAllowListEntryList(
    val items: List<URLOrbAllowListEntry>,
    @Json(name = "next_page_token")
    val nextPageToken: String?
)