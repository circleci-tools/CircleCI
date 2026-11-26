package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIServerSpec : Spek({
    val token = "server-token"
    val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
    val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
    val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
    val transport = OkHttpClient.Builder()
        .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager)
        .build()

    test("constructs both public V2 clients for an HTTPS origin with an optional port or trailing slash") {
        CircleCIAPIClient.server(token, "https://ci.example.com")
        CircleCIAPIClient.serverAsync(token, "https://ci.example.com:8443/")
    }

    listOf(
        "ci.example.com", "http://ci.example.com", "https:///ci.example.com",
        "https://ci.example.com:65536", "https://ci.example.com/team/",
        "https://ci.example.com/api/v2/", "https://ci.example.com/a/..",
        "https://user@ci.example.com", "https://ci.example.com/?", "https://ci.example.com/#",
        "https://ci.example.com/invalid path",
    ).forEach { url ->
        test("rejects $url in both Server factories") {
            assertThrows(IllegalArgumentException::class.java) { CircleCIAPIClient.server(token, url) }
            assertThrows(IllegalArgumentException::class.java) { CircleCIAPIClient.serverAsync(token, url) }
        }
    }

    listOf(false, true).forEach { asynchronous ->
        val mode = if (asynchronous) "asynchronous" else "synchronous"

        test("sends authenticated V2 workflow and job requests in $mode mode") {
            MockWebServer().use { origin ->
                origin.useHttps(serverTls.sslSocketFactory())
                origin.start()
                origin.enqueue(MockResponse(body = """{"items":[],"next_page_token":"next"}"""))
                origin.enqueue(MockResponse(body = """{"name":"build","number":123,"status":"success"}"""))
                val url = origin.url("/").toString()
                val client = CircleCIAPIClient.server(
                    token, if (asynchronous) url else url.removeSuffix("/"), asynchronous, transport,
                )
                val caller = Thread.currentThread()
                val page = client.getWorkflowJobs("workflow-id")
                    .doOnNext { assertEquals(asynchronous, Thread.currentThread() !== caller) }
                    .blockingSingle()
                assertEquals("next", page.nextPageToken)
                assertEquals("build", client.getJobDetails("gh/org/repo", "123").blockingSingle().name)

                listOf("/api/v2/workflow/workflow-id/job", "/api/v2/project/gh%2Forg%2Frepo/job/123").forEach { path ->
                    val request = origin.takeRequest(5, SECONDS)!!
                    assertEquals(path, request.target)
                    assertEquals(token, request.headers["Circle-Token"])
                }
            }
        }

        test("lists a job's artifacts and streams a same-origin artifact with the instance token in $mode mode") {
            MockWebServer().use { origin ->
                origin.useHttps(serverTls.sslSocketFactory())
                origin.start()
                val artifactUrl = origin.url("/artifact")
                origin.enqueue(MockResponse(body = """{"items":[{"url":"$artifactUrl","path":"report","node_index":0}]}"""))
                origin.enqueue(MockResponse(body = "artifact-body"))
                val client = CircleCIAPIClient.server(token, origin.url("/").toString(), asynchronous, transport)

                val artifact = client.getJobArtifacts("gh/org/repo", "123").blockingSingle().items.single()
                val body = client.downloadArtifact(artifact) { it.reader().readText() }.blockingGet()
                assertEquals("artifact-body", body)

                assertEquals(token, origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
                assertEquals(token, origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
            }
        }

        test("drops the instance token when a downloaded artifact redirects to another origin in $mode mode") {
            MockWebServer().use { origin ->
                MockWebServer().use { target ->
                    origin.useHttps(serverTls.sslSocketFactory())
                    target.useHttps(serverTls.sslSocketFactory())
                    origin.start()
                    target.start()
                    origin.enqueue(redirect(target.url("/blob").toString()))
                    target.enqueue(MockResponse(body = "blob"))
                    val client = CircleCIAPIClient.server(token, origin.url("/").toString(), asynchronous, transport)
                    val artifact = Artifact(origin.url("/artifact").toString(), "report", 0)

                    val body = client.downloadArtifact(artifact) { it.reader().readText() }.blockingGet()
                    assertEquals("blob", body)

                    assertEquals(token, origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
                    assertNull(target.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
                }
            }
        }

        test("blocks a cross-origin HTTPS redirect in $mode mode") {
            MockWebServer().use { origin ->
                MockWebServer().use { target ->
                    origin.useHttps(serverTls.sslSocketFactory())
                    target.useHttps(serverTls.sslSocketFactory())
                    origin.start()
                    target.start()
                    target.enqueue(MockResponse(body = """{"items":[]}"""))
                    origin.enqueue(MockResponse(code = 302, headers = headersOf("Location", target.url("/").toString())))
                    val client = CircleCIAPIClient.server(token, origin.url("/").toString(), asynchronous, transport)

                    client.getWorkflowJobs("workflow-id").test().awaitDone(5, SECONDS)
                        .assertError { it is HttpException && it.code() == 302 }
                    assertEquals(token, origin.takeRequest(5, SECONDS)!!.headers["Circle-Token"])
                    assertEquals(0, target.requestCount)
                }
            }
        }
    }
})

private fun redirect(location: String, status: Int = 302) =
    MockResponse(code = status, headers = headersOf("Location", location))
