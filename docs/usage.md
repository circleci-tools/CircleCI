# Usage and API Reference

See the [README](../README.md) for runnable examples (quick start, asynchronous calls,
API v3 runs, artifact downloads, CircleCI Server). This page indexes what each client
exposes so you can find the right method before reading its Retrofit declaration.

## Entry points

[`CircleCIAPIClient(token)`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClient.kt)
and `CircleCIAPIClient.async(token)` construct every client below for CircleCI Cloud.
`CircleCIAPIClient.server(token, instanceUrl)` and `.serverAsync(token, instanceUrl)`
construct a V2 client for a CircleCI Server instance instead.

- `clientV2()` — [`CircleCIAPIClientV2`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV2.kt), the current API.
- `clientV3()` — [`CircleCIAPIClientV3`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV3.kt), triggering and polling pipeline runs.
- `client()` and `client1_1()` / `clinet1_1()` — deprecated V1 and V1.1 clients, kept for existing callers.

Request bodies live under `v1/request`, `v2/request`, and `v3/request`; responses live
under the matching `v1/response`, `v2/response`, and `v3/response` packages.

## V2 (`clientV2()`)

`CircleCIAPIClientV2.kt` groups its methods with comments by resource. Each group maps to
the CircleCI API v2 resource of the same name:

- **User** — `getMe`, `getUser`, `getCollaborations`.
- **Context** — create, list, and delete contexts, their environment variables, and their restrictions.
- **Deploy** — list and get deploy components, component versions, and environments.
- **Insights** — branches, flaky tests, job and workflow metrics, workflow runs, workflow and test summaries.
- **Job** — job details, artifacts, tests, and cancellation.
- **OIDC Token Management** — get, patch, and delete organization and project OIDC claims.
- **Organization** — create, get, and delete organizations, and manage their orb allow list.
- **Groups** — list, create, get, and delete organization groups.
- **Usage** — create and fetch usage export jobs.
- **Policy Management** — decision logs and settings, policy bundles, and policy documents.
- **Pipeline** — list, get, trigger, and continue pipelines; read their config, values, and workflows.
- **Pipeline Definition** — list, create, get, update, and delete pipeline definitions and their triggers.
- **Project** — create, get, and delete projects; manage their checkout keys, environment variables, and settings.
- **Rollback** — roll back a deployed component to a previous version.
- **Trigger** — list, create, get, update, and delete pipeline definition triggers.
- **Schedule** — get, list, create, update, and delete project schedules.
- **Webhook** — get, list, create, update, and delete webhooks.
- **Workflow** — get a workflow, list its jobs, cancel or rerun it, and approve a job.

Read the method's Retrofit annotations in the source file for its exact path, query
parameters, and request/response types.

## V3 (`clientV3()`)

`CircleCIAPIClientV3` covers CircleCI Cloud pipeline runs: `triggerRun`, `getRun`,
`listProjectRuns`, and `listMyRuns`. The
[`waitForRun`](../src/main/kotlin/com/github/unhappychoice/circleci/RunPolling.kt)
extension polls `getRun` until a run ends; see the README's "CircleCI Cloud API v3"
section for its polling and concurrency behavior.

## CircleCI Server

`CircleCIAPIClient.server(token, instanceUrl)` and `.serverAsync(token, instanceUrl)`
return a `CircleCIAPIClientV2` scoped to that instance. See
[Architecture](architecture.md) and the README's "CircleCI Server on master" section for
which endpoints are tested against a live-shaped mock.

## V1 and V1.1 (deprecated)

[`CircleCIAPIClientV1`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV1.kt)
and [`CircleCIAPIClientV1_1`](../src/main/kotlin/com/github/unhappychoice/circleci/CircleCIAPIClientV1_1.kt)
cover legacy builds (trigger, retry, cancel, list by project or branch), artifacts, SSH
and Heroku keys, checkout keys, environment variables, test metadata, and legacy
pipeline triggering. Use `clientV2()` for new code; see the README's "Legacy SSH key
creation" section for one documented migration.

## Downloading artifacts

[`CircleCIAPIClient.downloadArtifact`](../src/main/kotlin/com/github/unhappychoice/circleci/ArtifactDownloader.kt)
streams a V2 `Artifact` to a callback. See the README's "Download a V2 job
artifact" section for pagination and the redirect and authentication rules it follows.
