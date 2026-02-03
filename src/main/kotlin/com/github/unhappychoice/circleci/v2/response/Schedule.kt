package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class Schedule(
    val id: String,
    val description: String,
    val name: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("project_slug") val projectSlug: String,
    @SerializedName("parameters") val parameters: Map<String, Any>,
    @SerializedName("actor") val actor: Actor,
    @SerializedName("timetables") val timetables: List<Timetable>
)

data class Actor(
    val id: String,
    val login: String,
    val name: String
)

data class Timetable(
    val id: String,
    val attribution: String,
    val days: List<String>,
    val hours: List<Int>,
    val minutes: List<Int>,
    @SerializedName("per_hour") val perHour: Int
)

data class ScheduleListResponse(
    val items: List<Schedule>,
    @SerializedName("next_page_token") val nextPageToken: String?
)
