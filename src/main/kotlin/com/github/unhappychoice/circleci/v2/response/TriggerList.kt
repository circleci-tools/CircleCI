package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class TriggerList(
    val items: List<Trigger>,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)