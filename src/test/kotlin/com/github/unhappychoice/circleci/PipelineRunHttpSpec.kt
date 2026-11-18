package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.TriggerPipelineRunRequest
import com.github.unhappychoice.circleci.v2.response.PipelineRunResult
import com.google.gson.JsonParseException
import com.sun.net.httpserver.HttpServer
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertNotNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object PipelineRunHttpSpec : Spek({
    test("clientV2 can be created with the default endpoint") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    test("triggerPipelineRun returns the skip message for HTTP 200") {
        val message = "Ignoring pipeline due to CI skip in the commit"
        triggerPipelineRun(200, """{"message":"$message"}""")
            .assertResult(PipelineRunResult.Skipped(message))
    }

    test("triggerPipelineRun returns the created pipeline ID for HTTP 201") {
        val id = "5034460f-c7c4-4c43-9457-de07e2029e7b"
        val createdAt = "2019-08-24T14:15:22Z"
        val body = """{"id":"$id","number":25,"state":"created","created_at":"$createdAt"}"""
        triggerPipelineRun(201, body)
            .assertResult(PipelineRunResult.Created(id, 25, "created", createdAt))
    }

    test("triggerPipelineRun accepts a created pipeline with only an ID") {
        val id = "5034460f-c7c4-4c43-9457-de07e2029e7b"
        triggerPipelineRun(201, """{"id":"$id"}""")
            .assertResult(PipelineRunResult.Created(id))
    }

    test("triggerPipelineRun rejects a response without an ID or skip message") {
        triggerPipelineRun(200, "{}")
            .assertNoValues().assertNotComplete().assertError { error ->
                error is JsonParseException && error.message == "Expected a pipeline ID or skip message"
            }
    }
})

private fun triggerPipelineRun(status: Int, body: String): TestObserver<PipelineRunResult> {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/api/v2/project/gh/unhappychoice/circleci/pipeline/run") { exchange ->
        exchange.use {
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.write(bytes)
        }
    }
    server.start()
    try {
        val client = CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/")
        val request = TriggerPipelineRunRequest(definitionId = "definition-id")
        return client.triggerPipelineRun("gh", "unhappychoice", "circleci", request).test()
    } finally {
        server.stop(0)
    }
}
