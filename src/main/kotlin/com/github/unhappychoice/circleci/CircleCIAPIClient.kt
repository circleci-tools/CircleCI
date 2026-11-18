package com.github.unhappychoice.circleci

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime

class CircleCIAPIClient(val token: String) {
  @Deprecated("V1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client(): CircleCIAPIClientV1 = retrofitV1("https://circleci.com/api/v1/").create(CircleCIAPIClientV1::class.java)
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun clinet1_1(): CircleCIAPIClientV1_1 = client1_1()
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client1_1(): CircleCIAPIClientV1_1 = client1_1("https://circleci.com/api/v1.1/")
  fun clientV2(): CircleCIAPIClientV2 = clientV2("https://circleci.com/api/v2/")

  internal fun client1_1(baseUrl: String): CircleCIAPIClientV1_1 =
    retrofitV1(baseUrl).create(CircleCIAPIClientV1_1::class.java)

  internal fun clientV2(baseUrl: String): CircleCIAPIClientV2 =
    retrofitV2(baseUrl).create(CircleCIAPIClientV2::class.java)

  internal val okHttpV1 = authenticatedClient("Authorization", "Basic ${encodedToken()}")
  internal val okHttpV2 = authenticatedClient("Circle-Token", token)

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
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

  internal fun retrofitV2(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV2)
    .baseUrl(baseUrl)
    .addConverterFactory(converterV2())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
      .registerTypeAdapter(
        OffsetDateTime::class.java,
        JsonDeserializer<OffsetDateTime> { json, _, _ -> OffsetDateTime.parse(json.asString) }
      )
      .create()
  )
}
