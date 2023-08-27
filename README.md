# CircleCI

[![](https://jitpack.io/v/circleci-tools/circleci.svg)](https://jitpack.io/#circleci-tools/circleci)
[![CircleCI](https://circleci.com/gh/circleci-tools/CircleCI.svg?style=shield)](https://circleci.com/gh/circleci-tools/CircleCI)
[![codecov](https://codecov.io/gh/circleci-tools/circleci/branch/master/graph/badge.svg)](https://codecov.io/gh/circleci-tools/circleci)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/circleci-tools/CircleCI.svg)](https://libraries.io/github/circleci-tools/CircleCI)
![GitHub](https://img.shields.io/github/license/circleci-tools/CircleCI.svg)

This is [CircleCI](https://circleci.com) API Client for Kotlin.

Only RxAdapter is supported for now.

## Usage
```kotlin
val token = "your-api-token"
val client = CircleCIAPIClient(token).client()
client.getMe().subscribe() // will returns response
```

## Installation

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
dependencies {
    compile 'com.github.circleci-tools:circleci:0.1.0'
}
```

## LISENCE
see [LISENCE](./LISENCE)
