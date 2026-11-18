package com.github.unhappychoice.circleci

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.ProtocolException

internal object SameOriginRedirectInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    var response = chain.proceed(chain.request())
    repeat(20) {
      val next = followUp(response) ?: return response
      response.close()
      response = chain.proceed(next)
    }
    if (followUp(response) == null) return response
    response.close()
    throw ProtocolException("Too many redirects")
  }

  private fun followUp(response: Response): Request? {
    val request = response.request
    val url = request.url
    if (!response.isRedirect) return null
    val location = response.header("Location") ?: return null
    val target = url.resolve(location) ?: return null
    if (!url.isHttps || !target.isHttps || target.host != url.host || target.port != url.port) return null

    val builder = request.newBuilder().url(target)
    val body = request.body
    if (response.code == 307 || response.code == 308 || request.method == "PROPFIND") {
      if (body?.isOneShot() == true || body?.isDuplex() == true) return null
    } else if (request.method != "GET" && request.method != "HEAD") {
      builder.method("GET", null)
        .removeHeader("Transfer-Encoding")
        .removeHeader("Content-Length")
        .removeHeader("Content-Type")
    }
    return builder.build()
  }
}
