package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import com.winterbe.expekt.expect
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.spekframework.spek2.Spek

object CircleCIAPIClientV2Spec : Spek({
    lateinit var client: CircleCIAPIClientV2
    val projectSlug = "gh/unhappychoice/circleci"

    group("CircleCIAPIClientV2") {
        beforeEachTest {
            client = MockCircleCIAPIClientV2()
            RxJavaPlugins.onComputationScheduler(TestScheduler())
            RxJavaPlugins.onIoScheduler(TestScheduler())
            RxJavaPlugins.onNewThreadScheduler(TestScheduler())
        }

        // User endpoints (3)
        group("User") {
            test("#getMe() should return response") {
                val subscriber = client.getMe().test()
                expect(subscriber.isFinished()).to.be.`true`

                val user = subscriber.values().first()
                expectNotNull(user)
            }

            test("#getUser() should return response") {
                val subscriber = client.getUser("user-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val user = subscriber.values().first()
                expectNotNull(user)
            }

            test("#getCollaborations() should return response") {
                val subscriber = client.getCollaborations().test()
                expect(subscriber.isFinished()).to.be.`true`

                val collaborations = subscriber.values().first()
                expect(collaborations).not.to.be.empty
                expectNotNull(collaborations.first())
            }
        }

        // Context endpoints (10)
        group("Context") {
            test("#getContext() should return response") {
                val subscriber = client.getContext("context-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val context = subscriber.values().first()
                expectNotNull(context)
            }

            test("#listContexts() should return response") {
                val subscriber = client.listContexts("owner-id", null, "account", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createContext() should return response") {
                val request = CreateContextRequest("name", CreateContextRequest.Owner("id", "type"))
                val subscriber = client.createContext(request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val context = subscriber.values().first()
                expectNotNull(context)
            }

            test("#deleteContext() should return response") {
                val subscriber = client.deleteContext("context-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#listEnvironmentVariablesFromContext() should return response") {
                val subscriber = client.listEnvironmentVariablesFromContext("context-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#addEnvironmentVariableToContext() should return response") {
                val subscriber = client.addEnvironmentVariableToContext("context-id", "VAR", AddEnvironmentVariableRequest("value")).test()
                expect(subscriber.isFinished()).to.be.`true`
            }

            test("#deleteEnvironmentVariableFromContext() should return response") {
                val subscriber = client.deleteEnvironmentVariableFromContext("context-id", "VAR").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getContextRestrictions() should return response") {
                val subscriber = client.getContextRestrictions("context-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.`null`
                expect(response.nextPageToken).not.to.be.`null`
            }

            test("#createContextRestriction() should return response") {
                val subscriber = client.createContextRestriction("context-id", CreateContextRestrictionRequest("project-id")).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.id).not.to.be.`null`
                expect(response.message).not.to.be.`null`
            }

            test("#deleteContextRestriction() should return response") {
                val subscriber = client.deleteContextRestriction("context-id", "restriction-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Deploy endpoints (5)
        group("Deploy") {
            test("#listComponents() should return response") {
                val subscriber = client.listComponents(null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getComponent() should return response") {
                val subscriber = client.getComponent("component-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val component = subscriber.values().first()
                expectNotNull(component)
            }

            test("#listComponentVersions() should return response") {
                val subscriber = client.listComponentVersions("component-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#listEnvironments() should return response") {
                val subscriber = client.listEnvironments(null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getEnvironment() should return response") {
                val subscriber = client.getEnvironment("environment-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val env = subscriber.values().first()
                expectNotNull(env)
            }
        }

        // Insights endpoints (10)
        group("Insights") {
            test("#getAllInsightsBranches() should return response") {
                val subscriber = client.getAllInsightsBranches(projectSlug, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.org_id).not.to.be.`null`
                expect(response.project_id).not.to.be.`null`
                expect(response.branches).not.to.be.empty
            }

            test("#getFlakyTests() should return response") {
                val subscriber = client.getFlakyTests(projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.`flaky-tests`).not.to.be.empty
                expectNotNull(response.`flaky-tests`.first())
            }

            test("#getJobTimeseries() should return response") {
                val subscriber = client.getJobTimeseries(projectSlug, "workflow", null, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.next_page_token).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getOrgSummaryData() should return response") {
                val subscriber = client.getOrgSummaryData("org-slug", null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.org_data).not.to.be.`null`
                expectNotNull(response.org_data)
                expect(response.org_project_data).not.to.be.empty
                expectNotNull(response.org_project_data.first())
                expect(response.all_projects).not.to.be.empty
            }

            test("#getProjectWorkflowsPageData() should return response") {
                val subscriber = client.getProjectWorkflowsPageData(projectSlug, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.org_id).not.to.be.`null`
                expect(response.project_id).not.to.be.`null`
                expect(response.project_data).not.to.be.`null`
                expectNotNull(response.project_data)
                expect(response.project_workflow_data).not.to.be.empty
                expectNotNull(response.project_workflow_data.first())
                expect(response.project_workflow_branch_data).not.to.be.empty
                expectNotNull(response.project_workflow_branch_data.first())
                expect(response.all_branches).not.to.be.empty
                expect(response.all_workflows).not.to.be.empty
            }

            test("#getProjectWorkflowJobMetrics() should return response") {
                val subscriber = client.getProjectWorkflowJobMetrics(projectSlug, "workflow", null, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getProjectWorkflowMetrics() should return response") {
                val subscriber = client.getProjectWorkflowMetrics(projectSlug, null, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getProjectWorkflowRuns() should return response") {
                val subscriber = client.getProjectWorkflowRuns(projectSlug, "workflow", null, null, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getProjectWorkflowTestMetrics() should return response") {
                val subscriber = client.getProjectWorkflowTestMetrics(projectSlug, "workflow", null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.averageTestCount).not.to.be.`null`
                expect(response.mostFailedTestsExtra).not.to.be.`null`
                expect(response.slowestTestsExtra).not.to.be.`null`
                expect(response.totalTestRuns).not.to.be.`null`
                expect(response.mostFailedTests).not.to.be.empty
                expectNotNull(response.mostFailedTests.first())
                expect(response.slowestTests).not.to.be.empty
                expectNotNull(response.slowestTests.first())
                expect(response.testRuns).not.to.be.empty
                expectNotNull(response.testRuns.first())
            }

            test("#getWorkflowSummary() should return response") {
                val subscriber = client.getWorkflowSummary(projectSlug, "workflow", null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.metrics).not.to.be.`null`
                expectNotNull(response.metrics)
                expect(response.trends).not.to.be.`null`
                expectNotNull(response.trends)
            }
        }

        // Job endpoints (5)
        group("Job") {
            test("#getJobDetails() should return response") {
                val subscriber = client.getJobDetails(projectSlug, "job-number").test()
                expect(subscriber.isFinished()).to.be.`true`

                val details = subscriber.values().first()
                expect(details.name).not.to.be.`null`
                expect(details.metrics).not.to.be.empty
                details.metrics.forEach { expectNotNull(it) }
                expect(details.trends).not.to.be.empty
                details.trends.forEach { expectNotNull(it) }
            }

            test("#cancelJob() should return response") {
                val subscriber = client.cancelJob(projectSlug, "job-number").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#cancelJobByJobId() should return response") {
                val subscriber = client.cancelJobByJobId("job-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getJobArtifacts() should return response") {
                val subscriber = client.getJobArtifacts(projectSlug, "job-number").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getTests() should return response") {
                val subscriber = client.getTests(projectSlug, "job-number").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }
        }

        // OIDC endpoints (6)
        group("OIDC") {
            test("#getOrgClaims() should return response") {
                val subscriber = client.getOrgClaims("org-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
                expect(claims.audience).not.to.be.empty
                expect(claims.audienceUpdatedAt).not.to.be.`null`
                expect(claims.ttl).not.to.be.`null`
            }

            test("#patchOrgClaims() should return response") {
                val subscriber = client.patchOrgClaims("org-id", PatchClaimsRequest(mapOf())).test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
            }

            test("#deleteOrgClaims() should return response") {
                val subscriber = client.deleteOrgClaims("org-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
            }

            test("#getProjectClaims() should return response") {
                val subscriber = client.getProjectClaims("org-id", projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
                expect(claims.projectId).not.to.be.`null`
            }

            test("#patchProjectClaims() should return response") {
                val subscriber = client.patchProjectClaims("org-id", projectSlug, PatchClaimsRequest(mapOf())).test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
            }

            test("#deleteProjectClaims() should return response") {
                val subscriber = client.deleteProjectClaims("org-id", projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val claims = subscriber.values().first()
                expect(claims.orgId).not.to.be.`null`
            }
        }

        // Organization endpoints (6)
        group("Organization") {
            test("#createOrganization() should return response") {
                val request = CreateOrganizationRequest("org-name", "vcs")
                val subscriber = client.createOrganization(request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val org = subscriber.values().first()
                expectNotNull(org)
            }

            test("#getOrganization() should return response") {
                val subscriber = client.getOrganization("org-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val org = subscriber.values().first()
                expectNotNull(org)
            }

            test("#deleteOrganization() should return response") {
                val subscriber = client.deleteOrganization("org-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#listUrlOrbAllowListEntries() should return response") {
                val subscriber = client.listUrlOrbAllowListEntries("org-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createUrlOrbAllowListEntry() should return response") {
                val request = CreateURLOrbAllowListEntryRequest("namespace", "origin", "auth")
                val subscriber = client.createUrlOrbAllowListEntry("org-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val entry = subscriber.values().first()
                expectNotNull(entry)
            }

            test("#removeUrlOrbAllowListEntry() should return response") {
                val subscriber = client.removeUrlOrbAllowListEntry("org-id", "entry-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Groups endpoints (4)
        group("Groups") {
            test("#getOrganizationGroups() should return response") {
                val subscriber = client.getOrganizationGroups("org-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createOrganizationGroup() should return response") {
                val subscriber = client.createOrganizationGroup("org-id", CreateGroupRequest("group-name")).test()
                expect(subscriber.isFinished()).to.be.`true`

                val group = subscriber.values().first()
                expectNotNull(group)
            }

            test("#getGroup() should return response") {
                val subscriber = client.getGroup("org-id", "group-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val group = subscriber.values().first()
                expectNotNull(group)
            }

            test("#deleteGroup() should return response") {
                val subscriber = client.deleteGroup("org-id", "group-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Usage endpoints (2)
        group("Usage") {
            test("#createUsageExport() should return response") {
                val request = CreateUsageExportRequest("start", "end")
                val subscriber = client.createUsageExport("org-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val job = subscriber.values().first()
                expect(job.id).not.to.be.`null`
                expect(job.status).not.to.be.`null`
                expect(job.createdAt).not.to.be.`null`
                expect(job.downloadUrl).not.to.be.`null`
            }

            test("#getUsageExport() should return response") {
                val subscriber = client.getUsageExport("org-id", "job-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val status = subscriber.values().first()
                expect(status.id).not.to.be.`null`
                expect(status.status).not.to.be.`null`
                expect(status.createdAt).not.to.be.`null`
                expect(status.downloadUrl).not.to.be.`null`
            }
        }

        // Policy endpoints (9)
        group("Policy") {
            test("#getDecisionLogs() should return response") {
                val subscriber = client.getDecisionLogs("owner-id", "context", null, null, null, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
            }

            test("#makeDecision() should return response") {
                val request = MakeDecisionRequest("input", null)
                val subscriber = client.makeDecision("owner-id", "context", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val decision = subscriber.values().first()
                expect(decision.decision).not.to.be.`null`
            }

            test("#getDecisionSettings() should return response") {
                val subscriber = client.getDecisionSettings("owner-id", "context").test()
                expect(subscriber.isFinished()).to.be.`true`

                val settings = subscriber.values().first()
                expect(settings.enabled).not.to.be.`null`
            }

            test("#setDecisionSettings() should return response") {
                val subscriber = client.setDecisionSettings("owner-id", "context", PatchDecisionSettingsRequest(true)).test()
                expect(subscriber.isFinished()).to.be.`true`

                val settings = subscriber.values().first()
                expect(settings.enabled).not.to.be.`null`
            }

            test("#getDecisionLog() should return response") {
                val subscriber = client.getDecisionLog("owner-id", "context", "decision-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val log = subscriber.values().first()
                expect(log.id).not.to.be.`null`
                expect(log.createdAt).not.to.be.`null`
                expect(log.decision).not.to.be.`null`
                expect(log.metadata).not.to.be.`null`
                expect(log.policies).not.to.be.`null`
                expect(log.timeTakenMs).not.to.be.`null`
            }

            test("#getDecisionLogPolicyBundle() should return response") {
                val subscriber = client.getDecisionLogPolicyBundle("owner-id", "context", "decision-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val bundle = subscriber.values().first()
                expect(bundle.id).not.to.be.`null`
            }

            test("#getPolicyBundle() should return response") {
                val subscriber = client.getPolicyBundle("owner-id", "context").test()
                expect(subscriber.isFinished()).to.be.`true`

                val bundle = subscriber.values().first()
                expect(bundle.id).not.to.be.`null`
            }

            test("#createPolicyBundle() should return response") {
                val subscriber = client.createPolicyBundle("owner-id", "context", false, BundlePayload("policy")).test()
                expect(subscriber.isFinished()).to.be.`true`

                val diff = subscriber.values().first()
                expect(diff.message).not.to.be.`null`
            }

            test("#getPolicyDocument() should return response") {
                val subscriber = client.getPolicyDocument("owner-id", "context", "policy-name").test()
                expect(subscriber.isFinished()).to.be.`true`

                val policy = subscriber.values().first()
                expect(policy.name).not.to.be.`null`
                expect(policy.content).not.to.be.`null`
            }
        }

        // Pipeline endpoints (11)
        group("Pipeline") {
            test("#listPipelines() should return response") {
                val subscriber = client.listPipelines("org-slug", null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#continuePipeline() should return response") {
                val request = ContinuePipelineRequest("key", "config", mapOf())
                val subscriber = client.continuePipeline(request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getPipelineById() should return response") {
                val subscriber = client.getPipelineById("pipeline-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val pipeline = subscriber.values().first()
                expectNotNull(pipeline)
            }

            test("#getPipelineConfigById() should return response") {
                val subscriber = client.getPipelineConfigById("pipeline-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val config = subscriber.values().first()
                expect(config.source).not.to.be.`null`
                expect(config.compiled).not.to.be.`null`
                expect(config.setupConfig).not.to.be.`null`
                expect(config.compiledSetupConfig).not.to.be.`null`
            }

            test("#getPipelineWorkflows() should return response") {
                val subscriber = client.getPipelineWorkflows("pipeline-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#getProjectPipelines() should return response") {
                val subscriber = client.getProjectPipelines(projectSlug, null, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
            }

            test("#getMyProjectPipelines() should return response") {
                val subscriber = client.getMyProjectPipelines(projectSlug, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
            }

            test("#triggerNewPipeline() should return response") {
                val request = TriggerNewPipelineRequest(branch = "main")
                val subscriber = client.triggerNewPipeline(projectSlug, request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.id).not.to.be.`null`
                expect(response.number).not.to.be.`null`
                expect(response.state).not.to.be.`null`
                expect(response.createdAt).not.to.be.`null`
            }

            test("#triggerPipelineRun() should return response") {
                val request = TriggerPipelineRunRequest("definition-id", "config", null, null)
                val subscriber = client.triggerPipelineRun("gh", "unhappychoice", "circleci", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val pipeline = subscriber.values().first()
                expectNotNull(pipeline)
            }

            test("#getPipelineByNumber() should return response") {
                val subscriber = client.getPipelineByNumber(projectSlug, 1).test()
                expect(subscriber.isFinished()).to.be.`true`

                val pipeline = subscriber.values().first()
                expectNotNull(pipeline)
            }

            test("#getPipelineValuesById() should return response") {
                val subscriber = client.getPipelineValuesById("pipeline-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val values = subscriber.values().first()
                expect(values.values).not.to.be.`null`
            }
        }

        // Pipeline Definition endpoints (5)
        group("Pipeline Definition") {
            test("#listPipelineDefinitions() should return response") {
                val subscriber = client.listPipelineDefinitions(projectSlug, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createPipelineDefinition() should return response") {
                val request = CreatePipelineDefinitionRequest("name", "config")
                val subscriber = client.createPipelineDefinition(projectSlug, request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val definition = subscriber.values().first()
                expectNotNull(definition)
            }

            test("#getPipelineDefinition() should return response") {
                val subscriber = client.getPipelineDefinition(projectSlug, "definition-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val definition = subscriber.values().first()
                expectNotNull(definition)
            }

            test("#updatePipelineDefinition() should return response") {
                val request = UpdatePipelineDefinitionRequest("name", "config")
                val subscriber = client.updatePipelineDefinition(projectSlug, "definition-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val definition = subscriber.values().first()
                expectNotNull(definition)
            }

            test("#deletePipelineDefinition() should return response") {
                val subscriber = client.deletePipelineDefinition(projectSlug, "definition-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Project endpoints (14)
        group("Project") {
            test("#getProjectBySlug() should return response") {
                val subscriber = client.getProjectBySlug(projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val project = subscriber.values().first()
                expectNotNull(project)
            }

            test("#createProject() should return response") {
                val request = CreateProjectRequest("vcs")
                val subscriber = client.createProject("provider", "org", "project", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val project = subscriber.values().first()
                expectNotNull(project)
            }

            test("#createProjectInOrg() should return response") {
                val request = CreateProjectRequest("vcs")
                val subscriber = client.createProjectInOrg("org-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val project = subscriber.values().first()
                expectNotNull(project)
            }

            test("#deleteProjectBySlug() should return response") {
                val subscriber = client.deleteProjectBySlug(projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getProjectCheckoutKeys() should return response") {
                val subscriber = client.getProjectCheckoutKeys(projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.`null`
                expect(response.items!!).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createProjectCheckoutKey() should return response") {
                val request = CreateCheckoutKeyRequest("deploy-key")
                val subscriber = client.createProjectCheckoutKey(projectSlug, request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val key = subscriber.values().first()
                expectNotNull(key)
            }

            test("#getProjectCheckoutKey() should return response") {
                val subscriber = client.getProjectCheckoutKey(projectSlug, "fingerprint").test()
                expect(subscriber.isFinished()).to.be.`true`

                val key = subscriber.values().first()
                expectNotNull(key)
            }

            test("#deleteProjectCheckoutKey() should return response") {
                val subscriber = client.deleteProjectCheckoutKey(projectSlug, "fingerprint").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getProjectEnvironmentVariables() should return response") {
                val subscriber = client.getProjectEnvironmentVariables(projectSlug).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createProjectEnvironmentVariable() should return response") {
                val request = CreateEnvVarRequest("value")
                val subscriber = client.createProjectEnvironmentVariable(projectSlug, request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val envVar = subscriber.values().first()
                expectNotNull(envVar)
            }

            test("#getProjectEnvironmentVariable() should return response") {
                val subscriber = client.getProjectEnvironmentVariable(projectSlug, "VAR").test()
                expect(subscriber.isFinished()).to.be.`true`

                val envVar = subscriber.values().first()
                expectNotNull(envVar)
            }

            test("#deleteProjectEnvironmentVariable() should return response") {
                val subscriber = client.deleteProjectEnvironmentVariable(projectSlug, "VAR").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#getProjectSettings() should return response") {
                val subscriber = client.getProjectSettings("github", "org", "project").test()
                expect(subscriber.isFinished()).to.be.`true`

                val settings = subscriber.values().first()
                expectNotNull(settings)
            }

            test("#patchProjectSettings() should return response") {
                val advancedSettings = PatchProjectSettingsRequest.AdvancedSettings(
                    autocancelBuilds = true,
                    buildForkPrs = true,
                    buildPrsOnly = false,
                    disableSsh = false,
                    forksReceiveSecretEnvVars = false,
                    oss = false,
                    setGithubStatus = true,
                    setupWorkflows = false,
                    writeSettingsRequiresAdmin = true,
                    prOnlyBranchOverrides = listOf("main")
                )
                val request = PatchProjectSettingsRequest(advancedSettings)
                val subscriber = client.patchProjectSettings("github", "org", "project", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val settings = subscriber.values().first()
                expectNotNull(settings)
            }
        }

        // Rollback endpoint (1)
        group("Rollback") {
            test("#rollbackProject() should return response") {
                val request = RollbackProjectRequest("version")
                val subscriber = client.rollbackProject(projectSlug, request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Schedule endpoints (5)
        group("Schedule") {
            test("#getProjectSchedules() should return response") {
                val subscriber = client.getProjectSchedules(projectSlug, null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createSchedule() should return response") {
                val params = CreateScheduleParameters("name", "description", projectSlug, mapOf(), listOf())
                val subscriber = client.createSchedule(projectSlug, params).test()
                expect(subscriber.isFinished()).to.be.`true`

                val schedule = subscriber.values().first()
                expectNotNull(schedule)
            }

            test("#getScheduleById() should return response") {
                val subscriber = client.getScheduleById("schedule-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val schedule = subscriber.values().first()
                expectNotNull(schedule)
            }

            test("#updateSchedule() should return response") {
                val params = UpdateScheduleParameters("name", "description", null, null)
                val subscriber = client.updateSchedule("schedule-id", params).test()
                expect(subscriber.isFinished()).to.be.`true`

                val schedule = subscriber.values().first()
                expectNotNull(schedule)
            }

            test("#deleteSchedule() should return response") {
                val subscriber = client.deleteSchedule("schedule-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Trigger endpoints (5)
        group("Trigger") {
            test("#listPipelineDefinitionTriggers() should return response") {
                val subscriber = client.listPipelineDefinitionTriggers(projectSlug, "definition-id", null).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createTrigger() should return response") {
                val request = CreateTriggerRequest("name", "description", "inbound-webhook", mapOf())
                val subscriber = client.createTrigger(projectSlug, "definition-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val trigger = subscriber.values().first()
                expectNotNull(trigger)
            }

            test("#getTrigger() should return response") {
                val subscriber = client.getTrigger("project-id", "trigger-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val trigger = subscriber.values().first()
                expectNotNull(trigger)
            }

            test("#updateTrigger() should return response") {
                val request = UpdateTriggerRequest("name", "description", "inbound-webhook", mapOf())
                val subscriber = client.updateTrigger("project-id", "trigger-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val trigger = subscriber.values().first()
                expectNotNull(trigger)
            }

            test("#deleteTrigger() should return response") {
                val subscriber = client.deleteTrigger("project-id", "trigger-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Webhook endpoints (5)
        group("Webhook") {
            test("#getWebhooks() should return response") {
                val subscriber = client.getWebhooks("project-id", "project").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#createWebhook() should return response") {
                val request = CreateWebhookRequest("name", "url", "secret", WebhookScopeRequest("project", "project-id"), listOf("workflow-completed"))
                val subscriber = client.createWebhook(request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val webhook = subscriber.values().first()
                expectNotNull(webhook)
            }

            test("#getWebhookById() should return response") {
                val subscriber = client.getWebhookById("webhook-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val webhook = subscriber.values().first()
                expectNotNull(webhook)
            }

            test("#updateWebhook() should return response") {
                val request = UpdateWebhookRequest("name", "url", "secret", listOf("workflow-completed"))
                val subscriber = client.updateWebhook("webhook-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val webhook = subscriber.values().first()
                expectNotNull(webhook)
            }

            test("#deleteWebhook() should return response") {
                val subscriber = client.deleteWebhook("webhook-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }

        // Workflow endpoints (5)
        group("Workflow") {
            test("#getWorkflowById() should return response") {
                val subscriber = client.getWorkflowById("workflow-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val workflow = subscriber.values().first()
                expectNotNull(workflow)
            }

            test("#getWorkflowJobs() should return response") {
                val subscriber = client.getWorkflowJobs("workflow-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.items).not.to.be.empty
                expect(response.nextPageToken).not.to.be.`null`
                expectNotNull(response.items.first())
            }

            test("#cancelWorkflow() should return response") {
                val subscriber = client.cancelWorkflow("workflow-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }

            test("#rerunWorkflow() should return response") {
                val request = RerunWorkflowRequest()
                val subscriber = client.rerunWorkflow("workflow-id", request).test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.workflowId).not.to.be.`null`
            }

            test("#approveJob() should return response") {
                val subscriber = client.approveJob("workflow-id", "approval-request-id").test()
                expect(subscriber.isFinished()).to.be.`true`

                val response = subscriber.values().first()
                expect(response.message).not.to.be.`null`
            }
        }
    }
})

private fun <T> TestObserver<T>.isFinished(): Boolean {
    assertNoErrors()
    assertComplete()
    return true
}

// User types
private fun expectNotNull(user: User) {
    expect(user.id).not.to.be.`null`
    expect(user.login).not.to.be.`null`
    expect(user.name).not.to.be.`null`
}

private fun expectNotNull(collaboration: Collaboration) {
    expect(collaboration.id).not.to.be.`null`
    expect(collaboration.vcsType).not.to.be.`null`
    expect(collaboration.name).not.to.be.`null`
    expect(collaboration.avatarUrl).not.to.be.`null`
    expect(collaboration.slug).not.to.be.`null`
}

// Context types
private fun expectNotNull(context: Context) {
    expect(context.id).not.to.be.`null`
    expect(context.name).not.to.be.`null`
    expect(context.createdAt).not.to.be.`null`
}

private fun expectNotNull(envVar: EnvironmentVariable) {
    expect(envVar.variable).not.to.be.`null`
    expect(envVar.createdAt).not.to.be.`null`
    expect(envVar.updatedAt).not.to.be.`null`
    expect(envVar.contextId).not.to.be.`null`
}

// Deploy types
private fun expectNotNull(component: Component) {
    expect(component.id).not.to.be.`null`
    expect(component.name).not.to.be.`null`
    expect(component.projectId).not.to.be.`null`
    expect(component.createdAt).not.to.be.`null`
    expect(component.updatedAt).not.to.be.`null`
}

private fun expectNotNull(componentVersion: ComponentVersion) {
    expect(componentVersion.id).not.to.be.`null`
    expect(componentVersion.componentId).not.to.be.`null`
    expect(componentVersion.version).not.to.be.`null`
    expect(componentVersion.createdAt).not.to.be.`null`
}

private fun expectNotNull(env: DeployEnvironment) {
    expect(env.id).not.to.be.`null`
    expect(env.name).not.to.be.`null`
    expect(env.organizationId).not.to.be.`null`
    expect(env.createdAt).not.to.be.`null`
    expect(env.updatedAt).not.to.be.`null`
}

// Job types
private fun expectNotNull(artifact: Artifact) {
    expect(artifact.path).not.to.be.`null`
    expect(artifact.url).not.to.be.`null`
    expect(artifact.nodeIndex).not.to.be.`null`
}

private fun expectNotNull(job: Job) {
    expect(job.id).not.to.be.`null`
    expect(job.name).not.to.be.`null`
    expect(job.type).not.to.be.`null`
    expect(job.status).not.to.be.`null`
    expect(job.startedAt).not.to.be.`null`
    expect(job.stoppedAt).not.to.be.`null`
    expect(job.approvalRequestId).not.to.be.`null`
    expect(job.dependencies).not.to.be.`null`
}

// Organization types
private fun expectNotNull(org: Organization) {
    expect(org.id).not.to.be.`null`
    expect(org.name).not.to.be.`null`
    expect(org.slug).not.to.be.`null`
    expect(org.vcsType).not.to.be.`null`
}

private fun expectNotNull(entry: URLOrbAllowListEntry) {
    expect(entry.id).not.to.be.`null`
    expect(entry.name).not.to.be.`null`
    expect(entry.prefix).not.to.be.`null`
    expect(entry.auth).not.to.be.`null`
}

// Group types
private fun expectNotNull(group: Group) {
    expect(group.id).not.to.be.`null`
    expect(group.name).not.to.be.`null`
    expect(group.organizationId).not.to.be.`null`
}

// Pipeline types
private fun expectNotNull(pipeline: Pipeline) {
    expect(pipeline.id).not.to.be.`null`
    expect(pipeline.errors).not.to.be.`null`
    pipeline.errors.forEach { expectNotNull(it) }
    expect(pipeline.projectSlug).not.to.be.`null`
    expect(pipeline.updatedAt).not.to.be.`null`
    expect(pipeline.number).not.to.be.`null`
    expect(pipeline.triggerParameters).not.to.be.`null`
    expect(pipeline.state).not.to.be.`null`
    expect(pipeline.createdAt).not.to.be.`null`
    expect(pipeline.trigger).not.to.be.`null`
    expectNotNull(pipeline.trigger)
    pipeline.vcs?.let { expectNotNull(it) }
}

private fun expectNotNull(error: Pipeline.PipelineError) {
    expect(error.type).not.to.be.`null`
    expect(error.message).not.to.be.`null`
}

private fun expectNotNull(trigger: Pipeline.Trigger) {
    expect(trigger.type).not.to.be.`null`
    expect(trigger.receivedAt).not.to.be.`null`
    expect(trigger.actor).not.to.be.`null`
    expectNotNull(trigger.actor)
}

private fun expectNotNull(actor: Pipeline.Trigger.Actor) {
    expect(actor.login).not.to.be.`null`
    expect(actor.avatarUrl).not.to.be.`null`
}

private fun expectNotNull(vcs: Pipeline.Vcs) {
    expect(vcs.providerName).not.to.be.`null`
    expect(vcs.targetRepositoryUrl).not.to.be.`null`
    expect(vcs.branch).not.to.be.`null`
    expect(vcs.reviewId).not.to.be.`null`
    expect(vcs.reviewUrl).not.to.be.`null`
    expect(vcs.revision).not.to.be.`null`
    expect(vcs.tag).not.to.be.`null`
    vcs.commit?.let { expectNotNull(it) }
    expect(vcs.originRepositoryUrl).not.to.be.`null`
}

private fun expectNotNull(commit: Pipeline.Vcs.Commit) {
    expect(commit.subject).not.to.be.`null`
    expect(commit.body).not.to.be.`null`
}

private fun expectNotNull(definition: PipelineDefinition) {
    expect(definition.id).not.to.be.`null`
    expect(definition.projectId).not.to.be.`null`
    expect(definition.name).not.to.be.`null`
    expect(definition.createdAt).not.to.be.`null`
    expect(definition.updatedAt).not.to.be.`null`
    expect(definition.definition).not.to.be.`null`
}

// Project types
private fun expectNotNull(project: Project) {
    expect(project.slug).not.to.be.`null`
    expect(project.name).not.to.be.`null`
    expect(project.id).not.to.be.`null`
    expect(project.organizationName).not.to.be.`null`
    expect(project.organizationSlug).not.to.be.`null`
    expect(project.organizationId).not.to.be.`null`
    expect(project.vcsInfo).not.to.be.`null`
    expect(project.vcsInfo.vcsUrl).not.to.be.`null`
    expect(project.vcsInfo.provider).not.to.be.`null`
    expect(project.vcsInfo.defaultBranch).not.to.be.`null`
}

private fun expectNotNull(checkoutKey: CheckoutKey) {
    expect(checkoutKey.publicKey).not.to.be.`null`
    expect(checkoutKey.type).not.to.be.`null`
    expect(checkoutKey.fingerprint).not.to.be.`null`
    expect(checkoutKey.preferred).not.to.be.`null`
    expect(checkoutKey.createdAt).not.to.be.`null`
}

private fun expectNotNull(envVar: ProjectEnvironmentVariable) {
    expect(envVar.name).not.to.be.`null`
    expect(envVar.value).not.to.be.`null`
}

private fun expectNotNull(settings: ProjectSettings) {
    expect(settings.advanced).not.to.be.`null`
    val advanced = settings.advanced
    expect(advanced.autocancelBuilds).not.to.be.`null`
    expect(advanced.buildForkPrs).not.to.be.`null`
    expect(advanced.buildPrsOnly).not.to.be.`null`
    expect(advanced.disableSsh).not.to.be.`null`
    expect(advanced.forksReceiveSecretEnvVars).not.to.be.`null`
    expect(advanced.oss).not.to.be.`null`
    expect(advanced.setGithubStatus).not.to.be.`null`
    expect(advanced.setupWorkflows).not.to.be.`null`
    expect(advanced.writeSettingsRequiresAdmin).not.to.be.`null`
    expect(advanced.prOnlyBranchOverrides).not.to.be.`null`
}

// Schedule types
private fun expectNotNull(schedule: Schedule) {
    expect(schedule.id).not.to.be.`null`
    expect(schedule.description).not.to.be.`null`
    expect(schedule.name).not.to.be.`null`
    expect(schedule.createdAt).not.to.be.`null`
    expect(schedule.updatedAt).not.to.be.`null`
    expect(schedule.projectSlug).not.to.be.`null`
    expect(schedule.parameters).not.to.be.`null`
    expect(schedule.actor).not.to.be.`null`
    expectNotNull(schedule.actor)
    expect(schedule.timetables).not.to.be.empty
    schedule.timetables.forEach { expectNotNull(it) }
}

private fun expectNotNull(actor: Actor) {
    expect(actor.id).not.to.be.`null`
    expect(actor.login).not.to.be.`null`
    expect(actor.name).not.to.be.`null`
}

private fun expectNotNull(timetable: Timetable) {
    expect(timetable.id).not.to.be.`null`
    expect(timetable.attribution).not.to.be.`null`
    expect(timetable.days).not.to.be.empty
    expect(timetable.hours).not.to.be.empty
    expect(timetable.minutes).not.to.be.empty
    expect(timetable.perHour).not.to.be.`null`
}

// Trigger types
private fun expectNotNull(trigger: Trigger) {
    expect(trigger.id).not.to.be.`null`
    expect(trigger.pipelineDefinitionId).not.to.be.`null`
    expect(trigger.projectId).not.to.be.`null`
    expect(trigger.name).not.to.be.`null`
    expect(trigger.description).not.to.be.`null`
    expect(trigger.createdAt).not.to.be.`null`
    expect(trigger.updatedAt).not.to.be.`null`
    expect(trigger.type).not.to.be.`null`
    expect(trigger.parameters).not.to.be.`null`
}

// Webhook types
private fun expectNotNull(webhook: Webhook) {
    expect(webhook.id).not.to.be.`null`
    expect(webhook.name).not.to.be.`null`
    expect(webhook.url).not.to.be.`null`
    expect(webhook.createdAt).not.to.be.`null`
    expect(webhook.updatedAt).not.to.be.`null`
    expect(webhook.signingSecret).not.to.be.`null`
    expect(webhook.scope).not.to.be.`null`
    expectNotNull(webhook.scope)
    expect(webhook.events).not.to.be.empty
}

private fun expectNotNull(scope: WebhookScope) {
    expect(scope.type).not.to.be.`null`
    expect(scope.id).not.to.be.`null`
}

// Workflow types
private fun expectNotNull(workflow: Workflow) {
    expect(workflow.id).not.to.be.`null`
    expect(workflow.name).not.to.be.`null`
    expect(workflow.projectSlug).not.to.be.`null`
    expect(workflow.status).not.to.be.`null`
    expect(workflow.createdAt).not.to.be.`null`
    expect(workflow.stoppedAt).not.to.be.`null`
    expect(workflow.pipelineId).not.to.be.`null`
    expect(workflow.pipelineNumber).not.to.be.`null`
    expect(workflow.startedBy).not.to.be.`null`
    expect(workflow.canceledBy).not.to.be.`null`
    expect(workflow.erroredBy).not.to.be.`null`
    expect(workflow.tag).not.to.be.`null`
}

// Insights types
private fun expectNotNull(metric: WorkflowMetricsResponse.WorkflowMetric) {
    expect(metric.name).not.to.be.`null`
    expect(metric.metrics).not.to.be.`null`
    expectNotNull(metric.metrics)
}

private fun expectNotNull(metrics: WorkflowMetricsResponse.WorkflowMetrics) {
    expect(metrics.totalRuns).not.to.be.`null`
    expect(metrics.successfulRuns).not.to.be.`null`
    expect(metrics.mttr).not.to.be.`null`
    expect(metrics.totalCreditsUsed).not.to.be.`null`
    expect(metrics.failedRuns).not.to.be.`null`
    expect(metrics.successRate).not.to.be.`null`
    expect(metrics.durationMetrics).not.to.be.`null`
    expectNotNull(metrics.durationMetrics)
    expect(metrics.totalRecoveries).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
}

private fun expectNotNull(duration: WorkflowMetricsResponse.DurationMetrics) {
    expect(duration.min).not.to.be.`null`
    expect(duration.mean).not.to.be.`null`
    expect(duration.median).not.to.be.`null`
    expect(duration.p95).not.to.be.`null`
    expect(duration.max).not.to.be.`null`
    expect(duration.standardDeviation).not.to.be.`null`
}

private fun expectNotNull(item: WorkflowRunsResponse.WorkflowRunItem) {
    expect(item.id).not.to.be.`null`
    expect(item.branch).not.to.be.`null`
    expect(item.duration).not.to.be.`null`
    expect(item.createdAt).not.to.be.`null`
    expect(item.stoppedAt).not.to.be.`null`
    expect(item.creditsUsed).not.to.be.`null`
    expect(item.status).not.to.be.`null`
}

private fun expectNotNull(metric: JobMetricsResponse.JobMetric) {
    expect(metric.name).not.to.be.`null`
    expect(metric.metrics).not.to.be.`null`
    expectNotNull(metric.metrics)
}

private fun expectNotNull(metrics: JobMetricsResponse.JobMetrics) {
    expect(metrics.totalRuns).not.to.be.`null`
    expect(metrics.failedRuns).not.to.be.`null`
    expect(metrics.successfulRuns).not.to.be.`null`
    expect(metrics.durationMetrics).not.to.be.`null`
    expectNotNull(metrics.durationMetrics)
    expect(metrics.totalCreditsUsed).not.to.be.`null`
    expect(metrics.successRate).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
}

private fun expectNotNull(duration: JobMetricsResponse.DurationMetrics) {
    expect(duration.min).not.to.be.`null`
    expect(duration.mean).not.to.be.`null`
    expect(duration.median).not.to.be.`null`
    expect(duration.p95).not.to.be.`null`
    expect(duration.max).not.to.be.`null`
    expect(duration.standardDeviation).not.to.be.`null`
}

private fun expectNotNull(metrics: WorkflowSummary.Metrics) {
    expect(metrics.totalRuns).not.to.be.`null`
    expect(metrics.failedRuns).not.to.be.`null`
    expect(metrics.successfulRuns).not.to.be.`null`
    expect(metrics.successRate).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
    expect(metrics.mttr).not.to.be.`null`
    expect(metrics.durationMetrics).not.to.be.`null`
    expectNotNull(metrics.durationMetrics)
    expect(metrics.totalCreditsUsed).not.to.be.`null`
}

private fun expectNotNull(trends: WorkflowSummary.Trends) {
    expect(trends.totalRuns).not.to.be.`null`
    expect(trends.failedRuns).not.to.be.`null`
    expect(trends.successfulRuns).not.to.be.`null`
    expect(trends.successRate).not.to.be.`null`
    expect(trends.throughput).not.to.be.`null`
    expect(trends.mttr).not.to.be.`null`
    expect(trends.totalCreditsUsed).not.to.be.`null`
}

private fun expectNotNull(duration: WorkflowSummary.DurationMetrics) {
    expect(duration.min).not.to.be.`null`
    expect(duration.mean).not.to.be.`null`
    expect(duration.median).not.to.be.`null`
    expect(duration.p95).not.to.be.`null`
    expect(duration.max).not.to.be.`null`
    expect(duration.standardDeviation).not.to.be.`null`
}

private fun expectNotNull(test: TestMetricsResponse.FailedTest) {
    expect(test.testName).not.to.be.`null`
    expect(test.className).not.to.be.`null`
    expect(test.file).not.to.be.`null`
    expect(test.failedRuns).not.to.be.`null`
    expect(test.totalRuns).not.to.be.`null`
}

private fun expectNotNull(test: TestMetricsResponse.SlowTest) {
    expect(test.testName).not.to.be.`null`
    expect(test.className).not.to.be.`null`
    expect(test.file).not.to.be.`null`
    expect(test.p95Duration).not.to.be.`null`
}

private fun expectNotNull(run: TestMetricsResponse.TestRun) {
    expect(run.pipelineNumber).not.to.be.`null`
    expect(run.workflowId).not.to.be.`null`
    expect(run.successRate).not.to.be.`null`
    expect(run.testCounts).not.to.be.`null`
    expectNotNull(run.testCounts)
}

private fun expectNotNull(counts: TestMetricsResponse.TestCounts) {
    expect(counts.error).not.to.be.`null`
    expect(counts.failure).not.to.be.`null`
    expect(counts.skipped).not.to.be.`null`
    expect(counts.success).not.to.be.`null`
    expect(counts.total).not.to.be.`null`
}

private fun expectNotNull(test: TestsResponse.Test) {
    expect(test.message).not.to.be.`null`
    expect(test.source).not.to.be.`null`
    expect(test.runTime).not.to.be.`null`
    expect(test.file).not.to.be.`null`
    expect(test.result).not.to.be.`null`
    expect(test.name).not.to.be.`null`
    expect(test.classname).not.to.be.`null`
}

private fun expectNotNull(test: FlakyTests.FlakyTest) {
    expect(test.`time-wasted`).not.to.be.`null`
    expect(test.`workflow-created-at`).not.to.be.`null`
    expect(test.`workflow-id`).not.to.be.`null`
    expect(test.classname).not.to.be.`null`
    expect(test.`pipeline-number`).not.to.be.`null`
    expect(test.`workflow-name`).not.to.be.`null`
    expect(test.`test-name`).not.to.be.`null`
    expect(test.`job-name`).not.to.be.`null`
    expect(test.`job-number`).not.to.be.`null`
    expect(test.`pipeline-id`).not.to.be.`null`
    expect(test.`rerun-workflow-id`).not.to.be.`null`
    expect(test.`test-result`).not.to.be.`null`
    expect(test.`workflow-rerun-number`).not.to.be.`null`
}

private fun expectNotNull(item: JobTimeseriesResponse.JobTimeseriesItem) {
    expect(item.name).not.to.be.`null`
    expect(item.min_started_at).not.to.be.`null`
    expect(item.max_ended_at).not.to.be.`null`
    expect(item.timestamp).not.to.be.`null`
    expect(item.metrics).not.to.be.`null`
    expectNotNull(item.metrics)
}

private fun expectNotNull(metrics: JobTimeseriesResponse.JobMetrics) {
    expect(metrics.total_runs).not.to.be.`null`
    expect(metrics.failed_runs).not.to.be.`null`
    expect(metrics.successful_runs).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
    expect(metrics.median_credits_used).not.to.be.`null`
    expect(metrics.total_credits_used).not.to.be.`null`
    expect(metrics.duration_metrics).not.to.be.`null`
    expectNotNull(metrics.duration_metrics)
}

private fun expectNotNull(duration: JobTimeseriesResponse.DurationMetrics) {
    expect(duration.min).not.to.be.`null`
    expect(duration.median).not.to.be.`null`
    expect(duration.max).not.to.be.`null`
    expect(duration.p95).not.to.be.`null`
    expect(duration.total).not.to.be.`null`
}

private fun expectNotNull(data: OrgSummaryData.OrgData) {
    expect(data.metrics).not.to.be.`null`
    expectNotNull(data.metrics)
    expect(data.trends).not.to.be.`null`
    expectNotNull(data.trends)
}

private fun expectNotNull(metrics: OrgSummaryData.OrgMetrics) {
    expect(metrics.total_runs).not.to.be.`null`
    expect(metrics.total_duration_secs).not.to.be.`null`
    expect(metrics.total_credits_used).not.to.be.`null`
    expect(metrics.success_rate).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
}

private fun expectNotNull(trends: OrgSummaryData.OrgTrends) {
    expect(trends.total_runs).not.to.be.`null`
    expect(trends.total_duration_secs).not.to.be.`null`
    expect(trends.total_credits_used).not.to.be.`null`
    expect(trends.success_rate).not.to.be.`null`
    expect(trends.throughput).not.to.be.`null`
}

private fun expectNotNull(data: OrgSummaryData.OrgProjectData) {
    expect(data.project_name).not.to.be.`null`
    expect(data.metrics).not.to.be.`null`
    expectNotNull(data.metrics)
    expect(data.trends).not.to.be.`null`
    expectNotNull(data.trends)
}

private fun expectNotNull(metrics: OrgSummaryData.ProjectMetrics) {
    expect(metrics.total_runs).not.to.be.`null`
    expect(metrics.total_duration_secs).not.to.be.`null`
    expect(metrics.total_credits_used).not.to.be.`null`
    expect(metrics.success_rate).not.to.be.`null`
}

private fun expectNotNull(trends: OrgSummaryData.ProjectTrends) {
    expect(trends.total_runs).not.to.be.`null`
    expect(trends.total_duration_secs).not.to.be.`null`
    expect(trends.total_credits_used).not.to.be.`null`
    expect(trends.success_rate).not.to.be.`null`
}

private fun expectNotNull(data: ProjectWorkflowsPageData.ProjectData) {
    expect(data.metrics).not.to.be.`null`
    expectNotNull(data.metrics)
    expect(data.trends).not.to.be.`null`
    expectNotNull(data.trends)
}

private fun expectNotNull(metrics: ProjectWorkflowsPageData.ProjectMetrics) {
    expect(metrics.total_runs).not.to.be.`null`
    expect(metrics.total_duration_secs).not.to.be.`null`
    expect(metrics.total_credits_used).not.to.be.`null`
    expect(metrics.success_rate).not.to.be.`null`
    expect(metrics.throughput).not.to.be.`null`
}

private fun expectNotNull(trends: ProjectWorkflowsPageData.ProjectTrends) {
    expect(trends.total_runs).not.to.be.`null`
    expect(trends.total_duration_secs).not.to.be.`null`
    expect(trends.total_credits_used).not.to.be.`null`
    expect(trends.success_rate).not.to.be.`null`
    expect(trends.throughput).not.to.be.`null`
}

private fun expectNotNull(data: ProjectWorkflowsPageData.ProjectWorkflowData) {
    expect(data.workflow_name).not.to.be.`null`
    expect(data.metrics).not.to.be.`null`
    expectNotNull(data.metrics)
    expect(data.trends).not.to.be.`null`
    expectNotNull(data.trends)
}

private fun expectNotNull(metrics: ProjectWorkflowsPageData.WorkflowMetrics) {
    expect(metrics.total_runs).not.to.be.`null`
    expect(metrics.total_credits_used).not.to.be.`null`
    expect(metrics.success_rate).not.to.be.`null`
    expect(metrics.p95_duration_secs).not.to.be.`null`
}

private fun expectNotNull(trends: ProjectWorkflowsPageData.WorkflowTrends) {
    expect(trends.total_runs).not.to.be.`null`
    expect(trends.total_credits_used).not.to.be.`null`
    expect(trends.success_rate).not.to.be.`null`
    expect(trends.p95_duration_secs).not.to.be.`null`
}

private fun expectNotNull(data: ProjectWorkflowsPageData.ProjectWorkflowBranchData) {
    expect(data.workflow_name).not.to.be.`null`
    expect(data.branch).not.to.be.`null`
    expect(data.metrics).not.to.be.`null`
    expectNotNull(data.metrics)
    expect(data.trends).not.to.be.`null`
    expectNotNull(data.trends)
}

private fun expectNotNull(metric: JobDetails.Metric) {
    expect(metric.name).not.to.be.`null`
    expect(metric.value).not.to.be.`null`
}

private fun expectNotNull(trend: JobDetails.Trend) {
    expect(trend.name).not.to.be.`null`
    expect(trend.value).not.to.be.`null`
}
