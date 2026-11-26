package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.SerializedName

data class Artifact(
    @Redacted val url: String,
    val path: String,
    @SerializedName("node_index") val nodeIndex: Int
) {
    override fun toString() = redactedToString(this)

    data class ArtifactListResponse(
        val items: List<Artifact>,
        @SerializedName("next_page_token") val nextPageToken: String?
    )
}
