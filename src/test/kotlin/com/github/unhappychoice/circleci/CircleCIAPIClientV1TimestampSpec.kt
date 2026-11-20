package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.response.Build
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import java.time.Instant
import java.util.TimeZone

object CircleCIAPIClientV1TimestampSpec : Spek({
  listOf("UTC", "Asia/Tokyo").forEach { zoneId ->
    test("decodes the build queue timestamp as UTC with JVM default zone $zoneId") {
      val originalZone = TimeZone.getDefault()
      try {
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
        val retrofit = CircleCIAPIClient("test-token").retrofitV1("https://circleci.com/api/v1/")
        val converter = retrofit.responseBodyConverter<Build>(Build::class.java, emptyArray())
        val json = checkNotNull(Build::class.java.getResource("/com/github/unhappychoice/circleci/json/build.json")).readText()
        val build = json.toResponseBody().use { converter.convert(it) }

        val expected = Instant.parse("2016-06-06T15:41:22.237Z")
        val mockV1Build = MockCircleCIAPIClientV1().getBuild("user", "project", 1).blockingSingle()
        val mockV1_1Build = MockCircleCIAPIClientV1_1().getBuild("github", "user", "project", 1).blockingSingle()

        assertEquals(expected, build?.queuedAt?.toInstant(), "Production converter")
        assertEquals(expected, mockV1Build.queuedAt?.toInstant(), "V1 test double")
        assertEquals(expected, mockV1_1Build.queuedAt?.toInstant(), "V1.1 test double")
      } finally {
        TimeZone.setDefault(originalZone)
      }
    }
  }
})
