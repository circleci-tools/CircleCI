package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateOrganizationRequest
import com.github.unhappychoice.circleci.v2.request.CreateProjectRequest
import com.github.unhappychoice.circleci.v2.response.Collaboration
import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import io.reactivex.Observable
import retrofit2.http.*

interface CircleCIAPIClientV2 {
    /**
     * GET: /me
     *
     * Provides information about the signed in user.
     */
    @GET("me")
    fun getMe(): Observable<User>

    /**
     * GET: /me/collaborations
     *
     * Provides the set of organizations of which a user is a member or a collaborator.
     */
    @GET("me/collaborations")
    fun getCollaborations(): Observable<List<Collaboration>>

    /**
     * GET: /project
     *
     * List of all the projects you're following on CircleCI, with build information organized by branch.
     */
    @GET("project")
    fun getProjects(): Observable<List<Project>>

    /**
     * GET: /project/{project-slug}
     *
     * Get a project by project-slug.
     */
    @GET("project/{project-slug}")
    fun getProject(@Path("project-slug") projectSlug: String): Observable<Project>

    /**
     * GET: /project/{vcs-type}/{organization-name}/{project-name}
     *
     * Get a project by VCS type, organization name and project name.
     */
    @GET("project/{vcs-type}/{organization-name}/{project-name}")
    fun getProjectByVcsType(
        @Path("vcs-type") vcsType: String,
        @Path("organization-name") organizationName: String,
        @Path("project-name") projectName: String
    ): Observable<Project>

    /**
     * GET: /project/{project-slug}/settings
     *
     * Get project settings.
     */
    @GET("project/{project-slug}/settings")
    fun getProjectSettings(@Path("project-slug") projectSlug: String): Observable<ProjectSettings>

    /**
     * GET: /workflow/{id}
     *
     * Get a workflow by ID.
     */
    @GET("workflow/{id}")
    fun getWorkflow(@Path("id") id: String): Observable<Workflow>

    /**
     * GET: /workflow/{id}/job
     *
     * Get a workflow's jobs.
     */
    @GET("workflow/{id}/job")
    fun getWorkflowJobs(@Path("id") id: String): Observable<List<Job>>

    /**
     * GET: /pipeline
     *
     * Get all pipelines.
     */
    @GET("pipeline")
    fun getPipelines(): Observable<List<Pipeline>>

    /**
     * GET: /pipeline/{pipeline-id}
     *
     * Get a pipeline by ID.
     */
    @GET("pipeline/{pipeline-id}")
    fun getPipelineById(@Path("pipeline-id") pipelineId: String): Observable<Pipeline>

    /**
     * GET: /project/{project-slug}/pipeline
     *
     * Get all pipelines for a project.
     */
    @GET("project/{project-slug}/pipeline")
    fun getProjectPipelines(@Path("project-slug") projectSlug: String): Observable<List<Pipeline>>

    /**
     * GET: /pipeline/{pipeline-id}/workflow
     *
     * Get a pipeline's workflows.
     */
    @GET("pipeline/{pipeline-id}/workflow")
    fun getPipelineWorkflows(@Path("pipeline-id") pipelineId: String): Observable<List<Workflow>>

    /**
     * POST: /project/{project-slug}/pipeline
     *
     * Triggers a new pipeline.
     */
    @POST("project/{project-slug}/pipeline")
    fun triggerNewPipeline(
        @Path("project-slug") projectSlug: String,
        @Body request: TriggerNewPipelineRequest
    ): Observable<Pipeline>

    /**
     * GET: /project/{project-slug}/pipeline/{pipeline-number}
     *
     * Get a pipeline by project-slug and pipeline-number.
     */
    @GET("project/{project-slug}/pipeline/{pipeline-number}")
    fun getPipelineByNumber(
        @Path("project-slug") projectSlug: String,
        @Path("pipeline-number") pipelineNumber: Int
    ): Observable<Pipeline>

