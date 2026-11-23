package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.ProjectBranches
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsBranchesHttpSpec : Spek({
    test("preserves null identifiers and the branch list") {
        val response = decodeInsightsBranches(
            """{"org_id":null,"project_id":null,"branches":["main","feature/example"]}"""
        )

        assertEquals(ProjectBranches(null, null, listOf("main", "feature/example")), response)
    }

    test("preserves populated identifiers and the branch list") {
        val response = decodeInsightsBranches(
            """{"org_id":"example-org","project_id":"example-project","branches":["main"]}"""
        )

        assertEquals(ProjectBranches("example-org", "example-project", listOf("main")), response)
    }
})

private fun decodeInsightsBranches(body: String): ProjectBranches = MockWebServer().use { server ->
    server.start()
    server.enqueue(MockResponse(body = body))
    CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        .getAllInsightsBranches("gh/example/project")
        .blockingSingle()
}
