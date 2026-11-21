package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateGroupRequest
import com.github.unhappychoice.circleci.v2.response.Group
import com.github.unhappychoice.circleci.v2.response.GroupListResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.spekframework.spek2.Spek

object GroupHttpSpec : Spek({
    val orgId = "b9291e0d-a11e-41fb-8517-c545388b5953"
    val group = Group(
        id = "bdbe5e8d-50a5-4f73-a917-074592949844",
        name = "Release Managers",
        organizationId = null,
        description = "Can manage releases."
    )
    val groupJson = """
        {"id":"bdbe5e8d-50a5-4f73-a917-074592949844","name":"Release Managers","description":"Can manage releases."}
    """.trimIndent()

    test("getOrganizationGroups decodes descriptions without organization IDs") {
        withGroupResponse("""{"items":[$groupJson],"next_page_token":"next"}""") { client ->
            client.getOrganizationGroups(orgId).test().assertResult(GroupListResponse(listOf(group), "next"))
        }
    }

    test("createOrganizationGroup decodes the description without an organization ID") {
        withGroupResponse(groupJson, status = 201) { client ->
            client.createOrganizationGroup(orgId, CreateGroupRequest(group.name)).test()
                .assertResult(group)
        }
    }

    test("getGroup decodes the description without an organization ID") {
        withGroupResponse(groupJson) { client ->
            client.getGroup(orgId, group.id).test().assertResult(group)
        }
    }

    test("getGroup preserves an organization ID when provided") {
        val body = """{"id":"${group.id}","name":"${group.name}","organization_id":"$orgId"}"""
        withGroupResponse(body) { client ->
            client.getGroup(orgId, group.id).test().assertResult(group.copy(organizationId = orgId, description = null))
        }
    }
})

private fun withGroupResponse(body: String, status: Int = 200, verify: (CircleCIAPIClientV2) -> Unit) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(MockResponse(code = status, headers = headersOf("Content-Type", "application/json"), body = body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        verify(client)
    }
}
