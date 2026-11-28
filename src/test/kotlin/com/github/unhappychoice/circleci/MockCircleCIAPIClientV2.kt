package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Proxy
import java.time.OffsetDateTime

class MockCircleCIAPIClientV2 : CircleCIAPIClientV2 by unsupportedEndpoints() {
    override fun getMe(): Observable<User> = Observable.just(User("id", "login", "name"))

    override fun getUser(id: String): Observable<User> = Observable.just(User("id", "login", "name"))

    override fun getCollaborations(): Observable<List<Collaboration>> = Observable.just(
        listOf(
            Collaboration(
                id = "id",
                vcsType = "vcs-type",
                name = "name",
                avatarUrl = "avatar_url",
                slug = "slug"
            )
        )
    )

    override fun getContext(contextId: String): Observable<Context> = Observable.just(
        Context(
            id = "id",
            name = "name",
            createdAt = OffsetDateTime.now()
        )
    )

    override fun listContexts(
        ownerId: String?,
        ownerSlug: String?,
        ownerType: String?,
        pageToken: String?
    ): Observable<ContextListResponse> = Observable.just(
        ContextListResponse(
            items = listOf(
                Context(
                    id = "id",
                    name = "name",
                    createdAt = OffsetDateTime.now()
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun createContext(request: CreateContextRequest): Observable<Context> = Observable.just(
        Context(
            id = "id",
            name = "name",
            createdAt = OffsetDateTime.now()
        )
    )

    override fun listEnvironmentVariablesFromContext(
        contextId: String,
        pageToken: String?
    ): Observable<EnvironmentVariableListResponse> = Observable.just(
        EnvironmentVariableListResponse(
            items = listOf(
                EnvironmentVariable(
                    variable = "variable",
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    contextId = "context_id"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    // Deploy endpoints
    override fun listComponents(
        orgId: String,
        pageSize: Int,
        pageToken: String?,
        projectId: String?
    ): Observable<ComponentListResponse> = Observable.just(
        ComponentListResponse(
            items = listOf(Component(id = "id", name = "name", projectId = "project_id", createdAt = "created_at", updatedAt = "updated_at")),
            nextPageToken = "next_page_token"
        )
    )

    override fun getComponent(componentId: String): Observable<Component> = Observable.just(
        Component(id = "id", name = "name", projectId = "project_id", createdAt = "created_at", updatedAt = "updated_at")
    )

    override fun listComponentVersions(
        componentId: String,
        pageToken: String?,
        environmentId: String?
    ): Observable<ComponentVersionListResponse> = Observable.just(
        ComponentVersionListResponse(
            items = listOf(
                ComponentVersion(
                    name = "1.0.0",
                    environmentId = "environment_id",
                    namespace = "default",
                    isLive = true,
                    pipelineId = "pipeline_id",
                    workflowId = "workflow_id",
                    jobId = "job_id",
                    jobNumber = 123,
                    lastDeployedAt = "2026-01-13T18:51:40Z"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun listEnvironments(orgId: String, pageSize: Int, pageToken: String?): Observable<DeployEnvironmentListResponse> = Observable.just(
        DeployEnvironmentListResponse(
            items = listOf(DeployEnvironment(id = "id", name = "name", organizationId = "org_id", createdAt = "created_at", updatedAt = "updated_at")),
            nextPageToken = "next_page_token"
        )
    )

    override fun getEnvironment(environmentId: String): Observable<DeployEnvironment> = Observable.just(
        DeployEnvironment(id = "id", name = "name", organizationId = "org_id", createdAt = "created_at", updatedAt = "updated_at")
    )

    // Insights endpoints
    override fun getAllInsightsBranches(projectSlug: String, workflowName: String?): Observable<ProjectBranches> = Observable.just(
        ProjectBranches(org_id = "org_id", project_id = "project_id", branches = listOf("branch"))
    )

    override fun getFlakyTests(projectSlug: String): Observable<FlakyTests> = Observable.just(
        FlakyTests(
            `flaky-tests` = listOf(
                FlakyTests.FlakyTest(
                    `time-wasted` = 1, `workflow-created-at` = "workflow-created-at", `workflow-id` = "workflow-id",
                    classname = "classname", `pipeline-number` = 1, `workflow-name` = "workflow-name", `test-name` = "test-name",
                    `job-name` = "job-name", `job-number` = 1, `pipeline-id` = "pipeline-id", `rerun-workflow-id` = "rerun-workflow-id",
                    `test-result` = "test-result", `workflow-rerun-number` = 1,
                    `times-flaked` = 2, source = "junit", file = "ExampleTest.kt"
                )
            ),
            `total-flaky-tests` = 1.0
        )
    )

    override fun getJobTimeseries(
        projectSlug: String, workflowName: String, branch: String?, granularity: String?, startDate: String?, endDate: String?
    ): Observable<JobTimeseriesResponse> =
        getJobTimeseries(projectSlug, workflowName, branch, granularity, startDate, endDate, pageToken = null)

    override fun getJobTimeseries(
        projectSlug: String, workflowName: String, branch: String?, granularity: String?, startDate: String?, endDate: String?,
        pageToken: String?
    ): Observable<JobTimeseriesResponse> = Observable.just(
        JobTimeseriesResponse(
            items = listOf(
                JobTimeseriesResponse.JobTimeseriesItem(
                    name = "name", min_started_at = "min_started_at", max_ended_at = "max_ended_at", timestamp = "timestamp",
                    metrics = JobTimeseriesResponse.JobMetrics(
                        total_runs = 1, failed_runs = 1, successful_runs = 1, throughput = 1f, median_credits_used = 1, total_credits_used = 1,
                        duration_metrics = JobTimeseriesResponse.DurationMetrics(min = 1, median = 1, max = 1, p95 = 1, total = 1)
                    )
                )
            ),
            next_page_token = "next_page_token"
        )
    )

    override fun getOrgSummaryData(orgSlug: String, reportingWindow: String?, projectNames: List<String>?): Observable<OrgSummaryData> = Observable.just(
        OrgSummaryData(
            org_data = OrgSummaryData.OrgData(
                metrics = SummaryMetrics(total_runs = 1, total_duration_secs = 1, total_credits_used = 1, success_rate = 1f, throughput = 1f),
                trends = SummaryTrends(total_runs = 1f, total_duration_secs = 1f, total_credits_used = 1f, success_rate = 1f, throughput = 1f)
            ),
            org_project_data = listOf(
                OrgSummaryData.OrgProjectData(
                    project_name = "project_name",
                    metrics = OrgSummaryData.ProjectMetrics(total_runs = 1, total_duration_secs = 1, total_credits_used = 1, success_rate = 1f),
                    trends = OrgSummaryData.ProjectTrends(total_runs = 1f, total_duration_secs = 1f, total_credits_used = 1f, success_rate = 1f)
                )
            ),
            all_projects = listOf("project")
        )
    )

    override fun getProjectWorkflowsPageData(
        projectSlug: String, reportingWindow: String?, branches: List<String>?, workflowNames: List<String>?
    ): Observable<ProjectWorkflowsPageData> = Observable.just(
        ProjectWorkflowsPageData(
            org_id = "org_id", project_id = "project_id",
            project_data = ProjectWorkflowsPageData.ProjectData(
                metrics = SummaryMetrics(total_runs = 1, total_duration_secs = 1, total_credits_used = 1, success_rate = 1f, throughput = 1f),
                trends = SummaryTrends(total_runs = 1f, total_duration_secs = 1f, total_credits_used = 1f, success_rate = 1f, throughput = 1f)
            ),
            project_workflow_data = listOf(
                ProjectWorkflowsPageData.ProjectWorkflowData(
                    workflow_name = "workflow_name",
                    metrics = ProjectWorkflowsPageData.WorkflowMetrics(total_runs = 1, total_credits_used = 1, success_rate = 1f, p95_duration_secs = 1f),
                    trends = ProjectWorkflowsPageData.WorkflowTrends(total_runs = 1f, total_credits_used = 1f, success_rate = 1f, p95_duration_secs = 1f)
                )
            ),
            project_workflow_branch_data = listOf(
                ProjectWorkflowsPageData.ProjectWorkflowBranchData(
                    workflow_name = "workflow_name", branch = "branch",
                    metrics = ProjectWorkflowsPageData.WorkflowMetrics(total_runs = 1, total_credits_used = 1, success_rate = 1f, p95_duration_secs = 1f),
                    trends = ProjectWorkflowsPageData.WorkflowTrends(total_runs = 1f, total_credits_used = 1f, success_rate = 1f, p95_duration_secs = 1f)
                )
            ),
            all_branches = listOf("branch"),
            all_workflows = listOf("workflow")
        )
    )

    override fun getProjectWorkflowMetrics(
        projectSlug: String, pageToken: String?, allBranches: Boolean?, branch: String?, reportingWindow: String?
    ): Observable<WorkflowMetricsResponse> = Observable.just(
        WorkflowMetricsResponse(
            items = listOf(
                WorkflowMetricsResponse.WorkflowMetric(
                    name = "workflow",
                    metrics = WorkflowMetricsResponse.WorkflowMetrics(
                        totalRuns = 100, successfulRuns = 90, mttr = 60, totalCreditsUsed = 1000, failedRuns = 10,
                        successRate = 0.9f, throughput = 5f, totalRecoveries = 5,
                        durationMetrics = DurationMetrics(min = 10, mean = 100, median = 90, p95 = 200, max = 300, standardDeviation = 50f)
                    )
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun getProjectWorkflowRuns(
        projectSlug: String, workflowName: String, pageToken: String?, allBranches: Boolean?, branch: String?, startDate: String?, endDate: String?
    ): Observable<WorkflowRunsResponse> = Observable.just(
        WorkflowRunsResponse(
            items = listOf(WorkflowRunsResponse.WorkflowRunItem(id = "id", branch = "main", duration = 100, createdAt = "2024-01-01T00:00:00Z", stoppedAt = "2024-01-01T00:01:00Z", creditsUsed = 10, status = "success")),
            nextPageToken = "next_page_token"
        )
    )

    override fun getProjectWorkflowJobMetrics(
        projectSlug: String, workflowName: String, pageToken: String?, allBranches: Boolean?, branch: String?, reportingWindow: String?
    ): Observable<JobMetricsResponse> = Observable.just(
        JobMetricsResponse(
            items = listOf(
                JobMetricsResponse.JobMetric(
                    name = "build",
                    metrics = JobMetricsResponse.JobMetrics(
                        totalRuns = 100, failedRuns = 5, successfulRuns = 95, totalCreditsUsed = 500, successRate = 0.95f, throughput = 10f,
                        durationMetrics = DurationMetrics(min = 5, mean = 50, median = 45, p95 = 100, max = 150, standardDeviation = 25f)
                    )
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun getWorkflowSummary(projectSlug: String, workflowName: String, allBranches: Boolean?, branch: String?): Observable<WorkflowSummary> = Observable.just(
        WorkflowSummary(
            metrics = WorkflowSummary.Metrics(
                totalRuns = 100, failedRuns = 10, successfulRuns = 90, successRate = 0.9f, throughput = 5f, mttr = 60, totalCreditsUsed = 1000,
                durationMetrics = DurationMetrics(min = 10, mean = 100, median = 90, p95 = 200, max = 300, standardDeviation = 50f)
            ),
            trends = WorkflowSummary.Trends(totalRuns = 0.1f, failedRuns = -0.05f, successfulRuns = 0.1f, successRate = 0.02f, throughput = 0.1f, mttr = -0.1f, totalCreditsUsed = 0.05f)
        )
    )

    override fun getProjectWorkflowTestMetrics(projectSlug: String, workflowName: String, allBranches: Boolean?, branch: String?): Observable<TestMetricsResponse> = Observable.just(
        TestMetricsResponse(
            averageTestCount = 100, mostFailedTestsExtra = 0, slowestTestsExtra = 0, totalTestRuns = 1000,
            mostFailedTests = listOf(TestMetricsResponse.FailedTest(testName = "test1", className = "TestClass", file = "test.kt", failedRuns = 5, totalRuns = 100)),
            slowestTests = listOf(TestMetricsResponse.SlowTest(testName = "slowTest", className = "SlowClass", file = "slow.kt", p95Duration = 5000f)),
            testRuns = listOf(TestMetricsResponse.TestRun(pipelineNumber = 1, workflowId = "wf1", successRate = 0.95f, testCounts = TestMetricsResponse.TestCounts(error = 1, failure = 4, skipped = 5, success = 90, total = 100)))
        )
    )

    // Job endpoints
    override fun getJobDetails(projectSlug: String, jobNumber: String): Observable<JobDetails> =
        Observable.error(UnsupportedOperationException("Job details are covered by the HTTP spec"))

    override fun getJobArtifacts(projectSlug: String, jobNumber: String): Observable<Artifact.ArtifactListResponse> =
        getJobArtifacts(projectSlug, jobNumber, null)

    override fun getJobArtifacts(projectSlug: String, jobNumber: String, pageToken: String?): Observable<Artifact.ArtifactListResponse> = Observable.just(
        Artifact.ArtifactListResponse(items = listOf(Artifact(path = "path", nodeIndex = 0, url = "url")), nextPageToken = "next_page_token")
    )

    override fun getTests(projectSlug: String, jobNumber: String): Observable<TestsResponse> =
        getTests(projectSlug, jobNumber, null)

    override fun getTests(projectSlug: String, jobNumber: String, pageToken: String?): Observable<TestsResponse> = Observable.just(
        TestsResponse(
            items = listOf(TestsResponse.Test(message = "message", source = "source", runTime = 1.0, file = "file", result = "result", name = "name", classname = "classname")),
            nextPageToken = "next_page_token"
        )
    )

    override fun cancelJob(projectSlug: String, jobNumber: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun cancelJobByJobId(jobId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    // OIDC Token Management endpoints
    override fun getOrgClaims(orgId: String): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = listOf("aud1"), audienceUpdatedAt = "2024-01-01T00:00:00Z", orgId = orgId, projectId = null, ttl = "1h")
    )

    override fun patchOrgClaims(orgId: String, request: PatchClaimsRequest): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = listOf("aud1"), audienceUpdatedAt = "2024-01-01T00:00:00Z", orgId = orgId, projectId = null, ttl = "1h")
    )

    override fun deleteOrgClaims(orgId: String, claims: String): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = null, audienceUpdatedAt = null, orgId = orgId, projectId = null, ttl = null)
    )

    override fun getProjectClaims(orgId: String, projectId: String): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = listOf("aud1"), audienceUpdatedAt = "2024-01-01T00:00:00Z", orgId = orgId, projectId = projectId, ttl = "1h")
    )

    override fun patchProjectClaims(orgId: String, projectId: String, request: PatchClaimsRequest): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = listOf("aud1"), audienceUpdatedAt = "2024-01-01T00:00:00Z", orgId = orgId, projectId = projectId, ttl = "1h")
    )

    override fun deleteProjectClaims(orgId: String, projectId: String, claims: String): Observable<OidcClaims> = Observable.just(
        OidcClaims(audience = null, audienceUpdatedAt = null, orgId = orgId, projectId = projectId, ttl = null)
    )

    // Organization endpoints
    override fun createOrganization(request: CreateOrganizationRequest): Observable<Organization> = Observable.just(
        Organization(id = "id", name = request.name, slug = "slug", vcsType = request.vcsType)
    )

    override fun getOrganization(orgSlugOrId: String): Observable<Organization> = Observable.just(
        Organization(id = "id", name = "name", slug = "slug", vcsType = "github")
    )

    override fun deleteOrganization(orgSlugOrId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun listUrlOrbAllowListEntries(orgSlugOrId: String, pageToken: String?): Observable<URLOrbAllowListEntryList> = Observable.just(
        URLOrbAllowListEntryList(items = listOf(URLOrbAllowListEntry(id = "id", name = "name", prefix = "https://", auth = "none")), nextPageToken = "next_page_token")
    )

    override fun createUrlOrbAllowListEntry(orgSlugOrId: String, request: CreateURLOrbAllowListEntryRequest): Observable<URLOrbAllowListEntryAcknowledgment> = Observable.just(
        URLOrbAllowListEntryAcknowledgment(id = "entry-id", message = "Created.")
    )

    override fun removeUrlOrbAllowListEntry(orgSlugOrId: String, allowListEntryId: String): Observable<URLOrbAllowListEntryAcknowledgment> = Observable.just(URLOrbAllowListEntryAcknowledgment(id = allowListEntryId, message = "Removed."))

    // Groups endpoints
    override fun getOrganizationGroups(orgId: String, pageToken: String?): Observable<GroupListResponse> = Observable.just(
        GroupListResponse(items = listOf(Group(id = "id", name = "name", organizationId = orgId)), nextPageToken = "next_page_token")
    )

    override fun createOrganizationGroup(orgId: String, request: CreateGroupRequest): Observable<Group> = Observable.just(
        Group(id = "id", name = request.name, organizationId = orgId)
    )

    override fun getGroup(orgId: String, groupId: String): Observable<Group> = Observable.just(
        Group(id = groupId, name = "name", organizationId = orgId)
    )

    override fun deleteGroup(orgId: String, groupId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    // Usage endpoints
    override fun createUsageExport(orgId: String, request: CreateUsageExportRequest): Observable<UsageExportJob> = Observable.just(
        UsageExportJob(
            id = "id",
            state = "created",
            start = request.startDate,
            end = request.endDate,
            downloadUrls = emptyList()
        )
    )

    override fun getUsageExport(orgId: String, usageExportJobId: String): Observable<GetUsageExportJobStatus> = Observable.just(
        GetUsageExportJobStatus(
            id = usageExportJobId,
            state = "completed",
            downloadUrls = listOf("https://example.com/download")
        )
    )

    // Policy Management endpoints
    override fun getDecisionLogs(
        ownerId: String, context: String, status: String?, after: String?, before: String?, branch: String?, projectId: String?, buildNumber: String?, offset: Int?
    ): Observable<List<DecisionLog>> =
        getDecisionLog(ownerId, context, "id").map { listOf(it) }

    override fun makeDecision(ownerId: String, context: String, request: MakeDecisionRequest): Observable<Decision> = Observable.just(
        Decision(status = "PASS")
    )

    override fun getDecisionSettings(ownerId: String, context: String): Observable<DecisionSettings> = Observable.just(
        DecisionSettings(enabled = true)
    )

    override fun setDecisionSettings(ownerId: String, context: String, request: PatchDecisionSettingsRequest): Observable<DecisionSettings> = Observable.just(
        DecisionSettings(enabled = request.enabled ?: true)
    )

    override fun getDecisionLog(ownerId: String, context: String, decisionId: String): Observable<DecisionLog> = Observable.just(
        DecisionLog(id = decisionId, createdAt = "2024-01-01T00:00:00Z", decision = Decision(status = "PASS"), metadata = mapOf("key" to "value"), policies = mapOf("policy" to "rego"), timeTakenMs = 100)
    )

    override fun getDecisionLogPolicyBundle(ownerId: String, context: String, decisionId: String): Observable<PolicyBundle> = Observable.just(
        mapOf("policy-name" to listOf(Policy(name = "policy-name", content = "policy content")))
    )

    override fun getPolicyBundle(ownerId: String, context: String): Observable<PolicyBundle> = Observable.just(
        mapOf("policy-name" to listOf(Policy(name = "policy-name", content = "policy content")))
    )

    override fun createPolicyBundle(ownerId: String, context: String, dry: Boolean?, request: BundlePayload): Observable<BundleDiff> = Observable.just(
        BundleDiff(created = request.policies.keys.toList())
    )

    override fun getPolicyDocument(ownerId: String, context: String, policyName: String): Observable<Policy> = Observable.just(
        Policy(name = policyName, content = "policy content")
    )

    // Pipeline endpoints
    override fun listPipelines(orgSlug: String?, pageToken: String?, mine: Boolean?): Observable<PipelineListResponse> =
        Observable.just(createMockPipelineListResponse())

    override fun getPipelineById(pipelineId: String): Observable<Pipeline> = Observable.just(createMockPipeline())

    override fun getPipelineByNumber(projectSlug: String, pipelineNumber: Int): Observable<Pipeline> =
        getPipelineByNumber(projectSlug, pipelineNumber.toLong())

    override fun getPipelineByNumber(projectSlug: String, pipelineNumber: Long): Observable<Pipeline> = Observable.just(createMockPipeline())

    override fun getPipelineConfigById(pipelineId: String): Observable<PipelineConfig> = Observable.just(
        PipelineConfig(source = "source", compiled = "compiled", setupConfig = "setup_config", compiledSetupConfig = "compiled_setup_config")
    )

    override fun getPipelineValuesById(pipelineId: String): Observable<PipelineValues> = Observable.just(
        PipelineValues(values = mapOf("key" to "value"))
    )

    override fun getPipelineWorkflows(pipelineId: String, pageToken: String?): Observable<WorkflowListResponse> = Observable.just(
        WorkflowListResponse(
            items = listOf(createMockWorkflow()),
            nextPageToken = "next_page_token"
        )
    )

    override fun getProjectPipelines(projectSlug: String, branch: String?, pageToken: String?): Observable<PipelineListResponse> =
        Observable.just(createMockPipelineListResponse())

    override fun getMyProjectPipelines(projectSlug: String, pageToken: String?): Observable<PipelineListResponse> =
        Observable.just(createMockPipelineListResponse())

    override fun continuePipeline(request: ContinuePipelineRequest): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun triggerNewPipeline(projectSlug: String, request: TriggerNewPipelineRequest): Observable<TriggerNewPipelineResponse> = Observable.just(
        TriggerNewPipelineResponse(number = 1, state = "state", id = "id", createdAt = "created_at")
    )

    override fun triggerPipelineRun(provider: String, organization: String, project: String, request: TriggerPipelineRunRequest): Observable<PipelineRunResult> =
        Observable.just(PipelineRunResult.Created("pipeline-id"))

    // Pipeline Definition endpoints
    override fun listPipelineDefinitions(projectId: String, pageToken: String?): Observable<PipelineDefinitionList> = Observable.just(
        PipelineDefinitionList(
            items = listOf(PipelineDefinition(id = "id", projectId = projectId, name = "name", createdAt = "created_at", updatedAt = "updated_at", definition = "definition")),
            nextPageToken = "next_page_token"
        )
    )

    override fun createPipelineDefinition(projectId: String, request: CreatePipelineDefinitionRequest): Observable<PipelineDefinition> = Observable.just(
        PipelineDefinition(id = "id", projectId = projectId, name = request.name, createdAt = "created_at", updatedAt = "updated_at", definition = "definition")
    )

    override fun getPipelineDefinition(projectId: String, pipelineDefinitionId: String): Observable<PipelineDefinition> = Observable.just(
        PipelineDefinition(id = pipelineDefinitionId, projectId = projectId, name = "name", createdAt = "created_at", updatedAt = "updated_at", definition = "definition")
    )

    override fun updatePipelineDefinition(projectId: String, pipelineDefinitionId: String, request: UpdatePipelineDefinitionRequest): Observable<PipelineDefinition> = Observable.just(
        PipelineDefinition(id = pipelineDefinitionId, projectId = projectId, name = request.name ?: "name", createdAt = "created_at", updatedAt = "updated_at", definition = "definition")
    )

    override fun deletePipelineDefinition(projectId: String, pipelineDefinitionId: String): Observable<PipelineDefinitionDeleted> = Observable.just(
        PipelineDefinitionDeleted(message = "deleted")
    )

    // Project endpoints
    override fun getProjectBySlug(projectSlug: String): Observable<Project> = Observable.just(createMockProject())

    override fun deleteProjectBySlug(projectSlug: String): Observable<MessageResponse> = Observable.just(MessageResponse("deleted"))

    override fun createProjectInOrg(orgSlugOrId: String, request: CreateProjectRequest): Observable<Project> = Observable.just(createMockProject())

    override fun createProject(provider: String, organization: String, project: String, request: CreateProjectRequest): Observable<Project> = Observable.just(createMockProject())

    override fun getProjectCheckoutKeys(projectSlug: String): Observable<CheckoutKeyListResponse> = Observable.just(
        CheckoutKeyListResponse(
            items = listOf(CheckoutKey(publicKey = "public_key", type = "type", fingerprint = "fingerprint", preferred = false, createdAt = OffsetDateTime.now())),
            nextPageToken = "next_page_token"
        )
    )

    override fun getProjectCheckoutKeys(projectSlug: String, digest: String?): Observable<CheckoutKeyListResponse> =
        getProjectCheckoutKeys(projectSlug)

    override fun getProjectCheckoutKeys(projectSlug: String, digest: String?, pageToken: String?): Observable<CheckoutKeyListResponse> =
        getProjectCheckoutKeys(projectSlug, digest)

    override fun createProjectCheckoutKey(projectSlug: String, request: CreateCheckoutKeyRequest): Observable<CheckoutKey> = Observable.just(
        CheckoutKey(publicKey = "public_key", type = "type", fingerprint = "fingerprint", preferred = false, createdAt = OffsetDateTime.now())
    )

    override fun getProjectCheckoutKey(projectSlug: String, fingerprint: String): Observable<CheckoutKey> = Observable.just(
        CheckoutKey(publicKey = "public_key", type = "type", fingerprint = "fingerprint", preferred = false, createdAt = OffsetDateTime.now())
    )

    override fun deleteProjectCheckoutKey(projectSlug: String, fingerprint: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getProjectEnvironmentVariables(projectSlug: String): Observable<ProjectEnvironmentVariableListResponse> = Observable.just(
        ProjectEnvironmentVariableListResponse(items = listOf(ProjectEnvironmentVariable(name = "name", value = "value")), nextPageToken = "next_page_token")
    )

    override fun getProjectEnvironmentVariables(projectSlug: String, pageToken: String?) = getProjectEnvironmentVariables(projectSlug)

    override fun createProjectEnvironmentVariable(projectSlug: String, request: CreateEnvVarRequest): Observable<ProjectEnvironmentVariable> = Observable.just(
        ProjectEnvironmentVariable(name = "name", value = "value")
    )

    override fun getProjectEnvironmentVariable(projectSlug: String, name: String): Observable<ProjectEnvironmentVariable> = Observable.just(
        ProjectEnvironmentVariable(name = name, value = "value")
    )

    override fun deleteProjectEnvironmentVariable(projectSlug: String, name: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getProjectSettings(provider: String, organization: String, project: String): Observable<ProjectSettings> = Observable.just(createMockProjectSettings())

    override fun patchProjectSettings(provider: String, organization: String, project: String, request: PatchProjectSettingsRequest): Observable<ProjectSettings> = Observable.just(createMockProjectSettings())

    // Rollback endpoint
    override fun rollbackProject(projectId: String, request: RollbackProjectRequest): Observable<RollbackProjectResponse> = Observable.just(
        RollbackProjectResponse(id = "123e4567-e89b-12d3-a456-426614174000", rollbackType = "PIPELINE")
    )

    // Trigger endpoints
    override fun listPipelineDefinitionTriggers(projectId: String, pipelineDefinitionId: String, pageToken: String?): Observable<TriggerList> = Observable.just(
        TriggerList(items = listOf(createMockTrigger()), nextPageToken = "next_page_token")
    )

    override fun createTrigger(projectId: String, pipelineDefinitionId: String, request: CreateTriggerRequest): Observable<Trigger> = Observable.just(
        createMockTrigger().copy(
            eventName = request.eventName.orEmpty(),
            eventSource = Trigger.EventSource(provider = request.eventSource.provider)
        )
    )

    override fun getTrigger(projectId: String, triggerId: String): Observable<Trigger> = Observable.just(
        createMockTrigger().copy(id = triggerId)
    )

    override fun updateTrigger(projectId: String, triggerId: String, request: UpdateTriggerRequest): Observable<Trigger> = Observable.just(
        createMockTrigger().copy(id = triggerId, eventName = request.eventName ?: "name")
    )

    override fun deleteTrigger(projectId: String, triggerId: String): Observable<TriggerDeleted> = Observable.just(
        TriggerDeleted(message = "deleted")
    )

    // Schedule endpoints
    override fun getScheduleById(scheduleId: String): Observable<Schedule> = Observable.just(createMockSchedule())

    override fun getProjectSchedules(projectSlug: String, pageToken: String?): Observable<ScheduleListResponse> = Observable.just(
        ScheduleListResponse(items = listOf(createMockSchedule()), nextPageToken = "next_page_token")
    )

    override fun createSchedule(projectSlug: String, params: CreateScheduleParameters): Observable<Schedule> = Observable.just(createMockSchedule())

    override fun updateSchedule(scheduleId: String, params: UpdateScheduleParameters): Observable<Schedule> = Observable.just(createMockSchedule())

    override fun deleteSchedule(scheduleId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    // Webhook endpoints
    override fun getWebhookById(webhookId: String): Observable<Webhook> = Observable.just(createMockWebhook())

    override fun getWebhooks(scopeId: String, scopeType: String): Observable<WebhookListResponse> =
        getWebhooks(scopeId, scopeType, null)

    override fun getWebhooks(scopeId: String, scopeType: String, isEnabled: Boolean?): Observable<WebhookListResponse> =
        getWebhooks(scopeId, scopeType, isEnabled, pageToken = null)

    override fun getWebhooks(
        scopeId: String,
        scopeType: String,
        isEnabled: Boolean?,
        pageToken: String?
    ): Observable<WebhookListResponse> = Observable.just(
        WebhookListResponse(items = listOf(createMockWebhook()), nextPageToken = "next_page_token")
    )

    override fun createWebhook(request: CreateWebhookRequest): Observable<Webhook> = Observable.just(createMockWebhook())

    override fun updateWebhook(webhookId: String, request: UpdateWebhookRequest): Observable<Webhook> = Observable.just(createMockWebhook())

    override fun deleteWebhook(webhookId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    // Workflow endpoints
    override fun getWorkflowById(workflowId: String): Observable<Workflow> = Observable.just(createMockWorkflow())

    override fun getWorkflowJobs(workflowId: String): Observable<JobListResponse> = getWorkflowJobs(workflowId, null)

    override fun getWorkflowJobs(workflowId: String, pageToken: String?): Observable<JobListResponse> = Observable.just(
        JobListResponse(
            items = listOf(Job(id = "id", name = "name", type = "type", status = "status", jobNumber = 1, projectSlug = "project_slug", startedAt = "started_at", stoppedAt = "stopped_at", approvalRequestId = "approval_request_id", dependencies = listOf("dependency"))),
            nextPageToken = "next_page_token"
        )
    )

    override fun cancelWorkflow(workflowId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun rerunWorkflow(workflowId: String, request: RerunWorkflowRequest): Observable<WorkflowId> = Observable.just(WorkflowId("workflow_id"))

    override fun approveJob(workflowId: String, approvalRequestId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    // Helper methods
    private fun createMockPipeline() = Pipeline(
        id = "id", errors = listOf(Pipeline.PipelineError(type = "config", message = "error message")), projectSlug = "project_slug", updatedAt = "updated_at", number = 1,
        triggerParameters = emptyMap(), state = "state", createdAt = "created_at",
        trigger = Pipeline.Trigger(type = "type", receivedAt = "received_at", actor = Pipeline.Trigger.Actor(login = "login", avatarUrl = "avatar_url")),
        vcs = Pipeline.Vcs(
            providerName = "provider_name", targetRepositoryUrl = "target_repository_url", branch = "branch",
            reviewId = "review_id", reviewUrl = "review_url", revision = "revision", tag = "tag",
            commit = Pipeline.Vcs.Commit(subject = "subject", body = "body"), originRepositoryUrl = "origin_repository_url"
        )
    )

    private fun createMockPipelineListResponse() = PipelineListResponse(items = listOf(createMockPipeline()), nextPageToken = "next_page_token")

    private fun createMockWorkflow() = Workflow(
        id = "id", name = "name", projectSlug = "project_slug", status = "status", createdAt = "created_at",
        stoppedAt = "stopped_at", pipelineId = "pipeline_id", pipelineNumber = 1, startedBy = "started_by",
        canceledBy = "canceled_by", erroredBy = "errored_by", tag = "tag"
    )

    private fun createMockProject() = Project(
        slug = "slug", name = "name", id = "id", organizationName = "organization_name",
        organizationSlug = "organization_slug", organizationId = "organization_id",
        vcsInfo = Project.VcsInfo(vcsUrl = "vcs_url", provider = "provider", defaultBranch = "default_branch")
    )

    private fun createMockProjectSettings() = ProjectSettings(
        advanced = ProjectSettings.AdvancedSettings(
            autocancelBuilds = true, buildForkPrs = true, buildPrsOnly = true, disableSsh = true,
            forksReceiveSecretEnvVars = true, oss = true, setGithubStatus = true, setupWorkflows = true,
            writeSettingsRequiresAdmin = true, prOnlyBranchOverrides = listOf("override")
        )
    )

    private fun createMockSchedule() = Schedule(
        id = "id", name = "name", description = "description", createdAt = "created_at", updatedAt = "updated_at",
        projectSlug = "project_slug", parameters = emptyMap(),
        actor = Actor(id = "id", login = "login", name = "name", avatarUrl = "avatar_url"),
        timetable = ScheduleTimetable(1, listOf(1), listOf("TUE"), null, null)
    )

    private fun createMockWebhook() = Webhook(
        id = "id", name = "name", url = "url", createdAt = "created_at", updatedAt = "updated_at",
        signingSecret = "signing_secret", scope = WebhookScope(id = "id", type = "type"), events = listOf("event")
    )

    private fun createMockTrigger() = Trigger(
        id = "id",
        eventName = "name",
        eventSource = Trigger.EventSource(provider = "schedule")
    )
}

private fun unsupportedEndpoints(): CircleCIAPIClientV2 {
    val api = CircleCIAPIClientV2::class.java
    return api.cast(Proxy.newProxyInstance(api.classLoader, arrayOf(api)) { _, method, _ ->
        error("No stub for ${method.name}")
    })
}
