package com.github.unhappychoice.circleci


import com.github.unhappychoice.circleci.v2.request.*
import com.github.unhappychoice.circleci.v2.response.*
import com.winterbe.expekt.expect
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.spekframework.spek2.Spek

object CircleCIAPIClientV2Spec: Spek({
  lateinit var clientV2: CircleCIAPIClientV2

  group("CircleCIAPIClientV2") {
    beforeEachTest {
      clientV2 = MockCircleCIAPIClientV2()
      RxJavaPlugins.onComputationScheduler(TestScheduler())
      RxJavaPlugins.onIoScheduler(TestScheduler())
      RxJavaPlugins.onNewThreadScheduler(TestScheduler())
    }

    test("#getMe() should return response") {
      val subscriber = clientV2.getMe().test()
      expect(subscriber.isFinished()).to.be.`true`

      val user = subscriber.values().first()
      expectNotNull(user)
    }

    test("#getProjects() should return response") {
      val subscriber = clientV2.getProjects().test()
      expect(subscriber.isFinished()).to.be.`true`

      val projects = subscriber.values().first()
      expect(projects).not.to.be.empty
      expectNotNull(projects.first())
    }

    test("#getProject() should return response") {
      val subscriber = clientV2.getProject("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val project = subscriber.values().first()
      expectNotNull(project)
    }

    test("#getProjectByVcsType() should return response") {
      val subscriber = clientV2.getProjectByVcsType("github", "unhappychoice", "circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val project = subscriber.values().first()
      expectNotNull(project)
    }

    test("#getProjectSettings() should return response") {
      val subscriber = clientV2.getProjectSettings("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val settings = subscriber.values().first()
      expectNotNull(settings)
    }

    test("#getWorkflow() should return response") {
      val subscriber = clientV2.getWorkflow("workflow-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val workflow = subscriber.values().first()
      expectNotNull(workflow)
    }

    test("#getWorkflowJobs() should return response") {
      val subscriber = clientV2.getWorkflowJobs("workflow-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val jobs = subscriber.values().first()
      expect(jobs).not.to.be.empty
      expectNotNull(jobs.first())
    }

    test("#getPipelines() should return response") {
      val subscriber = clientV2.getPipelines().test()
      expect(subscriber.isFinished()).to.be.`true`

      val pipelines = subscriber.values().first()
      expect(pipelines).not.to.be.empty
      expectNotNull(pipelines.first())
    }

    test("#getPipelineById() should return response") {
      val subscriber = clientV2.getPipelineById("pipeline-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val pipeline = subscriber.values().first()
      expectNotNull(pipeline)
    }

    test("#getProjectPipelines() should return response") {
      val subscriber = clientV2.getProjectPipelines("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val pipelines = subscriber.values().first()
      expect(pipelines).not.to.be.empty
      expectNotNull(pipelines.first())
    }

    test("#getPipelineWorkflows() should return response") {
      val subscriber = clientV2.getPipelineWorkflows("pipeline-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val workflows = subscriber.values().first()
      expect(workflows).not.to.be.empty
      expectNotNull(workflows.first())
    }

    test("#triggerNewPipeline() should return response") {
      val request = TriggerNewPipelineRequest()
      val subscriber = clientV2.triggerNewPipeline("gh/unhappychoice/circleci", request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val pipeline = subscriber.values().first()
      expectNotNull(pipeline)
    }

    test("#getPipelineByNumber() should return response") {
      val subscriber = clientV2.getPipelineByNumber("gh/unhappychoice/circleci", 1).test()
      expect(subscriber.isFinished()).to.be.`true`

      val pipeline = subscriber.values().first()
      expectNotNull(pipeline)
    }

    test("#getJobDetails() should return response") {
      val subscriber = clientV2.getJobDetails("job-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val job = subscriber.values().first()
      expectNotNull(job)
    }

    test("#cancelJob() should return response") {
      val subscriber = clientV2.cancelJob("job-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val job = subscriber.values().first()
      expectNotNull(job)
    }

    test("#getJobArtifacts() should return response") {
      val subscriber = clientV2.getJobArtifacts("job-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val artifacts = subscriber.values().first()
      expect(artifacts).not.to.be.empty
      expectNotNull(artifacts.first())
    }

    test("#getProjectWorkflowMetrics() should return response") {
      val subscriber = clientV2.getProjectWorkflowMetrics("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val metrics = subscriber.values().first()
      expectNotNull(metrics)
    }

    test("#getWorkflowMetrics() should return response") {
      val subscriber = clientV2.getWorkflowMetrics("gh/unhappychoice/circleci", "workflow-name").test()
      expect(subscriber.isFinished()).to.be.`true`

      val metrics = subscriber.values().first()
      expectNotNull(metrics)
    }

    test("#getWorkflowRuns() should return response") {
      val subscriber = clientV2.getWorkflowRuns("gh/unhappychoice/circleci", "workflow-name").test()
      expect(subscriber.isFinished()).to.be.`true`

      val runs = subscriber.values().first()
      expect(runs).not.to.be.empty
      expectNotNull(runs.first())
    }

    test("#getProjectJobMetrics() should return response") {
      val subscriber = clientV2.getProjectJobMetrics("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val metrics = subscriber.values().first()
      expectNotNull(metrics)
    }

    test("#getJobMetrics() should return response") {
      val subscriber = clientV2.getJobMetrics("gh/unhappychoice/circleci", "job-name").test()
      expect(subscriber.isFinished()).to.be.`true`

      val metrics = subscriber.values().first()
      expectNotNull(metrics)
    }

    test("#getJobRuns() should return response") {
      val subscriber = clientV2.getJobRuns("gh/unhappychoice/circleci", "job-name").test()
      expect(subscriber.isFinished()).to.be.`true`

      val runs = subscriber.values().first()
      expect(runs).not.to.be.empty
      expectNotNull(runs.first())
    }

    test("#getContexts() should return response") {
      val subscriber = clientV2.getContexts().test()
      expect(subscriber.isFinished()).to.be.`true`

      val contexts = subscriber.values().first()
      expect(contexts).not.to.be.empty
      expectNotNull(contexts.first())
    }

    test("#getContext() should return response") {
      val subscriber = clientV2.getContext("context-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val context = subscriber.values().first()
      expectNotNull(context)
    }

    test("#getContextEnvironmentVariables() should return response") {
      val subscriber = clientV2.getContextEnvironmentVariables("context-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val envVars = subscriber.values().first()
      expect(envVars).not.to.be.empty
      expectNotNull(envVars.first())
    }

    test("#addContextEnvironmentVariable() should return response") {
      val request = AddEnvironmentVariableRequest("ENV_VAR_NAME", "ENV_VAR_VALUE")
      val subscriber = clientV2.addContextEnvironmentVariable("context-id", request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val envVar = subscriber.values().first()
      expectNotNull(envVar)
    }

    test("#updateContextEnvironmentVariable() should return response") {
      val request = AddEnvironmentVariableRequest("ENV_VAR_NAME", "NEW_ENV_VAR_VALUE")
      val subscriber = clientV2.updateContextEnvironmentVariable("context-id", "ENV_VAR_NAME", request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val envVar = subscriber.values().first()
      expectNotNull(envVar)
    }

    test("#deleteContextEnvironmentVariable() should return response") {
      val subscriber = clientV2.deleteContextEnvironmentVariable("context-id", "ENV_VAR_NAME").test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    test("#getSchedules() should return response") {
      val subscriber = clientV2.getSchedules().test()
      expect(subscriber.isFinished()).to.be.`true`

      val schedules = subscriber.values().first()
      expect(schedules).not.to.be.empty
      expectNotNull(schedules.first())
    }

    test("#getScheduleById() should return response") {
      val subscriber = clientV2.getScheduleById("schedule-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val schedule = subscriber.values().first()
      expectNotNull(schedule)
    }

    test("#getProjectSchedules() should return response") {
      val subscriber = clientV2.getProjectSchedules("gh/unhappychoice/circleci").test()
      expect(subscriber.isFinished()).to.be.`true`

      val schedules = subscriber.values().first()
      expect(schedules).not.to.be.empty
      expectNotNull(schedules.first())
    }

    test("#getWebhooks() should return response") {
      val subscriber = clientV2.getWebhooks().test()
      expect(subscriber.isFinished()).to.be.`true`

      val webhooks = subscriber.values().first()
      expect(webhooks).not.to.be.empty
      expectNotNull(webhooks.first())
    }

    test("#getWebhookById() should return response") {
      val subscriber = clientV2.getWebhookById("webhook-id").test()
      expect(subscriber.isFinished()).to.be.`true`

      val webhook = subscriber.values().first()
      expectNotNull(webhook)
    }

    test("#createWebhook() should return response") {
      val request = CreateWebhookRequest("webhook-name", "http://example.com/webhook", "signing-secret", WebhookScopeRequest("project", "webhook-scope-id"), listOf("workflow-completed"))
      val subscriber = clientV2.createWebhook(request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val webhook = subscriber.values().first()
      expectNotNull(webhook)
    }

    test("#updateWebhook() should return response") {
      val request = CreateWebhookRequest("updated-webhook-name", "http://example.com/updated-webhook", "updated-signing-secret", WebhookScopeRequest("project", "webhook-scope-id"), listOf("workflow-completed"))
      val subscriber = clientV2.updateWebhook("webhook-id", request).test()
      expect(subscriber.isFinished()).to.be.`true`

      val webhook = subscriber.values().first()
      expectNotNull(webhook)
    }

    test("#deleteWebhook() should return response") {
      val subscriber = clientV2.deleteWebhook("webhook-id").test()
      expect(subscriber.isFinished()).to.be.`true`
    }

    
  }
})

private fun <T> TestObserver<T>.isFinished(): Boolean {
  assertNoErrors()
  assertComplete()
  return true
}

private fun expectNotNull(user: User) {
  expect(user.id).not.to.be.`null`
  expect(user.name).not.to.be.`null`
  expect(user.login).not.to.be.`null`
  expect(user.avatarUrl).not.to.be.`null`
  expect(user.createdAt).not.to.be.`null`
}

private fun expectNotNull(project: Project) {
  expect(project.id).not.to.be.`null`
  expect(project.name).not.to.be.`null`
  expect(project.slug).not.to.be.`null`
  expect(project.organizationSlug).not.to.be.`null`
  expect(project.organizationName).not.to.be.`null`
  expect(project.vcsInfo.vcsUrl).not.to.be.`null`
}

private fun expectNotNull(settings: ProjectSettings) {
  expect(settings.advanced).not.to.be.`null`
  expect(settings.advanced.oss).not.to.be.`null`
  expect(settings.advanced.buildForkPrs).not.to.be.`null`
  expect(settings.advanced.buildPrsOnly).not.to.be.`null`
  expect(settings.advanced.disableSsh).not.to.be.`null`
  expect(settings.advanced.forksReceiveSecretEnvVars).not.to.be.`null`
  expect(settings.advanced.setGithubStatus).not.to.be.`null`
  expect(settings.advanced.setupWorkflows).not.to.be.`null`
  expect(settings.advanced.writeSettingsRequiresAdmin).not.to.be.`null`
}

private fun expectNotNull(workflow: Workflow) {
  expect(workflow.id).not.to.be.`null`
  expect(workflow.name).not.to.be.`null`
  expect(workflow.projectSlug).not.to.be.`null`
  expect(workflow.status).not.to.be.`null`
  expect(workflow.createdAt).not.to.be.`null`
  // expect(workflow.url).not.to.be.`null`
}

private fun expectNotNull(job: Job) {
  expect(job.id).not.to.be.`null`
  expect(job.name).not.to.be.`null`
  expect(job.type).not.to.be.`null`
  expect(job.status).not.to.be.`null`
  expect(job.startedAt).not.to.be.`null`
  expect(job.dependencies).not.to.be.`null`
}

private fun expectNotNull(pipeline: Pipeline) {
  expect(pipeline.id).not.to.be.`null`
  expect(pipeline.errors).not.to.be.`null`
  expect(pipeline.projectSlug).not.to.be.`null`
  expect(pipeline.createdAt).not.to.be.`null`
  expect(pipeline.vcs).not.to.be.`null`
  expect(pipeline.number).not.to.be.`null`
  expect(pipeline.state).not.to.be.`null`
  expect(pipeline.trigger).not.to.be.`null`
}

private fun expectNotNull(artifact: Artifact) {
  expect(artifact.path).not.to.be.`null`
  expect(artifact.url).not.to.be.`null`
  expect(artifact.nodeIndex).not.to.be.`null`
}

private fun expectNotNull(metrics: ProjectMetricsTimeSeries) {
  expect(metrics.metrics).not.to.be.`null`
  expect(metrics.trends).not.to.be.`null`
}

private fun expectNotNull(run: WorkflowRun) {
  expect(run.id).not.to.be.`null`
  expect(run.status).not.to.be.`null`
  expect(run.createdAt).not.to.be.`null`
  expect(run.duration).not.to.be.`null`
  expect(run.creditsUsed).not.to.be.`null`
  expect(run.isRerun).not.to.be.`null`
}

private fun expectNotNull(run: JobRun) {
  expect(run.id).not.to.be.`null`
  expect(run.status).not.to.be.`null`
  expect(run.startedAt).not.to.be.`null`
  expect(run.duration).not.to.be.`null`
  expect(run.creditsUsed).not.to.be.`null`
  expect(run.isRerun).not.to.be.`null`
}

private fun expectNotNull(context: Context) {
  expect(context.id).not.to.be.`null`
  expect(context.name).not.to.be.`null`
  expect(context.createdAt).not.to.be.`null`
  expect(context.organizationId).not.to.be.`null`
}

private fun expectNotNull(envVar: EnvironmentVariable) {
  expect(envVar.name).not.to.be.`null`
  expect(envVar.value).not.to.be.`null`
  expect(envVar.createdAt).not.to.be.`null`
  // expect(envVar.updatedAt).not.to.be.`null`
}

private fun expectNotNull(schedule: Schedule) {
  expect(schedule.id).not.to.be.`null`
  expect(schedule.description).not.to.be.`null`
  expect(schedule.name).not.to.be.`null`
  expect(schedule.createdAt).not.to.be.`null`
  
  expect(schedule.projectSlug).not.to.be.`null`
  expect(schedule.parameters).not.to.be.`null`
  expect(schedule.actor).not.to.be.`null`
  expect(schedule.timetables).not.to.be.`null`
}

private fun expectNotNull(webhook: Webhook) {
  expect(webhook.id).not.to.be.`null`
  expect(webhook.name).not.to.be.`null`
  expect(webhook.url).not.to.be.`null`
  expect(webhook.createdAt).not.to.be.`null`
  expect(webhook.updatedAt).not.to.be.`null`
  expect(webhook.signingSecret).not.to.be.`null`
  expect(webhook.scope).not.to.be.`null`
  expect(webhook.events).not.to.be.`null`
}


