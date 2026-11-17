package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.PipelineValues
import com.sun.net.httpserver.HttpServer
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object PipelineValuesSpec : Spek({
    test("getPipelineValuesById decodes top-level keys and their value types") {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        try {
            server.createContext("/api/v2/pipeline/pipeline-id/values") { exchange ->
                val body = """{"some.key":"some-value","different.key":true}""".toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            server.start()

            val client = CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/")
            client.getPipelineValuesById("pipeline-id").test()
                .assertResult(PipelineValues(mapOf("some.key" to "some-value", "different.key" to true)))
        } finally {
            server.stop(0)
        }
    }
})
