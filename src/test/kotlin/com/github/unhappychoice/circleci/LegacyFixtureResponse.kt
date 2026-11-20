package com.github.unhappychoice.circleci

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody.Companion.toResponseBody
import java.lang.reflect.Type

internal fun <T : Any> legacyFixtureResponse(fileName: String, type: Type): Observable<T> {
  val retrofit = CircleCIAPIClient("test-token").retrofitV1("https://circleci.com/api/v1/")
  val converter = retrofit.responseBodyConverter<T>(type, emptyArray())
  val json = checkNotNull(MockCircleCIAPIClientV1::class.java.getResource(fileName)).readText()
  return Observable.just(checkNotNull(json.toResponseBody().use(converter::convert)))
}
