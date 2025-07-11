package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import com.winterbe.expekt.should
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class CircleCIAPIClientV2Spec : Spek({
    lateinit var client: CircleCIAPIClientV2

    beforeEachTest {
        client = MockCircleCIAPIClientV2()
    }

    describe("CircleCI API v2 client") {
        it("gets current user") {
            client.getMe().blockingGet().run {
                id.should.not.be.empty
                login.should.not.be.empty
                name.should.not.be.empty
            }
        }

        it("gets user by id") {
            client.getUser("id").blockingGet().run {
                id.should.not.be.empty
                login.should.not.be.empty
                name.should.not.be.empty
            }
        }

        it("gets user's collaborations") {
            client.getCollaborations().blockingGet().first().run {
                slug.should.not.be.empty
                vcsType.should.not.be.empty
                name.should.not.be.empty
                avatarUrl.should.not.be.empty
            }
        }

        it("gets a context") {
            client.getContext("context-id").blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                createdAt.should.not.be.null
            }
        }

        it("gets contexts") {
            client.listContexts("owner-id", "owner-slug", "account").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    name.should.not.be.empty
                    createdAt.should.not.be.null
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("creates a context") {
            val request = CreateContextRequest(
                name = "name",
                owner = CreateContextRequest.Owner(id = "id", type = "type")
            )
            client.createContext(request).blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                createdAt.should.not.be.null
            }
        }

        it("deletes a context") {
            client.deleteContext("context-id").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets environment variables from a context") {
            client.listEnvironmentVariablesFromContext("context-id").blockingGet().run {
                items.first().run {
                    variable.should.not.be.empty
                    contextId.should.not.be.empty
                    createdAt.should.not.be.null
                    updatedAt.should.not.be.null
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("adds or updates an environment variable") {
            val request = AddEnvironmentVariableRequest(value = "value")
            (client.addEnvironmentVariableToContext("context-id", "env-var-name", request).blockingGet() as EnvironmentVariable).run {
                variable.should.not.be.empty
                contextId.should.not.be.empty
                createdAt.should.not.be.null
                updatedAt.should.not.be.null
            }
        }

        it("deletes an environment variable from a context") {
            client.deleteEnvironmentVariableFromContext("context-id", "env-var-name").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets all insights branches") {
            client.getAllInsightsBranches("project-slug", "workflow-name").blockingGet().run {
                org_id.should.not.be.empty
                project_id.should.not.be.empty
                branches.first().should.not.be.empty
            }
        }

        it("gets flaky tests") {
            client.getFlakyTests("project-slug").blockingGet().run {
                `flaky-tests`.first().run {
                    `time-wasted`.should.be.above(0)
                    `workflow-created-at`.should.not.be.empty
                    `workflow-id`.should.not.be.empty
                    `pipeline-number`.should.be.above(0)
                    `workflow-name`.should.not.be.empty
                    `test-name`.should.not.be.empty
                    `job-name`.should.not.be.empty
                    `job-number`.should.be.above(0)
                    `pipeline-id`.should.not.be.empty
                }
            }
        }

        it("gets job timeseries") {
            client.getJobTimeseries("project-slug", "workflow-name", "branch", "daily", "start-date", "end-date").blockingGet().run {
                items.first().run {
                    name.should.not.be.empty
                    min_started_at.should.not.be.empty
                    max_ended_at.should.not.be.empty
                    timestamp.should.not.be.empty
                    metrics.run {
                        total_runs.should.be.above(0)
                        failed_runs.should.be.above(0)
                        successful_runs.should.be.above(0)
                        throughput.should.be.above(0f)
                        median_credits_used.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        duration_metrics.run {
                            min.should.be.above(0)
                            median.should.be.above(0)
                            max.should.be.above(0)
                            p95.should.be.above(0)
                            total.should.be.above(0)
                        }
                    }
                }
                next_page_token.should.not.be.empty
            }
        }

        it("gets org summary data") {
            client.getOrgSummaryData("org-slug", "reporting-window", null).blockingGet().run {
                org_data.run {
                    metrics.run {
                        total_runs.should.be.above(0)
                        total_duration_secs.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        success_rate.should.be.above(0f)
                        throughput.should.be.above(0f)
                    }
                    trends.run {
                        total_runs.should.be.above(0f)
                        total_duration_secs.should.be.above(0f)
                        total_credits_used.should.be.above(0f)
                        success_rate.should.be.above(0f)
                        throughput.should.be.above(0f)
                    }
                }
                org_project_data.first().run {
                    project_name.should.not.be.empty
                    metrics.run {
                        total_runs.should.be.above(0)
                        total_duration_secs.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        success_rate.should.be.above(0f)
                    }
                    trends.run {
                        total_runs.should.be.above(0f)
                        total_duration_secs.should.be.above(0f)
                        total_credits_used.should.be.above(0f)
                        success_rate.should.be.above(0f)
                    }
                }
                all_projects.first().should.not.be.empty
            }
        }

        it("gets project workflows page data") {
            client.getProjectWorkflowsPageData("project-slug", "reporting-window", null, null).blockingGet().run {
                org_id.should.not.be.empty
                project_id.should.not.be.empty
                project_data.run {
                    metrics.run {
                        total_runs.should.be.above(0)
                        total_duration_secs.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        success_rate.should.be.above(0f)
                        throughput.should.be.above(0f)
                    }
                    trends.run {
                        total_runs.should.be.above(0f)
                        total_duration_secs.should.be.above(0f)
                        total_credits_used.should.be.above(0f)
                        success_rate.should.be.above(0f)
                        throughput.should.be.above(0f)
                    }
                }
                project_workflow_data.first().run {
                    workflow_name.should.not.be.empty
                    metrics.run {
                        total_runs.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        success_rate.should.be.above(0f)
                        p95_duration_secs.should.be.above(0f)
                    }
                    trends.run {
                        total_runs.should.be.above(0f)
                        total_credits_used.should.be.above(0f)
                        success_rate.should.be.above(0f)
                        p95_duration_secs.should.be.above(0f)
                    }
                }
                project_workflow_branch_data.first().run {
                    workflow_name.should.not.be.empty
                    branch.should.not.be.empty
                    metrics.run {
                        total_runs.should.be.above(0)
                        total_credits_used.should.be.above(0)
                        success_rate.should.be.above(0f)
                        p95_duration_secs.should.be.above(0f)
                    }
                    trends.run {
                        total_runs.should.be.above(0f)
                        total_credits_used.should.be.above(0f)
                        success_rate.should.be.above(0f)
                        p95_duration_secs.should.be.above(0f)
                    }
                }
                all_branches.first().should.not.be.empty
                all_workflows.first().should.not.be.empty
            }
        }

        it("gets a job's details") {
            client.getJobDetails("project-slug", "job-number").blockingGet().run {
                name.should.not.be.empty
            }
        }

        it("gets a job's artifacts") {
            client.getJobArtifacts("project-slug", "job-number").blockingGet().run {
                items.first().run {
                    path.should.not.be.empty
                    nodeIndex.should.be.above(-1)
                    url.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("gets a job's test metadata") {
            client.getTests("project-slug", "job-number").blockingGet().run {
                items.first().run {
                    message.should.not.be.empty
                    source.should.not.be.empty
                    runTime.should.be.above(0.0)
                    file.should.not.be.empty
                    result.should.not.be.empty
                    name.should.not.be.empty
                    classname.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("cancels a job") {
            client.cancelJob("project-slug", "job-number").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets an organization") {
            client.getOrganization("organization-id").blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                slug.should.not.be.empty
                vcsType.should.not.be.empty
            }
        }

        it("gets a pipeline by id") {
            client.getPipelineById("pipeline-id").blockingGet().run {
                id.should.not.be.empty
                errors.should.be.empty
                projectSlug.should.not.be.empty
                number.should.be.above(0)
                state.should.not.be.empty
                createdAt.should.not.be.empty
                trigger.run {
                    type.should.not.be.empty
                    receivedAt.should.not.be.empty
                    actor.run {
                        login.should.not.be.empty
                    }
                }
                vcs?.run {
                    providerName.should.not.be.empty
                    targetRepositoryUrl.should.not.be.empty
                    revision.should.not.be.empty
                    originRepositoryUrl.should.not.be.empty
                }
            }
        }

        it("gets a pipeline by number") {
            client.getPipelineByNumber("project-slug", 1).blockingGet().run {
                id.should.not.be.empty
                errors.should.be.empty
                projectSlug.should.not.be.empty
                number.should.be.above(0)
                state.should.not.be.empty
                createdAt.should.not.be.empty
                trigger.run {
                    type.should.not.be.empty
                    receivedAt.should.not.be.empty
                    actor.run {
                        login.should.not.be.empty
                    }
                }
                vcs?.run {
                    providerName.should.not.be.empty
                    targetRepositoryUrl.should.not.be.empty
                    revision.should.not.be.empty
                    originRepositoryUrl.should.not.be.empty
                }
            }
        }

        it("gets a pipeline's configuration") {
            client.getPipelineConfigById("pipeline-id").blockingGet().run {
                source.should.not.be.empty
                compiled.should.not.be.empty
            }
        }

        it("gets a pipeline's workflows") {
            client.getPipelineWorkflows("pipeline-id").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    name.should.not.be.empty
                    projectSlug.should.not.be.empty
                    status.should.not.be.empty
                    createdAt.should.not.be.empty
                    pipelineId.should.not.be.empty
                    pipelineNumber.should.be.above(0)
                    startedBy.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("gets project's pipelines") {
            client.getProjectPipelines("project-slug", "branch").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    errors.should.be.empty
                    projectSlug.should.not.be.empty
                    number.should.be.above(0)
                    state.should.not.be.empty
                    createdAt.should.not.be.empty
                    trigger.run {
                        type.should.not.be.empty
                        receivedAt.should.not.be.empty
                        actor.run {
                            login.should.not.be.empty
                        }
                    }
                    vcs?.run {
                        providerName.should.not.be.empty
                        targetRepositoryUrl.should.not.be.empty
                        revision.should.not.be.empty
                        originRepositoryUrl.should.not.be.empty
                    }
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("continues a pipeline") {
            client.continuePipeline(null).blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("triggers a new pipeline") {
            client.triggerNewPipeline("project-slug", null).blockingGet().run {
                number.should.be.above(0)
                state.should.not.be.empty
                id.should.not.be.empty
                createdAt.should.not.be.empty
            }
        }

        it("gets a project by slug") {
            client.getProjectBySlug("project-slug").blockingGet().run {
                slug.should.not.be.empty
                name.should.not.be.empty
                id.should.not.be.empty
                organizationName.should.not.be.empty
                organizationSlug.should.not.be.empty
                organizationId.should.not.be.empty
                vcsInfo.run {
                    vcsUrl.should.not.be.empty
                    provider.should.not.be.empty
                    defaultBranch.should.not.be.empty
                }
            }
        }

        it("gets a project's checkout keys") {
            client.getProjectCheckoutKeys("project-slug").blockingGet().run {
                items?.first()?.run {
                    publicKey.should.not.be.empty
                    type.should.not.be.empty
                    fingerprint.should.not.be.empty
                    preferred.should.be.`false`
                    createdAt.should.not.be.null
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("creates a project's checkout key") {
            client.createProjectCheckoutKey("project-slug", null).blockingGet().run {
                publicKey.should.not.be.empty
                type.should.not.be.empty
                fingerprint.should.not.be.empty
                preferred.should.be.`false`
                createdAt.should.not.be.null
            }
        }

        it("gets a project's checkout key") {
            client.getProjectCheckoutKey("project-slug", "fingerprint").blockingGet().run {
                publicKey.should.not.be.empty
                type.should.not.be.empty
                fingerprint.should.not.be.empty
                preferred.should.be.`false`
                createdAt.should.not.be.null
            }
        }

        it("deletes a project's checkout key") {
            client.deleteProjectCheckoutKey("project-slug", "fingerprint").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets a project's environment variables") {
            client.getProjectEnvironmentVariables("project-slug").blockingGet().run {
                items.first().run {
                    name.should.not.be.empty
                    value.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("creates a project's environment variable") {
            client.createProjectEnvironmentVariable("project-slug", null).blockingGet().run {
                name.should.not.be.empty
                value.should.not.be.empty
            }
        }

        it("deletes a project's environment variable") {
            client.deleteProjectEnvironmentVariable("project-slug", "name").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets project settings") {
            client.getProjectSettings("project-slug").blockingGet().run {
                advanced.run {
                    autocancelBuilds.should.not.be.null
                    buildForkPrs.should.not.be.null
                    buildPrsOnly.should.not.be.null
                    disableSsh.should.not.be.null
                    forksReceiveSecretEnvVars.should.not.be.null
                    oss.should.not.be.null
                    setGithubStatus.should.not.be.null
                    setupWorkflows.should.not.be.null
                    writeSettingsRequiresAdmin.should.not.be.null
                    prOnlyBranchOverrides.should.not.be.null
                }
            }
        }

        it("gets a schedule by id") {
            client.getScheduleById("schedule-id").blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                description.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                projectSlug.should.not.be.empty
                parameters.should.not.be.empty
                actor.run {
                    id.should.not.be.empty
                    login.should.not.be.empty
                    name.should.not.be.empty
                }
                timetables.first().run {
                    id.should.not.be.empty
                    attribution.should.not.be.empty
                    days.should.not.be.empty
                    hours.should.not.be.empty
                    minutes.should.not.be.empty
                    perHour.should.be.above(-1)
                }
            }
        }

        it("gets project schedules") {
            client.getProjectSchedules("project-slug", "page-token").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    name.should.not.be.empty
                    description.should.not.be.empty
                    createdAt.should.not.be.empty
                    updatedAt.should.not.be.empty
                    projectSlug.should.not.be.empty
                    parameters.should.not.be.empty
                    actor.run {
                        id.should.not.be.empty
                        login.should.not.be.empty
                        name.should.not.be.empty
                    }
                    timetables.first().run {
                        id.should.not.be.empty
                        attribution.should.not.be.empty
                        days.should.not.be.empty
                        hours.should.not.be.empty
                        minutes.should.not.be.empty
                        perHour.should.be.above(-1)
                    }
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("creates a schedule") {
            client.createSchedule("project-slug", null).blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                description.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                projectSlug.should.not.be.empty
                parameters.should.not.be.empty
                actor.run {
                    id.should.not.be.empty
                    login.should.not.be.empty
                    name.should.not.be.empty
                }
                timetables.first().run {
                    id.should.not.be.empty
                    attribution.should.not.be.empty
                    days.should.not.be.empty
                    hours.should.not.be.empty
                    minutes.should.not.be.empty
                    perHour.should.be.above(-1)
                }
            }
        }

        it("updates a schedule") {
            client.updateSchedule("schedule-id", null).blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                description.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                projectSlug.should.not.be.empty
                parameters.should.not.be.empty
                actor.run {
                    id.should.not.be.empty
                    login.should.not.be.empty
                    name.should.not.be.empty
                }
                timetables.first().run {
                    id.should.not.be.empty
                    attribution.should.not.be.empty
                    days.should.not.be.empty
                    hours.should.not.be.empty
                    minutes.should.not.be.empty
                    perHour.should.be.above(-1)
                }
            }
        }

        it("deletes a schedule") {
            client.deleteSchedule("schedule-id").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets a webhook by id") {
            client.getWebhookById("webhook-id").blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                url.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                signingSecret.should.not.be.empty
                scope.run {
                    id.should.not.be.empty
                    type.should.not.be.empty
                }
                events.should.not.be.empty
            }
        }

        it("gets webhooks") {
            client.getWebhooks("scope-id", "scope-type").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    name.should.not.be.empty
                    url.should.not.be.empty
                    createdAt.should.not.be.empty
                    updatedAt.should.not.be.empty
                    signingSecret.should.not.be.empty
                    scope.run {
                        id.should.not.be.empty
                        type.should.not.be.empty
                    }
                    events.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("creates a webhook") {
            client.createWebhook(null).blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                url.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                signingSecret.should.not.be.empty
                scope.run {
                    id.should.not.be.empty
                    type.should.not.be.empty
                }
                events.should.not.be.empty
            }
        }

        it("updates a webhook") {
            client.updateWebhook("webhook-id", null).blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                url.should.not.be.empty
                createdAt.should.not.be.empty
                updatedAt.should.not.be.empty
                signingSecret.should.not.be.empty
                scope.run {
                    id.should.not.be.empty
                    type.should.not.be.empty
                }
                events.should.not.be.empty
            }
        }

        it("deletes a webhook") {
            client.deleteWebhook("webhook-id").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("gets a workflow by id") {
            client.getWorkflowById("workflow-id").blockingGet().run {
                id.should.not.be.empty
                name.should.not.be.empty
                projectSlug.should.not.be.empty
                status.should.not.be.empty
                createdAt.should.not.be.empty
                pipelineId.should.not.be.empty
                pipelineNumber.should.be.above(0)
                startedBy.should.not.be.empty
            }
        }

        it("gets a workflow's jobs") {
            client.getWorkflowJobs("workflow-id").blockingGet().run {
                items.first().run {
                    id.should.not.be.empty
                    name.should.not.be.empty
                    type.should.not.be.empty
                    status.should.not.be.empty
                    startedAt.should.not.be.empty
                    dependencies.should.not.be.empty
                }
                nextPageToken.should.not.be.empty
            }
        }

        it("cancels a workflow") {
            client.cancelWorkflow("workflow-id").blockingGet().run {
                message.should.not.be.empty
            }
        }

        it("reruns a workflow") {
            client.rerunWorkflow("workflow-id", null).blockingGet().run {
                workflowId.should.not.be.empty
            }
        }

        it("approves a job") {
            client.approveJob("workflow-id", "approval-request-id").blockingGet().run {
                message.should.not.be.empty
            }
        }
    }
})
