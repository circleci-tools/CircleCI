package com.github.unhappychoice.circleci.v2.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Schedule(
    val id: String,
    val description: String?,
    val name: String,
    @SerializedName("created-at") val createdAt: String,
    @SerializedName("updated-at") val updatedAt: String,
    @SerializedName("project-slug") val projectSlug: String,
    @JsonAdapter(PipelineParametersDeserializer::class) @Redacted val parameters: Map<String, Any>,
    @SerializedName("actor") val actor: Actor,
    val timetable: ScheduleTimetable
) {
    override fun toString() = redactedToString(this)
}

data class Actor(
    val id: String,
    val login: String,
    val name: String
)

data class ScheduleTimetable(
    @SerializedName("per-hour") val perHour: Int,
    @SerializedName("hours-of-day") val hoursOfDay: List<Int>,
    @SerializedName("days-of-week") val daysOfWeek: List<String>?,
    @SerializedName("days-of-month") val daysOfMonth: List<Int>?,
    val months: List<String>?
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
