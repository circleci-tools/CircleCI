package com.github.unhappychoice.circleci

import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object DeployListingRequestSpec : Spek({
    val orgId = "b9291e0d-a11e-41fb-8517-c545388b5953"
    val pageSize = 25
    val pageToken = "next+/="

    group("Deploy listing requests") {
        test("lists the first page of components with the organization and page size") {
            val request = captureRequest { it.listComponents(orgId, pageSize) }
            assertListingRequest(request, "components", orgId, pageSize)
        }

        test("lists the next page of components with the organization, page size, and token") {
            val request = captureRequest { it.listComponents(orgId, pageSize, pageToken) }
            assertListingRequest(request, "components", orgId, pageSize, pageToken)
        }

        test("lists components filtered by project") {
            val projectId = "39723015-b399-4601-9ff6-bd1bfbed8fa8"
            val request = captureRequest { it.listComponents(orgId, pageSize, pageToken, projectId = projectId) }
            assertListingRequest(request, "components", orgId, pageSize, pageToken, projectId)
        }

        test("lists component versions without an environment filter") {
            val request = captureRequest { it.listComponentVersions("component-id") }
            assertDeployRequest(request, "components/component-id/versions", emptyMap())
        }

        test("lists component versions filtered by environment") {
            val environmentId = "6e342ef2-0013-4bd4-8203-a6782e1c38fa"
            val request = captureRequest {
                it.listComponentVersions("component-id", pageToken, environmentId = environmentId)
            }
            assertDeployRequest(
                request,
                "components/component-id/versions",
                mapOf("page-token" to listOf(pageToken), "environment-id" to listOf(environmentId))
            )
        }

        test("lists the first page of environments with the organization and page size") {
            val request = captureRequest { it.listEnvironments(orgId, pageSize) }
            assertListingRequest(request, "environments", orgId, pageSize)
        }

        test("lists the next page of environments with the organization, page size, and token") {
            val request = captureRequest { it.listEnvironments(orgId, pageSize, pageToken) }
            assertListingRequest(request, "environments", orgId, pageSize, pageToken)
        }
    }
})

private fun captureRequest(list: (CircleCIAPIClientV2) -> Observable<*>): Request {
    lateinit var request: Request
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        request = chain.request()
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("""{"items":[]}""".toResponseBody("application/json".toMediaType()))
            .build()
    }.build()
    val client = Retrofit.Builder()
        .baseUrl("https://circleci.com/api/v2/")
        .client(httpClient)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CircleCIAPIClientV2::class.java)
    list(client).test().assertComplete().assertNoErrors().assertValueCount(1)
    return request
}

private fun assertListingRequest(
    request: Request,
    resource: String,
    orgId: String,
    pageSize: Int,
    pageToken: String? = null,
    projectId: String? = null
) {
    val expectedQuery = buildMap {
        put("org-id", listOf(orgId))
        put("page-size", listOf(pageSize.toString()))
        pageToken?.let { put("page-token", listOf(it)) }
        projectId?.let { put("project-id", listOf(it)) }
    }
    assertDeployRequest(request, resource, expectedQuery)
}

private fun assertDeployRequest(request: Request, resource: String, expectedQuery: Map<String, List<String>>) {
    assertEquals("GET", request.method)
    assertEquals("/api/v2/deploy/$resource", request.url.encodedPath)
    assertEquals(expectedQuery, request.url.queryParameterNames.associateWith(request.url::queryParameterValues))
}
