# CircleCI

[![](https://jitpack.io/v/circleci-tools/circleci.svg)](https://jitpack.io/#circleci-tools/circleci)
[![CI](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml/badge.svg)](https://github.com/circleci-tools/CircleCI/actions/workflows/ci.yml)
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
    compile 'com.github.circleci-tools:circleci:0.1.0'
}
```

## LISENCE
see [LISENCE](./LISENCE)
