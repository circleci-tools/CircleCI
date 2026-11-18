package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateScheduleParameters
import com.github.unhappychoice.circleci.v2.request.CreateScheduleTimetable
import com.github.unhappychoice.circleci.v2.request.ScheduleAttributionActor
import com.github.unhappychoice.circleci.v2.request.UpdateScheduleParameters
import com.github.unhappychoice.circleci.v2.request.UpdateScheduleTimetable
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ScheduleRequestSpec : Spek({
    lateinit var request: Request
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        request = chain.request()
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(if (request.method == "POST") 201 else 200)
            .message("OK")
            .body("""{"id":"schedule-id"}""".toResponseBody())
            .build()
    }.build()
    val client = Retrofit.Builder()
        .baseUrl("https://circleci.com/api/v2/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
        .build()
        .create(CircleCIAPIClientV2::class.java)

    test("createSchedule sends the timetable, attribution actor, and pipeline parameters") {
        val parameters = CreateScheduleParameters(
            name = "Nightly",
            timetable = CreateScheduleTimetable(1, listOf(2), daysOfWeek = listOf("MON", "FRI")),
            attributionActor = ScheduleAttributionActor.SYSTEM,
            parameters = mapOf("branch" to "main", "deploy_prod" to true)
        )
        client.createSchedule("gh/example/project", parameters).blockingFirst()

        assertEquals("POST", request.method)
        assertRequestBody(
            request,
            """
            {
                "name": "Nightly",
                "timetable": {"per-hour": 1, "hours-of-day": [2], "days-of-week": ["MON", "FRI"]},
                "attribution-actor": "system",
                "parameters": {"branch": "main", "deploy_prod": true}
            }
            """
        )
    }

    test("updateSchedule sends only the new hours when patching the timetable") {
        val parameters = UpdateScheduleParameters(timetable = UpdateScheduleTimetable(hoursOfDay = listOf(5, 17)))
        client.updateSchedule("schedule-id", parameters).blockingFirst()

        assertEquals("PATCH", request.method)
        assertRequestBody(request, """{"timetable":{"hours-of-day":[5,17]}}""")
    }
})

private fun assertRequestBody(request: Request, expectedJson: String) {
    val buffer = Buffer()
    requireNotNull(request.body).writeTo(buffer)
    assertEquals(JsonParser.parseString(expectedJson), JsonParser.parseString(buffer.readUtf8()))
}
