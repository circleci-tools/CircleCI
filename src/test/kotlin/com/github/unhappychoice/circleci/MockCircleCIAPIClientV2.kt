package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import io.reactivex.Observable
import java.time.OffsetDateTime

class MockCircleCIAPIClientV2 : CircleCIAPIClientV2 {
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

    override fun deleteContext(contextId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

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

    override fun addEnvironmentVariableToContext(
        contextId: String,
        envVarName: String,
        request: AddEnvironmentVariableRequest
    ): Observable<Any> = Observable.just(
        EnvironmentVariable(
            variable = "variable",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            contextId = "context_id"
        )
    )

    override fun deleteEnvironmentVariableFromContext(contextId: String, envVarName: String): Observable<MessageResponse> =
        Observable.just(MessageResponse("message"))

    override fun getAllInsightsBranches(projectSlug: String, workflowName: String?): Observable<ProjectBranches> = Observable.just(
        ProjectBranches(
            org_id = "org_id",
            project_id = "project_id",
            branches = listOf("branch")
        )
    )

    override fun getFlakyTests(projectSlug: String): Observable<FlakyTests> = Observable.just(
        FlakyTests(
            `flaky-tests` = listOf(
                FlakyTests.FlakyTest(
                    `time-wasted` = 1,
                    `workflow-created-at` = "workflow-created-at",
                    `workflow-id` = "workflow-id",
                    classname = "classname",
                    `pipeline-number` = 1,
                    `workflow-name` = "workflow-name",
                    `test-name` = "test-name",
                    `job-name` = "job-name",
                    `job-number` = 1,
                    `pipeline-id` = "pipeline-id",
                    `rerun-workflow-id` = "rerun-workflow-id",
                    `test-result` = "test-result",
                    `workflow-rerun-number` = 1
                )
            )
        )
    )

    override fun getJobTimeseries(
        projectSlug: String,
        workflowName: String,
        branch: String?,
        granularity: String?,
        startDate: String?,
        endDate: String?
    ): Observable<JobTimeseriesResponse> = Observable.just(
        JobTimeseriesResponse(
            items = listOf(
                JobTimeseriesResponse.JobTimeseriesItem(
                    name = "name",
                    min_started_at = "min_started_at",
                    max_ended_at = "max_ended_at",
                    timestamp = "timestamp",
                    metrics = JobTimeseriesResponse.JobMetrics(
                        total_runs = 1,
                        failed_runs = 1,
                        successful_runs = 1,
                        throughput = 1f,
                        median_credits_used = 1,
                        total_credits_used = 1,
                        duration_metrics = JobTimeseriesResponse.DurationMetrics(
                            min = 1,
                            median = 1,
                            max = 1,
                            p95 = 1,
                            total = 1
                        )
                    )
                )
            ),
            next_page_token = "next_page_token"
        )
    )

    override fun getOrgSummaryData(
        orgSlug: String,
        reportingWindow: String?,
        projectNames: Any?
    ): Observable<OrgSummaryData> = Observable.just(
        OrgSummaryData(
            org_data = OrgSummaryData.OrgData(
                metrics = OrgSummaryData.OrgMetrics(
                    total_runs = 1,
                    total_duration_secs = 1,
                    total_credits_used = 1,
                    success_rate = 1f,
                    throughput = 1f
                ),
                trends = OrgSummaryData.OrgTrends(
                    total_runs = 1f,
                    total_duration_secs = 1f,
                    total_credits_used = 1f,
                    success_rate = 1f,
                    throughput = 1f
                )
            ),
            org_project_data = listOf(
                OrgSummaryData.OrgProjectData(
                    project_name = "project_name",
                    metrics = OrgSummaryData.ProjectMetrics(
                        total_runs = 1,
                        total_duration_secs = 1,
                        total_credits_used = 1,
                        success_rate = 1f
                    ),
                    trends = OrgSummaryData.ProjectTrends(
                        total_runs = 1f,
                        total_duration_secs = 1f,
                        total_credits_used = 1f,
                        success_rate = 1f
                    )
                )
            ),
            all_projects = listOf("project")
        )
    )

    override fun getProjectWorkflowsPageData(
        projectSlug: String,
        reportingWindow: String?,
        branches: Any?,
        workflowNames: Any?
    ): Observable<ProjectWorkflowsPageData> = Observable.just(
        ProjectWorkflowsPageData(
            org_id = "org_id",
            project_id = "project_id",
            project_data = ProjectWorkflowsPageData.ProjectData(
                metrics = ProjectWorkflowsPageData.ProjectMetrics(
                    total_runs = 1,
                    total_duration_secs = 1,
                    total_credits_used = 1,
                    success_rate = 1f,
                    throughput = 1f
                ),
                trends = ProjectWorkflowsPageData.ProjectTrends(
                    total_runs = 1f,
                    total_duration_secs = 1f,
                    total_credits_used = 1f,
                    success_rate = 1f,
                    throughput = 1f
                )
            ),
            project_workflow_data = listOf(
                ProjectWorkflowsPageData.ProjectWorkflowData(
                    workflow_name = "workflow_name",
                    metrics = ProjectWorkflowsPageData.WorkflowMetrics(
                        total_runs = 1,
                        total_credits_used = 1,
                        success_rate = 1f,
                        p95_duration_secs = 1f
                    ),
                    trends = ProjectWorkflowsPageData.WorkflowTrends(
                        total_runs = 1f,
                        total_credits_used = 1f,
                        success_rate = 1f,
                        p95_duration_secs = 1f
                    )
                )
            ),
            project_workflow_branch_data = listOf(
                ProjectWorkflowsPageData.ProjectWorkflowBranchData(
                    workflow_name = "workflow_name",
                    branch = "branch",
                    metrics = ProjectWorkflowsPageData.WorkflowMetrics(
                        total_runs = 1,
                        total_credits_used = 1,
                        success_rate = 1f,
                        p95_duration_secs = 1f
                    ),
                    trends = ProjectWorkflowsPageData.WorkflowTrends(
                        total_runs = 1f,
                        total_credits_used = 1f,
                        success_rate = 1f,
                        p95_duration_secs = 1f
                    )
                )
            ),
            all_branches = listOf("branch"),
            all_workflows = listOf("workflow")
        )
    )

    override fun getJobDetails(projectSlug: String, jobNumber: String): Observable<JobDetails> = Observable.just(
        JobDetails(
            name = "name",
            metrics = listOf(Metric("metric name", 0.0)),
            trends = listOf(Trend("trend name", 0.0))
        )
    )

    override fun getJobArtifacts(projectSlug: String, jobNumber: String): Observable<Artifact.ArtifactListResponse> = Observable.just(
        Artifact.ArtifactListResponse(
            items = listOf(
                Artifact(
                    path = "path",
                    nodeIndex = 0,
                    url = "url"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun getTests(projectSlug: String, jobNumber: String): Observable<TestsResponse> = Observable.just(
        TestsResponse(
            items = listOf(
                TestsResponse.Test(
                    message = "message",
                    source = "source",
                    runTime = 1.0,
                    file = "file",
                    result = "result",
                    name = "name",
                    classname = "classname"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun cancelJob(projectSlug: String, jobNumber: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getOrganization(organizationId: String): Observable<Organization> = Observable.just(
        Organization(
            id = "id",
            name = "name",
            slug = "slug",
            vcsType = "github"
        )
    )

    override fun getPipelineById(pipelineId: String): Observable<Pipeline> = Observable.just(
        Pipeline(
            id = "id",
            errors = emptyList(),
            projectSlug = "project_slug",
            updatedAt = "updated_at",
            number = 1,
            triggerParameters = emptyMap(),
            state = "state",
            createdAt = "created_at",
            trigger = Pipeline.Trigger(
                type = "type",
                receivedAt = "received_at",
                actor = Pipeline.Trigger.Actor(
                    login = "login",
                    avatarUrl = "avatar_url"
                )
            ),
            vcs = Pipeline.Vcs(
                providerName = "provider_name",
                targetRepositoryUrl = "target_repository_url",
                branch = "branch",
                reviewId = "review_id",
                reviewUrl = "review_url",
                revision = "revision",
                tag = "tag",
                commit = Pipeline.Vcs.Commit(
                    subject = "subject",
                    body = "body"
                ),
                originRepositoryUrl = "origin_repository_url"
            )
        )
    )

    override fun getPipelineByNumber(projectSlug: String, pipelineNumber: Int): Observable<Pipeline> = Observable.just(
        Pipeline(
            id = "id",
            errors = emptyList(),
            projectSlug = "project_slug",
            updatedAt = "updated_at",
            number = 1,
            triggerParameters = emptyMap(),
            state = "state",
            createdAt = "created_at",
            trigger = Pipeline.Trigger(
                type = "type",
                receivedAt = "received_at",
                actor = Pipeline.Trigger.Actor(
                    login = "login",
                    avatarUrl = "avatar_url"
                )
            ),
            vcs = Pipeline.Vcs(
                providerName = "provider_name",
                targetRepositoryUrl = "target_repository_url",
                branch = "branch",
                reviewId = "review_id",
                reviewUrl = "review_url",
                revision = "revision",
                tag = "tag",
                commit = Pipeline.Vcs.Commit(
                    subject = "subject",
                    body = "body"
                ),
                originRepositoryUrl = "origin_repository_url"
            )
        )
    )

    override fun getPipelineConfigById(pipelineId: String): Observable<PipelineConfig> = Observable.just(
        PipelineConfig(
            source = "source",
            compiled = "compiled",
            setupConfig = "setup_config",
            compiledSetupConfig = "compiled_setup_config"
        )
    )

    override fun getPipelineWorkflows(pipelineId: String, pageToken: String?): Observable<WorkflowListResponse> = Observable.just(
        WorkflowListResponse(
            items = listOf(
                Workflow(
                    id = "id",
                    name = "name",
                    projectSlug = "project_slug",
                    status = "status",
                    createdAt = "created_at",
                    stoppedAt = "stopped_at",
                    pipelineId = "pipeline_id",
                    pipelineNumber = 1,
                    startedBy = "started_by",
                    canceledBy = "canceled_by",
                    erroredBy = "errored_by",
                    tag = "tag"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun getProjectPipelines(projectSlug: String, branch: String?, pageToken: String?): Observable<PipelineListResponse> = Observable.just(
        PipelineListResponse(
            items = listOf(
                Pipeline(
                    id = "id",
                    errors = emptyList(),
                    projectSlug = "project_slug",
                    updatedAt = "updated_at",
                    number = 1,
                    triggerParameters = emptyMap(),
                    state = "state",
                    createdAt = "created_at",
                    trigger = Pipeline.Trigger(
                        type = "type",
                        receivedAt = "received_at",
                        actor = Pipeline.Trigger.Actor(
                            login = "login",
                            avatarUrl = "avatar_url"
                        )
                    ),
                    vcs = Pipeline.Vcs(
                        providerName = "provider_name",
                        targetRepositoryUrl = "target_repository_url",
                        branch = "branch",
                        reviewId = "review_id",
                        reviewUrl = "review_url",
                        revision = "revision",
                        tag = "tag",
                        commit = Pipeline.Vcs.Commit(
                            subject = "subject",
                            body = "body"
                        ),
                        originRepositoryUrl = "origin_repository_url"
                    )
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun continuePipeline(request: ContinuePipelineRequest?): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun triggerNewPipeline(projectSlug: String, request: TriggerNewPipelineRequest?): Observable<TriggerNewPipelineResponse> = Observable.just(
        TriggerNewPipelineResponse(
            number = 1,
            state = "state",
            id = "id",
            createdAt = "created_at"
        )
    )

    override fun getProjectBySlug(projectSlug: String): Observable<Project> = Observable.just(
        Project(
            slug = "slug",
            name = "name",
            id = "id",
            organizationName = "organization_name",
            organizationSlug = "organization_slug",
            organizationId = "organization_id",
            vcsInfo = Project.VcsInfo(
                vcsUrl = "vcs_url",
                provider = "provider",
                defaultBranch = "default_branch"
            )
        )
    )

    override fun getProjectCheckoutKeys(projectSlug: String): Observable<CheckoutKeyListResponse> = Observable.just(
        CheckoutKeyListResponse(
            items = listOf(
                CheckoutKey(
                    publicKey = "public_key",
                    type = "type",
                    fingerprint = "fingerprint",
                    preferred = false,
                    createdAt = OffsetDateTime.now()
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun createProjectCheckoutKey(projectSlug: String, request: CreateCheckoutKeyRequest?): Observable<CheckoutKey> = Observable.just(
        CheckoutKey(
            publicKey = "public_key",
            type = "type",
            fingerprint = "fingerprint",
            preferred = false,
            createdAt = OffsetDateTime.now()
        )
    )

    override fun getProjectCheckoutKey(projectSlug: String, fingerprint: String): Observable<CheckoutKey> = Observable.just(
        CheckoutKey(
            publicKey = "public_key",
            type = "type",
            fingerprint = "fingerprint",
            preferred = false,
            createdAt = OffsetDateTime.now()
        )
    )

    override fun deleteProjectCheckoutKey(projectSlug: String, fingerprint: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getProjectEnvironmentVariables(projectSlug: String): Observable<ProjectEnvironmentVariableListResponse> = Observable.just(
        ProjectEnvironmentVariableListResponse(
            items = listOf(
                ProjectEnvironmentVariable(
                    name = "name",
                    value = "value"
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun createProjectEnvironmentVariable(projectSlug: String, request: CreateEnvVarRequest?): Observable<ProjectEnvironmentVariable> = Observable.just(
        ProjectEnvironmentVariable(
            name = "name",
            value = "value"
        )
    )

    override fun deleteProjectEnvironmentVariable(projectSlug: String, name: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getProjectSettings(projectSlug: String): Observable<ProjectSettings> = Observable.just(
        ProjectSettings(
            advanced = ProjectSettings.AdvancedSettings(
                autocancelBuilds = true,
                buildForkPrs = true,
                buildPrsOnly = true,
                disableSsh = true,
                forksReceiveSecretEnvVars = true,
                oss = true,
                setGithubStatus = true,
                setupWorkflows = true,
                writeSettingsRequiresAdmin = true,
                prOnlyBranchOverrides = listOf("override")
            )
        )
    )

    override fun getScheduleById(scheduleId: String): Observable<Schedule> = Observable.just(
        Schedule(
            id = "id",
            name = "name",
            description = "description",
            createdAt = "created_at",
            updatedAt = "updated_at",
            projectSlug = "project_slug",
            parameters = emptyMap(),
            actor = com.github.unhappychoice.circleci.v2.response.Actor(
                id = "id",
                login = "login",
                name = "name"
            ),
            timetables = listOf(
                com.github.unhappychoice.circleci.v2.response.Timetable(
                    id = "id",
                    attribution = "attribution",
                    days = listOf("day"),
                    hours = listOf(1),
                    minutes = listOf(1),
                    perHour = 1
                )
            )
        )
    )

    override fun getProjectSchedules(projectSlug: String, pageToken: String?): Observable<ScheduleListResponse> = Observable.just(
        ScheduleListResponse(
            items = listOf(
                Schedule(
                    id = "id",
                    name = "name",
                    description = "description",
                    createdAt = "created_at",
                    updatedAt = "updated_at",
                    projectSlug = "project_slug",
                    parameters = emptyMap(),
                    actor = com.github.unhappychoice.circleci.v2.response.Actor(
                        id = "id",
                        login = "login",
                        name = "name"
                    ),
                    timetables = listOf(
                        com.github.unhappychoice.circleci.v2.response.Timetable(
                            id = "id",
                            attribution = "attribution",
                            days = listOf("day"),
                            hours = listOf(1),
                            minutes = listOf(1),
                            perHour = 1
                        )
                    )
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun createSchedule(projectSlug: String, params: CreateScheduleParameters?): Observable<Schedule> = Observable.just(
        Schedule(
            id = "id",
            name = "name",
            description = "description",
            createdAt = "created_at",
            updatedAt = "updated_at",
            projectSlug = "project_slug",
            parameters = emptyMap(),
            actor = com.github.unhappychoice.circleci.v2.response.Actor(
                id = "id",
                login = "login",
                name = "name"
            ),
            timetables = listOf(
                com.github.unhappychoice.circleci.v2.response.Timetable(
                    id = "id",
                    attribution = "attribution",
                    days = listOf("day"),
                    hours = listOf(1),
                    minutes = listOf(1),
                    perHour = 1
                )
            )
        )
    )

    override fun updateSchedule(scheduleId: String, params: UpdateScheduleParameters?): Observable<Schedule> = Observable.just(
        Schedule(
            id = "id",
            name = "name",
            description = "description",
            createdAt = "created_at",
            updatedAt = "updated_at",
            projectSlug = "project_slug",
            parameters = emptyMap(),
            actor = com.github.unhappychoice.circleci.v2.response.Actor(
                id = "id",
                login = "login",
                name = "name"
            ),
            timetables = listOf(
                com.github.unhappychoice.circleci.v2.response.Timetable(
                    id = "id",
                    attribution = "attribution",
                    days = listOf("day"),
                    hours = listOf(1),
                    minutes = listOf(1),
                    perHour = 1
                )
            )
        )
    )

    override fun deleteSchedule(scheduleId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getWebhookById(webhookId: String): Observable<Webhook> = Observable.just(
        Webhook(
            id = "id",
            name = "name",
            url = "url",
            createdAt = "created_at",
            updatedAt = "updated_at",
            signingSecret = "signing_secret",
            scope = com.github.unhappychoice.circleci.v2.response.WebhookScope(
                id = "id",
                type = "type"
            ),
            events = listOf("event")
        )
    )

    override fun getWebhooks(scopeId: String, scopeType: String): Observable<WebhookListResponse> = Observable.just(
        WebhookListResponse(
            items = listOf(
                Webhook(
                    id = "id",
                    name = "name",
                    url = "url",
                    createdAt = "created_at",
                    updatedAt = "updated_at",
                    signingSecret = "signing_secret",
                    scope = com.github.unhappychoice.circleci.v2.response.WebhookScope(
                        id = "id",
                        type = "type"
                    ),
                    events = listOf("event")
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun createWebhook(request: CreateWebhookRequest?): Observable<Webhook> = Observable.just(
        Webhook(
            id = "id",
            name = "name",
            url = "url",
            createdAt = "created_at",
            updatedAt = "updated_at",
            signingSecret = "signing_secret",
            scope = com.github.unhappychoice.circleci.v2.response.WebhookScope(
                id = "id",
                type = "type"
            ),
            events = listOf("event")
        )
    )

    override fun updateWebhook(webhookId: String, request: UpdateWebhookRequest?): Observable<Webhook> = Observable.just(
        Webhook(
            id = "id",
            name = "name",
            url = "url",
            createdAt = "created_at",
            updatedAt = "updated_at",
            signingSecret = "signing_secret",
            scope = com.github.unhappychoice.circleci.v2.response.WebhookScope(
                id = "id",
                type = "type"
            ),
            events = listOf("event")
        )
    )

    override fun deleteWebhook(webhookId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun getWorkflowById(workflowId: String): Observable<Workflow> = Observable.just(
        Workflow(
            id = "id",
            name = "name",
            projectSlug = "project_slug",
            status = "status",
            createdAt = "created_at",
            stoppedAt = "stopped_at",
            pipelineId = "pipeline_id",
            pipelineNumber = 1,
            startedBy = "started_by",
            canceledBy = "canceled_by",
            erroredBy = "errored_by",
            tag = "tag"
        )
    )

    override fun getWorkflowJobs(workflowId: String): Observable<JobListResponse> = Observable.just(
        JobListResponse(
            items = listOf(
                Job(
                    id = "id",
                    name = "name",
                    type = "type",
                    status = "status",
                    startedAt = "started_at",
                    stoppedAt = "stopped_at",
                    approvalRequestId = "approval_request_id",
                    dependencies = listOf("dependency")
                )
            ),
            nextPageToken = "next_page_token"
        )
    )

    override fun cancelWorkflow(workflowId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))

    override fun rerunWorkflow(workflowId: String, request: RerunWorkflowRequest?): Observable<WorkflowId> = Observable.just(WorkflowId("workflow_id"))

    override fun approveJob(workflowId: String, approvalRequestId: String): Observable<MessageResponse> = Observable.just(MessageResponse("message"))
}











