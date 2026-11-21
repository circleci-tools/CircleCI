package com.github.unhappychoice.circleci

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek

@Suppress("DEPRECATION")
object LegacyTransportSpec : Spek({
    test("leaves the legacy transport uninitialized after a V2 request") {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(userResponse())
            val api = CircleCIAPIClient.async("test-token", server.url("/api/").toString())
            assertFalse(api.isLegacyTransportInitialized())

            api.clientV2().getMe().test().awaitDone(5, SECONDS).assertValueCount(1).assertComplete().assertNoErrors()

            assertFalse(api.isLegacyTransportInitialized())
        }
    }

    test("shares the legacy connection pool across concurrent first access and authenticates both versions") {
        MockWebServer().use { server ->
            server.start()
            repeat(2) { server.enqueue(userResponse()) }
            val api = CircleCIAPIClient("test-token", server.url("/api/").toString())
            val barrier = CyclicBarrier(2)
            val executor = Executors.newFixedThreadPool(2)
            try {
                assertFalse(api.isLegacyTransportInitialized())
                val v1 = executor.submit<CircleCIAPIClientV1> {
                    barrier.await(5, SECONDS)
                    api.client()
                }
                val v1_1 = executor.submit<CircleCIAPIClientV1_1> {
                    barrier.await(5, SECONDS)
                    api.client1_1()
                }
                val clientV1 = v1.get(5, SECONDS)
                val clientV1_1 = v1_1.get(5, SECONDS)
                assertTrue(api.isLegacyTransportInitialized())

                clientV1.getMe().test().assertValueCount(1).assertComplete().assertNoErrors()
                clientV1_1.getMe().test().assertValueCount(1).assertComplete().assertNoErrors()

                listOf("/api/v1/me", "/api/v1.1/me").forEachIndexed { index, path ->
                    val request = checkNotNull(server.takeRequest(5, SECONDS))
                    assertEquals(path, request.target)
                    assertEquals("Basic dGVzdC10b2tlbjo=", request.headers["Authorization"])
                    assertEquals(index, request.exchangeIndex)
                }
            } finally {
                executor.shutdownNow()
            }
        }
    }
})

private fun CircleCIAPIClient.isLegacyTransportInitialized(): Boolean {
    val delegate = CircleCIAPIClient::class.java.getDeclaredField("okHttpV1\$delegate")
    delegate.isAccessible = true
    return (delegate.get(this) as Lazy<*>).isInitialized()
}

private fun userResponse() = MockResponse(body = """{"id":"user-id","login":"octocat","name":"Octocat"}""")
