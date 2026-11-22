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

## Features

- Trigger pipelines, inspect workflows and jobs, and retrieve job artifacts and
  test results with the CircleCI API v2 client.
- Manage context environment variables and restrictions, and query workflow
  and job metrics through Insights.
- On master, trigger and list CircleCI Cloud API v3 runs and poll for completion.
- On master, connect to an HTTPS CircleCI Server instance with the API v2 client.

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

### Download a V2 job artifact

List the job's artifacts, select the path you want, and copy its stream to a local file.

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient
import java.io.File

val api = CircleCIAPIClient(token)
val page = api.clientV2().getJobArtifacts("gh/my-org/my-project", "123").blockingSingle()
val artifact = page.items.first { it.path == "reports/junit.xml" }
api.downloadArtifact(artifact) { input ->
    File("junit.xml").outputStream().use { output -> input.copyTo(output) }
}.blockingGet()
```

If the selected path is on a later page, pass `nextPageToken` to `getJobArtifacts`.
The download starts on subscription and runs the callback on an OkHttp thread.
Read or copy the stream within the callback. The client closes the response when
the callback exits, including on failure. Dispose the subscription to cancel
the HTTP call and interrupt pending network reads.

Downloads require HTTPS. For CircleCI Cloud, the client sends `Circle-Token` to
`circleci.com`, `dl.circleci.com`, and subdomains of `circle-artifacts.com` on port 443.
It keeps the token on same-origin redirects and drops it when the origin changes.
This supports storage redirects such as the [S3 redirects described by CircleCI](https://circleci.com/docs/guides/security/security/).

### V2 context environment variables

`addEnvironmentVariableToContext` returns `Observable<EnvironmentVariable>`.
Use the `variable`, `contextId`, `createdAt`, and `updatedAt` properties in place of
map lookups. Both timestamps are `OffsetDateTime` values.
Update explicit `Observable<Any>` declarations and custom client overrides to
`Observable<EnvironmentVariable>`. If a caller still requires `Observable<Any>`,
use `.map<Any> { it }` at that boundary.

### V2 context restrictions

`getContextRestrictions` returns a `ContextProjectRestrictionsList` whose `items` are now
`List<ContextRestriction>`. Replace map casts and key lookups with the `id`, `contextId`,
`restrictionType`, `restrictionValue`, and nullable `name` properties. For project restrictions,
read the project ID from `restrictionValue`. Use `nextPageToken` to retrieve the next page.

## CircleCI Server on master

Set `CIRCLECI_SERVER_URL` to the instance's HTTPS origin (for example,
`https://ci.example.com:8443`) and `CIRCLECI_TOKEN` to a personal API token issued by
that instance.

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient

val instanceUrl = requireNotNull(System.getenv("CIRCLECI_SERVER_URL"))
val token = requireNotNull(System.getenv("CIRCLECI_TOKEN"))
val client = CircleCIAPIClient.server(token, instanceUrl)
val jobs = client.getWorkflowJobs("your-workflow-id").blockingSingle()
println(jobs.items)
```

Use `CircleCIAPIClient.serverAsync(token, instanceUrl)` for asynchronous subscriptions.
Both factories return a V2 client and send requests under `/api/v2/`. The instance
URL accepts an optional port and trailing slash. Reverse-proxy path prefixes are
unsupported. The client follows redirects only within the same HTTPS origin.

The [CircleCI Server automation guide](https://circleci.com/docs/guides/server/orchestrate/analyze-pipelines-during-an-incident/)
documents listing workflow jobs and getting job details. This library tests those
two endpoints against a local HTTPS mock. Compatibility with live CircleCI Server
versions and other V2 endpoints is unverified.

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
