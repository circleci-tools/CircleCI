package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class OidcClaims(
    val audience: List<String>?,
    @SerializedName("audience_updated_at") val audienceUpdatedAt: String?,
    @SerializedName("org_id") val orgId: String?,
    @SerializedName("project_id") val projectId: String?,
    @SerializedName("ttl") val ttl: String?
)
