# CircleCI

[![](https://jitpack.io/v/circleci-tools/circleci.svg)](https://jitpack.io/#circleci-tools/circleci)
[![CI](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml/badge.svg)](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/circleci-tools/circleci/branch/master/graph/badge.svg)](https://codecov.io/gh/circleci-tools/circleci)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/circleci-tools/CircleCI.svg)](https://libraries.io/github/circleci-tools/CircleCI)
![GitHub](https://img.shields.io/github/license/circleci-tools/CircleCI.svg)

This is [CircleCI](https://circleci.com) API Client for Kotlin.

The client returns RxJava 3 observables (`io.reactivex.rxjava3.core.Observable`).
Existing RxJava 2 callers must update their imports and reactive integrations to RxJava 3.

## Usage
```kotlin
val token = "your-api-token"
val client = CircleCIAPIClient.async(token).clientV2()
client.getMe().subscribe({ user -> println(user.login) }, { error -> error.printStackTrace() })
```

With `CircleCIAPIClient.async(token)`, `subscribe()` returns without waiting for the HTTP response.
Use `CircleCIAPIClient(token)` to keep synchronous subscriptions.

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

For CircleCI Server, use `clientV2()`.

## Installation

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
dependencies {
    implementation 'com.github.circleci-tools:circleci:0.1.0'
}
```

## LISENCE
see [LISENCE](./LISENCE)
