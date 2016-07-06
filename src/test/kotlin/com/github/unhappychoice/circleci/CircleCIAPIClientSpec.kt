package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.*
import com.winterbe.expekt.expect
import io.polymorphicpanda.kspec.KSpec
import io.polymorphicpanda.kspec.describe
import io.polymorphicpanda.kspec.it
import io.polymorphicpanda.kspec.junit.JUnitKSpecRunner
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import rx.plugins.RxJavaPlugins
import rx.plugins.RxJavaSchedulersHook
import rx.schedulers.Schedulers

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
        RxJavaPlugins.getInstance().registerSchedulersHook(object : RxJavaSchedulersHook() {
          override fun getNewThreadScheduler() = Schedulers.immediate()
          override fun getComputationScheduler() = Schedulers.immediate()
          override fun getIOScheduler() = Schedulers.immediate()
        })
      }

      it("#getMe() should return response") {
        val subscriber = TestSubscriber<User>()
        client.getMe().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val user = subscriber.onNextEvents.first()
        expectNotNull(user)
      }

      it("#getProjects() should return response") {
        val subscriber = TestSubscriber<List<Project>>()
        client.getProjects().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val projects = subscriber.onNextEvents.first()
        expect(projects).not.to.be.empty

        val project = projects.first()
        expectNotNull(project)
      }

      it("#getProjectBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getProjectBuilds(userName, project).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.onNextEvents.first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getBranchBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getBranchBuilds(userName, project, branch).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.onNextEvents.first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getRecentBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getRecentBuilds().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val builds = subscriber.onNextEvents.first()
        expect(builds).not.to.be.empty

        val build = builds.first()
        expectNotNull(build)
      }

      it("#getBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.getBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#getArtifacts() should return response") {
        val subscriber = TestSubscriber<List<Artifact>>()
        client.getArtifacts(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val artifacts = subscriber.onNextEvents.first()
        expect(artifacts).not.to.be.empty

        val artifact = artifacts.first()
        expectNotNull(artifact)
      }

      it("#retryBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.retryBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#cancelBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.cancelBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#addSSHUser() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.addSSHUser(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#triggerNewBuild() should return response") {
        val request = TriggerNewBuildRequest()
        val subscriber = TestSubscriber<Build>()
        client.triggerNewBuild(userName, project, request).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#triggerNewBuildWithBranch() should return response") {
        val request = TriggerNewBuildWithBranchRequest()
        val subscriber = TestSubscriber<Build>()
        client.triggerNewBuildWithBranch(userName, project, branch, request).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val build = subscriber.onNextEvents.first()
        expectNotNull(build)
      }

      it("#deleteCache() should return response") {
        val subscriber = TestSubscriber<Unit>()
        client.deleteCache(userName, project).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }
    }
  }

  private fun <T> TestSubscriber<T>.isFinished(): Boolean {
    assertNoErrors()
    assertCompleted()
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

  private fun expectNotNull(artifact: Artifact) {
    expect(artifact.nodeIndex).not.to.be.`null`
    expect(artifact.path).not.to.be.`null`
    expect(artifact.prettyPath).not.to.be.`null`
    expect(artifact.url).not.to.be.`null`
  }
}