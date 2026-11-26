package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddHerokuKeyRequest
import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.v1.response.Artifact
import com.github.unhappychoice.circleci.v1.response.Build
import com.github.unhappychoice.circleci.v1.response.EnvironmentVariable
import com.github.unhappychoice.circleci.v2.request.ContinuePipelineRequest
import com.github.unhappychoice.circleci.v2.request.CreateEnvVarRequest
import com.github.unhappychoice.circleci.v2.request.CreateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.RollbackProjectRequest
import com.github.unhappychoice.circleci.v2.request.TriggerNewPipelineRequest
import com.github.unhappychoice.circleci.v2.request.TriggerPipelineRunRequest
import com.github.unhappychoice.circleci.v2.request.UpdateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.WebhookScopeRequest
import com.github.unhappychoice.circleci.v2.response.Pipeline
import com.github.unhappychoice.circleci.v2.response.Webhook
import com.github.unhappychoice.circleci.v2.response.WebhookScope
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import com.github.unhappychoice.circleci.v1.request.AddEnvironmentVariableRequest as V1AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.request.AddEnvironmentVariableRequest as V2AddEnvironmentVariableRequest
import com.github.unhappychoice.circleci.v2.response.Artifact as V2Artifact

object CredentialRedactionSpec : Spek({
    val v1Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    val v2Gson = Gson()

    test("redacts V2 pipeline trigger request parameters and preserves them in JSON") {
        val secret = "dummy-deploy-token"
        val parameters = mapOf("deploy_token" to secret, "deploy" to true)
        val newPipeline = TriggerNewPipelineRequest(parameters = parameters)
        val pipelineRun = TriggerPipelineRunRequest(parameters = parameters)

        assertEquals(parameters, newPipeline.parameters)
        assertEquals(parameters, pipelineRun.parameters)
        listOf(newPipeline, pipelineRun).forEach { request ->
            assertRedacted(request, "parameters", secret)
            assertFalse(request.toString().contains("deploy_token"))
            assertEquals(v2Gson.toJsonTree(parameters), v2Gson.toJsonTree(request).asJsonObject["parameters"])
        }
    }

    test("redacts decoded V2 pipeline trigger parameters and preserves the property value") {
        val secret = "dummy-deploy-token"
        val parameters = mapOf("deploy_token" to secret, "deploy" to true)
        val pipeline = v2Gson.fromJson(
            """
            {
                "id": "pipeline-id",
                "errors": [],
                "project_slug": "gh/example/project",
                "number": 1,
                "trigger_parameters": {"deploy_token": "$secret", "deploy": true},
                "state": "created",
                "created_at": "2026-01-01T00:00:00Z",
                "trigger": {
                    "type": "api",
                    "received_at": "2026-01-01T00:00:00Z",
                    "actor": {"login": "example"}
                }
            }
            """.trimIndent(),
            Pipeline::class.java
        )

        assertEquals(parameters, pipeline.triggerParameters)
        assertRedacted(pipeline, "triggerParameters", secret)
        assertFalse(pipeline.toString().contains("deploy_token"))
        assertTrue(pipeline.toString().contains("id=pipeline-id"))
    }

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

    test("redacts the Heroku API key and preserves the original property value") {
        val key = "dummy-key"
        val request = AddHerokuKeyRequest(key)

        assertRedacted(request, "apikey", key)
        assertEquals(key, request.apikey)
    }

    test("redacts the SSH private key and preserves it in request JSON") {
        val key = "dummy-private-key"
        val request = AddSshKeyRequest("example.test", key)

        assertEquals("AddSshKeyRequest(hostname=example.test, private_key=[REDACTED])", request.toString())
        assertEquals(key, v1Gson.toJsonTree(request).asJsonObject["private_key"].asString)
    }

    test("redacts build parameters and preserves them in legacy trigger request JSON") {
        val value = "dummy-secret"
        val parameters = mapOf("DEPLOY_TOKEN" to value)
        val requests = listOf(
            TriggerNewBuildRequest(buildParameters = parameters),
            TriggerNewBuildWithBranchRequest(buildParameters = parameters)
        )

        requests.forEach { request ->
            assertRedacted(request, "buildParameters", value)
            val json = v1Gson.toJsonTree(request).asJsonObject
            assertEquals(value, json.getAsJsonObject("build_parameters")["DEPLOY_TOKEN"].asString)
        }
    }

    test("redacts the continuation key and preserves it in request JSON") {
        val key = "dummy-continuation-key"
        val request = ContinuePipelineRequest(key, "version: 2.1", null)

        assertRedacted(request, "continuationKey", key)
        assertEquals(key, v2Gson.toJsonTree(request).asJsonObject["continuation-key"].asString)
    }

    test("redacts the continuation configuration and parameters, preserving them in request JSON") {
        val secret = "dummy-deploy-token"
        val configuration = "version: 2.1\njobs:\n  deploy:\n    environment:\n      DEPLOY_TOKEN: $secret"
        val parameters = mapOf("deploy_token" to secret)
        val request = ContinuePipelineRequest("dummy-continuation-key", configuration, parameters)

        assertRedacted(request, "configuration", secret)
        assertRedacted(request, "parameters", secret)
        assertFalse(request.toString().contains("deploy_token"))
        val json = v2Gson.toJsonTree(request).asJsonObject
        assertEquals(configuration, json["configuration"].asString)
        assertEquals(v2Gson.toJsonTree(parameters), json["parameters"])
    }

    test("redacts the rollback request parameters and preserves them in request JSON") {
        val secret = "dummy-rollback-token"
        val parameters = mapOf("deploy_token" to secret)
        val request = RollbackProjectRequest(
            environmentName = "production",
            componentName = "api",
            currentVersion = "1.0.0",
            targetVersion = "0.9.0",
            parameters = parameters
        )

        assertRedacted(request, "parameters", secret)
        assertFalse(request.toString().contains("deploy_token"))
        assertTrue(request.toString().contains("environmentName=production"))
        val json = v2Gson.toJsonTree(request).asJsonObject
        assertEquals(v2Gson.toJsonTree(parameters), json["parameters"])
    }

    test("redacts the webhook create credentials and preserves them in request JSON") {
        val secret = "dummy-request-signing-secret"
        val token = "dummy-create-token"
        val url = "https://receiver.example.test/hooks/$token"
        val request = CreateWebhookRequest(
            "example", url, secret, WebhookScopeRequest("project", "project-id"),
            listOf("workflow-completed"), verifyTls = false
        )

        assertRedacted(request, "signingSecret", secret)
        assertRedacted(request, "url", token)
        assertEquals(url, request.url)
        assertTrue(request.toString().contains("verifyTls=false"))
        val json = v2Gson.toJsonTree(request).asJsonObject
        assertEquals(secret, json["signing-secret"].asString)
        assertEquals(url, json["url"].asString)
        assertFalse(json["verify-tls"].asBoolean)
    }

    test("redacts the webhook update credentials and preserves them in request JSON") {
        val secret = "dummy-update-signing-secret"
        val token = "dummy-update-token"
        val url = "https://receiver.example.test/hooks/$token"
        val request = UpdateWebhookRequest(url = url, signingSecret = secret)

        assertRedacted(request, "signingSecret", secret)
        assertRedacted(request, "url", token)
        assertEquals(url, request.url)
        val json = v2Gson.toJsonTree(request).asJsonObject
        assertEquals(secret, json["signing-secret"].asString)
        assertEquals(url, json["url"].asString)
    }

    test("redacts the environment variable value and preserves it in request JSON") {
        val value = "dummy-environment-value"
        val request = CreateEnvVarRequest(name = "EXAMPLE", value = value)

        assertRedacted(request, "value", value)
        assertEquals(value, v2Gson.toJsonTree(request).asJsonObject["value"].asString)
    }

    test("redacts the v2 context environment variable value and preserves it in request JSON") {
        val value = "dummy-context-value"
        val request = V2AddEnvironmentVariableRequest(value)

        assertRedacted(request, "value", value)
        assertEquals(value, v2Gson.toJsonTree(request).asJsonObject["value"].asString)
    }

    test("redacts the v1 environment variable value and preserves it in request JSON") {
        val value = "dummy-v1-request-value"
        val request = V1AddEnvironmentVariableRequest("EXAMPLE", value)

        assertRedacted(request, "value", value)
        assertTrue(request.toString().contains("name=EXAMPLE"))
        val json = v1Gson.toJsonTree(request).asJsonObject
        assertEquals("EXAMPLE", json["name"].asString)
        assertEquals(value, json["value"].asString)
    }

    test("redacts the v1 environment variable value after deserializing response JSON") {
        val value = "dummy-v1-response-value"
        val json = """{"name":"EXAMPLE","value":"$value"}"""
        val response = v1Gson.fromJson(json, EnvironmentVariable::class.java)

        assertEquals("EXAMPLE", response.name)
        assertEquals(value, response.value)
        assertRedacted(response, "value", value)
        assertTrue(response.toString().contains("name=EXAMPLE"))
    }

    test("redacts the webhook response credentials and preserves them in JSON") {
        val secret = "dummy-response-signing-secret"
        val token = "dummy-response-token"
        val url = "https://receiver.example.test/hooks/$token"
        val webhook = Webhook(
            "webhook-id", "example", url, "created", "updated", secret,
            WebhookScope("project", "project-id"), listOf("workflow-completed")
        )

        assertRedacted(webhook, "signingSecret", secret)
        assertRedacted(webhook, "url", token)
        assertEquals(url, webhook.url)
        val json = v2Gson.toJsonTree(webhook).asJsonObject
        assertEquals(secret, json["signing_secret"].asString)
        assertEquals(url, json["url"].asString)
        assertEquals(url, v2Gson.fromJson(json, Webhook::class.java).url)
    }

    test("redacts a signed artifact download URL from the V1 artifact and preserves it in JSON") {
        val signature = "dummy-v1-artifact-signature"
        val url = "https://circle-production-customer-artifacts.s3.amazonaws.com/artifact" +
            "?AWSAccessKeyId=dummy-key&Expires=2000000000&Signature=$signature"
        val json = """{"node_index":0,"path":"tmp/output.log","pretty_path":"output.log","url":"$url"}"""
        val artifact = v1Gson.fromJson(json, Artifact::class.java)

        assertEquals(url, artifact.url)
        assertRedacted(artifact, "url", signature)
        assertEquals(url, v1Gson.toJsonTree(artifact).asJsonObject["url"].asString)
    }

    test("redacts a signed artifact download URL from the V2 artifact and preserves it in JSON") {
        val signature = "dummy-v2-artifact-signature"
        val url = "https://circle-production-customer-artifacts.s3.amazonaws.com/artifact" +
            "?AWSAccessKeyId=dummy-key&Expires=2000000000&Signature=$signature"
        val json = """{"url":"$url","path":"tmp/output.log","node_index":0}"""
        val artifact = v2Gson.fromJson(json, V2Artifact::class.java)

        assertEquals(url, artifact.url)
        assertRedacted(artifact, "url", signature)
        assertEquals(url, v2Gson.toJsonTree(artifact).asJsonObject["url"].asString)
    }
})

private fun assertRedacted(model: Any, field: String, credential: String) {
    val description = model.toString()
    assertFalse(description.contains(credential))
    assertTrue(description.contains("$field=[REDACTED]"))
}
