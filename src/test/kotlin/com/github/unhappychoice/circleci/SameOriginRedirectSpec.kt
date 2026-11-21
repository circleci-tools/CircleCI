package com.github.unhappychoice.circleci

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okio.BufferedSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import java.net.ProtocolException
import java.util.concurrent.TimeUnit

object SameOriginRedirectSpec : Spek({
    val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
    val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
    val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
    val client = CircleCIAPIClient("test-token").okHttpV2.newBuilder()
        .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()
    val payload = """{"branch":"main"}"""
    val json = "application/json".toMediaType()

    fun withOrigin(block: (MockWebServer) -> Unit) = MockWebServer().use { origin ->
        origin.useHttps(serverTls.sslSocketFactory())
        origin.start()
        block(origin)
    }

    fun MockWebServer.redirect(code: Int = 302, location: String? = "/next") {
        val response = MockResponse.Builder().code(code)
        location?.let { response.addHeader("Location", it) }
        enqueue(response.build())
    }

    fun OkHttpClient.get(origin: MockWebServer) =
        newCall(Request.Builder().url(origin.url("/start")).build())

    listOf(302, 307, 308).forEach { code ->
        test("same-origin $code handles POST method and body") {
            withOrigin { origin ->
                origin.redirect(code)
                origin.enqueue(MockResponse())
                val request = Request.Builder().url(origin.url("/start")).post(payload.toRequestBody(json)).build()

                client.newCall(request).execute().use { assertEquals(200, it.code) }

                assertEquals(2, origin.requestCount)
                assertEquals(payload, origin.takeRequest().body?.utf8())
                val redirected = origin.takeRequest()
                assertEquals("/next", redirected.target)
                assertEquals("test-token", redirected.headers["Circle-Token"])
                if (code == 302) {
                    assertEquals("GET", redirected.method)
                    assertEquals(0L, redirected.bodySize)
                    listOf("Content-Type", "Content-Length", "Transfer-Encoding").forEach {
                        assertNull(redirected.headers[it])
                    }
                } else {
                    assertEquals("POST", redirected.method)
                    assertEquals(payload, redirected.body?.utf8())
                }
            }
        }
    }

    test("same-origin 307 does not replay a one-shot POST body") {
        withOrigin { origin ->
            origin.redirect(307)
            val body = object : RequestBody() {
                override fun contentType() = json
                override fun isOneShot() = true
                override fun writeTo(sink: BufferedSink) {
                    sink.writeUtf8(payload)
                }
            }
            val request = Request.Builder().url(origin.url("/start")).post(body).build()

            client.newCall(request).execute().use { assertEquals(307, it.code) }

            assertEquals(1, origin.requestCount)
            assertEquals(payload, origin.takeRequest().body?.utf8())
        }
    }

    listOf(null, "https://[invalid").forEach { location ->
        test("returns a redirect with ${if (location == null) "no" else "an invalid"} Location") {
            withOrigin { origin ->
                origin.redirect(location = location)
                client.get(origin).execute().use { assertEquals(302, it.code) }
                assertEquals(1, origin.requestCount)
            }
        }
    }

    test("returns the response after following 20 redirects") {
        withOrigin { origin ->
            repeat(20) { origin.redirect() }
            origin.enqueue(MockResponse(body = "done"))

            client.get(origin).execute().use { assertEquals("done", it.body.string()) }

            assertEquals(21, origin.requestCount)
        }
    }

    test("throws when the response after 20 redirects would redirect again") {
        withOrigin { origin ->
            repeat(21) { origin.redirect() }
            origin.enqueue(MockResponse())

            val error = assertThrows(ProtocolException::class.java) {
                client.get(origin).execute().close()
            }

            assertEquals("Too many redirects", error.message)
            assertEquals(21, origin.requestCount)
        }
    }
})
