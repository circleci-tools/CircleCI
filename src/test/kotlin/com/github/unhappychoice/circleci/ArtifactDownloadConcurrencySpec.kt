package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import io.reactivex.rxjava3.subjects.SingleSubject
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockResponseBody
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okio.BufferedSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

object ArtifactDownloadConcurrencySpec : Spek({
    test("rejects nested downloads when all workers are consuming artifacts") {
        artifactServer().use { origin ->
            repeat(ARTIFACT_DOWNLOAD_LIMIT) { origin.enqueue(MockResponse(body = "outer")) }
            val api = artifactClient(origin)
            val entered = CountDownLatch(ARTIFACT_DOWNLOAD_LIMIT)
            val rejected = CountDownLatch(ARTIFACT_DOWNLOAD_LIMIT)
            val cleanup = SingleSubject.create<Int>()
            val downloads = List(ARTIFACT_DOWNLOAD_LIMIT) {
                api.downloadArtifact(artifactAt(origin, "/outer")) { outer ->
                    entered.countDown()
                    assertTrue(entered.await(5, SECONDS))
                    val error = assertThrows(RejectedExecutionException::class.java) {
                        val inner = api.downloadArtifact(artifactAt(origin, "/inner")) { it.read() }
                        cleanup.ambWith(inner).blockingGet()
                    }
                    rejected.countDown()
                    assertTrue(rejected.await(5, SECONDS))
                    assertEquals(
                        "Nested artifact download cannot start: all $ARTIFACT_DOWNLOAD_LIMIT download slots are in use",
                        error.message,
                    )
                    outer.reader().readText()
                }.test()
            }

            try {
                downloads.forEach { it.awaitDone(10, SECONDS).assertResult("outer") }
                assertEquals(ARTIFACT_DOWNLOAD_LIMIT, origin.requestCount)
            } finally {
                cleanup.onSuccess(-1)
                downloads.forEach { it.dispose() }
            }
        }
    }

    test("keeps active downloads at the limit while bodies are paused, and releasing one starts the next queued download") {
        artifactServer().use { origin ->
            val heldTargets = (0 until ARTIFACT_DOWNLOAD_LIMIT).map { "/held-$it" }
            val resumeGates = heldTargets.associateWith { CountDownLatch(1) }
            origin.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val gate = resumeGates[request.target] ?: return MockResponse(body = "ok")
                    return MockResponse.Builder().body(pausedBody(gate)).build()
                }
            }
            val api = artifactClient(origin)
            val held = heldTargets.map { target ->
                api.downloadArtifact(artifactAt(origin, target)) { it.readBytes() }.test()
            }
            try {
                val seen = held.map { origin.takeRequest(5, SECONDS)?.target }.toSet()
                assertEquals(heldTargets.toSet(), seen)

                val queued = api.downloadArtifact(artifactAt(origin, "/queued")) { it.readBytes() }.test()
                try {
                    assertNull(origin.takeRequest(200, MILLISECONDS))

                    resumeGates.values.first().countDown()
                    held.first().awaitDone(5, SECONDS).assertComplete().assertNoErrors()

                    assertEquals("/queued", origin.takeRequest(5, SECONDS)?.target)
                    queued.awaitDone(5, SECONDS).assertComplete().assertNoErrors()
                } finally {
                    queued.dispose()
                }
            } finally {
                resumeGates.values.forEach { it.countDown() }
                held.forEach { it.dispose() }
            }
        }
    }

    test("never sends a request for a download disposed while queued, and disposing an active download still cancels its network read") {
        artifactServer().use { origin ->
            val heldTargets = (0 until ARTIFACT_DOWNLOAD_LIMIT).map { "/held-$it" }
            val resumeGates = heldTargets.associateWith { CountDownLatch(1) }
            origin.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val gate = resumeGates[request.target] ?: return MockResponse(body = "ok")
                    return MockResponse.Builder().body(pausedBody(gate)).build()
                }
            }
            val api = artifactClient(origin)
            val cancelledRead = CountDownLatch(1)
            val activeToCancel = api.downloadArtifact(artifactAt(origin, heldTargets.first())) {
                assertEquals(PAUSED_BODY_FIRST[0].code, it.read())
                try {
                    it.copyTo(OutputStream.nullOutputStream())
                } catch (error: IOException) {
                    cancelledRead.countDown()
                    throw error
                }
            }.test()
            val otherHeld = heldTargets.drop(1).map { target ->
                api.downloadArtifact(artifactAt(origin, target)) { it.readBytes() }.test()
            }
            try {
                repeat(heldTargets.size) { origin.takeRequest(5, SECONDS) }

                val disposedQueued = api.downloadArtifact(artifactAt(origin, "/disposed-queued")) { it.readBytes() }.test()
                val queued = api.downloadArtifact(artifactAt(origin, "/queued")) { it.readBytes() }.test()
                try {
                    disposedQueued.dispose()
                    assertNull(origin.takeRequest(200, MILLISECONDS))

                    activeToCancel.dispose()
                    assertTrue(cancelledRead.await(5, SECONDS))

                    assertEquals("/queued", origin.takeRequest(5, SECONDS)?.target)
                    queued.awaitDone(5, SECONDS).assertComplete().assertNoErrors()
                    assertNull(origin.takeRequest(200, MILLISECONDS))
                } finally {
                    disposedQueued.dispose()
                    queued.dispose()
                }
            } finally {
                resumeGates.values.forEach { it.countDown() }
                activeToCancel.dispose()
                otherHeld.forEach { it.dispose() }
            }
        }
    }
})

private val concurrencyCertificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
private val concurrencyServerTls = HandshakeCertificates.Builder().heldCertificate(concurrencyCertificate).build()
private val concurrencyClientTls =
    HandshakeCertificates.Builder().addTrustedCertificate(concurrencyCertificate.certificate).build()

private fun artifactServer() = MockWebServer().apply {
    useHttps(concurrencyServerTls.sslSocketFactory())
    start()
}

private fun artifactClient(origin: MockWebServer): CircleCIAPIClient {
    val transport = OkHttpClient.Builder()
        .sslSocketFactory(concurrencyClientTls.sslSocketFactory(), concurrencyClientTls.trustManager)
        .build()
    return CircleCIAPIClient("test-token", origin.url("/api/").toString(), transport)
}

private fun artifactAt(server: MockWebServer, path: String) = Artifact(server.url(path).toString(), "report", 0)

private const val PAUSED_BODY_FIRST = "first"
private const val PAUSED_BODY_REST = "-rest"

private fun pausedBody(resume: CountDownLatch): MockResponseBody = object : MockResponseBody {
    override val contentLength: Long = (PAUSED_BODY_FIRST.length + PAUSED_BODY_REST.length).toLong()

    override fun writeTo(sink: BufferedSink) {
        sink.writeUtf8(PAUSED_BODY_FIRST)
        sink.flush()
        resume.await()
        sink.writeUtf8(PAUSED_BODY_REST)
    }
}
