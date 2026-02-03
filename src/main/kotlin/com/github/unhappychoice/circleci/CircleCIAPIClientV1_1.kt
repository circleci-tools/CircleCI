package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.*
import com.github.unhappychoice.circleci.v1.response.*
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

    /**
     * GET: /project/:vcs-type/:username/:project/:build_num/tests
     *
     * Provides test metadata for a job.
     */
    @GET("project/{vcs-type}/{username}/{project}/{build_num}/tests")
    fun getTestMetadata(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("build_num") buildNumber: Int
    ): Observable<TestMetadataResponse>

    /**
     * GET: /project/:vcs-type/:username/:project/envvar
     *
     * Lists the environment variables for a project.
     */
    @GET("project/{vcs-type}/{username}/{project}/envvar")
    fun getEnvironmentVariables(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String
    ): Observable<List<EnvironmentVariable>>

    /**
     * GET: /project/:vcs-type/:username/:project/envvar/:name
     *
     * Gets the hidden value of an environment variable.
     */
    @GET("project/{vcs-type}/{username}/{project}/envvar/{name}")
    fun getEnvironmentVariable(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("name") name: String
    ): Observable<EnvironmentVariable>

    /**
     * GET: /project/:vcs-type/:username/:project/latest/artifacts
     *
     * Returns an array of artifacts produced by the latest job run on a given branch.
     */
    @GET("project/{vcs-type}/{username}/{project}/latest/artifacts")
    fun getLatestArtifacts(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Query("branch") branch: String? = null,
        @Query("filter") filter: String? = null
    ): Observable<List<Artifact>>

    /**
     * POST: /project/:vcs-type/:username/:project/build
     *
     * Triggers a pipeline of the specified project, by branch, revision, or tag.
     */
    @POST("project/{vcs-type}/{username}/{project}/build")
    fun triggerPipeline(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: TriggerPipelineRequest
    ): Observable<TriggerPipelineResponse>

    /**
     * POST: /project/:vcs-type/:username/:project/envvar
     *
     * Creates a new environment variable.
     */
    @POST("project/{vcs-type}/{username}/{project}/envvar")
    fun addEnvironmentVariable(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: AddEnvironmentVariableRequest
    ): Observable<EnvironmentVariable>

    /**
     * DELETE: /project/:vcs-type/:username/:project/ssh-key
     *
     * Deletes an SSH key from the system by fingerprint.
     */
    @HTTP(method = "DELETE", path = "project/{vcs-type}/{username}/{project}/ssh-key", hasBody = true)
    fun deleteSshKey(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Body request: DeleteSshKeyRequest
    ): Observable<Unit>

    /**
     * DELETE: /project/:vcs-type/:username/:project/envvar/:name
     *
     * Deletes an environment variable.
     */
    @DELETE("project/{vcs-type}/{username}/{project}/envvar/{name}")
    fun deleteEnvironmentVariable(
        @Path("vcs-type") vcsType: String,
        @Path("username") userName: String,
        @Path("project") project: String,
        @Path("name") name: String
    ): Observable<Unit>
}


