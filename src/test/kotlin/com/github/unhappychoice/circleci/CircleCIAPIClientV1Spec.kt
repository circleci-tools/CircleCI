package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import com.github.unhappychoice.circleci.v1.response.User
import com.github.unhappychoice.circleci.v1.response.Project
import com.github.unhappychoice.circleci.v1.response.Build
import com.github.unhappychoice.circleci.v1.response.BuildStep
import com.github.unhappychoice.circleci.v1.response.BuildAction
import com.github.unhappychoice.circleci.v1.response.Commit
import com.github.unhappychoice.circleci.v1.response.Artifact
import com.github.unhappychoice.circleci.v1.response.SSHKey
import com.github.unhappychoice.circleci.v2.response.CheckoutKey
import com.winterbe.expekt.expect
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.spekframework.spek2.Spek

object CircleCIAPIClientV1Spec: Spek({
  lateinit var client: CircleCIAPIClientV1
  lateinit var client1_1: CircleCIAPIClientV1_1
  val vcsType = "github"
  val userName = "unhappychoice"
  val project = "circleci"
  val branch = "master"
  val buildNum = 1

  group("CircleCIAPIClientV1") {
    beforeEachTest {
      client = MockCircleCIAPIClientV1()
      RxJavaPlugins.onComputationScheduler(TestScheduler())
      RxJavaPlugins.onIoScheduler(TestScheduler())
      RxJavaPlugins.onNewThreadScheduler(TestScheduler())
    }

    test("#getMe() should return response") {
      val subscriber = client.getMe().test()
      expect(subscriber.isFinished()).to.be.`true`

      val user = subscriber.values().first()
      expectNotNull(user)
    }

    test("#getProjects() should return response") {
      val subscriber = client.getProjects().test()
      expect(subscriber.isFinished()).to.be.`true`

      val projects = subscriber.values().first()
      expect(projects).not.to.be.empty

      val project = projects.first()
      expectNotNull(project)
    }

    test("#getProjectBuilds() should return response") {
      val subscriber = client.getProjectBuilds(userName, project).test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getBranchBuilds() should return response") {
      val subscriber = client.getBranchBuilds(userName, project, branch).test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getRecentBuilds() should return response") {
      val subscriber = client.getRecentBuilds().test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getBuild() should return response") {
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

    test("#getArtifacts() should return response") {
      val subscriber = client.getArtifacts(userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val artifacts = subscriber.values().first()
      expect(artifacts).not.to.be.empty

      val artifact = artifacts.first()
      expectNotNull(artifact)
    }

    test("#retryBuild() should return response") {
      val subscriber = client.retryBuild(userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#cancelBuild() should return response") {
      val subscriber = client.cancelBuild(userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#addSSHUser() should return response") {
      val subscriber = client.addSSHUser(userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#triggerNewBuild() should return response") {
      val request = TriggerNewBuildRequest()
      val subscriber = client.triggerNewBuild(userName, project, request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#triggerNewBuildWithBranch() should return response") {
      val request = TriggerNewBuildWithBranchRequest()
      val subscriber = client.triggerNewBuildWithBranch(userName, project, branch, request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#deleteCache() should return response") {
      val subscriber = client.deleteCache(userName, project).test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    test("#addUserSshKey() should return response") {
      val request = AddSshKeyRequest("hostname", "private_key")
      val subscriber = client.addUserSshKey(request).test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    test("#addHerokuKey() should return response") {
      val subscriber = client.addHerokuKey("apikey").test()
      expect(subscriber.isFinished()).to.be.`true`
    }
  }

  group("CircleCIAPIClientV1_1") {
    beforeEachTest {
      client1_1 = MockCircleCIAPIClientV1_1()
      RxJavaPlugins.onComputationScheduler(TestScheduler())
      RxJavaPlugins.onIoScheduler(TestScheduler())
      RxJavaPlugins.onNewThreadScheduler(TestScheduler())
    }

    test("#getMe() should return response") {
      val subscriber = client1_1.getMe().test()
      expect(subscriber.isFinished()).to.be.`true`

      val user = subscriber.values().first()
      expectNotNull(user)
    }

    test("#getProjects() should return response") {
      val subscriber = client1_1.getProjects().test()
      expect(subscriber.isFinished()).to.be.`true`

      val projects = subscriber.values().first()
      expect(projects).not.to.be.empty

      val project = projects.first()
      expectNotNull(project)
    }

    test("#getProjectBuilds() should return response") {
      val subscriber = client1_1.getProjectBuilds(vcsType, userName, project).test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getBranchBuilds() should return response") {
      val subscriber = client1_1.getBranchBuilds(vcsType, userName, project, branch).test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getRecentBuilds() should return response") {
      val subscriber = client1_1.getRecentBuilds().test()
      expect(subscriber.isFinished()).to.be.`true`

      val builds = subscriber.values().first()
      expect(builds).not.to.be.empty

      val build = builds.first()
      expectNotNull(build)
    }

    test("#getBuild() should return response") {
      val subscriber = client1_1.getBuild(vcsType, userName, project, buildNum).test()
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

    test("#getArtifacts() should return response") {
      val subscriber = client1_1.getArtifacts(vcsType, userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val artifacts = subscriber.values().first()
      expect(artifacts).not.to.be.empty

      val artifact = artifacts.first()
      expectNotNull(artifact)
    }

    test("#retryBuild() should return response") {
      val subscriber = client1_1.retryBuild(vcsType, userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#cancelBuild() should return response") {
      val subscriber = client1_1.cancelBuild(vcsType, userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#addSSHUser() should return response") {
      val subscriber = client1_1.addSSHUser(vcsType, userName, project, buildNum).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#triggerNewBuild() should return response") {
      val request = TriggerNewBuildRequest()
      val subscriber = client1_1.triggerNewBuild(vcsType, userName, project, request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#triggerNewBuildWithBranch() should return response") {
      val request = TriggerNewBuildWithBranchRequest()
      val subscriber = client1_1.triggerNewBuildWithBranch(vcsType, userName, project, branch, request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val build = subscriber.values().first()
      expectNotNull(build)
    }

    test("#deleteCache() should return response") {
      val subscriber = client1_1.deleteCache(vcsType, userName, project).test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    test("#addUserSshKey() should return response") {
      val request = AddSshKeyRequest("hostname", "private_key")
      val subscriber = client1_1.addUserSshKey(request).test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    test("#addHerokuKey() should return response") {
      val subscriber = client1_1.addHerokuKey("apikey").test()
      expect(subscriber.isFinished()).to.be.`true`
    }
  }
})

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
  expect(user.pusherId).not.to.be.`null`
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
