package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ContextProjectRestrictionsList(
    val items: List<ContextRestriction>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
