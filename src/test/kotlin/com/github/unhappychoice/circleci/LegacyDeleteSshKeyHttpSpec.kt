package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.DeleteSshKeyRequest
import com.google.gson.JsonParser
import io.reactivex.rxjava3.core.Observable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.HttpException

/**
 * [CircleCIAPIClientV1.deleteSshKey] and [CircleCIAPIClientV1_1.deleteSshKey] are the only
 * endpoints that send a body on a DELETE request, via Retrofit's `@HTTP(hasBody = true)`.
 * This covers that the method and body actually reach the server as declared.
 */
@Suppress("DEPRECATION")
object LegacyDeleteSshKeyHttpSpec : Spek({
  val request = DeleteSshKeyRequest(fingerprint = "xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx", hostname = "example.com")
  val expectedBody = """{"fingerprint":"${request.fingerprint}","hostname":"${request.hostname}"}"""

  fun checkEndpoint(path: String, deleteSshKey: (CircleCIAPIClient) -> Observable<Unit>) {
    test("DELETE $path sends the fingerprint and hostname as a JSON body") {
      MockWebServer().use { server ->
        server.start()
        val body = """{"message":"Forbidden"}"""
        server.enqueue(MockResponse(code = 403, body = body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString())

        deleteSshKey(client).test().assertNotComplete().assertError { error ->
          error is HttpException && error.code() == 403 && error.response()?.errorBody()?.string() == body
        }

        assertEquals(1, server.requestCount)
        val recordedRequest = server.takeRequest()
        assertEquals("DELETE", recordedRequest.method)
        assertEquals(path, recordedRequest.target)
        assertEquals(JsonParser.parseString(expectedBody), JsonParser.parseString(requireNotNull(recordedRequest.body).utf8()))
      }
    }
  }

  checkEndpoint("/api/v1/project/example-org/example-project/ssh-key") {
    it.client().deleteSshKey("example-org", "example-project", request)
  }

  checkEndpoint("/api/v1.1/project/github/example-org/example-project/ssh-key") {
    it.client1_1().deleteSshKey("github", "example-org", "example-project", request)
  }
})
