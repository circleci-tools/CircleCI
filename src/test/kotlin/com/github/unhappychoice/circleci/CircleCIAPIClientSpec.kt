package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.Artifact
import com.github.unhappychoice.circleci.response.Build
import com.github.unhappychoice.circleci.response.Project
import com.github.unhappychoice.circleci.response.User
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
  override fun spec() {
    describe("CircleCIAPIClient") {
      val client = MockCircleCIAPIClientV1()
      val userName = "unhappychoice"
      val project = "circleci"
      val branch = "master"
      val buildNum = 1

      before {
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
      }

      it("#getProjects() should return response") {
        val subscriber = TestSubscriber<List<Project>>()
        client.getProjects().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#getProjectBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getProjectBuilds(userName, project).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#getBranchBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getBranchBuilds(userName, project, branch).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#getRecentBuilds() should return response") {
        val subscriber = TestSubscriber<List<Build>>()
        client.getRecentBuilds().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#getBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.getBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#getArtifacts() should return response") {
        val subscriber = TestSubscriber<List<Artifact>>()
        client.getArtifacts(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#retryBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.retryBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#cancelBuild() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.cancelBuild(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#addSSHUser() should return response") {
        val subscriber = TestSubscriber<Build>()
        client.addSSHUser(userName, project, buildNum).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#triggerNewBuild() should return response") {
        val request = TriggerNewBuildRequest()
        val subscriber = TestSubscriber<Build>()
        client.triggerNewBuild(userName, project, request).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#triggerNewBuildWithBranch() should return response") {
        val request = TriggerNewBuildWithBranchRequest()
        val subscriber = TestSubscriber<Build>()
        client.triggerNewBuildWithBranch(userName, project, branch, request).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }

      it("#deleteCache() should return response") {
        val subscriber = TestSubscriber<Unit>()
        client.deleteCache(userName, project).subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`
      }
    }
  }

  private fun <T> TestSubscriber<T>.isFinished(): Boolean {
    try {
      assertNoErrors()
      assertCompleted()
    } catch (e: Throwable) {
      e.printStackTrace()
      return false
    }
    return true
  }
}