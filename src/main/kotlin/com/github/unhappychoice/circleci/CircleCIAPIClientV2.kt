package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import io.reactivex.Observable
import retrofit2.http.*

interface CircleCIAPIClientV2 {
    // User endpoints
    @GET("me")
    fun getMe(): Observable<User>

    @GET("user/{id}")
    fun getUser(@Path("id") id: String): Observable<User>

    @GET("me/collaborations")
    fun getCollaborations(): Observable<List<Collaboration>>

    // Context endpoints
    @GET("context/{context-id}")
    fun getContext(@Path("context-id") contextId: String): Observable<Context>

    @GET("context")
    fun listContexts(
        @Query("owner-id") ownerId: String?,
        @Query("owner-slug") ownerSlug: String?,
        @Query("owner-type") ownerType: String?,
        @Query("page-token") pageToken: String? = null
    ): Observable<ContextListResponse>

    @POST("context")
    fun createContext(@Body request: CreateContextRequest): Observable<Context>

    @DELETE("context/{context-id}")
    fun deleteContext(@Path("context-id") contextId: String): Observable<MessageResponse>

    @GET("context/{context-id}/environment-variable")
    fun listEnvironmentVariablesFromContext(
        @Path("context-id") contextId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<EnvironmentVariableListResponse>

    @PUT("context/{context-id}/environment-variable/{env-var-name}")
    fun addEnvironmentVariableToContext(
        @Path("context-id") contextId: String,
        @Path("env-var-name") envVarName: String,
        @Body request: AddEnvironmentVariableRequest
    ): Observable<Any>

    @DELETE("context/{context-id}/environment-variable/{env-var-name}")
    fun deleteEnvironmentVariableFromContext(
        @Path("context-id") contextId: String,
        @Path("env-var-name") envVarName: String
    ): Observable<MessageResponse>

    @GET("context/{context-id}/restrictions")
    fun getContextRestrictions(
        @Path("context-id") contextId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<ContextProjectRestrictionsList>

    @POST("context/{context-id}/restrictions")
    fun createContextRestriction(
        @Path("context-id") contextId: String,
        @Body request: CreateContextRestrictionRequest
    ): Observable<RestrictionCreated>

    @DELETE("context/{context-id}/restrictions/{restriction-id}")
    fun deleteContextRestriction(
        @Path("context-id") contextId: String,
        @Path("restriction-id") restrictionId: String
    ): Observable<MessageResponse>

    // Deploy endpoints
    @GET("deploy/components")
    fun listComponents(
        @Query("page-token") pageToken: String? = null
    ): Observable<ComponentListResponse>

    @GET("deploy/components/{component-id}")
    fun getComponent(@Path("component-id") componentId: String): Observable<Component>

    @GET("deploy/components/{component-id}/versions")
    fun listComponentVersions(
        @Path("component-id") componentId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<ComponentVersionListResponse>

    @GET("deploy/environments")
    fun listEnvironments(
        @Query("page-token") pageToken: String? = null
    ): Observable<DeployEnvironmentListResponse>

    @GET("deploy/environments/{environment-id}")
    fun getEnvironment(@Path("environment-id") environmentId: String): Observable<DeployEnvironment>

    // Insights endpoints
    @GET("insights/{project-slug}/branches")
    fun getAllInsightsBranches(
        @Path("project-slug") projectSlug: String,
        @Query("workflow-name") workflowName: String? = null
    ): Observable<ProjectBranches>

    @GET("insights/{project-slug}/flaky-tests")
    fun getFlakyTests(@Path("project-slug") projectSlug: String): Observable<FlakyTests>

    @GET("insights/time-series/{project-slug}/workflows/{workflow-name}/jobs")
    fun getJobTimeseries(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("branch") branch: String? = null,
        @Query("granularity") granularity: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null
    ): Observable<JobTimeseriesResponse>

    @GET("insights/{org-slug}/summary")
    fun getOrgSummaryData(
        @Path("org-slug") orgSlug: String,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("project-names") projectNames: Any? = null
    ): Observable<OrgSummaryData>

    @GET("insights/pages/{project-slug}/summary")
    fun getProjectWorkflowsPageData(
        @Path("project-slug") projectSlug: String,
        @Query("reporting-window") reportingWindow: String? = null,
        @Query("branches") branches: Any? = null,
        @Query("workflow-names") workflowNames: Any? = null
    ): Observable<ProjectWorkflowsPageData>

    @GET("insights/{project-slug}/workflows")
    fun getProjectWorkflowMetrics(
        @Path("project-slug") projectSlug: String,
        @Query("page-token") pageToken: String? = null,
        @Query("all-branches") allBranches: Boolean? = null,
        @Query("branch") branch: String? = null,
        @Query("reporting-window") reportingWindow: String? = null
    ): Observable<WorkflowMetricsResponse>

    @GET("insights/{project-slug}/workflows/{workflow-name}")
    fun getProjectWorkflowRuns(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("page-token") pageToken: String? = null,
        @Query("all-branches") allBranches: Boolean? = null,
        @Query("branch") branch: String? = null,
        @Query("start-date") startDate: String? = null,
        @Query("end-date") endDate: String? = null
    ): Observable<WorkflowRunsResponse>

    @GET("insights/{project-slug}/workflows/{workflow-name}/jobs")
    fun getProjectWorkflowJobMetrics(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("page-token") pageToken: String? = null,
        @Query("all-branches") allBranches: Boolean? = null,
        @Query("branch") branch: String? = null,
        @Query("reporting-window") reportingWindow: String? = null
    ): Observable<JobMetricsResponse>

    @GET("insights/{project-slug}/workflows/{workflow-name}/summary")
    fun getWorkflowSummary(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("all-branches") allBranches: Boolean? = null,
        @Query("branch") branch: String? = null
    ): Observable<WorkflowSummary>

    @GET("insights/{project-slug}/workflows/{workflow-name}/test-metrics")
    fun getProjectWorkflowTestMetrics(
        @Path("project-slug") projectSlug: String,
        @Path("workflow-name") workflowName: String,
        @Query("all-branches") allBranches: Boolean? = null,
        @Query("branch") branch: String? = null
    ): Observable<TestMetricsResponse>

    // Job endpoints
    @GET("project/{project-slug}/job/{job-number}")
    fun getJobDetails(
        @Path("project-slug") projectSlug: String,
        @Path("job-number") jobNumber: String
    ): Observable<JobDetails>

    @GET("project/{project-slug}/{job-number}/artifacts")
    fun getJobArtifacts(
        @Path("project-slug") projectSlug: String,
        @Path("job-number") jobNumber: String
    ): Observable<Artifact.ArtifactListResponse>

    @GET("project/{project-slug}/{job-number}/tests")
    fun getTests(
        @Path("project-slug") projectSlug: String,
        @Path("job-number") jobNumber: String
    ): Observable<TestsResponse>

    @POST("project/{project-slug}/job/{job-number}/cancel")
    fun cancelJob(
        @Path("project-slug") projectSlug: String,
        @Path("job-number") jobNumber: String
    ): Observable<MessageResponse>

    @POST("jobs/{job-id}/cancel")
    fun cancelJobByJobId(@Path("job-id") jobId: String): Observable<MessageResponse>

    // OIDC Token Management endpoints
    @GET("org/{orgID}/oidc-custom-claims")
    fun getOrgClaims(@Path("orgID") orgId: String): Observable<OidcClaims>

    @PATCH("org/{orgID}/oidc-custom-claims")
    fun patchOrgClaims(
        @Path("orgID") orgId: String,
        @Body request: PatchClaimsRequest
    ): Observable<OidcClaims>

    @DELETE("org/{orgID}/oidc-custom-claims")
    fun deleteOrgClaims(@Path("orgID") orgId: String): Observable<OidcClaims>

    @GET("org/{orgID}/project/{projectID}/oidc-custom-claims")
    fun getProjectClaims(
        @Path("orgID") orgId: String,
        @Path("projectID") projectId: String
    ): Observable<OidcClaims>

    @PATCH("org/{orgID}/project/{projectID}/oidc-custom-claims")
    fun patchProjectClaims(
        @Path("orgID") orgId: String,
        @Path("projectID") projectId: String,
        @Body request: PatchClaimsRequest
    ): Observable<OidcClaims>

    @DELETE("org/{orgID}/project/{projectID}/oidc-custom-claims")
    fun deleteProjectClaims(
        @Path("orgID") orgId: String,
        @Path("projectID") projectId: String
    ): Observable<OidcClaims>

    // Organization endpoints
    @POST("organization")
    fun createOrganization(@Body request: CreateOrganizationRequest): Observable<Organization>

    @GET("organization/{org-slug-or-id}")
    fun getOrganization(@Path("org-slug-or-id") orgSlugOrId: String): Observable<Organization>

    @DELETE("organization/{org-slug-or-id}")
    fun deleteOrganization(@Path("org-slug-or-id") orgSlugOrId: String): Observable<MessageResponse>

    @GET("organization/{org-slug-or-id}/url-orb-allow-list")
    fun listUrlOrbAllowListEntries(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<URLOrbAllowListEntryList>

    @POST("organization/{org-slug-or-id}/url-orb-allow-list")
    fun createUrlOrbAllowListEntry(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Body request: CreateURLOrbAllowListEntryRequest
    ): Observable<URLOrbAllowListEntry>

    @DELETE("organization/{org-slug-or-id}/url-orb-allow-list/{allow-list-entry-id}")
    fun removeUrlOrbAllowListEntry(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Path("allow-list-entry-id") allowListEntryId: String
    ): Observable<MessageResponse>

    // Groups endpoints
    @GET("organizations/{org-id}/groups")
    fun getOrganizationGroups(
        @Path("org-id") orgId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<GroupListResponse>

    @POST("organizations/{org-id}/groups")
    fun createOrganizationGroup(
        @Path("org-id") orgId: String,
        @Body request: CreateGroupRequest
    ): Observable<Group>

    @GET("organizations/{org-id}/groups/{group-id}")
    fun getGroup(
        @Path("org-id") orgId: String,
        @Path("group-id") groupId: String
    ): Observable<Group>

    @DELETE("organizations/{org-id}/groups/{group-id}")
    fun deleteGroup(
        @Path("org-id") orgId: String,
        @Path("group-id") groupId: String
    ): Observable<MessageResponse>

    // Usage endpoints
    @POST("organizations/{org-id}/usage_export_job")
    fun createUsageExport(
        @Path("org-id") orgId: String,
        @Body request: CreateUsageExportRequest
    ): Observable<UsageExportJob>

    @GET("organizations/{org-id}/usage_export_job/{usage-export-job-id}")
    fun getUsageExport(
        @Path("org-id") orgId: String,
        @Path("usage-export-job-id") usageExportJobId: String
    ): Observable<GetUsageExportJobStatus>

    // Policy Management endpoints
    @GET("owner/{ownerID}/context/{context}/decision")
    fun getDecisionLogs(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Query("status") status: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("branch") branch: String? = null,
        @Query("project_id") projectId: String? = null,
        @Query("build_number") buildNumber: String? = null,
        @Query("offset") offset: Int? = null
    ): Observable<DecisionLogListResponse>

    @POST("owner/{ownerID}/context/{context}/decision")
    fun makeDecision(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Body request: MakeDecisionRequest
    ): Observable<Decision>

    @GET("owner/{ownerID}/context/{context}/decision/settings")
    fun getDecisionSettings(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String
    ): Observable<DecisionSettings>

    @PATCH("owner/{ownerID}/context/{context}/decision/settings")
    fun setDecisionSettings(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Body request: PatchDecisionSettingsRequest
    ): Observable<DecisionSettings>

    @GET("owner/{ownerID}/context/{context}/decision/{decisionID}")
    fun getDecisionLog(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Path("decisionID") decisionId: String
    ): Observable<DecisionLog>

    @GET("owner/{ownerID}/context/{context}/decision/{decisionID}/policy-bundle")
    fun getDecisionLogPolicyBundle(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Path("decisionID") decisionId: String
    ): Observable<PolicyBundle>

    @GET("owner/{ownerID}/context/{context}/policy-bundle")
    fun getPolicyBundle(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String
    ): Observable<PolicyBundle>

    @POST("owner/{ownerID}/context/{context}/policy-bundle")
    fun createPolicyBundle(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Query("dry") dry: Boolean? = null,
        @Body request: BundlePayload
    ): Observable<BundleDiff>

    @GET("owner/{ownerID}/context/{context}/policy-bundle/{policyName}")
    fun getPolicyDocument(
        @Path("ownerID") ownerId: String,
        @Path("context") context: String,
        @Path("policyName") policyName: String
    ): Observable<Policy>

    // Pipeline endpoints
    @GET("pipeline")
    fun listPipelines(
        @Query("org-slug") orgSlug: String? = null,
        @Query("page-token") pageToken: String? = null,
        @Query("mine") mine: Boolean? = null
    ): Observable<PipelineListResponse>

    @GET("pipeline/{pipeline-id}")
    fun getPipelineById(@Path("pipeline-id") pipelineId: String): Observable<Pipeline>

    @GET("project/{project-slug}/pipeline/{pipeline-number}")
    fun getPipelineByNumber(
        @Path("project-slug") projectSlug: String,
        @Path("pipeline-number") pipelineNumber: Int
    ): Observable<Pipeline>

    @GET("pipeline/{pipeline-id}/config")
    fun getPipelineConfigById(@Path("pipeline-id") pipelineId: String): Observable<PipelineConfig>

    @GET("pipeline/{pipeline-id}/values")
    fun getPipelineValuesById(@Path("pipeline-id") pipelineId: String): Observable<PipelineValues>

    @GET("pipeline/{pipeline-id}/workflow")
    fun getPipelineWorkflows(
        @Path("pipeline-id") pipelineId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<WorkflowListResponse>

    @GET("project/{project-slug}/pipeline")
    fun getProjectPipelines(
        @Path("project-slug") projectSlug: String,
        @Query("branch") branch: String? = null,
        @Query("page-token") pageToken: String? = null
    ): Observable<PipelineListResponse>

    @GET("project/{project-slug}/pipeline/mine")
    fun getMyProjectPipelines(
        @Path("project-slug") projectSlug: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<PipelineListResponse>

    @POST("pipeline/continue")
    fun continuePipeline(@Body request: ContinuePipelineRequest?): Observable<MessageResponse>

    @POST("project/{project-slug}/pipeline")
    fun triggerNewPipeline(
        @Path("project-slug") projectSlug: String,
        @Body request: TriggerNewPipelineRequest?
    ): Observable<TriggerNewPipelineResponse>

    @POST("project/{provider}/{organization}/{project}/pipeline/run")
    fun triggerPipelineRun(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String,
        @Body request: TriggerPipelineRunRequest?
    ): Observable<Pipeline>

    // Pipeline Definition endpoints
    @GET("projects/{project-id}/pipeline-definitions")
    fun listPipelineDefinitions(
        @Path("project-id") projectId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<PipelineDefinitionList>

    @POST("projects/{project-id}/pipeline-definitions")
    fun createPipelineDefinition(
        @Path("project-id") projectId: String,
        @Body request: CreatePipelineDefinitionRequest
    ): Observable<PipelineDefinition>

    @GET("projects/{project-id}/pipeline-definitions/{pipeline-definition-id}")
    fun getPipelineDefinition(
        @Path("project-id") projectId: String,
        @Path("pipeline-definition-id") pipelineDefinitionId: String
    ): Observable<PipelineDefinition>

    @PATCH("projects/{project-id}/pipeline-definitions/{pipeline-definition-id}")
    fun updatePipelineDefinition(
        @Path("project-id") projectId: String,
        @Path("pipeline-definition-id") pipelineDefinitionId: String,
        @Body request: UpdatePipelineDefinitionRequest
    ): Observable<PipelineDefinition>

    @DELETE("projects/{project-id}/pipeline-definitions/{pipeline-definition-id}")
    fun deletePipelineDefinition(
        @Path("project-id") projectId: String,
        @Path("pipeline-definition-id") pipelineDefinitionId: String
    ): Observable<PipelineDefinitionDeleted>

    // Project endpoints
    @GET("project/{project-slug}")
    fun getProjectBySlug(@Path("project-slug") projectSlug: String): Observable<Project>

    @DELETE("project/{project-slug}")
    fun deleteProjectBySlug(@Path("project-slug") projectSlug: String): Observable<MessageResponse>

    @POST("organization/{org-slug-or-id}/project")
    fun createProjectInOrg(
        @Path("org-slug-or-id") orgSlugOrId: String,
        @Body request: CreateProjectRequest
    ): Observable<Project>

    @POST("project/{provider}/{organization}/{project}")
    fun createProject(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String,
        @Body request: CreateProjectRequest?
    ): Observable<Project>

    @GET("project/{project-slug}/checkout-key")
    fun getProjectCheckoutKeys(@Path("project-slug") projectSlug: String): Observable<CheckoutKeyListResponse>

    @POST("project/{project-slug}/checkout-key")
    fun createProjectCheckoutKey(
        @Path("project-slug") projectSlug: String,
        @Body request: CreateCheckoutKeyRequest?
    ): Observable<CheckoutKey>

    @GET("project/{project-slug}/checkout-key/{fingerprint}")
    fun getProjectCheckoutKey(
        @Path("project-slug") projectSlug: String,
        @Path("fingerprint") fingerprint: String
    ): Observable<CheckoutKey>

    @DELETE("project/{project-slug}/checkout-key/{fingerprint}")
    fun deleteProjectCheckoutKey(
        @Path("project-slug") projectSlug: String,
        @Path("fingerprint") fingerprint: String
    ): Observable<MessageResponse>

    @GET("project/{project-slug}/envvar")
    fun getProjectEnvironmentVariables(@Path("project-slug") projectSlug: String): Observable<ProjectEnvironmentVariableListResponse>

    @POST("project/{project-slug}/envvar")
    fun createProjectEnvironmentVariable(
        @Path("project-slug") projectSlug: String,
        @Body request: CreateEnvVarRequest?
    ): Observable<ProjectEnvironmentVariable>

    @GET("project/{project-slug}/envvar/{name}")
    fun getProjectEnvironmentVariable(
        @Path("project-slug") projectSlug: String,
        @Path("name") name: String
    ): Observable<ProjectEnvironmentVariable>

    @DELETE("project/{project-slug}/envvar/{name}")
    fun deleteProjectEnvironmentVariable(
        @Path("project-slug") projectSlug: String,
        @Path("name") name: String
    ): Observable<MessageResponse>

    @GET("project/{provider}/{organization}/{project}/settings")
    fun getProjectSettings(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String
    ): Observable<ProjectSettings>

    @PATCH("project/{provider}/{organization}/{project}/settings")
    fun patchProjectSettings(
        @Path("provider") provider: String,
        @Path("organization") organization: String,
        @Path("project") project: String,
        @Body request: PatchProjectSettingsRequest
    ): Observable<ProjectSettings>

    // Rollback endpoint
    @POST("projects/{project-id}/rollback")
    fun rollbackProject(
        @Path("project-id") projectId: String,
        @Body request: RollbackProjectRequest
    ): Observable<RollbackProjectResponse>

    // Trigger endpoints
    @GET("projects/{project-id}/pipeline-definitions/{pipeline-definition-id}/triggers")
    fun listPipelineDefinitionTriggers(
        @Path("project-id") projectId: String,
        @Path("pipeline-definition-id") pipelineDefinitionId: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<TriggerList>

    @POST("projects/{project-id}/pipeline-definitions/{pipeline-definition-id}/triggers")
    fun createTrigger(
        @Path("project-id") projectId: String,
        @Path("pipeline-definition-id") pipelineDefinitionId: String,
        @Body request: CreateTriggerRequest
    ): Observable<Trigger>

    @GET("projects/{project-id}/triggers/{trigger-id}")
    fun getTrigger(
        @Path("project-id") projectId: String,
        @Path("trigger-id") triggerId: String
    ): Observable<Trigger>

    @PATCH("projects/{project-id}/triggers/{trigger-id}")
    fun updateTrigger(
        @Path("project-id") projectId: String,
        @Path("trigger-id") triggerId: String,
        @Body request: UpdateTriggerRequest
    ): Observable<Trigger>

    @DELETE("projects/{project-id}/triggers/{trigger-id}")
    fun deleteTrigger(
        @Path("project-id") projectId: String,
        @Path("trigger-id") triggerId: String
    ): Observable<TriggerDeleted>

    // Schedule endpoints
    @GET("schedule/{schedule-id}")
    fun getScheduleById(@Path("schedule-id") scheduleId: String): Observable<Schedule>

    @GET("project/{project-slug}/schedule")
    fun getProjectSchedules(
        @Path("project-slug") projectSlug: String,
        @Query("page-token") pageToken: String? = null
    ): Observable<ScheduleListResponse>

    @POST("project/{project-slug}/schedule")
    fun createSchedule(
        @Path("project-slug") projectSlug: String,
        @Body params: CreateScheduleParameters?
    ): Observable<Schedule>

    @PATCH("schedule/{schedule-id}")
    fun updateSchedule(
        @Path("schedule-id") scheduleId: String,
        @Body params: UpdateScheduleParameters?
    ): Observable<Schedule>

    @DELETE("schedule/{schedule-id}")
    fun deleteSchedule(@Path("schedule-id") scheduleId: String): Observable<MessageResponse>

    // Webhook endpoints
    @GET("webhook/{webhook-id}")
    fun getWebhookById(@Path("webhook-id") webhookId: String): Observable<Webhook>

    @GET("webhook")
    fun getWebhooks(
        @Query("scope-id") scopeId: String,
        @Query("scope-type") scopeType: String
    ): Observable<WebhookListResponse>

    @POST("webhook")
    fun createWebhook(@Body request: CreateWebhookRequest?): Observable<Webhook>

    @PUT("webhook/{webhook-id}")
    fun updateWebhook(
        @Path("webhook-id") webhookId: String,
        @Body request: UpdateWebhookRequest?
    ): Observable<Webhook>

    @DELETE("webhook/{webhook-id}")
    fun deleteWebhook(@Path("webhook-id") webhookId: String): Observable<MessageResponse>

    // Workflow endpoints
    @GET("workflow/{id}")
    fun getWorkflowById(@Path("id") workflowId: String): Observable<Workflow>

    @GET("workflow/{id}/job")
    fun getWorkflowJobs(@Path("id") workflowId: String): Observable<JobListResponse>

    @POST("workflow/{id}/cancel")
    fun cancelWorkflow(@Path("id") workflowId: String): Observable<MessageResponse>

    @POST("workflow/{id}/rerun")
    fun rerunWorkflow(
        @Path("id") workflowId: String,
        @Body request: RerunWorkflowRequest?
    ): Observable<WorkflowId>

    @POST("workflow/{id}/approve/{approval-request-id}")
    fun approveJob(
        @Path("id") workflowId: String,
        @Path("approval-request-id") approvalRequestId: String
    ): Observable<MessageResponse>
}
