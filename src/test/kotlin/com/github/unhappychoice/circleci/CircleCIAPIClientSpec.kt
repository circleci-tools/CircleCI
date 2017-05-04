package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.*
import com.winterbe.expekt.expect
import io.polymorphicpanda.kspec.KSpec
import io.polymorphicpanda.kspec.describe
import io.polymorphicpanda.kspec.it
import io.polymorphicpanda.kspec.junit.JUnitKSpecRunner
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.runner.RunWith

@RunWith(JUnitKSpecRunner::class)
class CircleCIAPIClientSpec: KSpec() {
  lateinit var client: CircleCIAPIClientV1
  val userName = "unhappychoice"
  val project = "circleci"
  val branch = "master"
  val buildNum = 1

  override fun spec() {
    describe("CircleCIAPIClient") {
      before {
        client = MockCircleCIAPIClientV1()
        RxJavaPlugins.onComputationScheduler(TestScheduler())
        RxJavaPlugins.onIoScheduler(TestScheduler())
        RxJavaPlugins.onNewThreadScheduler(TestScheduler())
      }

      it("#getMe() should return response") {
        val subscriber = client.getMe().test()
        expect(subscriber.isFinished()).to.be.`true`

        val user = subscriber.values().first()
        expectNotNull(user)
      }

      it("#getProjects() should return response") {
        val subscriber = client.getProjects().test()
        expect(subscriber.isFinished()).to.be.`true`

        val projects = subscriber.values().first()
        expect(projects).not.to.be.empty

        val project = projects.first()
        expectNotNull(project)
      }

      it("#getProjectBuilds() should return response") {
        val subscriber = client.getProjectBuilds(userName, project).test()
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.values().first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getBranchBuilds() should return response") {
        val subscriber = client.getBranchBuilds(userName, project, branch).test()
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.values().first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getRecentBuilds() should return response") {
        val subscriber = client.getRecentBuilds().test()
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.values().first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getBuild() should return response") {
        val subscriber = client.getBuild(userName, project, buildNum).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)

        val commit = build.allCommitDetails!!.first()
        expectNotNull(commit)

        val step = build.steps!!.first()
        expectNotNull(step)

        val action = step.actions.first()
        expectNotNull(action)
      }

      it("#getArtifacts() should return response") {
        val subscriber = client.getArtifacts(userName, project, buildNum).test()
        expect(subscriber.isFinished()).to.be.`true`

        val artifacts = subscriber.values().first()
        expect(artifacts).not.to.be.empty

        val artifact = artifacts.first()
        expectNotNull(artifact)
      }

      it("#retryBuild() should return response") {
        val subscriber = client.retryBuild(userName, project, buildNum).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)
      }

