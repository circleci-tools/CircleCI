package com.github.unhappychoice.circleci.v2.request

import com.squareup.moshi.Json

data class Timetable(
    val branches: Map<String, List<String>>?,
    val setup: Boolean,
    @Json(name = "continue_pipeline_at") val continuePipelineAt: String,
    val attachments: List<String>?
)
