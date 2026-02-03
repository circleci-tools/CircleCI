package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Group(
    val id: String,
    val name: String,
    @SerializedName("organization_id") val organizationId: String
)

data class GroupListResponse(
    val items: List<Group>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
