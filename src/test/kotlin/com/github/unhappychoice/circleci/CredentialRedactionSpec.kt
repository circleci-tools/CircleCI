package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import com.github.unhappychoice.circleci.v1.response.Build
import com.github.unhappychoice.circleci.v2.request.ContinuePipelineRequest
import com.github.unhappychoice.circleci.v2.request.CreateEnvVarRequest
import com.github.unhappychoice.circleci.v2.request.CreateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.WebhookScopeRequest
import com.github.unhappychoice.circleci.v2.response.Webhook
import com.github.unhappychoice.circleci.v2.response.WebhookScope
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek

object CredentialRedactionSpec : Spek({
    val v1Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    val v2Gson = Gson()

    test("redacts a signed output URL from the build action and containing build") {
        val signature = "dummy-output-signature"
        val url = "https://example.test/output?AWSAccessKeyId=dummy-key&Expires=2000000000&Signature=$signature"
        val build = v1Gson.fromJson(
            """{"steps":[{"name":"Test","actions":[{"name":"Run tests","output_url":"$url"}]}]}""",
            Build::class.java
        )
        val action = requireNotNull(build.steps).single().actions.single()

        assertEquals(url, action.outputUrl)
        assertRedacted(action, "outputUrl", url)
        assertRedacted(build, "outputUrl", url)
        assertFalse(action.toString().contains(signature))
        assertFalse(build.toString().contains(signature))
        assertTrue(action.toString().contains("name=Run tests"))
    }

    test("redacts the SSH private key and preserves it in request JSON") {
        val key = "dummy-private-key"
        val request = AddSshKeyRequest("example.test", key)

        assertEquals("AddSshKeyRequest(hostname=example.test, private_key=[REDACTED])", request.toString())
        assertEquals(key, v1Gson.toJsonTree(request).asJsonObject["private_key"].asString)
    }

    test("redacts the continuation key and preserves it in request JSON") {
        val key = "dummy-continuation-key"
        val request = ContinuePipelineRequest(key, "version: 2.1", null)

        assertRedacted(request, "continuationKey", key)
        assertEquals(key, v2Gson.toJsonTree(request).asJsonObject["continuation-key"].asString)
    }

    test("redacts the webhook request signing secret and preserves it in request JSON") {
        val secret = "dummy-request-signing-secret"
        val request = CreateWebhookRequest(
            "example", "https://example.test", secret, WebhookScopeRequest("project", "project-id"),
            listOf("workflow-completed"), verifyTls = false
        )

        assertRedacted(request, "signingSecret", secret)
        assertTrue(request.toString().contains("verifyTls=false"))
        val json = v2Gson.toJsonTree(request).asJsonObject
        assertEquals(secret, json["signing-secret"].asString)
        assertFalse(json["verify-tls"].asBoolean)
    }

    test("redacts the environment variable value and preserves it in request JSON") {
        val value = "dummy-environment-value"
        val request = CreateEnvVarRequest(value)

        assertRedacted(request, "value", value)
        assertEquals(value, v2Gson.toJsonTree(request).asJsonObject["value"].asString)
    }

    test("redacts the webhook response signing secret") {
        val secret = "dummy-response-signing-secret"
        val webhook = Webhook(
            "webhook-id", "example", "https://example.test", "created", "updated", secret,
            WebhookScope("project", "project-id"), listOf("workflow-completed")
        )

        assertRedacted(webhook, "signingSecret", secret)
        assertEquals(secret, v2Gson.toJsonTree(webhook).asJsonObject["signing_secret"].asString)
    }
})

private fun assertRedacted(model: Any, field: String, credential: String) {
    val description = model.toString()
    assertFalse(description.contains(credential))
    assertTrue(description.contains("$field=[REDACTED]"))
}
