package com.github.unhappychoice.circleci.v2.request

import com.squareup.moshi.Json

data class CreateScheduleParameters(
    val name: String,
    val description: String?,
    @Json(name = "project_slug") val projectSlug: String,
    val parameters: Map<String, Any>,
    val timetables: List<Timetable>
)