package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import com.github.unhappychoice.circleci.v2.response.PipelineParametersDeserializer
import com.github.unhappychoice.circleci.v2.response.PipelineValues
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import io.reactivex.rxjava3.core.Single
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.time.OffsetDateTime

class CircleCIAPIClient private constructor(
  val token: String,
  private val baseUrl: String,
  private val asynchronous: Boolean,
  baseTransport: OkHttpClient? = null,
  private val allowCloudArtifactHosts: Boolean = true
) {
  constructor(token: String) : this(token, BASE_URL)

  internal constructor(token: String, baseUrl: String) :
    this(token, baseUrl, asynchronous = false)

  internal constructor(token: String, baseUrl: String, transport: OkHttpClient) :
    this(token, baseUrl, asynchronous = false, baseTransport = transport)

  companion object {
    private const val BASE_URL = "https://circleci.com/api/"

    fun async(token: String): CircleCIAPIClient = async(token, BASE_URL)

    /** Creates a synchronous client for an HTTPS CircleCI Server origin that also streams artifacts. */
    fun server(token: String, instanceUrl: String): CircleCIServerClient =
      server(token, instanceUrl, asynchronous = false)

    /** Creates an asynchronous client for an HTTPS CircleCI Server origin that also streams artifacts. */
    fun serverAsync(token: String, instanceUrl: String): CircleCIServerClient =
      server(token, instanceUrl, asynchronous = true)

    internal fun async(token: String, baseUrl: String): CircleCIAPIClient =
      CircleCIAPIClient(token, baseUrl, asynchronous = true)

    internal fun server(
      token: String,
      instanceUrl: String,
      asynchronous: Boolean,
      transport: OkHttpClient? = null
    ): CircleCIServerClient {
      val baseUrl = serverApiBaseUrl(instanceUrl)
      return CircleCIServerClientImpl(
        CircleCIAPIClient(token, baseUrl, asynchronous, transport, allowCloudArtifactHosts = false)
      )
    }

    private fun serverApiBaseUrl(instanceUrl: String): String {
      val uri = try {
        URI(instanceUrl)
      } catch (_: URISyntaxException) {
        throw IllegalArgumentException("CircleCI Server URL must be an HTTPS origin")
      }
      require(uri.scheme.equals("https", ignoreCase = true) && uri.host != null &&
        (uri.port == -1 || uri.port in 1..65535) && uri.rawUserInfo == null &&
        uri.rawQuery == null && uri.rawFragment == null && uri.rawPath in listOf("", "/")) {
        "CircleCI Server URL must be an HTTPS origin"
      }
      return "https://${uri.rawAuthority}/api/"
    }
  }

  @Deprecated("V1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client(): CircleCIAPIClientV1 = serviceV1
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun clinet1_1(): CircleCIAPIClientV1_1 = serviceV1_1
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client1_1(): CircleCIAPIClientV1_1 = serviceV1_1
  fun clientV2(): CircleCIAPIClientV2 = serviceV2
  fun clientV3(): CircleCIAPIClientV3 = serviceV3

  /**
   * Consumes a V2 artifact with at most [ARTIFACT_DOWNLOAD_LIMIT] downloads, including [consume],
   * running at once. Downloads beyond the limit queue without a worker until one frees up.
   * The stream is valid only during [consume] and is closed before success or failure is
   * delivered. Disposing a queued download never starts it; disposing an active download
   * cancels the call.
   */
  fun <T : Any> downloadArtifact(artifact: Artifact, consume: (InputStream) -> T): Single<T> =
    Single.defer { artifactDownloader.download(artifact, consume) }

  private val artifactDownloader by lazy {
    ArtifactDownloader(transport, baseUrl.toHttpUrl(), headerToken(), allowCloudArtifactHosts)
  }

  private val transport by lazy { baseTransport ?: OkHttpClient() }
  private val callAdapterFactory = if (asynchronous) RxJava3CallAdapterFactory.create()
    else RxJava3CallAdapterFactory.createSynchronous()

  private val serviceV1 by lazy {
    retrofitV1("${baseUrl}v1/").create(CircleCIAPIClientV1::class.java)
  }

  private val serviceV1_1 by lazy {
    client1_1("${baseUrl}v1.1/")
  }

  private val serviceV2 by lazy {
    clientV2("${baseUrl}v2/")
  }

  private val serviceV3 by lazy {
    val service = retrofitV3("${baseUrl}v3/").create(CircleCIAPIClientV3::class.java)
    if (asynchronous) service else SynchronousRunPollingClient(service)
  }

  internal fun client1_1(baseUrl: String): CircleCIAPIClientV1_1 =
    retrofitV1(baseUrl).create(CircleCIAPIClientV1_1::class.java)

  internal fun clientV2(baseUrl: String): CircleCIAPIClientV2 =
    retrofitV2(baseUrl).create(CircleCIAPIClientV2Service::class.java)

  internal val okHttpV1 by lazy {
    authenticatedClient("Authorization", "Basic ${encodedToken(headerToken())}")
  }
  internal val okHttpV2 by lazy { authenticatedClient("Circle-Token", headerToken()) }
  private val okHttpV3 by lazy { authenticatedClient("Authorization", "Bearer ${headerToken()}") }

  private fun headerToken(): String {
    require(token.all { it == '\t' || it in ' '..'~' }) {
      "CircleCI API token contains invalid header characters"
    }
    return token
  }

  private fun authenticatedClient(header: String, value: String): OkHttpClient = transport.newBuilder()
    .followRedirects(false)
    .addInterceptor {
      val request = it.request().newBuilder()
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .addHeader(header, value)
        .build()
      it.proceed(request)
    }
    .addInterceptor(SameOriginRedirectInterceptor)
    .build()

  private fun encodedToken(token: String): String = String(Base64.encodeBase64("$token:".toByteArray()))

  internal fun retrofitV1(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV1)
    .baseUrl(baseUrl)
    .addConverterFactory(converterV1())
    .addCallAdapterFactory(callAdapterFactory)
    .build()

  internal fun retrofitV2(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV2)
    .baseUrl(baseUrl)
    .addConverterFactory(converterV2())
    .addCallAdapterFactory(callAdapterFactory)
    .build()

  private fun retrofitV3(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV3)
    .baseUrl(baseUrl)
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(callAdapterFactory)
    .build()

  private fun converterV1() = GsonConverterFactory.create(
    GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      .create()
  )

  private fun converterV2() = GsonConverterFactory.create(
    GsonBuilder()
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
      .registerTypeAdapter(PipelineValues::class.java, pipelineValuesDeserializer)
      .registerTypeAdapter(
        OffsetDateTime::class.java,
        JsonDeserializer<OffsetDateTime> { json, _, _ -> OffsetDateTime.parse(json.asString) }
      )
      .create()
  )
}

private val pipelineValuesDeserializer = JsonDeserializer<PipelineValues> { json, _, _ ->
  PipelineValues(PipelineParametersDeserializer.decode(json.asJsonObject))
}
