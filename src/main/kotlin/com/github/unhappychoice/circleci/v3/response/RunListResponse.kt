package com.github.unhappychoice.circleci.v3.response

import com.google.gson.annotations.SerializedName

data class RunListResponse(val data: List<RunSummary>, val page: Page) {
    data class RunSummary(val id: String, val attributes: Attributes, val references: References) {
        val branch: String? get() = references.event.attributes.vcs?.branch
        val revision: String? get() = references.event.attributes.vcs?.revision
    }

    data class Attributes(
        val phase: String,
        @SerializedName("current_outcome") val currentOutcome: String?,
        @SerializedName("created_at") val createdAt: String,
        val number: Int
    )

    data class References(val event: Event, val project: Project)

    data class Event(val attributes: EventAttributes)

    data class EventAttributes(val vcs: Vcs?)

    data class Vcs(val branch: String?, val revision: String?)

    data class Project(val id: String)

    data class Page(val next: String?, val prev: String?)
}
