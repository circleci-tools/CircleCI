# CircleCI
[![CircleCI](https://circleci.com/gh/unhappychoice/CircleCI.svg?style=svg)](https://circleci.com/gh/unhappychoice/CircleCI)
[![](https://jitpack.io/v/unhappychoice/circleci.svg)](https://jitpack.io/#unhappychoice/circleci)
[![codecov](https://codecov.io/gh/unhappychoice/circleci/branch/master/graph/badge.svg)](https://codecov.io/gh/unhappychoice/circleci)

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
    compile 'com.github.unhappychoice:circleci:0.1.0'
}
```

## LISENCE
see [LISENCE](./LISENCE)
