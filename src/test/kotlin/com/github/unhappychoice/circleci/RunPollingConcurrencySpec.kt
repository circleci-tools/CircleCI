package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.response.RunResponse
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RunPollingConcurrencySpec : Spek({
    test("synchronous polls queue at the limit and skip expired and disposed waits") {
        val releaseFirst = CountDownLatch(1)
        val releaseOthers = CountDownLatch(1)
        val heldTargets = (0 until SYNCHRONOUS_POLL_LIMIT).map { "/api/v3/runs/held-$it" }
        MockWebServer().use { server ->
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    when (request.target) {
                        heldTargets.first() -> releaseFirst.await(15, SECONDS)
                        in heldTargets -> releaseOthers.await(15, SECONDS)
                    }
                    return endedRun()
                }
            }
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()
            val held = (0 until SYNCHRONOUS_POLL_LIMIT).map {
                client.waitForRun("held-$it", 1.minutes, 1.minutes).test()
            }
            try {
                val targets = held.map { server.takeRequest(5, SECONDS)?.target }.toSet()
                assertEquals(heldTargets.toSet(), targets)

                val expired = client.waitForRun("expired", 1.minutes, 1.seconds).test()
                val disposed = client.waitForRun("disposed", 1.minutes, 1.minutes).test()
                val active = client.waitForRun("active", 1.minutes, 1.minutes).test()
                try {
                    disposed.dispose()
                    expired.awaitDone(5, SECONDS).assertFailure(TimeoutException::class.java)
                    assertNull(server.takeRequest(200, MILLISECONDS))

                    releaseFirst.countDown()
                    assertEquals("/api/v3/runs/active", server.takeRequest(5, SECONDS)?.target)
                    active.awaitDone(5, SECONDS).assertComplete().assertNoErrors().assertValueCount(1)
                    assertNull(server.takeRequest(200, MILLISECONDS))
                } finally {
                    expired.dispose()
                    disposed.dispose()
                    active.dispose()
                }
            } finally {
                releaseFirst.countDown()
                releaseOthers.countDown()
                held.forEach { it.dispose() }
            }
        }
    }

    test("a blocked success callback leaves a poll worker free for another wait") {
        val calledBack = CountDownLatch(SYNCHRONOUS_POLL_LIMIT)
        val releaseCallback = CountDownLatch(1)
        MockWebServer().use { server ->
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest) = endedRun()
            }
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()
            val held = (0 until SYNCHRONOUS_POLL_LIMIT).map {
                client.waitForRun("ended-$it", 1.minutes, 1.minutes)
                    .doOnSuccess {
                        calledBack.countDown()
                        releaseCallback.await(15, SECONDS)
                    }
                    .test()
            }
            try {
                repeat(SYNCHRONOUS_POLL_LIMIT) { server.takeRequest(5, SECONDS) }
                assertTrue(calledBack.await(5, SECONDS))

                val other = client.waitForRun("other", 1.minutes, 1.minutes).test()
                try {
                    assertEquals("/api/v3/runs/other", server.takeRequest(5, SECONDS)?.target)
                    other.awaitDone(5, SECONDS).assertComplete().assertNoErrors().assertValueCount(1)
                } finally {
                    other.dispose()
                }
            } finally {
                releaseCallback.countDown()
                held.forEach { it.awaitDone(5, SECONDS) }
            }
        }
    }

    test("a blocked HTTP error callback leaves a poll worker free for another wait") {
        val calledBack = CountDownLatch(SYNCHRONOUS_POLL_LIMIT)
        val releaseCallback = CountDownLatch(1)
        MockWebServer().use { server ->
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest) =
                    if (request.target == "/api/v3/runs/other") endedRun() else MockResponse(code = 500)
            }
            server.start()
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()
            val held = (0 until SYNCHRONOUS_POLL_LIMIT).map {
                client.waitForRun("failing-$it", 1.minutes, 1.minutes)
                    .doOnError {
                        calledBack.countDown()
                        releaseCallback.await(15, SECONDS)
                    }
                    .test()
            }
            try {
                repeat(SYNCHRONOUS_POLL_LIMIT) { server.takeRequest(5, SECONDS) }
                assertTrue(calledBack.await(5, SECONDS))

                val other = client.waitForRun("other", 1.minutes, 1.minutes).test()
                try {
                    assertEquals("/api/v3/runs/other", server.takeRequest(5, SECONDS)?.target)
                    other.awaitDone(5, SECONDS).assertComplete().assertNoErrors().assertValueCount(1)
                } finally {
                    other.dispose()
                }
            } finally {
                releaseCallback.countDown()
                held.forEach { it.awaitDone(5, SECONDS) }
            }
        }
    }

    test("a blocked timeout handler leaves another wait free to repeat and time out") {
        val handlerEntered = CountDownLatch(1)
        val releaseHandler = CountDownLatch(1)
        val otherPolls = CountDownLatch(2)
        val client = object : CircleCIAPIClientV3 by CircleCIAPIClient("test-token").clientV3() {
            override fun getRun(id: String) = Observable
                .just(RunResponse(RunResponse.Data(id, RunResponse.Attributes("queued", null))))
                .doAfterTerminate { if (id == "other") otherPolls.countDown() }
        }

        val slow = client.waitForRun("slow", 1.minutes, 10.milliseconds)
            .doOnError {
                handlerEntered.countDown()
                releaseHandler.await()
            }
            .test()
        try {
            assertTrue(handlerEntered.await(5, SECONDS))
            val other = client.waitForRun("other", 10.milliseconds, 1.seconds).test()
            try {
                assertTrue(otherPolls.await(5, SECONDS))
                other.awaitDone(5, SECONDS).assertFailure(TimeoutException::class.java)
                slow.assertEmpty()
            } finally {
                other.dispose()
            }
        } finally {
            releaseHandler.countDown()
            slow.awaitDone(5, SECONDS)
        }
        slow.assertFailure(TimeoutException::class.java)
    }

    test("a blocked synchronous repeat request leaves another wait's deadline free to fire") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(queuedRun())
            server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())
            server.enqueue(queuedRun())
            val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV3()
            val slow = client.waitForRun("slow", 10.milliseconds, 1.minutes).test()

            try {
                repeat(2) {
                    assertEquals("/api/v3/runs/slow", server.takeRequest(5, SECONDS)?.target)
                }
                client.waitForRun("other", 1.minutes, 1.seconds).test()
                    .awaitDone(5, SECONDS)
                    .assertFailure(TimeoutException::class.java)
                assertEquals("/api/v3/runs/other", server.takeRequest(5, SECONDS)?.target)
                slow.assertEmpty()
            } finally {
                slow.dispose()
            }
        }
    }
})

private fun queuedRun() = MockResponse(body = """{"data":{"id":"run-id","attributes":{"phase":"queued"}}}""")

private fun endedRun() = MockResponse(body = """{"data":{"id":"run-id","attributes":{"phase":"ended"}}}""")
