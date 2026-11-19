package com.github.unhappychoice.circleci

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.spekframework.spek2.Spek

@Suppress("DEPRECATION")
object CircleCIAPIClientSpec : Spek({
    val accessors = mapOf<String, (CircleCIAPIClient) -> Any>(
        "v1" to CircleCIAPIClient::client,
        "v1.1" to CircleCIAPIClient::client1_1,
        "v2" to CircleCIAPIClient::clientV2,
        "v3" to CircleCIAPIClient::clientV3
    )

    accessors.forEach { (version, accessor) ->
        group(version) {
            test("reuses the service on repeated calls") {
                val client = CircleCIAPIClient("token")
                assertSame(accessor(client), accessor(client))
            }

            test("creates separate services for separate clients") {
                assertNotSame(accessor(CircleCIAPIClient("token")), accessor(CircleCIAPIClient("token")))
            }

            test("reuses the service on concurrent first calls") {
                val client = CircleCIAPIClient("token")
                val barrier = CyclicBarrier(4)
                val executor = Executors.newFixedThreadPool(4)
                try {
                    val calls = List(4) {
                        executor.submit<Any> {
                            barrier.await(10, SECONDS)
                            accessor(client)
                        }
                    }
                    val services = calls.map { it.get(10, SECONDS) }
                    services.forEach { assertSame(services.first(), it) }
                } finally {
                    executor.shutdownNow()
                }
            }
        }
    }

    test("returns the same v1.1 service through the legacy alias") {
        val client = CircleCIAPIClient("token")
        assertSame(client.client1_1(), client.clinet1_1())
    }
})
