package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateGroupRequest
import com.github.unhappychoice.circleci.v2.response.Group
import com.github.unhappychoice.circleci.v2.response.GroupListResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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

    test("getOrganizationGroups omits both query parameters when called with only an organization ID") {
        withGroupResponse("""{"items":[$groupJson],"next_page_token":null}""") { client, server ->
            client.getOrganizationGroups(orgId).test().assertResult(GroupListResponse(listOf(group), null))
            val url = server.takeRequest().url
            assertEquals(emptySet<String>(), url.queryParameterNames)
        }
    }

    test("getOrganizationGroups omits the limit and decodes a page without a total count") {
        withGroupResponse("""{"items":[$groupJson],"next_page_token":"next"}""") { client, server ->
            client.getOrganizationGroups(orgId, "current").test()
                .assertResult(GroupListResponse(listOf(group), "next"))
            val request = server.takeRequest()
            val url = request.url
            assertEquals("GET", request.method)
            assertEquals("/api/v2/organizations/$orgId/groups", url.encodedPath)
            assertEquals("current", url.queryParameter("page-token"))
            assertFalse("limit" in url.queryParameterNames)
        }
    }

    test("getOrganizationGroups with a null limit uses the organization groups endpoint without a limit") {
        withGroupResponse("""{"items":[],"next_page_token":null}""") { client, server ->
            client.getOrganizationGroups(orgId, pageToken = "current", limit = null).test()
                .assertResult(GroupListResponse(emptyList(), null))
            val request = server.takeRequest()
            val url = request.url
            assertEquals("GET", request.method)
            assertEquals("/api/v2/organizations/$orgId/groups", url.encodedPath)
            assertEquals(setOf("page-token"), url.queryParameterNames)
            assertEquals("current", url.queryParameter("page-token"))
        }
    }

    test("getOrganizationGroups sends the limit without a page token for the first page") {
        withGroupResponse("""{"items":[],"next_page_token":null,"total_count":0}""") { client, server ->
            client.getOrganizationGroups(orgId, limit = 2).test()
                .assertResult(GroupListResponse(emptyList(), null, totalCount = 0))
            val url = server.takeRequest().url
            assertNull(url.queryParameter("page-token"))
            assertEquals("2", url.queryParameter("limit"))
        }
    }

    test("getOrganizationGroups sends the limit and decodes the total count for a two-item page") {
        val secondGroup = Group(id = "70dbc16f-a6b9-4c32-b258-4a61a861c24f", name = "Editors")
        val secondGroupJson = """{"id":"${secondGroup.id}","name":"${secondGroup.name}"}"""
        val body = """{"items":[$groupJson,$secondGroupJson],"next_page_token":"next","total_count":27}"""
        withGroupResponse(body) { client, server ->
            client.getOrganizationGroups(orgId, pageToken = "current", limit = 2).test()
                .assertResult(GroupListResponse(listOf(group, secondGroup), "next", totalCount = 27))
            val url = server.takeRequest().url
            assertEquals("current", url.queryParameter("page-token"))
            assertEquals("2", url.queryParameter("limit"))
        }
    }

    test("the pagination overload works with a client that only implements the original endpoint") {
        MockWebServer().use { server ->
            server.start()
            val body = """{"items":[],"next_page_token":null}"""
            server.enqueue(MockResponse(headers = headersOf("Content-Type", "application/json"), body = body))
            val baseUrl = server.url("/api/v2/").toString()
            val client = CircleCIAPIClient("test-token").retrofitV2(baseUrl).create(CircleCIAPIClientV2::class.java)

            client.getOrganizationGroups(orgId, pageToken = "current", limit = null).test()
                .assertResult(GroupListResponse(emptyList(), null))
            val url = server.takeRequest().url
            assertEquals(setOf("page-token"), url.queryParameterNames)
            assertEquals("current", url.queryParameter("page-token"))

            client.getOrganizationGroups(orgId, limit = 2).test()
                .assertFailure(UnsupportedOperationException::class.java)
            assertEquals(1, server.requestCount)
        }
    }

    test("createOrganizationGroup decodes the description without an organization ID") {
        withGroupResponse(groupJson, status = 201) { client, _ ->
            client.createOrganizationGroup(orgId, CreateGroupRequest(group.name)).test()
                .assertResult(group)
        }
    }

    test("createOrganizationGroup sends the name and description when a description is supplied") {
        withGroupResponse(groupJson, status = 201) { client, server ->
            client.createOrganizationGroup(orgId, CreateGroupRequest(group.name, group.description)).test()
                .assertResult(group)
            val body = server.takeRequest().body?.utf8()
            assertEquals("""{"name":"${group.name}","description":"${group.description}"}""", body)
        }
    }

    test("createOrganizationGroup omits the description when only a name is supplied") {
        withGroupResponse(groupJson, status = 201) { client, server ->
            client.createOrganizationGroup(orgId, CreateGroupRequest(group.name)).test()
                .assertResult(group)
            val body = server.takeRequest().body?.utf8()
            assertEquals("""{"name":"${group.name}"}""", body)
        }
    }

    test("getGroup decodes the description without an organization ID") {
        withGroupResponse(groupJson) { client, _ ->
            client.getGroup(orgId, group.id).test().assertResult(group)
        }
    }

    test("getGroup preserves an organization ID when provided") {
        val body = """{"id":"${group.id}","name":"${group.name}","organization_id":"$orgId"}"""
        withGroupResponse(body) { client, _ ->
            client.getGroup(orgId, group.id).test().assertResult(group.copy(organizationId = orgId, description = null))
        }
    }
})

private fun withGroupResponse(body: String, status: Int = 200, verify: (CircleCIAPIClientV2, MockWebServer) -> Unit) {
    MockWebServer().use { server ->
        server.start()
        server.enqueue(MockResponse(code = status, headers = headersOf("Content-Type", "application/json"), body = body))
        val client = CircleCIAPIClient("test-token", server.url("/api/").toString()).clientV2()
        verify(client, server)
    }
}
