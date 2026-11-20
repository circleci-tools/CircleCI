package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Policy
import com.github.unhappychoice.circleci.v2.response.PolicyBundle
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek

object PolicyBundleDecodingSpec : Spek({
    test("decodes the current policy bundle") {
        assertPolicyBundle("/api/v2/owner/owner-id/context/config/policy-bundle") {
            it.getPolicyBundle("owner-id", "config").blockingFirst()
        }
    }

    test("decodes the policy bundle recorded in a decision log") {
        assertPolicyBundle("/api/v2/owner/owner-id/context/config/decision/decision-id/policy-bundle") {
            it.getDecisionLogPolicyBundle("owner-id", "config", "decision-id").blockingFirst()
        }
    }
})

private fun assertPolicyBundle(path: String, read: (CircleCIAPIClientV2) -> PolicyBundle) {
    MockWebServer().use { server ->
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(bundleJson))
        val client = CircleCIAPIClient("token", server.url("/api/").toString()).clientV2()

        assertEquals(expectedBundle, read(client))
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals(path, request.path)
    }
}

private val expectedBundle: PolicyBundle = mapOf(
    "require-tests" to listOf(
        Policy("require-tests", "package require_tests", "2024-01-01T00:00:00Z", "user-1"),
        Policy("require-tests", "package require_tests\nimport data.rules", "2024-01-02T00:00:00Z", "user-2")
    ),
    "require-docs" to listOf(
        Policy("require-docs", "package require_docs", "2024-01-03T00:00:00Z", "user-3")
    )
)

private val bundleJson = """
    {
        "require-tests": [
            {
                "name": "require-tests",
                "content": "package require_tests",
                "created_at": "2024-01-01T00:00:00Z",
                "created_by": "user-1"
            },
            {
                "name": "require-tests",
                "content": "package require_tests\nimport data.rules",
                "created_at": "2024-01-02T00:00:00Z",
                "created_by": "user-2"
            }
        ],
        "require-docs": [
            {
                "name": "require-docs",
                "content": "package require_docs",
                "created_at": "2024-01-03T00:00:00Z",
                "created_by": "user-3"
            }
        ]
    }
""".trimIndent()
