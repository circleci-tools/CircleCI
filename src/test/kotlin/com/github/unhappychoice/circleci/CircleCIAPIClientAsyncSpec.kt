package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.User
import com.sun.net.httpserver.HttpServer
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertTrue
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

object CircleCIAPIClientAsyncSpec : Spek({
  test("subscribe returns while the server is withholding the response") {
    val requestReceived = CountDownLatch(1)
    val releaseResponse = CountDownLatch(1)
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    val subscriber = TestObserver<User>()
    val subscribingThread = Executors.newSingleThreadExecutor()

    server.createContext("/api/v2/me") { exchange ->
      exchange.use {
        requestReceived.countDown()
        releaseResponse.await()
        val body = """{"id":"user-id","login":"octocat","name":"Octocat"}""".toByteArray()
        it.responseHeaders.set("Content-Type", "application/json")
        it.sendResponseHeaders(200, body.size.toLong())
        it.responseBody.write(body)
      }
    }

    try {
      server.start()
      val client = CircleCIAPIClient.async("test-token", "http://127.0.0.1:${server.address.port}/api/").clientV2()
      val subscription = subscribingThread.submit { client.getMe().subscribe(subscriber) }

      assertTrue("The server should receive the request", requestReceived.await(5, SECONDS))
      subscription.get(5, SECONDS)
      subscriber.assertNoValues().assertNotComplete().assertNoErrors()

      releaseResponse.countDown()
      assertTrue("The subscriber should receive the response", subscriber.await(5, SECONDS))
      subscriber.assertResult(User("user-id", "octocat", "Octocat"))
    } finally {
      releaseResponse.countDown()
      subscriber.dispose()
      server.stop(0)
      subscribingThread.shutdownNow()
    }
  }
})
