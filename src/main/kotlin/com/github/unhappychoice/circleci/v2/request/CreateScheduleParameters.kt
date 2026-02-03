package com.github.unhappychoice.circleci.v2.request

import com.github.unhappychoice.circleci.v2.response.Timetable
import com.google.gson.annotations.SerializedName

data class CreateScheduleParameters(
    val name: String,
    val description: String?,
    @SerializedName("project_slug") val projectSlug: String,
    val parameters: Map<String, Any>,
    val timetables: List<Timetable>
)