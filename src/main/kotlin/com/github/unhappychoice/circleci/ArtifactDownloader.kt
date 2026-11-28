package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.exceptions.Exceptions
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.ProtocolException
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.Semaphore

internal class ArtifactDownloader(transport: OkHttpClient, private val apiUrl: HttpUrl, private val token: String) {
  fun <T : Any> download(artifact: Artifact, consume: (InputStream) -> T): Single<T> = Single.defer {
    val onDownloadWorker = Thread.currentThread() is ArtifactDownloadThread
    val download = Single.create<T> { emitter ->
      val result = try {
        withDownloadSlot(onDownloadWorker) {
          if (emitter.isDisposed) return@create
          val call = client.newCall(artifactRequest(artifact))
          emitter.setCancellable(call::cancel)
          if (emitter.isDisposed) return@create
          call.execute().use {
            if (emitter.isDisposed) return@create
            if (!it.isSuccessful) throw IOException("Artifact download failed with HTTP ${it.code}")
            consume(it.body.byteStream())
          }
        }
      } catch (error: Throwable) {
        Exceptions.throwIfFatal(error)
        emitter.tryOnError(error)
        return@create
      }
      emitter.onSuccess(result)
    }
    if (onDownloadWorker) download else download.subscribeOn(artifactDownloadScheduler)
  }

  private fun artifactRequest(artifact: Artifact): Request {
    val url = artifact.url.toHttpUrlOrNull()
    require(url != null && url.isArtifactUrl()) { "Expected an HTTPS artifact URL without user credentials" }
    return Request.Builder().url(url).apply {
      if (canAuthenticateArtifact(url, apiUrl)) header(TOKEN_HEADER, token)
    }.build()
  }

  private val client = transport.newBuilder()
    .followRedirects(false)
    .addInterceptor(ArtifactRedirectInterceptor)
    .build()
}

internal const val ARTIFACT_DOWNLOAD_LIMIT = 8

private val artifactDownloadSlots = Semaphore(ARTIFACT_DOWNLOAD_LIMIT)

private inline fun <T> withDownloadSlot(onDownloadWorker: Boolean, download: () -> T): T {
  if (!onDownloadWorker) {
    artifactDownloadSlots.acquire()
  } else if (!artifactDownloadSlots.tryAcquire()) {
    throw RejectedExecutionException(
      "Nested artifact download cannot start: all $ARTIFACT_DOWNLOAD_LIMIT download slots are in use",
    )
  }
  try {
    return download()
  } finally {
    artifactDownloadSlots.release()
  }
}

private val artifactDownloadScheduler by lazy {
  val executor = ScheduledThreadPoolExecutor(ARTIFACT_DOWNLOAD_LIMIT) { task ->
    ArtifactDownloadThread(task).apply { isDaemon = true }
  }.apply { removeOnCancelPolicy = true }
  Schedulers.from(executor)
}

private class ArtifactDownloadThread(task: Runnable) : Thread(task, "CircleCI-artifact-download")

internal fun canAuthenticateArtifact(url: HttpUrl, apiUrl: HttpUrl): Boolean =
  url.isArtifactUrl() && (url.hasSameOrigin(apiUrl) || (url.port == 443 &&
    (url.host == "dl.circleci.com" || url.host.endsWith(".circle-artifacts.com"))))

private const val TOKEN_HEADER = "Circle-Token"

private object ArtifactRedirectInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    var redirects = 0
    while (true) {
      val response = chain.proceed(request)
      if (!response.isRedirect) return response
      response.use {
        val target = response.header("Location")?.let(request.url::resolve)
        if (target == null || !target.isArtifactUrl()) throw ProtocolException("Invalid artifact redirect URL")
        if (redirects++ == 20) throw ProtocolException("Too many artifact redirects")
        request = request.newBuilder().url(target).apply {
          if (!request.url.hasSameOrigin(target)) removeHeader(TOKEN_HEADER)
        }.build()
      }
    }
  }
}

private fun HttpUrl.isArtifactUrl(): Boolean = isHttps && username.isEmpty() && password.isEmpty()

private fun HttpUrl.hasSameOrigin(other: HttpUrl): Boolean =
  scheme == other.scheme && host == other.host && port == other.port
