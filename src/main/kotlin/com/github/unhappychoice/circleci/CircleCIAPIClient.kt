package com.github.unhappychoice.circleci

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CircleCIAPIClient(val token: String) {
  fun client(): CircleCIAPIClientV1 = retrofit().create(CircleCIAPIClientV1::class.java)

  private val okHttp = OkHttpClient.Builder().addInterceptor {
    val builder = it.request().newBuilder()
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", "Basic ${encodedToken()}")
      .build()
    it.proceed(builder)
  }.build()

  private fun encodedToken(): String = String(Base64.encodeBase64("$token:".toByteArray()))

  private fun retrofit(): Retrofit = Retrofit.Builder()
    .client(okHttp)
    .baseUrl("https://circleci.com/api/v1/")
    .addConverterFactory(converter())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

  private fun converter() = GsonConverterFactory.create(
    GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .create()
  )
}