package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.User
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@Suppress("DEPRECATION")
object CircleCIAPIClientAsyncSpec : Spek({
  test("the public constructor and async factory preserve the token and create clients for every API version") {
    listOf(CircleCIAPIClient.async("test-token"), CircleCIAPIClient("test-token")).forEach {
      assertEquals("test-token", it.token)
      assertTrue(CircleCIAPIClientV1::class.java.isInstance(it.client()))
      assertTrue(CircleCIAPIClientV1_1::class.java.isInstance(it.client1_1()))
      assertTrue(CircleCIAPIClientV2::class.java.isInstance(it.clientV2()))
    }
  }

  test("the synchronous constructor delivers the response on the subscribing thread before returning") {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/api/v2/me") { exchange -> exchange.use { it.respondWithUser() } }

    try {
      server.start()
      val client = CircleCIAPIClient("test-token", server.baseUrl()).clientV2()
      val subscribingThread = Thread.currentThread()
      client.getMe()
        .doOnNext { assertSame(subscribingThread, Thread.currentThread()) }
        .test()
        .assertResult(User("user-id", "octocat", "Octocat"))
    } finally {
      server.stop(0)
    }
  }

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
        it.respondWithUser()
      }
    }

    try {
      server.start()
      val client = CircleCIAPIClient.async("test-token", server.baseUrl()).clientV2()
      val subscription = subscribingThread.submit { client.getMe().subscribe(subscriber) }

      assertTrue(requestReceived.await(5, SECONDS), "The server should receive the request")
      subscription.get(5, SECONDS)
      subscriber.assertNoValues().assertNotComplete().assertNoErrors()

      releaseResponse.countDown()
      assertTrue(subscriber.await(5, SECONDS), "The subscriber should receive the response")
      subscriber.assertResult(User("user-id", "octocat", "Octocat"))
    } finally {
      releaseResponse.countDown()
      subscriber.dispose()
      server.stop(0)
      subscribingThread.shutdownNow()
    }
  }
})

private fun HttpServer.baseUrl() = "http://127.0.0.1:${address.port}/api/"

private fun HttpExchange.respondWithUser() {
  val body = """{"id":"user-id","login":"octocat","name":"Octocat"}""".toByteArray()
  responseHeaders.set("Content-Type", "application/json")
  sendResponseHeaders(200, body.size.toLong())
  responseBody.write(body)
}
