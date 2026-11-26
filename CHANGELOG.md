# Changelog

All notable changes to this project are documented in this file. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and version numbers match the
tags on [GitHub Releases](https://github.com/circleci-tools/CircleCI/releases).

Versions published before `2.0.0` predate this changelog; see the
[tags](https://github.com/circleci-tools/CircleCI/tags) for that history.

## [Unreleased]

Changes on `master` that have not shipped in a tagged release yet:

### Changed
- Observables now use RxJava 3 (`io.reactivex.rxjava3.core.Observable`) instead of RxJava 2.
- `CircleCIAPIClient.async(token)` and the CircleCI Cloud API v3 client (`clientV3()`) move
  to master ahead of the next release.

### Added
- `CircleCIAPIClient.server(token, instanceUrl)` and `.serverAsync(token, instanceUrl)` to
  connect a V2 client to an HTTPS CircleCI Server instance.
- `Observable<T>.await()` and `Completable.await()` suspend extension functions
  (`Coroutines.kt`), built on `kotlinx-coroutines-rx3`, alongside the existing RxJava3 API.

## [2.1.2] - 2026-11-14

### Changed
- Updated build dependencies: Axion Release, Gradle Wrapper, Kotlin, and Commons Codec.

## [2.1.1] - 2026-09-12

### Changed
- Updated build dependencies: Kotlin, Gradle Wrapper, Commons Codec, JUnit Platform
  Launcher, and Axion Release.

## [2.1.0] - 2026-02-27

### Added
- `jobNumber` and `projectSlug` to the V2 `Job` response model.

## [2.0.0] - 2026-02-16

### Changed
- Replaced the CircleCI badge in the README with a GitHub Actions badge.
- Updated build dependencies: JUnit Platform Launcher and Kotlin.

[Unreleased]: https://github.com/circleci-tools/CircleCI/compare/2.1.2...master
[2.1.2]: https://github.com/circleci-tools/CircleCI/compare/2.1.1...2.1.2
[2.1.1]: https://github.com/circleci-tools/CircleCI/compare/2.1.0...2.1.1
[2.1.0]: https://github.com/circleci-tools/CircleCI/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/circleci-tools/CircleCI/compare/1.0.0...2.0.0
