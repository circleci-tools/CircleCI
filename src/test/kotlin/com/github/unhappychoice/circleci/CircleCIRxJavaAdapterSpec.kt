package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.User
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.assertSame
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress

object CircleCIRxJavaAdapterSpec : Spek({
    test("delivers the decoded response on the subscribing thread") {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/v2/me") { exchange ->
            val body = """{"id":"user-id","login":"circleci-user","name":"CircleCI User"}""".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val client = CircleCIAPIClient("test-token")
                .retrofitV2("http://127.0.0.1:${server.address.port}/api/v2/")
                .create(CircleCIAPIClientV2::class.java)
            val subscribingThread = Thread.currentThread()
            client.getMe()
                .doOnNext { assertSame(subscribingThread, Thread.currentThread()) }
                .test()
                .assertResult(User("user-id", "circleci-user", "CircleCI User"))
        } finally {
            server.stop(0)
        }
    }
})
