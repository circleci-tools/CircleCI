# Architecture

Start with [`CircleCIAPIClient.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClient.kt).
It takes an API token and creates Retrofit services for the versioned paths
under `https://circleci.com/api/`. Use `clientV2()` for API v2 and `clientV3()`
for triggering, fetching, and listing API v3 runs. The
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
   asynchronous one. OkHttp sends the token in `Circle-Token` for v2,
   uses Bearer authentication for v3, and uses Basic authentication for the
   legacy clients.
3. For v2, Retrofit uses Gson to serialize request body models in
   [`v2/request`](../src/main/kotlin/com/github/unhappychoice/circleci/v2/request)
   to JSON and decode JSON responses into models in
   [`v2/response`](../src/main/kotlin/com/github/unhappychoice/circleci/v2/response).
   The v2 Gson configuration in `CircleCIAPIClient.kt` includes custom
   deserializers for pipeline values and `OffsetDateTime`.
   For v3, Retrofit uses the default Gson converter with models in
   [`v3/request`](../src/main/kotlin/com/github/unhappychoice/circleci/v3/request)
   and [`v3/response`](../src/main/kotlin/com/github/unhappychoice/circleci/v3/response).

## Where to make changes

For a v2 endpoint change, start with its Retrofit declaration and follow its
parameter and return types to the corresponding models. Use
[`CircleCIAPIClientV2HttpSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV2HttpSpec.kt)
as an example of testing URL and query encoding, JSON bodies, and response
decoding against a local HTTP server.

For v3 run endpoints, start with
[`CircleCIAPIClientV3.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV3.kt)
and [`CircleCIAPIClientV3HttpSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV3HttpSpec.kt).
The `waitForRun` extension in
[`RunPolling.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/RunPolling.kt)
polls the `getRun` endpoint. Its tests live in
[`RunPollingSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/RunPollingSpec.kt).

For shared HTTP behavior, start with `authenticatedClient` in
`CircleCIAPIClient.kt` and
[`SameOriginRedirectInterceptor.kt`](../src/main/kotlin/com/github/unhappychoice/circleci/SameOriginRedirectInterceptor.kt).
The redirect interceptor only follows eligible redirects that stay on HTTPS
with the same host and port.
[`AuthenticatedTransportSpec.kt`](../src/test/kotlin/com/github/unhappychoice/circleci/AuthenticatedTransportSpec.kt)
covers authentication and redirect behavior.

See [Contributing](../CONTRIBUTING.md#run-the-checks) for the Gradle checks.
