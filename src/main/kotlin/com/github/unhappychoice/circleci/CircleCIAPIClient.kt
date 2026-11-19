package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.PipelineValues
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime

class CircleCIAPIClient private constructor(
  val token: String,
  private val baseUrl: String,
  private val callAdapterFactory: RxJava3CallAdapterFactory
) {
  constructor(token: String) : this(token, BASE_URL)

  internal constructor(token: String, baseUrl: String) :
    this(token, baseUrl, RxJava3CallAdapterFactory.createSynchronous())

  companion object {
    private const val BASE_URL = "https://circleci.com/api/"

    fun async(token: String): CircleCIAPIClient = async(token, BASE_URL)

    internal fun async(token: String, baseUrl: String): CircleCIAPIClient =
      CircleCIAPIClient(token, baseUrl, RxJava3CallAdapterFactory.create())
  }

  @Deprecated("V1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client(): CircleCIAPIClientV1 = serviceV1
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun clinet1_1(): CircleCIAPIClientV1_1 = client1_1()
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client1_1(): CircleCIAPIClientV1_1 = serviceV1_1
  fun clientV2(): CircleCIAPIClientV2 = serviceV2

  private val serviceV1 by lazy {
    retrofitV1("${baseUrl}v1/").create(CircleCIAPIClientV1::class.java)
  }

  private val serviceV1_1 by lazy {
    client1_1("${baseUrl}v1.1/")
  }

  private val serviceV2 by lazy {
    clientV2("${baseUrl}v2/")
  }

  internal fun client1_1(baseUrl: String): CircleCIAPIClientV1_1 =
    retrofitV1(baseUrl).create(CircleCIAPIClientV1_1::class.java)

  internal fun clientV2(baseUrl: String): CircleCIAPIClientV2 =
    retrofitV2(baseUrl).create(CircleCIAPIClientV2::class.java)

  internal val okHttpV1 = authenticatedClient("Authorization", "Basic ${encodedToken()}")
  internal val okHttpV2 by lazy {
    require(token.all { it == '\t' || it in ' '..'~' }) {
      "CircleCI API token contains invalid header characters"
    }
    authenticatedClient("Circle-Token", token)
  }

  private fun authenticatedClient(header: String, value: String): OkHttpClient = OkHttpClient.Builder()
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

  private fun encodedToken(): String = String(Base64.encodeBase64("$token:".toByteArray()))

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

private val pipelineValuesMapType = object : TypeToken<Map<String, Any>>() {}.type
private val pipelineValuesDeserializer = JsonDeserializer<PipelineValues> { json, _, context ->
  PipelineValues(context.deserialize(json.asJsonObject, pipelineValuesMapType))
}
