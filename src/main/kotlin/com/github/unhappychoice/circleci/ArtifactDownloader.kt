package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.exceptions.Exceptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.ProtocolException

internal class ArtifactDownloader(transport: OkHttpClient, private val apiUrl: HttpUrl, private val token: String) {
  fun <T : Any> download(artifact: Artifact, consume: (InputStream) -> T): Single<T> = Single.create { emitter ->
    val url = artifact.url.toHttpUrlOrNull()
    require(url != null && url.isArtifactUrl()) { "Expected an HTTPS artifact URL without user credentials" }
    val request = Request.Builder().url(url)
    if (canAuthenticateArtifact(url, apiUrl)) request.header(TOKEN_HEADER, token)
    val call = client.newCall(request.build())
    emitter.setCancellable(call::cancel)
    if (!emitter.isDisposed) call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        emitter.tryOnError(e)
      }

      override fun onResponse(call: Call, response: Response) {
        val result = try {
          response.use {
            if (emitter.isDisposed) return
            if (!it.isSuccessful) throw IOException("Artifact download failed with HTTP ${it.code}")
            consume(it.body.byteStream())
          }
        } catch (error: Throwable) {
          Exceptions.throwIfFatal(error)
          emitter.tryOnError(error)
          return
        }
        emitter.onSuccess(result)
      }
    })
  }

  private val client = transport.newBuilder()
    .followRedirects(false)
    .addInterceptor(ArtifactRedirectInterceptor)
    .build()
}

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
