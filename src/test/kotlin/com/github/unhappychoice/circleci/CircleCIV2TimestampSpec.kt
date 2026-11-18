package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.CheckoutKey
import com.github.unhappychoice.circleci.v2.response.Context
import com.github.unhappychoice.circleci.v2.response.EnvironmentVariableListResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import java.time.OffsetDateTime

object CircleCIV2TimestampSpec : Spek({
    val retrofit = CircleCIAPIClient("test-token").retrofitV2("https://circleci.com/api/v2/")

    test("decodes the UTC creation timestamp of a context") {
        val context = retrofit.decode<Context>(
            """{"id":"context-id","name":"example","created_at":"2020-01-01T00:00:00Z"}"""
        )

        assertEquals(OffsetDateTime.parse("2020-01-01T00:00:00Z"), context.createdAt)
    }

    test("decodes fractional timestamps in a context environment variable list") {
        val response = retrofit.decode<EnvironmentVariableListResponse>(
            """
            {
                "items": [{
                    "variable": "EXAMPLE",
                    "context_id": "context-id",
                    "created_at": "2020-01-01T00:00:00.123456Z",
                    "updated_at": "2020-01-02T03:04:05.987654321+05:30"
                }],
                "next_page_token": null
            }
            """.trimIndent()
        )

        val variable = response.items.single()
        assertEquals(OffsetDateTime.parse("2020-01-01T00:00:00.123456Z"), variable.createdAt)
        assertEquals(OffsetDateTime.parse("2020-01-02T03:04:05.987654321+05:30"), variable.updatedAt)
    }

    test("preserves the explicit negative offset of a checkout key timestamp") {
        val key = retrofit.decode<CheckoutKey>(
            """
            {
                "public-key": "ssh-rsa example",
                "type": "deploy-key",
                "fingerprint": "fingerprint",
                "preferred": true,
                "created-at": "2020-01-01T00:00:00.123-04:00"
            }
            """.trimIndent()
        )

        assertEquals(OffsetDateTime.parse("2020-01-01T00:00:00.123-04:00"), key.createdAt)
    }
})

private inline fun <reified T : Any> Retrofit.decode(json: String): T {
    val converter = responseBodyConverter<T>(T::class.java, emptyArray())
    return checkNotNull(json.toResponseBody().use(converter::convert))
}
