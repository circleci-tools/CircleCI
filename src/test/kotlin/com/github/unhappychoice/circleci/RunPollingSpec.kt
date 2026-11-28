package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest
import com.github.unhappychoice.circleci.v3.response.RunListResponse
import com.github.unhappychoice.circleci.v3.response.RunResponse
import com.github.unhappychoice.circleci.v3.response.TriggerRunResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object RunPollingSpec : Spek({
    mapOf("zero" to Duration.ZERO, "a negative duration" to (-1).seconds, "infinity" to Duration.INFINITE)
        .forEach { (name, duration) ->
            test("rejects $name as the poll interval before requesting a run") {
                val client = RunPollingClient()
                val error = assertThrows(IllegalArgumentException::class.java) {
                    client.waitForRun("run-id", duration, 10.seconds)
                }
                assertEquals("Poll interval must be positive and finite", error.message)
                assertTrue(client.requestedIds.isEmpty())
            }

            test("rejects $name as the timeout before requesting a run") {
                val client = RunPollingClient()
                val error = assertThrows(IllegalArgumentException::class.java) {
                    client.waitForRun("run-id", 2.seconds, duration)
                }
                assertEquals("Timeout must be positive and finite", error.message)
                assertTrue(client.requestedIds.isEmpty())
            }
        }

    test("polls sequentially and returns the ended response even after an earlier outcome") {
        val scheduler = TestScheduler()
        val first = PublishSubject.create<RunResponse>()
        val ended = runResponse("ended", "failed")
        val client = RunPollingClient(first, Observable.just(ended))
        val result = client.waitForRun("run-id", 2.seconds, 20.seconds, scheduler, scheduler).test()

        scheduler.advanceTimeBy(5, SECONDS)
        assertEquals(listOf("run-id"), client.requestedIds)
        first.onNext(runResponse("started", "succeeded"))
        result.assertEmpty()
        first.onComplete()
        scheduler.advanceTimeBy(1, SECONDS)
        assertEquals(listOf("run-id"), client.requestedIds)
        scheduler.advanceTimeBy(1, SECONDS)
        result.assertResult(ended)
        scheduler.advanceTimeBy(30, SECONDS)
        assertEquals(listOf("run-id", "run-id"), client.requestedIds)
    }

    test("times out at the overall deadline and disposes the active request") {
        val scheduler = TestScheduler()
        val active = PublishSubject.create<RunResponse>()
        val client = RunPollingClient(Observable.just(runResponse("queued")), active)
        val result = client.waitForRun("run-id", 2.seconds, 5.seconds, scheduler, scheduler).test()

        scheduler.advanceTimeBy(4, SECONDS)
        result.assertEmpty()
        assertTrue(active.hasObservers())
        scheduler.advanceTimeBy(1, SECONDS)
        result.assertFailure(TimeoutException::class.java)
        assertFalse(active.hasObservers())
        scheduler.advanceTimeBy(10, SECONDS)
        assertEquals(2, client.requestedIds.size)
    }

    test("disposing while waiting for the next poll prevents another request") {
        val scheduler = TestScheduler()
        val client = RunPollingClient(Observable.just(runResponse("queued")))
        val result = client.waitForRun("run-id", 2.seconds, 10.seconds, scheduler, scheduler).test()

        scheduler.triggerActions()
        result.dispose()
        scheduler.advanceTimeBy(20, SECONDS)
        result.assertEmpty()
        assertEquals(listOf("run-id"), client.requestedIds)
    }

    test("an expired queued poll does not start while the timeout timer is delayed") {
        val requests = TestScheduler()
        val timers = TestScheduler()
        val client = RunPollingClient()
        var now = 0L
        val observer = client.waitForRun(
            "run-id", 2.seconds, 5.seconds, requests, timers,
            nanoTime = { now }
        ).test()
        now = 5.seconds.inWholeNanoseconds
        requests.triggerActions()
        observer.assertEmpty()
        timers.advanceTimeBy(5, SECONDS)
        requests.triggerActions()
        observer.assertFailure(TimeoutException::class.java)
        assertTrue(client.requestedIds.isEmpty())
    }

    test("surfaces an HTTP 429 immediately") {
        val scheduler = TestScheduler()
        val error = HttpException(Response.error<RunResponse>(429, "".toResponseBody()))
        val client = RunPollingClient(Observable.error(error))
        val result = client.waitForRun("run-id", 2.seconds, 10.seconds, scheduler, scheduler).test()

        scheduler.triggerActions()
        result.assertError(error).assertNoValues()
        scheduler.advanceTimeBy(20, SECONDS)
        assertEquals(listOf("run-id"), client.requestedIds)
    }

    test("a nested wait restores the outer deadline before a synchronous poll is scheduled") {
        val timers = TestScheduler()
        val requests = Schedulers.trampoline()
        val timeout = 5.seconds
        val now = AtomicLong()
        val deadlineChecked = CountDownLatch(1)
        val ended = runResponse("ended")
        val delegate = RunPollingClient(Observable.just(ended))
        val synchronous = SynchronousV3Client(delegate)
        val decorated = object : CircleCIAPIClientV3 by synchronous {
            override fun getRun(id: String): Observable<RunResponse> {
                RunPollingClient(Observable.just(ended))
                    .waitForRun("nested", 2.seconds, timeout, requests, timers)
                    .test().assertResult(ended)
                return synchronous.getRun(id).also { now.set(timeout.inWholeNanoseconds) }
            }
        }
        val observer = decorated.waitForRun(
            "outer", 2.seconds, timeout, requests, timers,
            nanoTime = { now.get().also { if (it > 0) deadlineChecked.countDown() } }
        ).test()

        try {
            assertTrue(deadlineChecked.await(5, SECONDS))
            observer.assertEmpty()
            timers.advanceTimeBy(5, SECONDS)
            observer.assertFailure(TimeoutException::class.java)
            assertTrue(delegate.requestedIds.isEmpty())
        } finally {
            observer.dispose()
        }
    }
})

private class RunPollingClient(vararg responses: Observable<RunResponse>) : CircleCIAPIClientV3 {
    val requestedIds = mutableListOf<String>()
    private val pendingResponses = responses.iterator()

    override fun triggerRun(request: TriggerRunRequest, projectId: String?): Observable<TriggerRunResponse> =
        error("Unexpected triggerRun call")

    override fun listProjectRuns(projectId: String, pageSize: Int?, cursor: String?): Observable<RunListResponse> =
        error("Unexpected listProjectRuns call")

    override fun listMyRuns(pageSize: Int?, cursor: String?): Observable<RunListResponse> =
        error("Unexpected listMyRuns call")

    override fun getRun(id: String): Observable<RunResponse> {
        requestedIds.add(id)
        return pendingResponses.next()
    }
}

private fun runResponse(phase: String, outcome: String? = null) =
    RunResponse(RunResponse.Data("run-id", RunResponse.Attributes(phase, outcome)))
