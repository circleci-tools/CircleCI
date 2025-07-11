package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class EnvironmentVariable(
    val variable: String,
    @SerializedName("created_at") val createdAt: OffsetDateTime,
    @SerializedName("updated_at") val updatedAt: OffsetDateTime,
    @SerializedName("context_id") val contextId: String
)