package com.github.unhappychoice.circleci.v2.request

import com.squareup.moshi.Json

data class UpdateScheduleParameters(
    val name: String?,
    val description: String?,
    val parameters: Map<String, Any>?,
    val timetables: List<Timetable>?
)