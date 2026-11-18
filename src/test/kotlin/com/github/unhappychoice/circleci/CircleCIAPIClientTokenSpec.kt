package com.github.unhappychoice.circleci

import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

object CircleCIAPIClientTokenSpec : Spek({
    group("V2 API token") {
        mapOf("a trailing newline" to '\n', "a non-ASCII character" to '\u00E9').forEach { (description, character) ->
            test("rejects a token with $description without revealing the token") {
                val error = assertThrows(IllegalArgumentException::class.java) {
                    CircleCIAPIClient("secret-test-token$character").clientV2()
                }

                assertEquals("CircleCI API token contains invalid header characters", error.message)
                assertNull(error.cause)
                assertEquals(0, error.suppressed.size)
            }
        }

        test("accepts a token with an embedded tab through the public V2 client factory") {
            CircleCIAPIClient("secret\ttest-token").clientV2()
        }

        test("sends a valid token unchanged in the Circle-Token header") {
            val token = "AbC-123_xYz"
            val receivedToken = AtomicReference<String>()
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            server.createContext("/api/v2/me") { exchange ->
                exchange.use {
                    receivedToken.set(it.requestHeaders.getFirst("Circle-Token"))
                    val response = """{"id":"user-id","login":"test-user","name":"Test User"}""".toByteArray()
                    it.sendResponseHeaders(200, response.size.toLong())
                    it.responseBody.write(response)
                }
            }
            server.start()

            try {
                val client = CircleCIAPIClient(token).clientV2("http://127.0.0.1:${server.address.port}/api/v2/")
                client.getMe().test().assertComplete().assertNoErrors()
                assertEquals(token, receivedToken.get())
            } finally {
                server.stop(0)
            }
        }
    }
})
