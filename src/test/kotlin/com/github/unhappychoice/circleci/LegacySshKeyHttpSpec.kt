package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import io.reactivex.rxjava3.core.Completable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.HttpException

@Suppress("DEPRECATION")
object LegacySshKeyHttpSpec : Spek({
  val request = AddSshKeyRequest("example.com", "test-key")

  fun checkEndpoint(path: String, addSshKey: (CircleCIAPIClient) -> Completable) {
    test("POST $path completes for an empty HTTP 200 response") {
      MockWebServer().use { server ->
        server.start()
        server.enqueue(MockResponse(code = 200))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString())

        addSshKey(client).test().assertResult()

        assertEquals(1, server.requestCount)
        val recordedRequest = server.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals(path, recordedRequest.target)
      }
    }

    test("POST $path exposes HTTP 403 as an error") {
      MockWebServer().use { server ->
        server.start()
        val body = """{"message":"Forbidden"}"""
        server.enqueue(MockResponse(code = 403, body = body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString())

        addSshKey(client).test().assertNotComplete().assertError { error ->
          error is HttpException && error.code() == 403 && error.response()?.errorBody()?.string() == body
        }
      }
    }
  }

  checkEndpoint("/api/v1/project/example-org/example-project/ssh-key") {
    it.client().addSshKey("example-org", "example-project", request)
  }

  checkEndpoint("/api/v1.1/project/github/example-org/example-project/ssh-key") {
    it.client1_1().addSshKey("github", "example-org", "example-project", request)
  }
})
