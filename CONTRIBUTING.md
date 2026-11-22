# Contributing

## Set up the project

Install JDK 21, the version used by CI, and point `JAVA_HOME` to it. Fork the
repository on GitHub, clone your fork, and create a branch from `master`. Use
the Gradle wrapper included in the repository.

## Run the checks

From the repository root, run the same Gradle tasks as CI:

```sh
./gradlew build
./gradlew jacocoTestReport
```

The build treats Kotlin compiler warnings as errors in both production and test code.

On Windows, use `gradlew.bat` in place of `./gradlew`.

The Spek tests live in `src/test/kotlin`. When changing an API method, look for
the corresponding client spec and mock client there.

See the [architecture guide](docs/architecture.md) for the client structure and
where to start when changing an endpoint.

## Open a pull request

Push your branch to your fork and open a pull request against
`circleci-tools/CircleCI:master`. Describe what changed, why, and which checks
you ran. See the [Code of Conduct](CODE_OF_CONDUCT.md) for community guidelines.
