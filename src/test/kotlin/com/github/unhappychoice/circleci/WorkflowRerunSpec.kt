package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.RerunWorkflowRequest
import com.github.unhappychoice.circleci.v2.response.WorkflowId
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object WorkflowRerunSpec : Spek({
    test("reruns a workflow with an empty JSON object when options are omitted") {
        assertRerun("{}") { it.rerunWorkflow("workflow-id") }
    }

    test("reruns a workflow with omitted options through the JVM compatibility bridge") {
        val clientType = CircleCIAPIClientV2::class.java
        val bridge = clientType.declaredClasses.single { it.simpleName == "DefaultImpls" }
        val rerunWithDefaults = bridge.getMethod(
            "rerunWorkflow\$default",
            clientType, String::class.java, RerunWorkflowRequest::class.java, Int::class.javaPrimitiveType, Any::class.java
        )
        val omittedRequestMask = 0b10

        assertRerun("{}") { client ->
            @Suppress("UNCHECKED_CAST")
            (rerunWithDefaults.invoke(null, client, "workflow-id", null, omittedRequestMask, null) as Observable<WorkflowId>)
        }
    }

    test("serializes explicit workflow rerun options") {
        assertRerun("""{"jobs":["job-id"],"from_failed":false,"sparse_tree":true}""") {
            it.rerunWorkflow(
                "workflow-id",
                RerunWorkflowRequest(jobs = listOf("job-id"), fromFailed = false, sparseTree = true)
            )
        }
    }

    test("reruns selected jobs with SSH enabled") {
        assertRerun("""{"jobs":["job-id"],"enable_ssh":true}""") {
            it.rerunWorkflow("workflow-id", RerunWorkflowRequest(jobs = listOf("job-id"), enableSsh = true))
        }
    }

    test("retains the original JVM constructors for compiled callers") {
        val requestType = RerunWorkflowRequest::class.java
        val constructor = requestType.getConstructor(*legacyOptionTypes)
        val defaultConstructor = requestType.getConstructor(
            *legacyOptionTypes, Int::class.javaPrimitiveType, Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
        )

        assertEquals(RerunWorkflowRequest(), requestType.getConstructor().newInstance())
        assertEquals(
            RerunWorkflowRequest(listOf("job-id"), false, true),
            constructor.newInstance(listOf("job-id"), false, true)
        )
        assertEquals(
            RerunWorkflowRequest(fromFailed = false),
            defaultConstructor.newInstance(null, false, null, 0b101, null)
        )
    }

    test("retains the original JVM copy methods for compiled callers") {
        val requestType = RerunWorkflowRequest::class.java
        val copy = requestType.getMethod("copy", *legacyOptionTypes)
        val copyWithDefaults = requestType.getMethod(
            "copy\$default", requestType, *legacyOptionTypes, Int::class.javaPrimitiveType, Any::class.java
        )
        val request = RerunWorkflowRequest(listOf("job-id"), fromFailed = false, sparseTree = true, enableSsh = true)

        assertEquals(request.copy(fromFailed = null, sparseTree = false), copy.invoke(request, request.jobs, null, false))
        assertEquals(
            request.copy(sparseTree = false),
            copyWithDefaults.invoke(null, request, null, true, false, 0b011, null)
        )
    }
})

private val legacyOptionTypes = arrayOf(List::class.java, Boolean::class.javaObjectType, Boolean::class.javaObjectType)

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
