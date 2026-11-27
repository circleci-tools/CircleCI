package com.github.unhappychoice.circleci

import com.sun.net.httpserver.HttpServer
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

private val NON_ASCII_CHARACTER = 233.toChar()

object CircleCIAPIClientTokenSpec : Spek({
    group("V1 API token") {
        mapOf("a trailing newline" to '\n', "a non-ASCII character" to NON_ASCII_CHARACTER).forEach { (description, character) ->
            test("rejects a token with $description without revealing the token") {
                val error = assertThrows(IllegalArgumentException::class.java) {
                    @Suppress("DEPRECATION")
                    CircleCIAPIClient("secret-test-token$character").client()
                }

                assertEquals("CircleCI API token contains invalid header characters", error.message)
                assertNull(error.cause)
                assertEquals(0, error.suppressed.size)
            }

            test("rejects a token with $description from client1_1()") {
                val error = assertThrows(IllegalArgumentException::class.java) {
                    CircleCIAPIClient("secret-test-token$character").client1_1()
                }

                assertEquals("CircleCI API token contains invalid header characters", error.message)
            }
        }

        test("rejects an invalid token when accessing the internal V1 transport") {
            val api = CircleCIAPIClient("secret-test-token\n")
            val error = assertThrows(IllegalArgumentException::class.java) {
                api.okHttpV1
            }

            assertEquals("CircleCI API token contains invalid header characters", error.message)
        }

        test("accepts a token with an embedded tab through the public V1 client factory") {
            @Suppress("DEPRECATION")
            CircleCIAPIClient("secret\ttest-token").client()
        }

        test("sends a valid token unchanged in the Basic-auth header") {
            val token = "AbC-123_xYz"
            val receivedAuthorization = AtomicReference<String>()
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            server.createContext("/api/v1/me") { exchange ->
                exchange.use {
                    receivedAuthorization.set(it.requestHeaders.getFirst("Authorization"))
                    val response = "{}".toByteArray()
                    it.sendResponseHeaders(200, response.size.toLong())
                    it.responseBody.write(response)
                }
            }
            server.start()

            try {
                val api = CircleCIAPIClient(token, "http://127.0.0.1:${server.address.port}/api/")
                val request = okhttp3.Request.Builder()
                    .url("http://127.0.0.1:${server.address.port}/api/v1/me")
                    .build()
                api.okHttpV1.newCall(request).execute().use {}

                val expected = "Basic ${String(Base64.encodeBase64("$token:".toByteArray()))}"
                assertEquals(expected, receivedAuthorization.get())
            } finally {
                server.stop(0)
            }
        }
    }

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

        test("rejects an invalid token when accessing the internal V2 transport") {
            val api = CircleCIAPIClient("secret-test-token\n")
            val error = assertThrows(IllegalArgumentException::class.java) {
                api.okHttpV2
            }

            assertEquals("CircleCI API token contains invalid header characters", error.message)
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
