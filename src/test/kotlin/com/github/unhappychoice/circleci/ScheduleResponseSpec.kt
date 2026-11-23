package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Schedule
import com.github.unhappychoice.circleci.v2.response.ScheduleTimetable
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object ScheduleResponseSpec : Spek({
    val projectSlug = "gh/example/project"
    val scheduleId = "497f6eca-6276-4993-bfeb-53cbbbba6f08"
    fun scheduleJson(calendarFields: String, descriptionJson: String) = """
        {
          "id": "$scheduleId",
          "created-at": "2019-08-24T14:15:22Z",
          "updated-at": "2019-08-25T14:15:22Z",
          "project-slug": "$projectSlug",
          "name": "Scheduled build",
          "description": $descriptionJson,
          "parameters": {"branch": "main"},
          "actor": {"id": "$scheduleId", "login": "example", "name": "Example", "avatar_url": null},
          "timetable": {
            "per-hour": 2,
            "hours-of-day": [8, 16],
            $calendarFields
          }
        }
    """.trimIndent()

    test("decodes a schedule from the get-by-ID response") {
        val json = scheduleJson(""""days-of-week": ["TUE"]""", "null")
        withScheduleResponse("/api/v2/schedule/$scheduleId", json) { client ->
            val schedule = client.getScheduleById(scheduleId).blockingFirst()
            assertScheduleFields(projectSlug, schedule)
            assertNull(schedule.description)
            assertEquals(ScheduleTimetable(2, listOf(8, 16), listOf("TUE"), null, null), schedule.timetable)
        }
    }

    test("decodes schedules and the next page token from the project response") {
        val item = scheduleJson(""""days-of-month": [12], "months": ["MAR"]""", """"Build the main branch"""")
        val json = """{"items": [$item], "next_page_token": "next-page"}"""
        withScheduleResponse("/api/v2/project/$projectSlug/schedule", json) { client ->
            val response = client.getProjectSchedules(projectSlug).blockingFirst()
            val schedule = response.items.single()
            assertScheduleFields(projectSlug, schedule)
            assertEquals("Build the main branch", schedule.description)
            assertEquals(ScheduleTimetable(2, listOf(8, 16), null, listOf(12), listOf("MAR")), schedule.timetable)
            assertEquals("next-page", response.nextPageToken)
        }
    }
})

private fun assertScheduleFields(projectSlug: String, schedule: Schedule) {
    assertEquals("2019-08-24T14:15:22Z", schedule.createdAt)
    assertEquals("2019-08-25T14:15:22Z", schedule.updatedAt)
    assertEquals(projectSlug, schedule.projectSlug)
}

private fun withScheduleResponse(path: String, json: String, test: (CircleCIAPIClientV2) -> Unit) {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext(path) { exchange ->
        val body = json.toByteArray()
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseBody.use { it.write(body) }
    }
    try {
        server.start()
        test(CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/"))
    } finally {
        server.stop(0)
    }
}
