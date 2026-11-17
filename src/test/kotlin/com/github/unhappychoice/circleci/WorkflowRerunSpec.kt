package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.RerunWorkflowRequest
import com.github.unhappychoice.circleci.v2.response.WorkflowId
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import io.reactivex.Observable
import org.junit.Assert.assertNotNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object WorkflowRerunSpec : Spek({
    test("creates a V2 client with the default endpoint") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    test("reruns a workflow with an empty JSON object when options are omitted") {
        assertRerun("{}") { it.rerunWorkflow("workflow-id") }
    }

    test("serializes explicit workflow rerun options") {
        assertRerun("""{"jobs":["job-id"],"from_failed":false,"sparse_tree":true}""") {
            it.rerunWorkflow(
                "workflow-id",
                RerunWorkflowRequest(jobs = listOf("job-id"), fromFailed = false, sparseTree = true)
            )
        }
    }
})

private fun assertRerun(expectedBody: String, rerun: (CircleCIAPIClientV2) -> Observable<WorkflowId>) {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/api/v2/workflow/workflow-id/rerun") { exchange ->
        exchange.use {
            val body = it.requestBody.bufferedReader().use { reader -> reader.readText() }
            val matches = it.requestMethod == "POST" &&
                it.requestURI.path == "/api/v2/workflow/workflow-id/rerun" &&
                JsonParser.parseString(body) == JsonParser.parseString(expectedBody)
            val response = """{"workflow_id":"rerun-id"}""".toByteArray()
            it.responseHeaders.set("Content-Type", "application/json")
            it.sendResponseHeaders(if (matches) 202 else 400, response.size.toLong())
            it.responseBody.write(response)
        }
    }
    server.start()
    try {
        val client = CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/")
        rerun(client).test().assertResult(WorkflowId("rerun-id"))
    } finally {
        server.stop(0)
    }
}
