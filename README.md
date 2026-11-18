# CircleCI

[![](https://jitpack.io/v/circleci-tools/circleci.svg)](https://jitpack.io/#circleci-tools/circleci)
[![CI](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml/badge.svg)](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/circleci-tools/circleci/branch/master/graph/badge.svg)](https://codecov.io/gh/circleci-tools/circleci)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/circleci-tools/CircleCI.svg)](https://libraries.io/github/circleci-tools/CircleCI)
![GitHub](https://img.shields.io/github/license/circleci-tools/CircleCI.svg)

This is [CircleCI](https://circleci.com) API Client for Kotlin.

Only RxAdapter is supported for now.

## Quick start

Use version `2.1.2` in the dependency declaration below.
Set `CIRCLECI_TOKEN` to your CircleCI personal API token, then run the following in your Kotlin application to print the authenticated user's login.

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

## LISENCE
see [LISENCE](./LISENCE)
