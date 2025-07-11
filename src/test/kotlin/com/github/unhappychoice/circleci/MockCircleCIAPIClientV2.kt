package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import io.reactivex.Observable

class MockCircleCIAPIClientV2 : CircleCIAPIClientV2 {
    override fun getMe(): Observable<User> {
        return Observable.just(User(id = "mock-user-id", name = "Mock User", login = "mockuser", avatarUrl = "http://mock.url/avatar.png", createdAt = "2023-01-01T00:00:00Z"))
    }

    override fun getCollaborations(): Observable<List<Collaboration>> {
        return Observable.just(listOf(Collaboration(id = "mock-collaboration-id", vcsType = "github", name = "Mock Collaboration", avatarUrl = "http://mock.url/collaboration-avatar.png", slug = "gh/mock-collaboration")))
    }

    override fun getProjects(): Observable<List<Project>> {
        return Observable.just(listOf(Project(id = "mock-project-id", name = "Mock Project", slug = "gh/mockorg/mockproject", organizationName = "Mock Organization", organizationSlug = "mockorg", organizationId = "mock-org-id", vcsInfo = Project.VcsInfo(vcsUrl = "https://github.com/mockorg/mockproject", provider = "GitHub", defaultBranch = "main"))))
    }

    override fun getProject(projectSlug: String): Observable<Project> {
        return Observable.just(Project(id = "mock-project-id", name = "Mock Project", slug = projectSlug, organizationName = "Mock Organization", organizationSlug = "mockorg", organizationId = "mock-org-id", vcsInfo = Project.VcsInfo(vcsUrl = "https://github.com/mockorg/mockproject", provider = "GitHub", defaultBranch = "main")))
    }

    override fun getProjectByVcsType(vcsType: String, organizationName: String, projectName: String): Observable<Project> {
        return Observable.just(Project(id = "mock-project-id", name = projectName, slug = "$vcsType/$organizationName/$projectName", organizationName = organizationName, organizationSlug = organizationName, organizationId = "mock-org-id", vcsInfo = Project.VcsInfo(vcsUrl = "https://github.com/$organizationName/$projectName", provider = "GitHub", defaultBranch = "main")))
    }

    override fun getProjectSettings(projectSlug: String): Observable<ProjectSettings> {
        return Observable.just(ProjectSettings(advanced = ProjectSettings.AdvancedSettings(autocancelBuilds = true, buildForkPrs = true, buildPrsOnly = true, disableSsh = false, forksReceiveSecretEnvVars = true, oss = false, setGithubStatus = true, setupWorkflows = true, writeSettingsRequiresAdmin = true, prOnlyBranchOverrides = listOf())))
    }

