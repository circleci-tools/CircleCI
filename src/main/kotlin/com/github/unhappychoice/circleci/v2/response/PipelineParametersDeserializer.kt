package com.github.unhappychoice.circleci.v2.response

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal class PipelineParametersDeserializer : JsonDeserializer<Map<String, Any>> {
    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Map<String, Any> =
        decode(json)

    companion object {
        private val mapType = object : TypeToken<Map<String, Any>>() {}.type
        private val gson = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER).create()

        fun decode(json: JsonElement): Map<String, Any> = gson.fromJson(json, mapType)
    }
}
