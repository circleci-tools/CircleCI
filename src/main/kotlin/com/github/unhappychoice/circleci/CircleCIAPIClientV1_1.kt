package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.request.AddSshKeyRequest
import com.github.unhappychoice.circleci.request.CreateCheckoutKeyRequest
import com.github.unhappychoice.circleci.request.AddHerokuKeyRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.response.Artifact
import com.github.unhappychoice.circleci.response.Build
import com.github.unhappychoice.circleci.response.CheckoutKey
import com.github.unhappychoice.circleci.response.Project
import com.github.unhappychoice.circleci.response.SSHKey
import com.github.unhappychoice.circleci.response.User
import io.reactivex.Observable
import retrofit2.http.*

interface CircleCIAPIClientV1_1 {
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
     * GET: /project/:vcs-type/:username/:project
     *
     * Build summary for each of the last 30 builds for a single git repo.
     */
    @GET("project/{vcs-type}/{username}/{project}")
    fun getProjectBuilds(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): Observable<List<Build>>

    /**
     * GET: /project/:vcs-type/:username/:project
     *
     * Build summary for each of the last 30 builds for a single git repo.
     */
    @GET("project/{vcs-type}/{username}/{project}/tree/{branch}")
    fun getBranchBuilds(
        @Path("vcs-type") vcsType: String,
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
     * GET: /project/:vcs-type/:username/:project/:build_num
     *
     * Full details for a single build. The response includes all of the fields from the build summary.
     * This is also the payload for the [notification webhooks](/docs/configuration/#notify), in which case
     * this object is the value to a key named 'payload'.
     */
    @GET("project/{vcs-type}/{username}/{project}/{build_num}")
    fun getBuild(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<Build>

    /**
     * GET: /project/:vcs-type/:username/:project/:build_num/artifacts
     *
     * List the artifacts produced by a given build.
     */
    @GET("project/{vcs-type}/{username}/{project}/{build_num}/artifacts")
    fun getArtifacts(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<List<Artifact>>

    /**
     * POST: /project/:vcs-type/:username/:project/follow
     *
     * Follow a new Project on CircleCI.
     */
    @POST("project/{vcs-type}/{username}/{project}/follow")
    fun followProject(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String
    ): Observable<Unit>

    /**
     * POST: /project/:vcs-type/:username/:project/:build_num/retry
     *
     * Retries the build, returns a summary of the new build.
     */
    @POST("project/{vcs-type}/{username}/{project}/{build_num}/retry")
    fun retryBuild(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<Build>

    /**
     * POST: /project/:vcs-type/:username/:project/:build_num/cancel
     *
     * Cancels the build, returns a summary of the build.
     */
    @POST("project/{vcs-type}/{username}/{project}/{build_num}/cancel")
    fun cancelBuild(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<Build>

    /**
     * POST: /project/:vcs-type/:username/:project/:build_num/ssh-users
     *
     * Adds a user to the build's SSH permissions.
     */
    @POST("project/{vcs-type}/{username}/{project}/{build_num}/ssh-users")
    fun addSSHUser(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<Build>

    /**
     * POST: /project/:vcs-type/:username/:project
     *
     * Triggers a new build, returns a summary of the build. Optional build parameters can be set using an experimental API.
     */
    @POST("project/{vcs-type}/{username}/{project}")
    fun triggerNewBuild(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: TriggerNewBuildRequest
    ): Observable<Build>

    /**
     * POST: /project/:vcs-type/:username/:project/tree/:branch
     *
     * Triggers a new build, returns a summary of the build. Optional build parameters can be set using an experimental API.
     */
    @POST("project/{vcs-type}/{username}/{project}/tree/{branch}")
    fun triggerNewBuildWithBranch(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("branch") branch: String,
        @Body request: TriggerNewBuildWithBranchRequest
    ): Observable<Build>

    /**
     * DELETE: /project/:vcs-type/:username/:project/build-cache
     *
     * Clears the cache for a project.
     */
    @DELETE("project/{vcs-type}/{username}/{project}/build-cache")
    fun deleteCache(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String
    ): Observable<Unit>

    /**
     * POST: /project/:vcs-type/:username/:project/ssh-key
     *
     * Create an ssh key used to access external systems that require SSH key-based authentication
     */
    @POST("project/{vcs-type}/{username}/{project}/ssh-key")
    fun addSshKey(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: AddSshKeyRequest
    ): Observable<SSHKey>

    /**
     * GET: /project/:vcs-type/:username/:project/checkout-key
     *
     * Lists checkout keys.
     */
    @GET("project/{vcs-type}/{username}/{project}/checkout-key")
    fun getCheckoutKeys(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String
    ): Observable<List<CheckoutKey>>

    /**
     * POST: /project/:vcs-type/:username/:project/checkout-key
     *
     * Create a new checkout key.
     */
    @POST("project/{vcs-type}/{username}/{project}/checkout-key")
    fun createCheckoutKey(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: CreateCheckoutKeyRequest
    ): Observable<CheckoutKey>

    /**
     * GET: /project/:vcs-type/:username/:project/checkout-key/:fingerprint
     *
     * Get a checkout key.
     */
    @GET("project/{vcs-type}/{username}/{project}/checkout-key/{fingerprint}")
    fun getCheckoutKey(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("fingerprint") fingerprint: String
    ): Observable<CheckoutKey>

    /**
     * DELETE: /project/:vcs-type/:username/:project/checkout-key/:fingerprint
     *
     * Delete a checkout key.
     */
    @DELETE("project/{vcs-type}/{username}/{project}/checkout-key/{fingerprint}")
    fun deleteCheckoutKey(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("fingerprint") fingerprint: String
    ): Observable<Unit>


    /**
     * POST: /user/ssh-key
     *
     * Adds a CircleCI key to your GitHub User account.
     */
    @POST("user/ssh-key")
    fun addUserSshKey(@Body request: AddSshKeyRequest): Observable<Unit>

    /**
     * POST: /user/heroku-key
     *
     * Adds your Heroku API key to CircleCI, takes apikey as form param name.
     */
    @FormUrlEncoded
    @POST("user/heroku-key")
    fun addHerokuKey(@Field("apikey") apikey: String): Observable<Unit>
}