    override fun getWorkflow(id: String): Observable<Workflow> {
        return Observable.just(Workflow(id = id, name = "mock-workflow", projectSlug = "gh/mockorg/mockproject", status = "success", createdAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", pipelineId = "mock-pipeline-id", pipelineNumber = 1, startedBy = "mock-user-id"))
    }

    override fun getWorkflowJobs(id: String): Observable<List<Job>> {
        return Observable.just(listOf(Job(id = "mock-job-id", name = "mock-job", type = "build", status = "success", startedAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", approvalRequestId = null, dependencies = listOf())))
    }

    override fun getPipelines(): Observable<List<Pipeline>> {
        return Observable.just(listOf(Pipeline(id = "mock-pipeline-id", errors = listOf(), projectSlug = "gh/mockorg/mockproject", updatedAt = "2023-01-01T00:01:00Z", number = 1, trigger_parameters = null, state = "created", createdAt = "2023-01-01T00:00:00Z", trigger = Pipeline.Trigger(type = "api", receivedAt = "2023-01-01T00:00:00Z", actor = Pipeline.Trigger.Actor(login = "mockuser", avatarUrl = "http://mock.url/avatar.png")), vcs = Pipeline.Vcs(providerName = "GitHub", originRepositoryUrl = "https://github.com/mockorg/mockproject", targetRepositoryUrl = "https://github.com/mockorg/mockproject", revision = "mock-revision", branch = "main", reviewId = null, reviewUrl = null, tag = null, commit = null))))
    }

    override fun getPipelineById(pipelineId: String): Observable<Pipeline> {
        return Observable.just(Pipeline(id = pipelineId, errors = listOf(), projectSlug = "gh/mockorg/mockproject", updatedAt = "2023-01-01T00:01:00Z", number = 1, trigger_parameters = null, state = "created", createdAt = "2023-01-01T00:00:00Z", trigger = Pipeline.Trigger(type = "api", receivedAt = "2023-01-01T00:00:00Z", actor = Pipeline.Trigger.Actor(login = "mockuser", avatarUrl = "http://mock.url/avatar.png")), vcs = Pipeline.Vcs(providerName = "GitHub", originRepositoryUrl = "https://github.com/mockorg/mockproject", targetRepositoryUrl = "https://github.com/mockorg/mockproject", revision = "mock-revision", branch = "main", reviewId = null, reviewUrl = null, tag = null, commit = null)))
    }

    override fun getProjectPipelines(projectSlug: String): Observable<List<Pipeline>> {
        return Observable.just(listOf(Pipeline(id = "mock-pipeline-id", errors = listOf(), projectSlug = projectSlug, updatedAt = "2023-01-01T00:01:00Z", number = 1, trigger_parameters = null, state = "created", createdAt = "2023-01-01T00:00:00Z", trigger = Pipeline.Trigger(type = "api", receivedAt = "2023-01-01T00:00:00Z", actor = Pipeline.Trigger.Actor(login = "mockuser", avatarUrl = "http://mock.url/avatar.png")), vcs = Pipeline.Vcs(providerName = "GitHub", originRepositoryUrl = "https://github.com/mockorg/mockproject", targetRepositoryUrl = "https://github.com/mockorg/mockproject", revision = "mock-revision", branch = "main", reviewId = null, reviewUrl = null, tag = null, commit = null))))
    }

    override fun getPipelineWorkflows(pipelineId: String): Observable<List<Workflow>> {
        return Observable.just(listOf(Workflow(id = "mock-workflow-id", name = "mock-workflow", projectSlug = "gh/mockorg/mockproject", status = "success", createdAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", pipelineId = "mock-pipeline-id", pipelineNumber = 1, startedBy = "mock-user-id")))
    }

    override fun triggerNewPipeline(projectSlug: String, request: TriggerNewPipelineRequest): Observable<Pipeline> {
        return Observable.just(Pipeline(id = "new-mock-pipeline-id", errors = listOf(), projectSlug = projectSlug, updatedAt = "2023-01-01T00:01:00Z", number = 1, trigger_parameters = request.parameters, state = "created", createdAt = "2023-01-01T00:00:00Z", trigger = Pipeline.Trigger(type = "api", receivedAt = "2023-01-01T00:00:00Z", actor = Pipeline.Trigger.Actor(login = "mockuser", avatarUrl = "http://mock.url/avatar.png")), vcs = Pipeline.Vcs(providerName = "GitHub", originRepositoryUrl = "https://github.com/mockorg/mockproject", targetRepositoryUrl = "https://github.com/mockorg/mockproject", revision = "mock-revision", branch = "main", reviewId = null, reviewUrl = null, tag = null, commit = null)))
    }

    override fun getPipelineByNumber(projectSlug: String, pipelineNumber: Int): Observable<Pipeline> {
        return Observable.just(Pipeline(id = pipelineNumber.toString(), errors = listOf(), projectSlug = projectSlug, updatedAt = "2023-01-01T00:01:00Z", number = pipelineNumber, trigger_parameters = null, state = "created", createdAt = "2023-01-01T00:00:00Z", trigger = Pipeline.Trigger(type = "api", receivedAt = "2023-01-01T00:00:00Z", actor = Pipeline.Trigger.Actor(login = "mockuser", avatarUrl = "http://mock.url/avatar.png")), vcs = Pipeline.Vcs(providerName = "GitHub", originRepositoryUrl = "https://github.com/mockorg/mockproject", targetRepositoryUrl = "https://github.com/mockorg/mockproject", revision = "mock-revision", branch = "main", reviewId = null, reviewUrl = null, tag = null, commit = null)))
    }

    override fun getJobDetails(jobId: String): Observable<Job> {
        return Observable.just(Job(id = jobId, name = "mock-job", type = "build", status = "success", startedAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", approvalRequestId = null, dependencies = listOf()))
    }

    override fun cancelJob(jobId: String): Observable<Job> {
        return Observable.just(Job(id = jobId, name = "mock-job", type = "build", status = "canceled", startedAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", approvalRequestId = null, dependencies = listOf()))
    }

    override fun getJobArtifacts(jobId: String): Observable<List<Artifact>> {
        return Observable.just(listOf(Artifact(path = "mock-artifact.txt", url = "http://mock.url/artifact.txt", nodeIndex = 0)))
    }

    override fun getProjectWorkflowMetrics(
        projectSlug: String,
        branch: String?,
        workflowName: String?,
        startDate: String?,
        endDate: String?,
        reportingWindow: String?,
        pageToken: String?
    ): Observable<ProjectMetricsTimeSeries> {
        return Observable.just(ProjectMetricsTimeSeries(metrics = listOf(Metric("success_rate", 0.9)), trends = listOf(Trend("success_rate", 0.1))))
    }

    override fun getWorkflowMetrics(
        projectSlug: String,
        workflowName: String,
        branch: String?,
        startDate: String?,
        endDate: String?,
        reportingWindow: String?,
        pageToken: String?
    ): Observable<ProjectMetricsTimeSeries> {
        return Observable.just(ProjectMetricsTimeSeries(metrics = listOf(Metric("success_rate", 0.9)), trends = listOf(Trend("success_rate", 0.1))))
    }

    override fun getWorkflowRuns(
        projectSlug: String,
        workflowName: String,
        branch: String?,
        startDate: String?,
        endDate: String?,
        pageToken: String?
    ): Observable<List<WorkflowRun>> {
        return Observable.just(listOf(WorkflowRun(id = "mock-workflow-run-id", status = "success", createdAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", duration = 60, creditsUsed = 10, isRerun = false)))
    }

    override fun getProjectJobMetrics(
        projectSlug: String,
        branch: String?,
        jobName: String?,
        startDate: String?,
        endDate: String?,
        reportingWindow: String?,
        pageToken: String?
    ): Observable<ProjectMetricsTimeSeries> {
        return Observable.just(ProjectMetricsTimeSeries(metrics = listOf(Metric("success_rate", 0.9)), trends = listOf(Trend("success_rate", 0.1))))
    }

    override fun getJobMetrics(
        projectSlug: String,
        jobName: String,
        branch: String?,
        startDate: String?,
        endDate: String?,
        reportingWindow: String?,
        pageToken: String?
    ): Observable<ProjectMetricsTimeSeries> {
        return Observable.just(ProjectMetricsTimeSeries(metrics = listOf(Metric("success_rate", 0.9)), trends = listOf(Trend("success_rate", 0.1))))
    }

    override fun getJobRuns(
        projectSlug: String,
        jobName: String,
        branch: String?,
        startDate: String?,
        endDate: String?,
        pageToken: String?
    ): Observable<List<JobRun>> {
        return Observable.just(listOf(JobRun(id = "mock-job-run-id", status = "success", startedAt = "2023-01-01T00:00:00Z", stoppedAt = "2023-01-01T00:01:00Z", duration = 60, creditsUsed = 10, isRerun = false)))
    }

    override fun getContexts(): Observable<List<Context>> {
        return Observable.just(listOf(Context(id = "mock-context-id", name = "mock-context", createdAt = "2023-01-01T00:00:00Z", organizationId = "mock-org-id")))
    }

    override fun getContext(contextId: String): Observable<Context> {
        return Observable.just(Context(id = contextId, name = "mock-context", createdAt = "2023-01-01T00:00:00Z", organizationId = "mock-org-id"))
    }

    override fun getContextEnvironmentVariables(contextId: String): Observable<List<EnvironmentVariable>> {
        return Observable.just(listOf(EnvironmentVariable(name = "MOCK_ENV_VAR", value = "mock_value", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z")))
    }

    override fun addContextEnvironmentVariable(contextId: String, request: AddEnvironmentVariableRequest): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = request.name, value = request.value, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"))
    }

    override fun updateContextEnvironmentVariable(contextId: String, envVarName: String, request: AddEnvironmentVariableRequest): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = envVarName, value = request.value, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"))
    }

    override fun deleteContextEnvironmentVariable(contextId: String, envVarName: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun createContext(request: CreateContextRequest): Observable<Context> {
        return Observable.just(Context(id = "mock-context-id", name = request.name, createdAt = "2023-01-01T00:00:00Z", organizationId = "mock-org-id"))
    }

    override fun deleteContext(contextId: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun getProjectWorkflowsPageData(
        projectSlug: String,
        reportingWindow: String?,
        branches: List<String>?,
        workflowNames: List<String>?
    ): Observable<ProjectWorkflowsPageData> {
        return Observable.just(ProjectWorkflowsPageData(
            org_id = "mock-org-id",
            project_id = "mock-project-id",
            project_data = ProjectWorkflowsPageData.ProjectData(
                metrics = ProjectWorkflowsPageData.Metrics(total_runs = 100, total_duration_secs = 6000, total_credits_used = 1000, success_rate = 0.9f, throughput = 10f),
                trends = ProjectWorkflowsPageData.Trends(total_runs = 10f, total_duration_secs = 600f, total_credits_used = 100f, success_rate = 0.05f, throughput = 1f)
            ),
            project_workflow_data = listOf(),
            project_workflow_branch_data = listOf(),
            all_branches = listOf("main", "develop"),
            all_workflows = listOf("build", "test")
        ))
    }

    override fun getJobTimeseries(
        projectSlug: String,
        workflowName: String,
        branch: String?,
        granularity: String?,
        startDate: String?,
        endDate: String?
    ): Observable<JobTimeseriesResponse> {
        return Observable.just(JobTimeseriesResponse(items = listOf(JobTimeseriesResponse.JobTimeseriesItem(
            name = "mock-job",
            min_started_at = "2023-01-01T00:00:00Z",
            max_ended_at = "2023-01-01T00:01:00Z",
            timestamp = "2023-01-01T00:00:00Z",
            metrics = JobTimeseriesResponse.JobMetrics(
                total_runs = 10,
                failed_runs = 1,
                successful_runs = 9,
                throughput = 1.0f,
                median_credits_used = 100,
                total_credits_used = 1000,
                duration_metrics = JobTimeseriesResponse.DurationMetrics(
                    min = 30,
                    median = 60,
                    max = 90,
                    p95 = 80,
                    total = 600
                )
            )
        )), next_page_token = null))
    }

    override fun getOrgSummaryData(
        orgSlug: String,
        reportingWindow: String?,
        projectNames: List<String>?
    ): Observable<OrgSummaryData> {
        return Observable.just(OrgSummaryData(
            org_data = OrgSummaryData.OrgData(
                metrics = OrgSummaryData.Metrics(total_runs = 1000, total_duration_secs = 60000, total_credits_used = 10000, success_rate = 0.95f, throughput = 100f),
                trends = OrgSummaryData.Trends(total_runs = 50f, total_duration_secs = 3000f, total_credits_used = 500f, success_rate = 0.02f, throughput = 5f)
            ),
            org_project_data = listOf(),
            all_projects = listOf("project1", "project2")
        ))
    }

    override fun getAllInsightsBranches(
        projectSlug: String,
        workflowName: String?
    ): Observable<ProjectBranches> {
        return Observable.just(ProjectBranches(
            org_id = "mock-org-id",
            project_id = "mock-project-id",
            branches = listOf("main", "develop", "feature/mock")
        ))
    }

    override fun getFlakyTests(
        projectSlug: String,
        workflowName: String?
    ): Observable<FlakyTests> {
        return Observable.just(FlakyTests(listOf(
            FlakyTests.FlakyTest(
                `time-wasted` = 120,
                `workflow-created-at` = "2023-01-01T00:00:00Z",
                `workflow-id` = "mock-workflow-id",
                classname = "com.example.MyTest",
                `pipeline-number` = 123,
                `workflow-name` = "build-and-test",
                `test-name` = "shouldDoSomething",
                `job-name` = "test-job",
                `job-number` = 456,
                `pipeline-id` = "mock-pipeline-id",
                `rerun-workflow-id` = null,
                `test-result` = "flaky",
                `workflow-rerun-number` = null
            )
        )))
    }

    override fun getSchedules(): Observable<List<Schedule>> {
        return Observable.just(listOf(Schedule(id = "mock-schedule-id", description = "Mock Schedule", name = "mock-schedule", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", projectSlug = "gh/mockorg/mockproject", parameters = mapOf(), actor = Actor(id = "actor-id", login = "actor-login", name = "Actor Name"), timetables = listOf())))
    }

    override fun getScheduleById(scheduleId: String): Observable<Schedule> {
        return Observable.just(Schedule(id = scheduleId, description = "Mock Schedule", name = "mock-schedule", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", projectSlug = "gh/mockorg/mockproject", parameters = mapOf(), actor = Actor(id = "actor-id", login = "actor-login", name = "Actor Name"), timetables = listOf()))
    }

    override fun getProjectSchedules(projectSlug: String): Observable<List<Schedule>> {
        return Observable.just(listOf(Schedule(id = "mock-schedule-id", description = "Mock Schedule", name = "mock-schedule", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", projectSlug = projectSlug, parameters = mapOf(), actor = Actor(id = "actor-id", login = "actor-login", name = "Actor Name"), timetables = listOf())))
    }

    override fun getWebhooks(): Observable<List<Webhook>> {
        return Observable.just(listOf(Webhook(id = "mock-webhook-id", name = "mock-webhook", url = "http://mock.url/webhook", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", signingSecret = "mock-secret", scope = WebhookScope(type = "project", id = "mock-scope-id"), events = listOf("workflow-completed"))))
    }

    override fun getWebhookById(webhookId: String): Observable<Webhook> {
        return Observable.just(Webhook(id = webhookId, name = "mock-webhook", url = "http://mock.url/webhook", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", signingSecret = "mock-secret", scope = WebhookScope(type = "project", id = "mock-scope-id"), events = listOf("workflow-completed"))))
    }

    override fun createWebhook(request: CreateWebhookRequest): Observable<Webhook> {
        return Observable.just(Webhook(id = "new-mock-webhook-id", name = request.name, url = request.url, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", signingSecret = request.signingSecret, scope = WebhookScope(type = request.scope.type, id = request.scope.id), events = request.events))
    }

    override fun updateWebhook(webhookId: String, request: CreateWebhookRequest): Observable<Webhook> {
        return Observable.just(Webhook(id = webhookId, name = request.name, url = request.url, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", signingSecret = request.signingSecret, scope = WebhookScope(type = request.scope.type, id = request.scope.id), events = request.events))
    }

    override fun deleteWebhook(webhookId: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun createOrganization(request: CreateOrganizationRequest): Observable<Organization> {
        return Observable.just(Organization(id = "new-mock-org-id", name = request.name, slug = "${request.vcsType}/${request.name}", vcsType = request.vcsType))
    }

    override fun createProject(orgSlugOrId: String, request: CreateProjectRequest): Observable<Project> {
        return Observable.just(Project(id = "new-mock-project-id", name = request.name, slug = "$orgSlugOrId/${request.name}", organizationSlug = orgSlugOrId, organizationName = "Mock Organization", vcsInfo = Project.VcsInfo(vcsUrl = "https://github.com/mockorg/${request.name}", provider = "GitHub", defaultBranch = "main"), organizationId = "mock-org-id"))
    }

    override fun deleteOrganization(orgSlugOrId: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun listURLOrbAllowListEntries(orgSlugOrId: String): Observable<URLOrbAllowListEntryList> {
        return Observable.just(URLOrbAllowListEntryList(items = listOf(), nextPageToken = null))
    }

    override fun createURLOrbAllowListEntry(orgSlugOrId: String, request: CreateURLOrbAllowListEntryRequest): Observable<MessageResponse> {
        return Observable.just(MessageResponse(id = "new-mock-allow-list-entry-id", message = "Created."))
    }

    override fun removeURLOrbAllowListEntry(orgSlugOrId: String, allowListEntryId: String): Observable<MessageResponse> {
        return Observable.just(MessageResponse(id = allowListEntryId, message = "Deleted."))
    }

    override fun deleteOrgClaims(orgID: String, claims: String): Observable<ClaimResponse> {
        return Observable.just(ClaimResponse(message = "Claims deleted."))
    }

    override fun getOrgClaims(orgID: String): Observable<ClaimResponse> {
        return Observable.just(ClaimResponse(message = "Claims retrieved."))
    }

    override fun patchOrgClaims(orgID: String, request: PatchClaimsRequest): Observable<ClaimResponse> {
        return Observable.just(ClaimResponse(message = "Claims patched."))
    }

    override fun getDecisionLogs(
        ownerID: String,
        context: String,
        status: String?,
        after: String?,
        before: String?,
        branch: String?,
        projectId: String?,
        buildNumber: String?,
        offset: Int?
    ): Observable<List<DecisionLog>> {
        return Observable.just(listOf())
    }

    override fun makeDecision(ownerID: String, context: String, request: Map<String, Any>): Observable<Decision> {
        return Observable.just(Decision(decision = "approved"))
    }

    override fun getDecisionSettings(ownerID: String, context: String): Observable<DecisionSettings> {
        return Observable.just(DecisionSettings(enabled = true))
    }

    override fun setDecisionSettings(ownerID: String, context: String, request: DecisionSettings): Observable<DecisionSettings> {
        return Observable.just(DecisionSettings(enabled = request.enabled))
    }

    override fun getDecisionLog(ownerID: String, context: String, decisionID: String): Observable<DecisionLog> {
        return Observable.just(DecisionLog(id = decisionID, decision = "approved"))
    }

    override fun getDecisionLogPolicyBundle(ownerID: String, context: String, decisionID: String): Observable<PolicyBundle> {
        return Observable.just(PolicyBundle(id = "mock-policy-bundle-id"))
    }

    override fun getPolicyBundle(ownerID: String, context: String): Observable<PolicyBundle> {
        return Observable.just(PolicyBundle(id = "mock-policy-bundle-id"))
    }

    override fun createPolicyBundle(ownerID: String, context: String, dry: Boolean?, request: BundlePayload): Observable<BundleDiff> {
        return Observable.just(BundleDiff(message = "Bundle created."))
    }

    override fun getPolicyDocument(ownerID: String, context: String, policyName: String): Observable<Policy> {
        return Observable.just(Policy(name = policyName, content = "mock-policy-content"))
    }

    override fun getContextRestrictions(contextId: String): Observable<ContextProjectRestrictionsList> {
        return Observable.just(ContextProjectRestrictionsList(items = listOf()))
    }

    override fun createContextRestriction(contextId: String, request: CreateContextRestrictionRequest): Observable<RestrictionCreated> {
        return Observable.just(RestrictionCreated(id = "mock-restriction-id", message = "Restriction created."))
    }

    override fun deleteContextRestriction(contextId: String, restrictionId: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun deleteProjectBySlug(projectSlug: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun listEnvVars(projectSlug: String): Observable<EnvironmentVariableListResponse> {
        return Observable.just(EnvironmentVariableListResponse(items = listOf(), nextPageToken = null))
    }

    override fun createEnvVar(projectSlug: String, request: CreateEnvVarRequest): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = request.name, value = request.value, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"))
    }

    override fun deleteEnvVar(projectSlug: String, name: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun getEnvVar(projectSlug: String, name: String): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = name, value = "mock-value", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z"))
    }

    override fun patchProjectSettings(
        provider: String,
        organization: String,
        project: String,
        request: ProjectSettings
    ): Observable<ProjectSettings> {
        return Observable.just(request)
    }

    override fun getProjectSettings(
        provider: String,
        organization: String,
        project: String
    ): Observable<ProjectSettings> {
        return Observable.just(ProjectSettings(oss = false, buildPrs = true, buildForks = true, testInsights = true, isPublic = false, visualReviewApp = false, ossEnabled = false, setGithubStatus = true, setBitbucketStatus = true, featureFlags = ProjectSettings.FeatureFlags(buildPrs = true, buildForks = true, testInsights = true, oss = false, setGithubStatus = true, setBitbucketStatus = true, visualReviewApp = false, advanced = null)))
    }

    override fun getTests(projectSlug: String, jobNumber: Int): Observable<TestsResponse> {
        return Observable.just(TestsResponse(items = listOf()))
    }

    override fun createSchedule(projectSlug: String, request: CreateScheduleParameters): Observable<Schedule> {
        return Observable.just(Schedule(id = "mock-schedule-id", description = "Mock Schedule", name = request.name, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", projectSlug = projectSlug, parameters = request.parameters, actor = Actor(id = "actor-id", login = "actor-login", name = "Actor Name"), timetables = request.timetables))
    }

    override fun updateSchedule(scheduleId: String, request: UpdateScheduleParameters): Observable<Schedule> {
        return Observable.just(Schedule(id = scheduleId, description = "Mock Schedule", name = request.name, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", projectSlug = "gh/mockorg/mockproject", parameters = request.parameters, actor = Actor(id = "actor-id", login = "actor-login", name = "Actor Name"), timetables = request.timetables))
    }

    override fun listPipelineDefinitions(projectId: String): Observable<PipelineDefinitionList> {
        return Observable.just(PipelineDefinitionList(items = listOf(), nextPageToken = null)))
    }

    override fun createPipelineDefinition(projectId: String, request: CreatePipelineDefinitionRequest): Observable<PipelineDefinition> {
        return Observable.just(PipelineDefinition(id = "mock-pipeline-definition-id", projectId = projectId, name = request.name, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", definition = request.definition))
    }

    override fun getPipelineDefinition(projectId: String, pipelineDefinitionId: String): Observable<PipelineDefinition> {
        return Observable.just(PipelineDefinition(id = pipelineDefinitionId, projectId = projectId, name = "mock-pipeline-definition", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", definition = "mock-definition"))
    }

    override fun updatePipelineDefinition(
        projectId: String,
        pipelineDefinitionId: String,
        request: UpdatePipelineDefinitionRequest
    ): Observable<PipelineDefinition> {
        return Observable.just(PipelineDefinition(id = pipelineDefinitionId, projectId = projectId, name = request.name, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", definition = request.definition))
    }

    override fun deletePipelineDefinition(projectId: String, pipelineDefinitionId: String): Observable<PipelineDefinitionDeleted> {
        return Observable.just(PipelineDefinitionDeleted(message = "Deleted."))
    }

    override fun listPipelineDefinitionTriggers(projectId: String, pipelineDefinitionId: String): Observable<TriggerList> {
        return Observable.just(TriggerList(items = listOf(), nextPageToken = null)))
    }

    override fun createTrigger(projectId: String, pipelineDefinitionId: String, request: CreateTriggerRequest): Observable<Trigger> {
        return Observable.just(Trigger(id = "mock-trigger-id", pipelineDefinitionId = pipelineDefinitionId, projectId = projectId, name = request.name, description = request.description, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", type = request.type, parameters = request.parameters))
    }

    override fun getTrigger(projectId: String, triggerId: String): Observable<Trigger> {
        return Observable.just(Trigger(id = triggerId, pipelineDefinitionId = "mock-pipeline-definition-id", projectId = projectId, name = "mock-trigger", description = "mock-description", createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", type = "manual", parameters = mapOf())))
    }

    override fun updateTrigger(projectId: String, triggerId: String, request: UpdateTriggerRequest): Observable<Trigger> {
        return Observable.just(Trigger(id = triggerId, pipelineDefinitionId = "mock-pipeline-definition-id", projectId = projectId, name = request.name, description = request.description, createdAt = "2023-01-01T00:00:00Z", updatedAt = "2023-01-01T00:00:00Z", type = request.type, parameters = request.parameters))
    }

    override fun deleteTrigger(projectId: String, triggerId: String): Observable<TriggerDeleted> {
        return Observable.just(TriggerDeleted(message = "Deleted."))
    }

    override fun rollbackProject(projectId: String, request: RollbackProjectRequest): Observable<RollbackProjectResponse> {
        return Observable.just(RollbackProjectResponse(message = "Rollback initiated."))
    }

    override fun createUsageExport(orgId: String, request: Map<String, Any>): Observable<UsageExportJob> {
        return Observable.just(UsageExportJob(id = "mock-usage-export-job-id", status = "created", createdAt = "2023-01-01T00:00:00Z", downloadUrl = null))
    }

    override fun getUsageExport(orgId: String, usageExportJobId: String): Observable<GetUsageExportJobStatus> {
        return Observable.just(GetUsageExportJobStatus(id = usageExportJobId, status = "completed", createdAt = "2023-01-01T00:00:00Z", downloadUrl = "http://mock.url/download"))
    }
}