    /**
     * GET: /job/{job-id}
     *
     * Get a job's details.
     */
    @GET("job/{job-id}")
    fun getJobDetails(@Path("job-id") jobId: String): Observable<Job>

    /**
     * POST: /job/{job-id}/cancel
     *
     * Cancel a job.
     */
    @POST("job/{job-id}/cancel")
    fun cancelJob(@Path("job-id") jobId: String): Observable<Job>

    /**
     * GET: /job/{job-id}/artifacts
     *
     * Get a job's artifacts.
     */
    @GET("job/{job-id}/artifacts")
    fun getJobArtifacts(@Path("job-id") jobId: String): Observable<List<Artifact>>

    /**
     * GET: /insights/{project-slug}/workflows
     *
     * Get project workflow metrics.
     */
    @GET("insights/{project-slug}/workflows")
    fun getProjectWorkflowMetrics(
        @Path("project-slug") projectSlug: String,
        @Query("branch") branch: String? = null,
        @Query("workflow-name") workflowName: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<ProjectMetricsTimeSeries>

    /**
     * GET: /insights/{project-slug}/workflows/{workflow-name}
     *
     * Get workflow metrics.
     */
    @GET("insights/{project-slug}/workflows/{workflow-name}")
    fun getWorkflowMetrics(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("branch") branch: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<ProjectMetricsTimeSeries>

    /**
     * GET: /insights/{project-slug}/workflows/{workflow-name}/runs
     *
     * Get workflow runs.
     */
    @GET("insights/{project-slug}/workflows/{workflow-name}/runs")
    fun getWorkflowRuns(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("branch") branch: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<List<WorkflowRun>>

    /**
     * GET: /insights/{project-slug}/jobs
     *
     * Get project job metrics.
     */
    @GET("insights/{project-slug}/jobs")
    fun getProjectJobMetrics(
        @Path("project-slug") projectSlug: String,
        @Query("branch") branch: String? = null,
        @Query("job-name") jobName: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<ProjectMetricsTimeSeries>

    /**
     * GET: /insights/{project-slug}/jobs/{job-name}
     *
     * Get job metrics.
     */
    @GET("insights/{project-slug}/jobs/{job-name}")
    fun getJobMetrics(
        @Path("project-slug") projectSlug: String,
        @Path("job-name") jobName: String,
        @Query("branch") branch: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<ProjectMetricsTimeSeries>

    /**
     * GET: /insights/{project-slug}/jobs/{job-name}/runs
     *
     * Get job runs.
     */
    @GET("insights/{project-slug}/jobs/{job-name}/runs")
    fun getJobRuns(
        @Path("project-slug") projectSlug: String,
        @Path("job-name") jobName: String,
        @Query("branch") branch: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<List<JobRun>>

    /**
     * GET: /context
     *
     * Get all contexts.
     */
    @GET("context")
    fun getContexts(): Observable<List<Context>>

    /**
     * GET: /context/{context-id}
     *
     * Get a context.
     */
    @GET("context/{context-id}")
    fun getContext(@Path("context-id") contextId: String): Observable<Context>

    /**
     * GET: /context/{context-id}/environment-variable
     *
     * Get all environment variables for a context.
     */
    @GET("context/{context-id}/environment-variable")
    fun getContextEnvironmentVariables(@Path("context-id") contextId: String): Observable<List<EnvironmentVariable>>

    /**
     * POST: /context/{context-id}/environment-variable
     *
     * Add an environment variable to a context.
     */
    @POST("context/{context-id}/environment-variable")
    fun addContextEnvironmentVariable(
        @Path("context-id") contextId: String,
        @Body request: AddEnvironmentVariableRequest
    ): Observable<EnvironmentVariable>

    /**
     * PUT: /context/{context-id}/environment-variable/{env-var-name}
     *
     * Update an environment variable in a context.
     */
    @PUT("context/{context-id}/environment-variable/{env-var-name}")
    fun updateContextEnvironmentVariable(
        @Path("context-id") contextId: String,
        @Path("env-var-name") envVarName: String,
        @Body request: AddEnvironmentVariableRequest
    ): Observable<EnvironmentVariable>

    /**
     * DELETE: /context/{context-id}/environment-variable/{env-var-name}
     *
     * Delete an environment variable from a context.
     */
    @DELETE("context/{context-id}/environment-variable/{env-var-name}")
    fun deleteContextEnvironmentVariable(
        @Path("context-id") contextId: String,
        @Path("env-var-name") envVarName: String
    ): Observable<Unit>

    /**
     * POST: /context
     *
     * Creates a new context.
     */
    @POST("context")
    fun createContext(@Body request: com.github.unhappychoice.circleci.v2.request.CreateContextRequest): Observable<Context>

    /**
     * DELETE: /context/{context-id}
     *
     * Delete a context.
     */
    @DELETE("context/{context-id}")
    fun deleteContext(@Path("context-id") contextId: String): Observable<Unit>

    /**
     * GET: /insights/pages/{project-slug}/summary
     *
     * Get summary metrics and trends for a project across it's workflows and branches.
     */
    @GET("insights/pages/{project-slug}/summary")
    fun getProjectWorkflowsPageData(
        @Path("project-slug") projectSlug: String,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("branches") branches: List<String>? = null,
        @Query("workflow-names") workflowNames: List<String>? = null
    ): Observable<ProjectWorkflowsPageData>

    /**
     * GET: /insights/time-series/{project-slug}/workflows/{workflow-name}/jobs
     *
     * Job timeseries data.
     */
    @GET("insights/time-series/{project-slug}/workflows/{workflow-name}/jobs")
    fun getJobTimeseries(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("branch") branch: String? = null,
        @Query("granularity") granularity: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null
    ): Observable<JobTimeseriesResponse>

    /**
     * GET: /insights/{org-slug}/summary
     *
     * Get summary metrics with trends for the entire org, and for each project.
     */
    @GET("insights/{org-slug}/summary")
    fun getOrgSummaryData(
        @Path("org-slug") orgSlug: String,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("project-names") projectNames: List<String>? = null
    ): Observable<OrgSummaryData>

    /**
     * GET: /insights/{project-slug}/branches
     *
     * Get all branches for a project.
     */
    @GET("insights/{project-slug}/branches")
    fun getAllInsightsBranches(
        @Path("project-slug") projectSlug: String,
        @Query("workflow-name") workflowName: String? = null
    ): Observable<ProjectBranches>

    /**
     * GET: /insights/{project-slug}/flaky-tests
     *
     * Get flaky tests for a project.
     */
    @GET("insights/{project-slug}/flaky-tests")
    fun getFlakyTests(
        @Path("project-slug") projectSlug: String,
        @Query("workflow-name") workflowName: String? = null
    ): Observable<FlakyTests>

    /**
     * GET: /schedule
     *
     * Get all schedules.
     */
    @GET("schedule")
    fun getSchedules(): Observable<List<Schedule>>

    /**
     * GET: /schedule/{schedule-id}
     *
     * Get a schedule by ID.
     */
    @GET("schedule/{schedule-id}")
    fun getScheduleById(@Path("schedule-id") scheduleId: String): Observable<Schedule>

    /**
     * GET: /project/{project-slug}/schedule
     *
     * Get all schedules for a project.
     */
    @GET("project/{project-slug}/schedule")
    fun getProjectSchedules(@Path("project-slug") projectSlug: String): Observable<List<Schedule>>

    /**
     * GET: /webhook
     *
     * Get all webhooks.
     */
    @GET("webhook")
    fun getWebhooks(): Observable<List<Webhook>>

    /**
     * GET: /webhook/{webhook-id}
     *
     * Get a webhook by ID.
     */
    @GET("webhook/{webhook-id}")
    fun getWebhookById(@Path("webhook-id") webhookId: String): Observable<Webhook>

    /**
     * POST: /webhook
     *
     * Create a new webhook.
     */
    @POST("webhook")
    fun createWebhook(@Body request: CreateWebhookRequest): Observable<Webhook>

    /**
     * PUT: /webhook/{webhook-id}
     *
     * Update a webhook.
     */
    @PUT("webhook/{webhook-id}")
    fun updateWebhook(
        @Path("webhook-id") webhookId: String,
        @Body request: CreateWebhookRequest
    ): Observable<Webhook>

    /**
     * DELETE: /webhook/{webhook-id}
     *
     * Delete a webhook.
     */
    @DELETE("webhook/{webhook-id}")
    fun deleteWebhook(@Path("webhook-id") webhookId: String): Observable<Unit>

    

    

    /**
     * DELETE: /org/{orgID}/oidc-custom-claims
     *
     * Deletes org-level custom claims of OIDC identity tokens
     */
    @DELETE("org/{orgID}/oidc-custom-claims")
    fun deleteOrgClaims(
        @Path("orgID") orgID: String,
        @Query("claims") claims: String
    ): Observable<ClaimResponse>

    /**
     * GET: /org/{orgID}/oidc-custom-claims
     *
     * Fetches org-level custom claims of OIDC identity tokens
     */
    @GET("org/{orgID}/oidc-custom-claims")
    fun getOrgClaims(
        @Path("orgID") orgID: String
    ): Observable<ClaimResponse>

    /**
     * PATCH: /org/{orgID}/oidc-custom-claims
     *
     * Creates/Updates org-level custom claims of OIDC identity tokens
     */
    @PATCH("org/{orgID}/oidc-custom-claims")
    fun patchOrgClaims(
        @Path("orgID") orgID: String,
        @Body request: PatchClaimsRequest
    ): Observable<ClaimResponse>

    /**
     * GET: /owner/{ownerID}/context/{context}/decision
     *
     * Retrieves the owner's decision audit logs.
     */
    @GET("owner/{ownerID}/context/{context}/decision")
    fun getDecisionLogs(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Query("status") status: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("branch") branch: String? = null,
        @Query("project_id") projectId: String? = null,
        @Query("build_number") buildNumber: String? = null,
        @Query("offset") offset: Int? = null
    ): Observable<List<DecisionLog>>

    /**
     * POST: /owner/{ownerID}/context/{context}/decision
     *
     * This endpoint will evaluate input data (config+metadata) against owner's stored policies and return a decision.
     */
    @POST("owner/{ownerID}/context/{context}/decision")
    fun makeDecision(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Body request: Map<String, Any> // Using Map<String, Any> as a placeholder for the request body
    ): Observable<Decision>

    /**
     * GET: /owner/{ownerID}/context/{context}/decision/settings
     *
     * Get the decision settings
     */
    @GET("owner/{ownerID}/context/{context}/decision/settings")
    fun getDecisionSettings(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String
    ): Observable<DecisionSettings>

    /**
     * PATCH: /owner/{ownerID}/context/{context}/decision/settings
     *
     * Set the decision settings
     */
    @PATCH("owner/{ownerID}/context/{context}/decision/settings")
    fun setDecisionSettings(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Body request: DecisionSettings
    ): Observable<DecisionSettings>

    /**
     * GET: /owner/{ownerID}/context/{context}/decision/{decisionID}
     *
     * Retrieves the owner's decision audit log by given decisionID
     */
    @GET("owner/{ownerID}/context/{context}/decision/{decisionID}")
    fun getDecisionLog(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Path("decisionID") decisionID: String
    ): Observable<DecisionLog>

    /**
     * GET: /owner/{ownerID}/context/{context}/decision/{decisionID}/policy-bundle
     *
     * Retrieves Policy Bundle for a given decision log ID
     */
    @GET("owner/{ownerID}/context/{context}/decision/{decisionID}/policy-bundle")
    fun getDecisionLogPolicyBundle(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Path("decisionID") decisionID: String
    ): Observable<PolicyBundle>

    /**
     * GET: /owner/{ownerID}/context/{context}/policy-bundle
     *
     * Retrieves Policy Bundle
     */
    @GET("owner/{ownerID}/context/{context}/policy-bundle")
    fun getPolicyBundle(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String
    ): Observable<PolicyBundle>

    /**
     * POST: /owner/{ownerID}/context/{context}/policy-bundle
     *
     * Creates policy bundle for the context
     */
    @POST("owner/{ownerID}/context/{context}/policy-bundle")
    fun createPolicyBundle(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Query("dry") dry: Boolean? = null,
        @Body request: BundlePayload
    ): Observable<BundleDiff>

    /**
     * GET: /owner/{ownerID}/context/{context}/policy-bundle/{policyName}
     *
     * Retrieves a policy document
     */
    @GET("owner/{ownerID}/context/{context}/policy-bundle/{policyName}")
    fun getPolicyDocument(
        @Path("ownerID") ownerID: String,
        @Path("context") context: String,
        @Path("policyName") policyName: String
    ): Observable<Policy>

    /**
     * GET: /context/{context_id}/restrictions
     *
     * Get context restrictions
     */
    @GET("context/{context_id}/restrictions")
    fun getContextRestrictions(
        @Path("context_id") contextId: String
    ): Observable<ContextProjectRestrictionsList>

    /**
     * POST: /context/{context_id}/restrictions
     *
     * Create context restriction
     */
    @POST("context/{context_id}/restrictions")
    fun createContextRestriction(
        @Path("context_id") contextId: String,
        @Body request: CreateContextRestrictionRequest
    ): Observable<RestrictionCreated>

    /**
     * DELETE: /context/{context_id}/restrictions/{restriction_id}
     *
     * Delete context restriction
     */
    @DELETE("context/{context_id}/restrictions/{restriction_id}")
    fun deleteContextRestriction(
        @Path("context_id") contextId: String,
        @Path("restriction_id") restrictionId: String
    ): Observable<Unit>

    /**
     * POST: /organization/{org-slug-or-id}/project
     *
     * Create a new project
     */
    @POST("organization/{org-slug-or-id}/project")
    fun createProject(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Body request: CreateProjectRequest
    ): Observable<Project>

    /**
     * DELETE: /project/{project-slug}
     *
     * Delete a project
     */
    @DELETE("project/{project-slug}")
    fun deleteProjectBySlug(
        @Path("project-slug") projectSlug: String
    ): Observable<Unit>

    

    /**
     * GET: /project/{project-slug}/envvar
     *
     * List all environment variables
     */
    @GET("project/{project-slug}/envvar")
    fun listEnvVars(
        @Path("project-slug") projectSlug: String
    ): Observable<EnvironmentVariableListResponse>

    /**
     * POST: /project/{project-slug}/envvar
     *
     * Create an environment variable
     */
    @POST("project/{project-slug}/envvar")
    fun createEnvVar(
        @Path("project-slug") projectSlug: String,
        @Body request: CreateEnvVarRequest
    ): Observable<EnvironmentVariable>

    /**
     * DELETE: /project/{project-slug}/envvar/{name}
     *
     * Delete an environment variable
     */
    @DELETE("project/{project-slug}/envvar/{name}")
    fun deleteEnvVar(
        @Path("project-slug") projectSlug: String,
        @Path("name") name: String
    ): Observable<Unit>

    /**
     * GET: /project/{project-slug}/envvar/{name}
     *
     * Get a masked environment variable
     */
    @GET("project/{project-slug}/envvar/{name}")
    fun getEnvVar(
        @Path("project-slug") projectSlug: String,
        @Path("name") name: String
    ): Observable<EnvironmentVariable>

    /**
     * PATCH: /project/{provider}/{organization}/{project}/settings
     *
     * Update project settings
     */
    @PATCH("project/{provider}/{organization}/{project}/settings")
    fun patchProjectSettings(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String,
        @Body request: ProjectSettings
    ): Observable<ProjectSettings>

    

    /**
     * GET: /project/{provider}/{organization}/{project}/settings
     *
     * Get project settings
     */
    @GET("project/{provider}/{organization}/{project}/settings")
    fun getProjectSettings(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String
    ): Observable<ProjectSettings>

    /**
     * GET: /project/{project-slug}/{job-number}/tests
     *
     * Get test metadata
     */
    @GET("project/{project-slug}/{job-number}/tests")
    fun getTests(
        @Path("project-slug") projectSlug: String,
        @Path("job-number") jobNumber: Int
    ): Observable<TestsResponse>

    /**
     * POST: /project/{project-slug}/schedule
     *
     * Create a schedule
     */
    @POST("project/{project-slug}/schedule")
    fun createSchedule(
        @Path("project-slug") projectSlug: String,
        @Body request: CreateScheduleParameters
    ): Observable<Schedule>

    /**
     * PATCH: /schedule/{schedule-id}
     *
     * Update a schedule
     */
    @PATCH("schedule/{schedule-id}")
    fun updateSchedule(
        @Path("schedule-id") scheduleId: String,
        @Body request: UpdateScheduleParameters
    ): Observable<Schedule>

    /**
     * GET: /projects/{project_id}/pipeline-definitions
     *
     * List pipeline definitions
     */
    @GET("projects/{project_id}/pipeline-definitions")
    fun listPipelineDefinitions(
        @Path("project_id") projectId: String
    ): Observable<PipelineDefinitionList>

    /**
     * POST: /projects/{project_id}/pipeline-definitions
     *
     * Create pipeline definition
     */
    @POST("projects/{project_id}/pipeline-definitions")
    fun createPipelineDefinition(
        @Path("project_id") projectId: String,
        @Body request: CreatePipelineDefinitionRequest
    ): Observable<PipelineDefinition>

    /**
     * GET: /projects/{project_id}/pipeline-definitions/{pipeline_definition_id}
     *
     * Get pipeline definition
     */
    @GET("projects/{project_id}/pipeline-definitions/{pipeline_definition_id}")
    fun getPipelineDefinition(
        @Path("project_id") projectId: String,
        @Path("pipeline_definition_id") pipelineDefinitionId: String
    ): Observable<PipelineDefinition>

    /**
     * PATCH: /projects/{project_id}/pipeline-definitions/{pipeline_definition_id}
     *
     * Update pipeline definition
     */
    @PATCH("projects/{project_id}/pipeline-definitions/{pipeline_definition_id}")
    fun updatePipelineDefinition(
        @Path("project_id") projectId: String,
        @Path("pipeline_definition_id") pipelineDefinitionId: String,
        @Body request: UpdatePipelineDefinitionRequest
    ): Observable<PipelineDefinition>

    /**
     * DELETE: /projects/{project_id}/pipeline-definitions/{pipeline_definition_id}
     *
     * Delete pipeline definition
     */
    @DELETE("projects/{project_id}/pipeline-definitions/{pipeline_definition_id}")
    fun deletePipelineDefinition(
        @Path("project_id") projectId: String,
        @Path("pipeline_definition_id") pipelineDefinitionId: String
    ): Observable<PipelineDefinitionDeleted>

    /**
     * GET: /projects/{project_id}/pipeline-definitions/{pipeline_definition_id}/triggers
     *
     * List pipeline definition triggers
     */
    @GET("projects/{project_id}/pipeline-definitions/{pipeline_definition_id}/triggers")
    fun listPipelineDefinitionTriggers(
        @Path("project_id") projectId: String,
        @Path("pipeline_definition_id") pipelineDefinitionId: String
    ): Observable<TriggerList>

    /**
     * POST: /projects/{project_id}/pipeline-definitions/{pipeline_definition_id}/triggers
     *
     * Create trigger
     */
    @POST("projects/{project_id}/pipeline-definitions/{pipeline_definition_id}/triggers")
    fun createTrigger(
        @Path("project_id") projectId: String,
        @Path("pipeline_definition_id") pipelineDefinitionId: String,
        @Body request: CreateTriggerRequest
    ): Observable<Trigger>

    /**
     * GET: /projects/{project_id}/triggers/{trigger_id}
     *
     * Get trigger
     */
    @GET("projects/{project_id}/triggers/{trigger_id}")
    fun getTrigger(
        @Path("project_id") projectId: String,
        @Path("trigger_id") triggerId: String
    ): Observable<Trigger>

    /**
     * PATCH: /projects/{project_id}/triggers/{trigger_id}
     *
     * Update trigger
     */
    @PATCH("projects/{project_id}/triggers/{trigger_id}")
    fun updateTrigger(
        @Path("project_id") projectId: String,
        @Path("trigger_id") triggerId: String,
        @Body request: UpdateTriggerRequest
    ): Observable<Trigger>

    /**
     * DELETE: /projects/{project_id}/triggers/{trigger_id}
     *
     * Delete trigger
     */
    @DELETE("projects/{project_id}/triggers/{trigger_id}")
    fun deleteTrigger(
        @Path("project_id") projectId: String,
        @Path("trigger_id") triggerId: String
    ): Observable<TriggerDeleted>

    /**
     * POST: /projects/{project_id}/rollback
     *
     * Rollback a project
     */
    @POST("projects/{project_id}/rollback")
    fun rollbackProject(
        @Path("project_id") projectId: String,
        @Body request: RollbackProjectRequest
    ): Observable<RollbackProjectResponse>

    /**
     * POST: /organizations/{org_id}/usage_export_job
     *
     * Create a usage export
     */
    @POST("organizations/{org_id}/usage_export_job")
    fun createUsageExport(
        @Path("org_id") orgId: String,
        @Body request: Map<String, Any> // Placeholder for request body
    ): Observable<UsageExportJob>

    /**
     * GET: /organizations/{org_id}/usage_export_job/{usage_export_job_id}
     *
     * Get a usage export
     */
    @GET("organizations/{org_id}/usage_export_job/{usage_export_job_id}")
    fun getUsageExport(
        @Path("org_id") orgId: String,
        @Path("usage_export_job_id") usageExportJobId: String
    ): Observable<GetUsageExportJobStatus>

    /**
     * POST: /organization
     *
     * Create a new organization
     */
    @POST("organization")
    fun createOrganization(
        @Body request: CreateOrganizationRequest
    ): Observable<Organization>

    /**
     * DELETE: /organization/{org-slug-or-id}
     *
     * Delete an organization
     */
    @DELETE("organization/{org-slug-or-id}")
    fun deleteOrganization(
        @Path("org-slug-or-id") orgSlugOrId: String
    ): Observable<Unit>

    /**
     * GET: /organization/{org-slug-or-id}/url-orb-allow-list
     *
     * List the entries in the org's URL Orb allow-list
     */
    @GET("organization/{org-slug-or-id}/url-orb-allow-list")
    fun listURLOrbAllowListEntries(
        @Path("org-slug-or-id") orgSlugOrId: String
    ): Observable<URLOrbAllowListEntryList>

    /**
     * POST: /organization/{org-slug-or-id}/url-orb-allow-list
     *
     * Create a new URL Orb allow-list entry
     */
    @POST("organization/{org-slug-or-id}/url-orb-allow-list")
    fun createURLOrbAllowListEntry(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Body request: CreateURLOrbAllowListEntryRequest
    ): Observable<MessageResponse>

    /**
     * DELETE: /organization/{org-slug-or-id}/url-orb-allow-list/{allow-list-entry-id}
     *
     * Remove an entry from the org's URL orb allow-list
     */
    @DELETE("organization/{org-slug-or-id}/url-orb-allow-list/{allow-list-entry-id}")
    fun removeURLOrbAllowListEntry(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Path("allow-list-entry-id") allowListEntryId: String
    ): Observable<MessageResponse>
}
