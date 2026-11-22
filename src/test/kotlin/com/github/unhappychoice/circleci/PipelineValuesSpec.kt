package com.github.unhappychoice.circleci

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object PipelineValuesSpec : Spek({
    test("clientV2 creates a client without a custom base URL") {
        assertNotNull(CircleCIAPIClient("test-token").clientV2())
    }

    test("getPipelineValuesById decodes top-level keys and their value types") {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        try {
            server.createContext("/api/v2/pipeline/pipeline-id/values") { exchange ->
                val body = """{"some.key":"some-value","different.key":true,"replicas":2,"marker":9007199254740993,"large":9223372036854775808}"""
                    .toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            server.start()

            val client = CircleCIAPIClient("test-token").clientV2("http://127.0.0.1:${server.address.port}/api/v2/")
            val values = client.getPipelineValuesById("pipeline-id").blockingFirst().values
            assertEquals(setOf("some.key", "different.key", "replicas", "marker", "large"), values.keys)
            assertEquals("some-value", values["some.key"])
            assertEquals(true, values["different.key"])
            assertEquals("2", assertInstanceOf(Number::class.java, values["replicas"]).toString())
            assertEquals("9007199254740993", assertInstanceOf(Number::class.java, values["marker"]).toString())
            assertEquals("9223372036854775808", assertInstanceOf(Number::class.java, values["large"]).toString())
        } finally {
            server.stop(0)
        }
    }
})
