package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.response.RunResponse
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RunPollingConcurrencySpec : Spek({
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
