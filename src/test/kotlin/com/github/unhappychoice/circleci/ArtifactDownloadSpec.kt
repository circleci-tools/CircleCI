package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okio.Buffer
import org.junit.jupiter.api.Assertions.*
import org.spekframework.spek2.Spek
import java.io.IOException
import java.io.InputStream
import java.net.ProtocolException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

object ArtifactDownloadSpec : Spek({
    test("streams a listed binary artifact through relative and cross-origin redirects") {
        artifactServer().use { origin ->
            artifactServer().use { storage ->
                val bytes = byteArrayOf(0, 1, -1, 10)
                val url = origin.url("/artifact")
                origin.enqueue(MockResponse(body = """{"items":[{"url":"$url","path":"report","node_index":0}]}"""))
                origin.enqueue(redirect("/next", 307))
                origin.enqueue(redirect(storage.url("/blob?signature=abc").toString()))
                storage.enqueue(MockResponse.Builder().body(Buffer().write(bytes)).build())

                val api = artifactClient(origin)
                val artifact = api.clientV2().getJobArtifacts("gh/org/repo", "123").blockingSingle().items.single()
                val download = api.downloadArtifact(artifact) { it.readBytes() }
                assertEquals(1, origin.requestCount)
                assertArrayEquals(bytes, download.blockingGet())

                repeat(3) { assertEquals("test-token", origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"]) }
                val received = storage.takeRequest(5, SECONDS)!!
                assertEquals("/blob?signature=abc", received.target)
                assertNull(received.headers["Circle-Token"])
                assertNull(received.headers["Authorization"])
            }
        }
    }

    test("an unauthenticated download stays unauthenticated when redirected to the API origin") {
        artifactServer().use { origin ->
            artifactServer().use { storage ->
                storage.enqueue(redirect(origin.url("/artifact").toString()))
                origin.enqueue(MockResponse(body = "data"))

                assertEquals("data", artifactClient(origin).downloadArtifact(artifactAt(storage)) {
                    it.reader().readText()
                }.blockingGet())

                assertNull(storage.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
                assertNull(origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
            }
        }
    }

    test("closes the stream after the consumer returns or throws") {
        artifactServer().use { origin ->
            origin.enqueue(MockResponse(body = "data"))
            origin.enqueue(MockResponse(body = "data"))
            val api = artifactClient(origin)
            lateinit var stream: InputStream

            assertEquals(100, api.downloadArtifact(artifactAt(origin)) {
                stream = it
                it.read()
            }.blockingGet())
            assertThrows(IOException::class.java) { stream.read() }

            val failure = IOException("Consumer failed")
            api.downloadArtifact(artifactAt(origin)) {
                stream = it
                throw failure
            }.test().awaitDone(5, SECONDS).assertError { it === failure }
            assertThrows(IOException::class.java) { stream.read() }
        }
    }

    test("reports HTTP errors and rejects insecure redirects before consuming bytes") {
        artifactServer().use { origin ->
            MockWebServer().use { target ->
                target.start()
                origin.enqueue(MockResponse(code = 404))
                origin.enqueue(redirect(target.url("/insecure").toString()))
                val download = artifactClient(origin).downloadArtifact(artifactAt(origin)) { it.read() }

                download.test().awaitDone(5, SECONDS).assertNoValues().assertError {
                    it is IOException && it.message == "Artifact download failed with HTTP 404"
                }
                download.test().awaitDone(5, SECONDS).assertNoValues().assertError(ProtocolException::class.java)
                assertEquals(0, target.requestCount)
            }
        }
    }

    test("delivers the first bytes before the full body and cancels pending reads on disposal") {
        artifactServer().use { origin ->
            origin.enqueue(MockResponse.Builder().body("x".repeat(32_768))
                .throttleBody(8192, 30, SECONDS).build())
            val reading = CountDownLatch(1)
            val cancelledRead = CountDownLatch(1)
            val observer = artifactClient(origin).downloadArtifact(artifactAt(origin)) {
                assertEquals(120, it.read())
                reading.countDown()
                try {
                    it.copyTo(java.io.OutputStream.nullOutputStream())
                } catch (error: IOException) {
                    cancelledRead.countDown()
                    throw error
                }
            }.test()

            try {
                assertTrue(reading.await(5, SECONDS))
            } finally {
                observer.dispose()
            }
            assertTrue(cancelledRead.await(5, SECONDS))
            observer.assertNoValues().assertNoErrors().assertNotComplete()
        }
    }

    test("rejects invalid artifact URLs without sending a request") {
        artifactServer().use { origin ->
            val api = artifactClient(origin)
            val url = origin.url("/artifact")
            listOf("not a URL", url.newBuilder().scheme("http").build().toString(),
                url.newBuilder().username("user").build().toString(),
                url.newBuilder().password("secret").build().toString()).forEach { invalid ->
                api.downloadArtifact(Artifact(invalid, "report", 0)) { it.read() }
                    .test().assertNoValues().assertError(IllegalArgumentException::class.java)
            }
            assertEquals(0, origin.requestCount)
        }
    }

    test("does not send a request when disposed on subscription") {
        artifactServer().use { origin ->
            artifactClient(origin).downloadArtifact(artifactAt(origin)) { it.read() }
                .test(true).assertNoValues().assertNoErrors().assertNotComplete()
            assertNull(origin.takeRequest(100, MILLISECONDS))
        }
    }

    test("rejects redirects with a missing or unresolvable location") {
        artifactServer().use { origin ->
            origin.enqueue(MockResponse(code = 302))
            origin.enqueue(redirect("https://[invalid]/"))
            val download = artifactClient(origin).downloadArtifact(artifactAt(origin)) { it.read() }
            repeat(2) {
                download.test().awaitDone(5, SECONDS).assertNoValues().assertError {
                    it is ProtocolException && it.message == "Invalid artifact redirect URL"
                }
            }
            assertEquals(2, origin.requestCount)
        }
    }

    test("follows at most twenty redirects") {
        artifactServer().use { origin ->
            repeat(20) { origin.enqueue(redirect("/artifact")) }
            origin.enqueue(MockResponse(body = "ok"))
            repeat(21) { origin.enqueue(redirect("/artifact")) }
            val download = artifactClient(origin).downloadArtifact(artifactAt(origin)) { it.reader().readText() }
            assertEquals("ok", download.blockingGet())
            download.test().awaitDone(5, SECONDS).assertNoValues().assertError {
                it is ProtocolException && it.message == "Too many artifact redirects"
            }
            assertEquals(42, origin.requestCount)
        }
    }

    test("authenticates only documented CircleCI HTTPS origins") {
        val apiUrl = "https://circleci.com/api/".toHttpUrl()
        listOf("https://circleci.com/a", "https://dl.circleci.com/a",
            "https://output.circle-artifacts.com/a", "https://24-88881093-gh.circle-artifacts.com/a").forEach {
            assertTrue(canAuthenticateArtifact(it.toHttpUrl(), apiUrl), it)
        }
        listOf("http://dl.circleci.com/a", "https://dl.circleci.com:8443/a", "https://user@dl.circleci.com/a",
            "https://circle-artifacts.com/a", "https://dl.circleci.com.example.org/a",
            "https://fake-circle-artifacts.com/a", "https://bucket.s3.amazonaws.com/a").forEach {
            assertFalse(canAuthenticateArtifact(it.toHttpUrl(), apiUrl), it)
        }
        assertFalse(canAuthenticateArtifact("https://circleci.com/a".toHttpUrl(), "http://circleci.com:443/".toHttpUrl()))
    }
})

private val artifactCertificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
private val artifactServerTls = HandshakeCertificates.Builder().heldCertificate(artifactCertificate).build()
private val artifactClientTls = HandshakeCertificates.Builder().addTrustedCertificate(artifactCertificate.certificate).build()

private fun artifactServer() = MockWebServer().apply {
    useHttps(artifactServerTls.sslSocketFactory())
    start()
}

private fun artifactClient(origin: MockWebServer): CircleCIAPIClient {
    val transport = OkHttpClient.Builder()
        .sslSocketFactory(artifactClientTls.sslSocketFactory(), artifactClientTls.trustManager)
        .build()
    return CircleCIAPIClient("test-token", origin.url("/api/").toString(), transport)
}

private fun artifactAt(server: MockWebServer) = Artifact(server.url("/artifact").toString(), "report", 0)

private fun redirect(location: String, status: Int = 302) =
    MockResponse(code = status, headers = headersOf("Location", location))
