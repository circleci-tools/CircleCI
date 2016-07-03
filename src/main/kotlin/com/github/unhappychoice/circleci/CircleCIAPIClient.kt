package com.github.unhappychoice.circleci

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.converter.GsonConverter

class CircleCIAPIClient(val token: String) {
  fun client(): CircleCIAPIClientV1 = adapter().create(CircleCIAPIClientV1::class.java)

  private fun adapter(): RestAdapter = RestAdapter
    .Builder()
    .setEndpoint("https://circleci.com/api/v1")
    .setRequestInterceptor(intercepter())
    .setLogLevel(RestAdapter.LogLevel.BASIC)
    .setConverter(converter())
    .build()

  private fun intercepter(): RequestInterceptor = RequestInterceptor { request ->
    request.addHeader("Accept", "application/json")
    request.addQueryParam("circle-token", token)
  }

  private fun converter(): GsonConverter = GsonConverter(
    GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .create()
  )
}