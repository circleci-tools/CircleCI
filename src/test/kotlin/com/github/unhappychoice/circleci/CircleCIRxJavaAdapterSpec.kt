package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.response.User as V1User
import com.github.unhappychoice.circleci.v2.response.User as V2User
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertSame
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object CircleCIRxJavaAdapterSpec : Spek({
    test("delivers the decoded V1 response on the subscribing thread") {
        val user = V1User("avatar", "circleci-user", "CircleCI User", emptyMap(), "user@example.com", "pusher")
        val json = """{"avatar_url":"avatar","login":"circleci-user","name":"CircleCI User",
            "projects":{},"selected_email":"user@example.com","pusher_id":"pusher"}"""
        withMeResponse("/api/v1/", json) { baseUrl ->
            val client = CircleCIAPIClient("test-token").retrofitV1(baseUrl).create(CircleCIAPIClientV1::class.java)
            val subscribingThread = Thread.currentThread()
            client.getMe()
                .doOnNext { assertSame(subscribingThread, Thread.currentThread()) }
                .test()
                .assertResult(user)
        }
    }

    test("delivers the decoded V2 response on the subscribing thread") {
        val json = """{"id":"user-id","login":"circleci-user","name":"CircleCI User"}"""
        withMeResponse("/api/v2/", json) { baseUrl ->
            val client = CircleCIAPIClient("test-token").retrofitV2(baseUrl).create(CircleCIAPIClientV2::class.java)
            val subscribingThread = Thread.currentThread()
            client.getMe()
                .doOnNext { assertSame(subscribingThread, Thread.currentThread()) }
                .test()
                .assertResult(V2User("user-id", "circleci-user", "CircleCI User"))
        }
    }
})

private fun withMeResponse(path: String, json: String, test: (String) -> Unit) {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("${path}me") { exchange ->
        val body = json.toByteArray()
        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseBody.use { it.write(body) }
    }
    server.start()
    try {
        test("http://127.0.0.1:${server.address.port}$path")
    } finally {
        server.stop(0)
    }
}
