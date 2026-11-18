package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.TriggerPipelineRunRequest
import com.google.gson.JsonParser
import com.winterbe.expekt.expect
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object TriggerPipelineRunRequestSpec : Spek({
    test("triggerPipelineRun sends the documented JSON fields") {
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
            config = TriggerPipelineRunRequest.Ref(branch = "main"),
            checkout = TriggerPipelineRunRequest.Ref(tag = "v2"),
            parameters = mapOf("deploy" to true)
        )

        client.triggerPipelineRun("gh", "unhappychoice", "circleci", request).test().assertComplete()

        val expected = JsonParser.parseString(
            """
            {
                "definition_id": "2338d0ae-5541-4bbf-88a2-55e9f7281f80",
                "config": {"branch": "main"},
                "checkout": {"tag": "v2"},
                "parameters": {"deploy": true}
            }
            """.trimIndent()
        )
        expect(JsonParser.parseString(body.readUtf8())).to.equal(expected)
    }
})
