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
look up its status. `currentOutcome` can be null while the run is in progress.

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient
import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest

val v3 = CircleCIAPIClient("your-api-token").clientV3()
val created = v3.triggerRun(TriggerRunRequest(pipelineId = "your-pipeline-id")).blockingSingle()
val run = v3.getRun(created.data.id).blockingSingle().data
println("${run.id}: ${run.attributes.phase} (${run.attributes.currentOutcome})")
```

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
