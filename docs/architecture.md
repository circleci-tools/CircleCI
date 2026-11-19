# Architecture

Start with [`CircleCIAPIClient.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClient.kt).
It takes an API token and creates Retrofit services for the versioned paths
under `https://circleci.com/api/`. Use `clientV2()` for API v2. The
[`CircleCIAPIClientV1`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV1.kt)
and [`CircleCIAPIClientV1_1`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV1_1.kt)
interfaces describe the legacy endpoints behind the deprecated accessors.

## How a request flows

1. [`CircleCIAPIClientV2.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV2.kt)
   declares the HTTP methods, paths, query parameters, and bodies using
   Retrofit annotations. For example, `getMe()` maps to `GET /api/v2/me`
   and returns an RxJava 3 `Observable<User>`.
2. Subscribing runs the HTTP call. `CircleCIAPIClient(token)` uses a
   synchronous call adapter, and `CircleCIAPIClient.async(token)` uses an
   asynchronous one. Retrofit uses Gson to serialize request bodies, and
   OkHttp sends the token in `Circle-Token` for v2 or uses Basic
   authentication for the legacy clients.
3. Retrofit uses Gson to decode responses into
   the Kotlin types in [`v2/request`](../src/main/kotlin/com/github/unhappychoice/circleci/v2/request)
   and [`v2/response`](../src/main/kotlin/com/github/unhappychoice/circleci/v2/response).
   The v2 Gson configuration in `CircleCIAPIClient.kt` includes custom
   deserializers for pipeline values and `OffsetDateTime`.

## Where to make changes

For a v2 endpoint change, start with its Retrofit declaration and follow its
parameter and return types to the corresponding models. Use
[`CircleCIAPIClientV2HttpSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV2HttpSpec.kt)
as an example of testing URL and query encoding, JSON bodies, and response
decoding against a local HTTP server.

For shared HTTP behavior, start with `authenticatedClient` in
`CircleCIAPIClient.kt` and
[`SameOriginRedirectInterceptor.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/SameOriginRedirectInterceptor.kt).
The redirect interceptor only follows eligible redirects that stay on HTTPS
with the same host and port.
[`AuthenticatedTransportSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/AuthenticatedTransportSpec.kt)
covers authentication and redirect behavior.

See [Contributing](../CONTRIBUTING.md#run-the-checks) for the Gradle checks.
