package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.MakeDecisionRequest
import com.github.unhappychoice.circleci.v2.response.Decision
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object PolicyDecisionDecodingSpec : Spek({
    val passingDecision = """{"status":"PASS"}"""
    val failingDecision = """
        {
            "status": "HARD_FAIL",
            "enabled_rules": ["require_tests", "recommend_docs"],
            "hard_failures": [{"rule": "require_tests", "reason": "Tests are missing"}],
            "soft_failures": [{"rule": "recommend_docs", "reason": "Docs are missing"}],
            "reason": "Policy checks failed"
        }
    """.trimIndent()
    val failed = Decision(
        status = "HARD_FAIL",
        enabledRules = listOf("require_tests", "recommend_docs"),
        hardFailures = listOf(Decision.RuleFailure("require_tests", "Tests are missing")),
        softFailures = listOf(Decision.RuleFailure("recommend_docs", "Docs are missing")),
        reason = "Policy checks failed"
    )
    val logId = "aecac64d-77c2-4188-9b24-9af08159ccf6"
    val log = """{"id":"$logId","decision":$failingDecision,"time_taken_ms":12}"""
    val request = MakeDecisionRequest("{}", null)

    test("decodes an empty decision log array") {
        val logs = decisionClient("[]").getDecisionLogs("owner-id", "config").blockingFirst()
        assertTrue(logs.isEmpty())
    }

    test("decodes a populated decision log array") {
        val logs = decisionClient("[$log]").getDecisionLogs("owner-id", "config").blockingFirst()
        assertEquals(logId, logs.single().id)
        assertEquals(failed, logs.single().decision)
    }

    test("decodes a single log with a nested decision") {
        val result = decisionClient(log).getDecisionLog("owner-id", "config", logId).blockingFirst()
        assertEquals(logId, result.id)
        assertEquals(12L, result.timeTakenMs)
        assertEquals(failed, result.decision)
    }

    test("decodes a direct decision with rule failures") {
        val decision = decisionClient(failingDecision).makeDecision("owner-id", "config", request).blockingFirst()
        assertEquals(failed, decision)
    }

    test("decodes a direct decision with only the required status") {
        val decision = decisionClient(passingDecision).makeDecision("owner-id", "config", request).blockingFirst()
        assertEquals("PASS", decision.status)
        assertNull(decision.enabledRules)
        assertNull(decision.hardFailures)
        assertNull(decision.softFailures)
        assertNull(decision.reason)
    }
})

private fun decisionClient(json: String): CircleCIAPIClientV2 = Retrofit.Builder()
    .baseUrl("https://example.com/api/v2/")
    .client(OkHttpClient.Builder().addInterceptor { chain ->
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody())
            .build()
    }.build())
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
    .build()
    .create(CircleCIAPIClientV2::class.java)
