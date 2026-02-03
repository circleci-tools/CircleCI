package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class CheckoutKeyListResponse(
    val items: List<CheckoutKey>?,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)
