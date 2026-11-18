package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class UpdateScheduleParameters(
    val name: String? = null,
    val description: String? = null,
    val parameters: Map<String, Any>? = null,
    val timetable: UpdateScheduleTimetable? = null,
    @SerializedName("attribution-actor") val attributionActor: ScheduleAttributionActor? = null
)

data class UpdateScheduleTimetable(
    @SerializedName("per-hour") val perHour: Int? = null,
    @SerializedName("hours-of-day") val hoursOfDay: List<Int>? = null,
    @SerializedName("days-of-week") val daysOfWeek: List<String>? = null,
    @SerializedName("days-of-month") val daysOfMonth: List<Int>? = null,
    val months: List<String>? = null
)