      it("#cancelBuild() should return response") {
        val subscriber = client.cancelBuild(userName, project, buildNum).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)
      }

      it("#addSSHUser() should return response") {
        val subscriber = client.addSSHUser(userName, project, buildNum).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)
      }

      it("#triggerNewBuild() should return response") {
        val request = TriggerNewBuildRequest()
        val subscriber = client.triggerNewBuild(userName, project, request).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)
      }

      it("#triggerNewBuildWithBranch() should return response") {
        val request = TriggerNewBuildWithBranchRequest()
        val subscriber = client.triggerNewBuildWithBranch(userName, project, branch, request).test()
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.values().first()
        expectNotNull(build)
      }

      it("#deleteCache() should return response") {
        val subscriber = client.deleteCache(userName, project).test()
        expect(subscriber.isFinished()).to.be.`true`
      }
    }
  }

  private fun <T> TestObserver<T>.isFinished(): Boolean {
    assertNoErrors()
    assertComplete()
    return true
  }

  private fun expectNotNull(user: User) {
    expect(user.avatarUrl).not.to.be.`null`
    expect(user.login).not.to.be.`null`
    expect(user.name).not.to.be.`null`
    expect(user.projects).not.to.be.`null`
    expect(user.selectedEmail).not.to.be.`null`
  }

  private fun expectNotNull(project: Project) {
    expect(project.branches!!).not.to.be.`null`
    expect(project.parallel).not.to.be.`null`
    expect(project.reponame).not.to.be.`null`
    expect(project.username).not.to.be.`null`
    expect(project.vcsUrl).not.to.be.`null`
  }

  private fun expectNotNull(build: Build) {
    expect(build.authorEmail).not.to.be.`null`
    expect(build.authorDate).not.to.be.`null`
    expect(build.authorName).not.to.be.`null`
    expect(build.body).not.to.be.`null`
    expect(build.branch).not.to.be.`null`
    expect(build.buildNum).not.to.be.`null`
    expect(build.buildTimeMillis).not.to.be.`null`
    expect(build.buildUrl).not.to.be.`null`
    expect(build.canceled).not.to.be.`null`
    expect(build.canceler).to.be.`null`
    expect(build.committerEmail).not.to.be.`null`
    expect(build.committerDate).not.to.be.`null`
    expect(build.committerName).not.to.be.`null`
    expect(build.compare).to.be.`null`
    expect(build.dontBuild).to.be.`null`
    expect(build.failed).to.be.`null`
    expect(build.infrastructureFail).not.to.be.`null`
    expect(build.isFirstGreenBuild).not.to.be.`null`
    expect(build.jobName).to.be.`null`
    expect(build.lifecycle).not.to.be.`null`
    expect(build.outcome).not.to.be.`null`
    expect(build.queuedAt).not.to.be.`null`
    expect(build.retryOf).to.be.`null`
    expect(build.reponame).not.to.be.`null`
    expect(build.startTime).not.to.be.`null`
    expect(build.status).not.to.be.`null`
    expect(build.stopTime).not.to.be.`null`
    expect(build.subject).not.to.be.`null`
    expect(build.timedout).not.to.be.`null`
    expect(build.usageQueuedAt).not.to.be.`null`
    expect(build.username).not.to.be.`null`
    expect(build.vcsRevision).not.to.be.`null`
    expect(build.vcsTag).to.be.`null`
    expect(build.vcsUrl).not.to.be.`null`
    expect(build.why).not.to.be.`null`
  }

  private fun expectNotNull(step: BuildStep) {
    expect(step.name).not.to.be.`null`
    expect(step.actions).not.to.be.`null`
  }

  private fun expectNotNull(action: BuildAction) {
    expect(action.bashCommand).to.be.`null`
    expect(action.canceled).to.be.`null`
    expect(action.endTime).not.to.be.`null`
    expect(action.exitCode).to.be.`null`
    expect(action.failed).to.be.`null`
    expect(action.hasOutput).not.to.be.`null`
    expect(action.infrastructureFail).to.be.`null`
    expect(action.index).not.to.be.`null`
    expect(action.name).not.to.be.`null`
    expect(action.outputUrl).not.to.be.`null`
    expect(action.parallel).not.to.be.`null`
    expect(action.runTimeMillis).not.to.be.`null`
    expect(action.startTime).not.to.be.`null`
    expect(action.status).not.to.be.`null`
    expect(action.step).not.to.be.`null`
    expect(action.timedout).to.be.`null`
    expect(action.truncated).not.to.be.`null`
    expect(action.type).not.to.be.`null`
  }

  private fun expectNotNull(commit: Commit) {
    expect(commit.authorDate).not.to.be.`null`
    expect(commit.authorEmail).not.to.be.`null`
    expect(commit.authorLogin).not.to.be.`null`
    expect(commit.authorName).not.to.be.`null`
    expect(commit.body).not.to.be.`null`
    expect(commit.branch).not.to.be.`null`
    expect(commit.commit).not.to.be.`null`
    expect(commit.commitUrl).not.to.be.`null`
    expect(commit.committerDate).not.to.be.`null`
    expect(commit.committerEmail).not.to.be.`null`
    expect(commit.committerLogin).not.to.be.`null`
    expect(commit.committerName).not.to.be.`null`
    expect(commit.subject).not.to.be.`null`
  }

  private fun expectNotNull(artifact: Artifact) {
    expect(artifact.nodeIndex).not.to.be.`null`
    expect(artifact.path).not.to.be.`null`
    expect(artifact.prettyPath).not.to.be.`null`
    expect(artifact.url).not.to.be.`null`
  }
}