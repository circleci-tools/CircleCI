package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.response.RunResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

/**
 * Polls [CircleCIAPIClientV3.getRun] on subscription until the phase is `ended`.
 * The first request starts immediately, with [pollInterval] between each response and the next request.
 * Both durations must be positive and finite. [timeout] bounds the entire wait, including requests.
 * A timeout emits a [java.util.concurrent.TimeoutException]. Request errors, including HTTP 429,
 * are forwarded immediately. Disposing stops polling. Later reruns may change the observed outcome.
 */
fun CircleCIAPIClientV3.waitForRun(id: String, pollInterval: Duration, timeout: Duration): Single<RunResponse> =
    waitForRun(id, pollInterval, timeout, Schedulers.io(), Schedulers.single())

internal fun CircleCIAPIClientV3.waitForRun(
    id: String,
    pollInterval: Duration,
    timeout: Duration,
    requestScheduler: Scheduler,
    timerScheduler: Scheduler
): Single<RunResponse> {
    require(pollInterval.isPositive() && pollInterval.isFinite()) { "Poll interval must be positive and finite" }
    require(timeout.isPositive() && timeout.isFinite()) { "Timeout must be positive and finite" }

    return Observable.defer { getRun(id) }
        .subscribeOn(requestScheduler)
        .repeatWhen { completed -> completed.delay(pollInterval.inWholeNanoseconds, NANOSECONDS, timerScheduler) }
        .filter { it.data.attributes.phase == "ended" }
        .firstOrError()
        .timeout(
            timeout.inWholeNanoseconds,
            NANOSECONDS,
            timerScheduler,
            Single.error<RunResponse> { TimeoutException("Run $id did not end within $timeout") }
                .subscribeOn(requestScheduler)
        )
}
