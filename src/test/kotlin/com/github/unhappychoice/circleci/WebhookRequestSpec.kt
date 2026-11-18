package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.UpdateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.WebhookScopeRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object WebhookRequestSpec : Spek({
    val gson = Gson()

    test("create sends all required webhook fields") {
        val request = CreateWebhookRequest(
            name = "webhook",
            url = "https://example.com/webhook",
            signingSecret = "secret",
            scope = WebhookScopeRequest("project", "project-id"),
            events = listOf("workflow-completed")
        )
        val expected = JsonParser.parseString(
            """
            {
                "name": "webhook",
                "url": "https://example.com/webhook",
                "signing-secret": "secret",
                "scope": {"type": "project", "id": "project-id"},
                "events": ["workflow-completed"],
                "verify-tls": true
            }
            """.trimIndent()
        )
        assertEquals(expected, gson.toJsonTree(request))
        expected.asJsonObject.addProperty("verify-tls", false)
        assertEquals(expected, gson.toJsonTree(request.copy(verifyTls = false)))
    }

    test("update sends the signing secret by itself") {
        val request = UpdateWebhookRequest(signingSecret = "new-secret")
        assertEquals(JsonParser.parseString("""{"signing-secret":"new-secret"}"""), gson.toJsonTree(request))
    }

    test("update sends the TLS setting by itself, including false") {
        val request = UpdateWebhookRequest(verifyTls = false)
        assertEquals(JsonParser.parseString("""{"verify-tls":false}"""), gson.toJsonTree(request))
        assertEquals(JsonParser.parseString("""{"verify-tls":true}"""), gson.toJsonTree(request.copy(verifyTls = true)))
    }
})
