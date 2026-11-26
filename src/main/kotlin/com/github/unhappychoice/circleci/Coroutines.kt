package com.github.unhappychoice.circleci

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.await as awaitCompletion
import kotlinx.coroutines.rx3.awaitSingle

/**
 * Suspends until this [Observable] emits its single item, matching the semantics of
 * [Observable.blockingSingle] used by synchronous callers: every client method in this
 * library emits exactly one item on success. Cancelling the calling coroutine disposes
 * the underlying subscription, stopping the in-flight HTTP call.
 */
suspend fun <T : Any> Observable<T>.await(): T = awaitSingle()

/**
 * Suspends until this [Completable] completes, matching the semantics of
 * [Completable.blockingAwait] used by synchronous callers. Cancelling the calling
 * coroutine disposes the underlying subscription, stopping the in-flight HTTP call.
 */
suspend fun Completable.await() = awaitCompletion()
