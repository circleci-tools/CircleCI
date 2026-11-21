package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.BundlePayload
import com.github.unhappychoice.circleci.v2.response.BundleDiff
import com.google.gson.JsonParser
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object PolicyBundleHttpSpec : Spek({
    val payload = BundlePayload(
        mapOf("new-policy" to "package new_policy\n", "existing-policy" to "package existing_policy")
    )
    val expectedRequest = JsonParser.parseString(
        """{"policies":{"new-policy":"package new_policy\n","existing-policy":"package existing_policy"}}"""
    )

    listOf(true to 200, false to 201).forEach { (dry, status) ->
        test("createPolicyBundle sends the policy map and decodes the diff for HTTP $status with dry=$dry") {
            MockWebServer().use { server ->
                server.start()
                server.enqueue(
                    MockResponse(
                        code = status,
                        headers = headersOf("Content-Type", "application/json"),
                        body = """{"created":["new-policy"],"deleted":["old-policy"],"modified":["existing-policy"]}""",
                    )
                )
                val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()

                client.createPolicyBundle("owner-id", "config", dry, payload).test().assertResult(
                    BundleDiff(
                        created = listOf("new-policy"),
                        deleted = listOf("old-policy"),
                        modified = listOf("existing-policy")
                    )
                )

                val request = server.takeRequest()
                assertEquals("POST", request.method)
                assertEquals("/api/v2/owner/owner-id/context/config/policy-bundle?dry=$dry", request.target)
                assertEquals(expectedRequest, JsonParser.parseString(requireNotNull(request.body).utf8()))
            }
        }
    }
})
