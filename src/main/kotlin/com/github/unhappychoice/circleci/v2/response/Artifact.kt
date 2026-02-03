package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Artifact(
    val url: String,
    val path: String,
    @SerializedName("node_index") val nodeIndex: Int
) {
    data class ArtifactListResponse(
        val items: List<Artifact>,
        @SerializedName("next_page_token") val nextPageToken: String?
    )
}
