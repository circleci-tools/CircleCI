package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class CreateScheduleParameters(
    val name: String,
    val timetable: CreateScheduleTimetable,
    @SerializedName("attribution-actor") val attributionActor: ScheduleAttributionActor,
    val parameters: Map<String, Any>,
    val description: String? = null
)

data class CreateScheduleTimetable(
    @SerializedName("per-hour") val perHour: Int,
    @SerializedName("hours-of-day") val hoursOfDay: List<Int>,
    @SerializedName("days-of-week") val daysOfWeek: List<String>? = null,
    @SerializedName("days-of-month") val daysOfMonth: List<Int>? = null,
    val months: List<String>? = null
)

enum class ScheduleAttributionActor {
    @SerializedName("current")
    CURRENT,

    @SerializedName("system")
    SYSTEM
}
