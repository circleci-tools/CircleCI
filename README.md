# CircleCI

[![](https://jitpack.io/v/circleci-tools/circleci.svg)](https://jitpack.io/#circleci-tools/circleci)
[![CI](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml/badge.svg)](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/circleci-tools/circleci/branch/master/graph/badge.svg)](https://codecov.io/gh/circleci-tools/circleci)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/circleci-tools/CircleCI.svg)](https://libraries.io/github/circleci-tools/CircleCI)
![GitHub](https://img.shields.io/github/license/circleci-tools/CircleCI.svg)

This is [CircleCI](https://circleci.com) API Client for Kotlin.

The master branch returns RxJava 3 observables (`io.reactivex.rxjava3.core.Observable`).
Version `2.1.2`, used below, returns RxJava 2 observables. When upgrading to a version
that uses RxJava 3, update your imports and reactive integrations to RxJava 3.

## Quick start

Use version `2.1.2` in the dependency declaration below.
Set `CIRCLECI_TOKEN` to your CircleCI Cloud personal API token, then run the following in your Kotlin application to print the authenticated user's login.

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient

fun main() {
    val token = requireNotNull(System.getenv("CIRCLECI_TOKEN")) {
        "Set CIRCLECI_TOKEN to your CircleCI personal API token."
    }
    val user = CircleCIAPIClient(token).clientV2().getMe().blockingFirst()
    println(user.login)
}
```

With the public `CircleCIAPIClient(token)` constructor, `clientV2()` sends requests to
CircleCI Cloud (`https://circleci.com/api/v2/`). The `CircleCIAPIClient.async(token)` factory
on master uses the same URL.
For example, `getMe()` sends the supplied token to `https://circleci.com/api/v2/me`.
Connecting to a CircleCI Server instance requires configuring its API base URL,
which this library's public API does not support.

## Asynchronous calls on master

When using a build from the master branch, use the same `token` from the quick start:

```kotlin
val client = CircleCIAPIClient.async(token).clientV2()
client.getMe().subscribe(
    { user -> println(user.login) },
    { error -> error.printStackTrace() },
)
```

With `CircleCIAPIClient.async(token)`, `subscribe()` returns without waiting for the HTTP response.
Keep the application running until the success or error callback executes.
Use `CircleCIAPIClient(token)` for synchronous subscriptions.

### CircleCI Cloud API v3

Use a personal API token and the ID of an existing pipeline to trigger a run and
wait for it to finish before starting a dependent step. Use `getRun(id)` for a
single status snapshot.

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient
import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest
import com.github.unhappychoice.circleci.waitForRun
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val v3 = CircleCIAPIClient("your-api-token").clientV3()
val created = v3.triggerRun(TriggerRunRequest(pipelineId = "your-pipeline-id")).blockingSingle()
val run = v3.waitForRun(created.data.id, pollInterval = 5.seconds, timeout = 20.minutes)
    .blockingGet().data
check(run.attributes.currentOutcome == "succeeded") {
    "Run ${run.id} ended with outcome ${run.attributes.currentOutcome}"
}
```

`waitForRun` returns a `Single` that polls immediately on subscription, then
waits for the configured interval after each response. It returns the first
response whose phase is `ended`, even if an earlier response had a
`currentOutcome`. A later rerun can change the observed outcome.

The timeout covers the full wait, including time spent queued, awaiting
approval, or fetching status. It emits a `TimeoutException` when the deadline
expires. Request errors, including HTTP 429, terminate the wait immediately.
Dispose the subscription to stop polling. The CircleCI run itself continues
independently.

List recent runs for a project using its UUID, or call `listMyRuns()` for runs
triggered by the authenticated user across projects. Both methods accept an optional
`pageSize` (up to 20) and an opaque `cursor`. Pass `page.next` back to the same
method to fetch the next page. A null `page.next` marks the last page.

```kotlin
val projectId = "your-project-uuid"
val firstPage = v3.listProjectRuns(projectId, pageSize = 10).blockingSingle()
firstPage.data.forEach { summary ->
    println("${summary.id}: ${summary.attributes.phase} (${summary.branch})")
}

firstPage.data.firstOrNull()?.let { selected ->
    val details = v3.getRun(selected.id).blockingSingle().data
    println("${details.id}: ${details.attributes.currentOutcome}")
}

val nextPage = firstPage.page.next?.let { cursor ->
    v3.listProjectRuns(projectId, pageSize = 10, cursor = cursor).blockingSingle()
}
```

### V2 context restrictions

`getContextRestrictions` returns a `ContextProjectRestrictionsList` whose `items` are now
`List<ContextRestriction>`. Replace map casts and key lookups with the `id`, `contextId`,
`restrictionType`, `restrictionValue`, and nullable `name` properties. For project restrictions,
read the project ID from `restrictionValue`. Use `nextPageToken` to retrieve the next page.

## Installation

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
dependencies {
    implementation 'com.github.circleci-tools:circleci:2.1.2'
}
```

## License

[MIT License](./LICENSE)
