package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.response.RunResponse
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RunPollingConcurrencySpec : Spek({
    test("concurrent waits share one deadline thread") {
        val waitCount = 32
        val firstPolls = CountDownLatch(waitCount)
        val deadlineThreads = ConcurrentHashMap.newKeySet<Thread>()
        val client = object : CircleCIAPIClientV3 by CircleCIAPIClient("test-token").clientV3() {
            override fun getRun(id: String) = Observable
                .just(RunResponse(RunResponse.Data(id, RunResponse.Attributes("queued", null))))
                .doAfterTerminate { firstPolls.countDown() }
        }

        val waits = List(waitCount) { index ->
            client.waitForRun("run-$index", 1.minutes, 2.seconds)
                .doOnError { deadlineThreads.add(Thread.currentThread()) }
                .test()
        }
        try {
            assertTrue(firstPolls.await(5, SECONDS))
            waits.forEach { it.awaitDone(5, SECONDS).assertFailure(TimeoutException::class.java) }
            assertEquals(1, deadlineThreads.size)
        } finally {
            waits.forEach { it.dispose() }
        }
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
