package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.response.RunResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

/**
 * Polls [CircleCIAPIClientV3.getRun] on subscription until the phase is `ended`.
 * The first poll is scheduled on subscription, with [pollInterval] between subsequent polls.
 * Both durations must be positive and finite. [timeout] includes queueing and requests.
 * A timeout emits a [java.util.concurrent.TimeoutException]. Request errors, including HTTP 429,
 * are forwarded immediately. Disposing stops polling. Later reruns may change the observed outcome.
 * The terminal notification (success, error, or timeout) is delivered off the request scheduler,
 * so a blocking subscriber callback cannot hold a poll worker after its request has completed.
 */
fun CircleCIAPIClientV3.waitForRun(id: String, pollInterval: Duration, timeout: Duration): Single<RunResponse> =
    waitForRun(
        id, pollInterval, timeout,
        if (this is SynchronousRunPollingClient) synchronousPollScheduler else Schedulers.io(),
        Schedulers.single(), timeoutScheduler = Schedulers.io(), notifyScheduler = Schedulers.io(),
        nanoTime = System::nanoTime
    )

internal fun CircleCIAPIClientV3.waitForRun(
    id: String,
    pollInterval: Duration,
    timeout: Duration,
    requestScheduler: Scheduler,
    timerScheduler: Scheduler,
    timeoutScheduler: Scheduler = requestScheduler,
    notifyScheduler: Scheduler = requestScheduler,
    nanoTime: () -> Long = { timerScheduler.now(NANOSECONDS) }
): Single<RunResponse> {
    require(pollInterval.isPositive() && pollInterval.isFinite()) { "Poll interval must be positive and finite" }
    require(timeout.isPositive() && timeout.isFinite()) { "Timeout must be positive and finite" }

    val timeoutNanos = timeout.inWholeNanoseconds
    return Single.defer {
        val startedAt = nanoTime()
        Observable.defer {
            if (nanoTime() - startedAt >= timeoutNanos) Observable.never() else getRun(id)
        }
            .subscribeOn(requestScheduler)
            .repeatWhen { completed -> completed.delay(pollInterval.inWholeNanoseconds, NANOSECONDS, timerScheduler) }
            .filter { it.data.attributes.phase == "ended" }
            .firstOrError()
            .timeout(
                timeoutNanos,
                NANOSECONDS,
                timerScheduler,
                Single.error<RunResponse> { TimeoutException("Run $id did not end within $timeout") }
                    .subscribeOn(timeoutScheduler)
            )
            .observeOn(notifyScheduler)
    }
}

internal class SynchronousRunPollingClient(delegate: CircleCIAPIClientV3) : CircleCIAPIClientV3 by delegate

internal const val SYNCHRONOUS_POLL_LIMIT = 8

private val synchronousPollScheduler by lazy {
    val executor = ScheduledThreadPoolExecutor(SYNCHRONOUS_POLL_LIMIT) { task ->
        Thread(task, "CircleCI-run-poll").apply { isDaemon = true }
    }.apply { removeOnCancelPolicy = true }
    Schedulers.from(executor)
}
