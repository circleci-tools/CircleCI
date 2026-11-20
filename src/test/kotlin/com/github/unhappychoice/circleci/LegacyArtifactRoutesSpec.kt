package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.response.Artifact
import io.reactivex.rxjava3.core.Observable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek

@Suppress("DEPRECATION")
object LegacyArtifactRoutesSpec : Spek({
  val artifacts = (1..3).map { index ->
    Artifact(
      nodeIndex = 0,
      path = "/tmp/path/to/something$index",
      prettyPath = "\$CIRCLE_ARTIFACTS/path/to/something$index",
      url = "https://example.com/path/to/something$index"
    )
  }

  fun assertArtifactRoute(path: String, getArtifacts: (CircleCIAPIClient) -> Observable<List<Artifact>>) {
    MockWebServer().use { server ->
      val fixture = checkNotNull(Artifact::class.java.getResource("/com/github/unhappychoice/circleci/json/artifacts.json"))
      server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(fixture.readText()))

      val client = CircleCIAPIClient("test-token", server.url("/api/").toString())
      getArtifacts(client).test().assertResult(artifacts)

      assertEquals(1, server.requestCount)
      val request = server.takeRequest()
      assertEquals("GET", request.method)
      assertEquals(path, request.path)
    }
  }

  test("V1 GET requests and decodes build artifacts") {
    assertArtifactRoute("/api/v1/project/example-org/example-project/42/artifacts") {
      it.client().getArtifacts("example-org", "example-project", 42)
    }
  }

  test("V1.1 GET includes the VCS type and decodes build artifacts") {
    assertArtifactRoute("/api/v1.1/project/bitbucket/example-org/example-project/42/artifacts") {
      it.client1_1().getArtifacts("bitbucket", "example-org", "example-project", 42)
    }
  }
})
