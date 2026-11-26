package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Artifact
import io.reactivex.rxjava3.core.Single
import java.io.InputStream

/**
 * A V2 client for a CircleCI Server instance that streams artifacts using the same validated
 * instance origin and token used for V2 requests.
 */
interface CircleCIServerClient : CircleCIAPIClientV2 {
  fun <T : Any> downloadArtifact(artifact: Artifact, consume: (InputStream) -> T): Single<T>
}

internal class CircleCIServerClientImpl(private val client: CircleCIAPIClient) :
  CircleCIServerClient, CircleCIAPIClientV2 by client.clientV2() {
  override fun <T : Any> downloadArtifact(artifact: Artifact, consume: (InputStream) -> T): Single<T> =
    client.downloadArtifact(artifact, consume)
}
