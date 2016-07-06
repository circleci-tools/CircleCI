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

        val expected = User(
          "test@example.com",
          "test",
          "test",
          "https://avatars.githubusercontent.com/u/5608948?v=3",
          mapOf(
            "https://example.com/test/project1" to mapOf("on_dashboard" to true, "emails" to "default"),
            "https://example.com/test/project2" to mapOf("on_dashboard" to true, "emails" to "default"),
            "https://example.com/test/project3" to mapOf("on_dashboard" to true, "emails" to "default")
          )
        )
        expect(subscriber.onNextEvents[0]).to.equal(expected)
      }

      it("#getProjects() should return response") {
        val subscriber = TestSubscriber<List<Project>>()
        client.getProjects().subscribe(subscriber)
        expect(subscriber.isFinished()).to.be.`true`

        val expected = listOf(
          Project(
            1,
            "test-project",
            "test",
            "https://example.com/test/test-project",
            mapOf(
              "master" to mapOf(
                "last_non_success" to mapOf(
                  "added_at" to "2015-08-07T00:00:00.000Z",
                  "pushed_at" to "2015-08-07T00:00:00.000Z",
                  "vcs_revision" to "db5f8b5ef6c1df1dd9c3df388af2624aa930324a",
                  "build_num" to 159.0,
                  "status" to "failed",
                  "outcome" to "failed"
                ),
                "pusher_logins" to listOf("test"),
                "recent_builds" to listOf(
                  mapOf(
                    "added_at" to "2015-08-07T00:00:00.000Z",
                    "pushed_at" to "2015-08-07T00:00:00.000Z",
                    "vcs_revision" to "db5f8b5ef6c1df1dd9c3df388af2624aa930324a",
                    "build_num" to 159.0,
                    "status" to "failed",
                    "outcome" to "failed"
                  )
                ),
                "running_builds" to listOf<String>()
              )
            )
          )
        )
        expect(subscriber.onNextEvents[0]).to.equal(expected)
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
    assertNoErrors()
    assertCompleted()
    return true
  }
}