package com.github.unhappychoice.circleci

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek

object AuthenticatedTransportSpec : Spek({
    val api = CircleCIAPIClient("test-token")
    val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
    val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
    val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()

    fun OkHttpClient.withTestTls() = newBuilder()
        .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager)
        .build()

    listOf(false, true).forEach { downgrade ->
        val destination = if (downgrade) "HTTP" else "another HTTPS origin"

        test("V2 GET does not forward its token to $destination") {
            MockWebServer().use { origin ->
                MockWebServer().use { target ->
                    origin.useHttps(serverTls.sslSocketFactory(), false)
                    if (!downgrade) target.useHttps(serverTls.sslSocketFactory(), false)
                    target.enqueue(MockResponse())
                    origin.enqueue(MockResponse().setResponseCode(302).addHeader("Location", target.url("/me")))

                    val request = Request.Builder().url(origin.url("/me")).build()
                    api.okHttpV2.withTestTls().newCall(request).execute().use { assertEquals(302, it.code) }

                    assertEquals("test-token", origin.takeRequest().getHeader("Circle-Token"))
                    assertEquals(1, origin.requestCount)
                    assertEquals(0, target.requestCount)
                }
            }
        }

        test("V1 POST does not forward its body on a 307 to $destination") {
            MockWebServer().use { origin ->
                MockWebServer().use { target ->
                    origin.useHttps(serverTls.sslSocketFactory(), false)
                    if (!downgrade) target.useHttps(serverTls.sslSocketFactory(), false)
                    target.enqueue(MockResponse())
                    origin.enqueue(MockResponse().setResponseCode(307).addHeader("Location", target.url("/ssh-key")))

                    val body = """{"private_key":"test-private-key"}"""
                    val request = Request.Builder().url(origin.url("/ssh-key")).post(body.toRequestBody()).build()
                    api.okHttpV1.withTestTls().newCall(request).execute().use { assertEquals(307, it.code) }

                    val received = origin.takeRequest()
                    assertEquals("Basic dGVzdC10b2tlbjo=", received.getHeader("Authorization"))
                    assertEquals(body, received.body.readUtf8())
                    assertEquals(1, origin.requestCount)
                    assertEquals(0, target.requestCount)
                }
            }
        }
    }

    test("V2 authenticates ordinary requests and follows redirects on the same HTTPS origin") {
        MockWebServer().use { origin ->
            origin.useHttps(serverTls.sslSocketFactory(), false)
            origin.enqueue(MockResponse())
            origin.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "/new-me"))
            origin.enqueue(MockResponse())

            val client = api.okHttpV2.withTestTls()
            val request = Request.Builder().url(origin.url("/me")).build()
            repeat(2) {
                client.newCall(request).execute().use { assertEquals(200, it.code) }
            }

            assertEquals(3, origin.requestCount)
            listOf("/me", "/me", "/new-me").forEach { path ->
                val received = origin.takeRequest()
                assertEquals(path, received.path)
                assertEquals("test-token", received.getHeader("Circle-Token"))
            }
        }
    }
})
