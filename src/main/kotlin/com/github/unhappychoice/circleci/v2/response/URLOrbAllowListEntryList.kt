package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class URLOrbAllowListEntryList(
    val items: List<URLOrbAllowListEntry>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)