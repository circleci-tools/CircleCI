package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.TriggerPipelineRunRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.winterbe.expekt.expect
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object TriggerPipelineRunRequestSpec : Spek({
    test("rejects refs with both branch and tag or neither") {
        assertThrows(IllegalArgumentException::class.java) { TriggerPipelineRunRequest.Ref(branch = "main", tag = "v1") }
        assertThrows(IllegalArgumentException::class.java) { TriggerPipelineRunRequest.Ref() }
    }

    test("rejects mixed config and checkout reference types") {
        val branch = TriggerPipelineRunRequest.Ref(branch = "main")
        val tag = TriggerPipelineRunRequest.Ref(tag = "v1")
        assertThrows(IllegalArgumentException::class.java) { TriggerPipelineRunRequest(config = branch, checkout = tag) }
        assertThrows(IllegalArgumentException::class.java) { TriggerPipelineRunRequest(config = tag, checkout = branch) }
    }

    test("omits each unspecified ref from the request body") {
        val branch = TriggerPipelineRunRequest.Ref(branch = "main")
        val tag = TriggerPipelineRunRequest.Ref(tag = "v2")
        val cases = mapOf(
            TriggerPipelineRunRequest() to "{}",
            TriggerPipelineRunRequest(config = branch) to """{"config":{"branch":"main"}}""",
            TriggerPipelineRunRequest(checkout = tag) to """{"checkout":{"tag":"v2"}}"""
        )
        val gson = Gson()
        cases.forEach { (request, expected) ->
            expect(gson.toJsonTree(request)).to.equal(JsonParser.parseString(expected))
        }
    }

    listOf("branch" to "main", "tag" to "v2").forEach { (field, value) ->
        test("triggerPipelineRun sends the documented JSON fields for $field and $value") {
            val selection = TriggerPipelineRunRequest.Ref(
                branch = value.takeIf { field == "branch" },
                tag = value.takeIf { field == "tag" }
            )
            val body = Buffer()
            val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request()
                requireNotNull(request.body).writeTo(body)
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(201)
                    .message("Created")
                    .body("""{"id":"pipeline-id"}""".toResponseBody())
                    .build()
            }.build()
            val client = Retrofit.Builder()
                .baseUrl("https://circleci.com/api/v2/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
                .build()
                .create(CircleCIAPIClientV2::class.java)
            val request = TriggerPipelineRunRequest(
                definitionId = "2338d0ae-5541-4bbf-88a2-55e9f7281f80",
                config = selection,
                checkout = selection,
                parameters = mapOf("deploy" to true)
            )

            client.triggerPipelineRun("gh", "unhappychoice", "circleci", request).test().assertComplete()

            val expected = JsonParser.parseString(
                """
                {
                    "definition_id": "2338d0ae-5541-4bbf-88a2-55e9f7281f80",
                    "config": {"$field": "$value"},
                    "checkout": {"$field": "$value"},
                    "parameters": {"deploy": true}
                }
                """.trimIndent()
            )
            expect(JsonParser.parseString(body.readUtf8())).to.equal(expected)
        }
    }
})
