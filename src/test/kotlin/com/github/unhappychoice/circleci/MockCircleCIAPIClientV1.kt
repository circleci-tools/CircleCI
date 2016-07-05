package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.Artifact
import com.github.unhappychoice.circleci.response.Build
import com.github.unhappychoice.circleci.response.Project
import com.github.unhappychoice.circleci.response.User
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import rx.Observable
import java.io.File
import java.lang.reflect.Type
import java.util.*

class MockCircleCIAPIClientV1: CircleCIAPIClientV1 {
  override fun getMe(): Observable<User> {
    return mockResponse("json/me.json", User::class.java)
  }

  override fun getProjects(): Observable<List<Project>> {
    return mockResponse("json/projects.json", object: TypeToken<List<Project>>(){}.type)
  }

  override fun getProjectBuilds(userName: String, project: String, offset: Int, limit: Int): Observable<List<Build>> {
    return mockResponse("json/projectBuilds.json", object: TypeToken<List<Build>>(){}.type)
  }

  override fun getBranchBuilds(userName: String, project: String, branch: String, offset: Int, limit: Int): Observable<List<Build>> {
    return mockResponse("json/projectBuilds.json", object: TypeToken<List<Build>>(){}.type)
  }

  override fun getRecentBuilds(offset: Int, limit: Int): Observable<List<Build>> {
    return mockResponse("json/projectBuilds.json", object: TypeToken<List<Build>>(){}.type)
  }

  override fun getBuild(userName: String, project: String, buildNumber: Int): Observable<Build> {
    return mockResponse("json/build.json", Build::class.java)
  }

  override fun getArtifacts(userName: String, project: String, buildNumber: Int): Observable<List<Artifact>> {
    return mockResponse("json/projectBuilds.json", object: TypeToken<List<Artifact>>(){}.type)
  }

  override fun retryBuild(userName: String, project: String, buildNumber: Int): Observable<Build> {
    return mockResponse("json/retryBuild.json", Build::class.java)
  }

  override fun cancelBuild(userName: String, project: String, buildNumber: Int): Observable<Build> {
    return mockResponse("json/cancelBuild.json", Build::class.java)
  }

  override fun addSSHUser(userName: String, project: String, buildNumber: Int): Observable<Build> {
    return mockResponse("json/sshUser.json", Build::class.java)
  }

  override fun triggerNewBuild(userName: String, project: String, request: TriggerNewBuildRequest): Observable<Build> {
    return mockResponse("json/triggerNewBuild.json", Build::class.java)
  }

  override fun triggerNewBuildWithBranch(userName: String, project: String, branch: String, request: TriggerNewBuildWithBranchRequest): Observable<Build> {
    return mockResponse("json/triggerNewBuildWithBranch.json", Build::class.java)
  }

  override fun deleteCache(userName: String, project: String): Observable<Unit> {
    return Observable.just(Unit)
  }

  private fun <T> mockResponse(fileName: String, type: Type): Observable<T> {
    return Observable.just(converter().fromJson(readJson(fileName), type))
  }

  private fun converter() = GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .create()

  private fun readJson(fileName: String): String {
    val file = File(MockCircleCIAPIClientV1::class.java.getResource(fileName).file)
    val scanner = Scanner(file)
    try {
      return scanner.useDelimiter("\\A").next()
    } finally {
      scanner.close()
    }
  }
}
