package com.github.unhappychoice.circleci.v2.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

@JsonAdapter(PipelineRunResultDeserializer::class)
sealed class PipelineRunResult {
    data class Created(
        val id: String,
        val number: Int? = null,
        val state: String? = null,
        val createdAt: String? = null
    ) : PipelineRunResult()

    data class Skipped(val message: String) : PipelineRunResult()
}

internal class PipelineRunResultDeserializer : JsonDeserializer<PipelineRunResult> {
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): PipelineRunResult {
        val body = context.deserialize<PipelineRunBody>(json, PipelineRunBody::class.java)
        return body.id?.let { PipelineRunResult.Created(it, body.number, body.state, body.createdAt) }
            ?: body.message?.let { PipelineRunResult.Skipped(it) }
            ?: throw JsonParseException("Expected a pipeline ID or skip message")
    }

    private data class PipelineRunBody(
        val id: String?,
        val message: String?,
        val number: Int?,
        val state: String?,
        @SerializedName("created_at") val createdAt: String?
    )
}
