package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.CreateCheckoutKeyRequest
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.spekframework.spek2.Spek
import retrofit2.HttpException
import java.net.InetSocketAddress

object CircleCIAPIClientV1_1HttpSpec: Spek({
  @Suppress("DEPRECATION")
  test("#client1_1() should create a client that can prepare a user request") {
    val client = CircleCIAPIClient("test-token").client1_1()

    assertNotNull(client.getMe())
  }

  test("#getMe() should send Basic authentication and decode the user") {
    val body = """
      {
        "login": "octocat",
        "name": "Octo Cat",
        "avatar_url": "https://example.com/octocat.png",
        "projects": {},
        "selected_email": "octocat@example.com",
        "pusher_id": "octocat"
      }
    """.trimIndent()
    withResponse(200, body) { client ->
      val user = client.getMe().blockingSingle()
      assertEquals("octocat", user.login)
      assertEquals("https://example.com/octocat.png", user.avatarUrl)
      assertEquals("octocat@example.com", user.selectedEmail)
    }
  }

  test("#getMe() should expose an unauthorized response as an HTTP error") {
    val body = """{"message":"Unauthorized"}"""
    withResponse(401, body) { client ->
      client.getMe().test().assertNoValues().assertNotComplete().assertError { error ->
        error is HttpException && error.code() == 401 && error.response()?.errorBody()?.string() == body
      }
    }
  }

  group("checkout key issuance time") {
    val checkoutKeysPath = "/api/v1.1/project/github/octocat/example/checkout-key"
    val checkoutKeyBody = checkoutKeyFixture("checkout_key.json")

    test("#getCheckoutKeys() should decode each issuance time") {
      withResponse(200, checkoutKeyFixture("checkout_keys.json"), path = checkoutKeysPath) { client ->
        val keys = client.getCheckoutKeys("github", "octocat", "example").blockingSingle()
        assertEquals(listOf("2024-07-11T12:00:00Z", "2024-07-10T12:00:00Z"), keys.map { it.created_at })
      }
    }

    test("#getCheckoutKey() should decode the issuance time") {
      val fingerprint = "xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx"
      withResponse(200, checkoutKeyBody, path = "$checkoutKeysPath/$fingerprint") { client ->
        val key = client.getCheckoutKey("github", "octocat", "example", fingerprint).blockingSingle()
        assertEquals("2024-07-11T12:00:00Z", key.created_at)
      }
    }

    test("#createCheckoutKey() should decode the issuance time") {
      withResponse(201, checkoutKeyBody, method = "POST", path = checkoutKeysPath) { client ->
        val request = CreateCheckoutKeyRequest(type = "deploy-key", preferred = true)
        val key = client.createCheckoutKey("github", "octocat", "example", request).blockingSingle()
        assertEquals("2024-07-11T12:00:00Z", key.created_at)
      }
    }
  }
})

private fun checkoutKeyFixture(fileName: String): String =
  checkNotNull(CircleCIAPIClientV1_1HttpSpec::class.java.getResource("json/$fileName")).readText()

private fun withResponse(
  status: Int,
  body: String,
  method: String = "GET",
  path: String = "/api/v1.1/me",
  test: (CircleCIAPIClientV1_1) -> Unit
) {
  val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
  try {
    server.createContext("/") { exchange ->
      val request = Triple(
        exchange.requestMethod,
        exchange.requestURI.toString(),
        exchange.requestHeaders.getFirst("Authorization")
      )
      val expected = Triple(method, path, "Basic dGVzdC10b2tlbjo=")
      val bytes = body.toByteArray()
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(if (request == expected) status else 400, bytes.size.toLong())
      exchange.responseBody.use { it.write(bytes) }
    }
    server.start()
    test(CircleCIAPIClient("test-token").client1_1("http://127.0.0.1:${server.address.port}/api/v1.1/"))
  } finally {
    server.stop(0)
  }
}
