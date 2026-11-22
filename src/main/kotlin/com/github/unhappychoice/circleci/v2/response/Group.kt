package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Group(
    val id: String,
    val name: String,
    @SerializedName("organization_id") val organizationId: String? = null,
    val description: String? = null
)

data class GroupListResponse @JvmOverloads constructor(
    val items: List<Group>,
    @SerializedName("next_page_token") val nextPageToken: String?,
    @SerializedName("total_count") val totalCount: Int? = null
)
