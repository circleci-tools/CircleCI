package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.Artifact
import com.github.unhappychoice.circleci.response.Build
import com.github.unhappychoice.circleci.response.Project
import com.github.unhappychoice.circleci.response.User
import io.reactivex.Observable
import retrofit2.http.*

interface CircleCIAPIClientV1 {
  /**
   * GET: /me
   *
   * Provides information about the signed in user.
   */
  @GET("me")
  fun getMe(): Observable<User>

  /**
   * GET: /projects
   *
   * List of all the projects you're following on CircleCI, with build information organized by branch.
   */
  @GET("projects")
  fun getProjects(): Observable<List<Project>>

  /**
   * GET: /project/:username/:project
   *
   * Build summary for each of the last 30 builds for a single git repo.
   */
  @GET("project/{username}/{project}")
  fun getProjectBuilds(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Query("offset") offset: Int = 0,
    @Query("limit") limit: Int = 20
  ): Observable<List<Build>>

  /**
   * GET: /project/:username/:project
   *
   * Build summary for each of the last 30 builds for a single git repo.
   */
  @GET("project/{username}/{project}/tree/{branch}")
  fun getBranchBuilds(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("branch") branch: String,
    @Query("offset") offset: Int = 0,
    @Query("limit") limit: Int = 20
  ): Observable<List<Build>>

  /**
   * GET: /recent-builds
   *
   * Build summary for each of the last 30 recent builds, ordered by build_num.
   */
  @GET("recent-builds")
  fun getRecentBuilds(@Query("offset") offset: Int = 0, @Query("limit") limit: Int = 20): Observable<List<Build>>

  /**
   * GET: /project/:username/:project/:build_num
   *
   * Full details for a single build. The response includes all of the fields from the build summary.
   * This is also the payload for the [notification webhooks](/docs/configuration/#notify), in which case
   * this object is the value to a key named 'payload'.
   */
  @GET("project/{username}/{project}/{build_num}")
  fun getBuild(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("build_num") buildNumber: Int
  ): Observable<Build>

  /**
   * GET: /project/:username/:project/:build_num/artifacts
   *
   * List the artifacts produced by a given build.
   */
  @GET("project/{username}/{project}/{build_num}/artifacts")
  fun getArtifacts(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("build_num") buildNumber: Int
  ): Observable<List<Artifact>>

  /**
   * POST: /project/:username/:project/:build_num/retry
   *
   * Retries the build, returns a summary of the new build.
  */
  @POST("project/{username}/{project}/{build_num}/retry")
  fun retryBuild(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("build_num") buildNumber: Int
  ): Observable<Build>

  /**
   * POST: /project/:username/:project/:build_num/cancel
   *
   * Cancels the build, returns a summary of the build.
  */
  @POST("project/{username}/{project}/{build_num}/cancel")
  fun cancelBuild(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("build_num") buildNumber: Int
  ): Observable<Build>

  /**
   * POST: /project/:username/:project/:build_num/ssh-users
   *
   * Adds a user to the build's SSH permissions.
  */
  @POST("project/{username}/{project}/{build_num}/ssh-users")
  fun addSSHUser(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("build_num") buildNumber: Int
  ): Observable<Build>

  /**
   * POST: /project/:username/:project
   *
   * Triggers a new build, returns a summary of the build. Optional build parameters can be set using an experimental API.
   */
  @POST("project/{username}/{project}")
  fun triggerNewBuild(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Body request: TriggerNewBuildRequest
  ): Observable<Build>

  /**
   * POST: /project/:username/:project/tree/:branch
   *
   * Triggers a new build, returns a summary of the build. Optional build parameters can be set using an experimental API.
   */
  @POST("project/{username}/{project}/tree/{branch}")
  fun triggerNewBuildWithBranch(
    @Path("username") userName: String,
    @Path("project") project: String,
    @Path("branch") branch: String,
    @Body request: TriggerNewBuildWithBranchRequest
  ): Observable<Build>

  /**
   * DELETE: /project/:username/:project/build-cache
   *
   * Clears the cache for a project.
   */
  @DELETE("project/{username}/{project}/build-cache")
  fun deleteCache(
    @Path("username") userName: String,
    @Path("project") project: String
  ): Observable<Unit>

  /**
   * POST: /project/:username/:project/ssh-key
   *
   * Create an ssh key used to access external systems that require SSH key-based authentication
  */
  // TODO: Implement endpoint

  /**
   * GET: /project/:username/:project/checkout-key
   *
   * Lists checkout keys.
  */
  // TODO: Implement endpoint

  /**
   * POST: /project/:username/:project/checkout-key
   *
   * Create a new checkout key.
  */
  // TODO: Implement endpoint

  /**
   * GET: /project/:username/:project/checkout-key/:fingerprint
   *
   * Get a checkout key.
  */
  // TODO: Implement endpoint

  /**
   * DELETE: /project/:username/:project/checkout-key/:fingerprint
   *
   * Delete a checkout key.
  */
  // TODO: Implement endpoint

  /**
   * POST: /user/ssh-key
   *
   * Adds a CircleCI key to your GitHub User account.
  */
  // TODO: Implement endpoint

  /**
   * POST: /user/heroku-key
   *
   * Adds your Heroku API key to CircleCI, takes apikey as form param name.
   */
  // TODO: Implement endpoint
}


