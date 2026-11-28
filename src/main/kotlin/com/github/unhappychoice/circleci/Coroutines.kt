package com.github.unhappychoice.circleci

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.rx3.await as awaitCompletion
import kotlinx.coroutines.rx3.awaitSingle

/**
 * Suspends until this [Observable] emits its single item, matching the semantics of
 * [Observable.blockingSingle] used by synchronous callers: every client method in this
 * library emits exactly one item on success. Subscription runs on [Schedulers.io].
 * Cancelling the calling coroutine disposes the subscription, stopping the in-flight HTTP call.
 */
suspend fun <T : Any> Observable<T>.await(): T = subscribeOn(Schedulers.io()).awaitSingle()

/**
 * Suspends until this [Completable] completes, matching the semantics of
 * [Completable.blockingAwait] used by synchronous callers. Subscription runs on [Schedulers.io].
 * Cancelling the calling coroutine disposes the subscription, stopping the in-flight HTTP call.
 */
suspend fun Completable.await() = subscribeOn(Schedulers.io()).awaitCompletion()
