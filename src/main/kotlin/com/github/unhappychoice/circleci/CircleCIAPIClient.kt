package com.github.unhappychoice.circleci

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CircleCIAPIClient(val token: String) {
  @Deprecated("V1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client(): CircleCIAPIClientV1 = retrofitV1("https://circleci.com/api/v1/").create(CircleCIAPIClientV1::class.java)
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun clinet1_1(): CircleCIAPIClientV1_1 = client1_1()
  @Deprecated("V1.1 API is deprecated. Use clientV2() instead", ReplaceWith("clientV2()"))
  fun client1_1(): CircleCIAPIClientV1_1 = retrofitV1("https://circleci.com/api/v1.1/").create(CircleCIAPIClientV1_1::class.java)
  fun clientV2(): CircleCIAPIClientV2 = retrofitV2("https://circleci.com/api/v2/").create(CircleCIAPIClientV2::class.java)

  private val okHttpV1 = OkHttpClient.Builder().addInterceptor {
    val builder = it.request().newBuilder()
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", "Basic ${encodedToken()}")
      .build()
    it.proceed(builder)
  }.build()

  private val okHttpV2 = OkHttpClient.Builder().addInterceptor {
    val builder = it.request().newBuilder()
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json")
      .addHeader("Circle-Token", token)
      .build()
    it.proceed(builder)
  }.build()

  private fun encodedToken(): String = String(Base64.encodeBase64("$token:".toByteArray()))

  private fun retrofitV1(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV1)
    .baseUrl(baseUrl)
    .addConverterFactory(converterV1())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

  private fun retrofitV2(baseUrl: String): Retrofit = Retrofit.Builder()
    .client(okHttpV2)
    .baseUrl(baseUrl)
    .addConverterFactory(converterV2())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

  private fun converterV1() = GsonConverterFactory.create(
    GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .create()
  )

  private fun converterV2() = GsonConverterFactory.create(
    GsonBuilder()
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
      .create()
  )
}