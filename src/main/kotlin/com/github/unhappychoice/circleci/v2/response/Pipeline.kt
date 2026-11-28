package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Pipeline(
    val id: String,
    val errors: List<PipelineError>,
    @SerializedName("project_slug") val projectSlug: String,
    @SerializedName("updated_at") val updatedAt: String?,
    val number: Long,
    @JsonAdapter(PipelineParametersDeserializer::class)
    @SerializedName("trigger_parameters") @Redacted val triggerParameters: Map<String, Any>?,
    val state: String,
    @SerializedName("created_at") val createdAt: String,
    val trigger: Trigger,
    val vcs: Vcs?,
    val warnings: List<PipelineWarning>? = null
) {
    override fun toString() = redactedToString(this)

    data class PipelineError(
        val type: String,
        val message: String
    )

    data class PipelineWarning(
        val type: String,
        val message: String,
        val description: String,
        val url: String? = null
    )

    data class Trigger(
        val type: String,
        @SerializedName("received_at") val receivedAt: String,
        val actor: Actor
    ) {
        data class Actor(
            val login: String,
            @SerializedName("avatar_url") val avatarUrl: String?
        )
    }

    data class Vcs(
        @SerializedName("provider_name") val providerName: String,
        @SerializedName("target_repository_url") val targetRepositoryUrl: String,
        val branch: String?,
        @SerializedName("review_id") val reviewId: String?,
        @SerializedName("review_url") val reviewUrl: String?,
        val revision: String?,
        val tag: String?,
        val commit: Commit?,
        @SerializedName("origin_repository_url") val originRepositoryUrl: String
    ) {
        data class Commit(
            val subject: String?,
            val body: String?
        )
    }
}

data class TriggerNewPipelineResponse(
    val number: Long,
    val state: String,
    val id: String,
    @SerializedName("created_at") val createdAt: String
)
