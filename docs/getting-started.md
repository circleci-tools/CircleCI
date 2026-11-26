# Getting Started

This walks through adding the library to a Gradle project and calling a few
V2 endpoints end to end. See the [README](../README.md) for the shortest
possible quick start.

## Install

Add the JitPack repository and the dependency. Use version `2.1.2` for a
release build (RxJava 2 observables), or depend on a commit from `master` for
RxJava 3 observables, the asynchronous and CircleCI Server factories, and the
API v3 client.

Groovy DSL (`build.gradle`):

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

Kotlin DSL (`build.gradle.kts`):

```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.circleci-tools:circleci:2.1.2")
}
```

## A first walkthrough

Set `CIRCLECI_TOKEN` to a CircleCI Cloud personal API token before running
any of the snippets below.

### 1. Confirm the token works

```kotlin
import com.github.unhappychoice.circleci.CircleCIAPIClient

val token = requireNotNull(System.getenv("CIRCLECI_TOKEN"))
val api = CircleCIAPIClient(token)
val me = api.clientV2().getMe().blockingFirst()
println("Authenticated as ${me.login}")
```

### 2. Look up a project

Project slugs follow `<provider>/<org>/<repo>`, for example `gh/my-org/my-project`.

```kotlin
val project = api.clientV2().getProjectBySlug("gh/my-org/my-project").blockingFirst()
println("${project.name} (${project.organizationName})")
```

### 3. List its recent pipelines

```kotlin
val pipelines = api.clientV2().getProjectPipelines("gh/my-org/my-project").blockingFirst()
pipelines.items.forEach { pipeline ->
    println("#${pipeline.number} ${pipeline.state} on ${pipeline.vcs?.branch}")
}
```

### 4. Trigger a new pipeline

```kotlin
import com.github.unhappychoice.circleci.v2.request.TriggerNewPipelineRequest

val triggered = api.clientV2()
    .triggerNewPipeline("gh/my-org/my-project", TriggerNewPipelineRequest(branch = "main"))
    .blockingFirst()
println("Triggered pipeline #${triggered.number}, state ${triggered.state}")
```

### 5. Inspect its workflows

```kotlin
val workflows = api.clientV2().getPipelineWorkflows(triggered.id).blockingFirst()
workflows.items.forEach { workflow ->
    println("${workflow.name}: ${workflow.status}")
}
```

## Where to go next

- [Usage and API Reference](usage.md) indexes every client method by resource.
- [Architecture](architecture.md) explains how a request flows through the library and where to look when changing an endpoint.
- [Troubleshooting](troubleshooting.md) covers token and CircleCI Server URL errors.
- The README's "CircleCI Cloud API v3" and "Download a V2 job artifact" sections cover triggering and polling API v3 runs and streaming job artifacts, both available on `master`.
