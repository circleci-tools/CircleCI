package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class JobDetails(
    val name: String,
    val number: Long,
    val status: String?,
    val duration: Long?,
    @SerializedName("web_url") val webUrl: String,
    val project: Project,
    val executor: Executor,
    val parallelism: Long,
    @SerializedName("parallel_runs") val parallelRuns: List<ParallelRun>,
    @SerializedName("latest_workflow") val latestWorkflow: LatestWorkflow,
    val pipeline: Pipeline,
    val organization: Organization,
    val contexts: List<Context>,
    val messages: List<Message>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("queued_at") val queuedAt: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("stopped_at") val stoppedAt: String?
) {
    data class Project(
        val id: String?,
        val slug: String,
        val name: String,
        @SerializedName("external_url") val externalUrl: String
    )

    data class Executor(
        val type: String,
        @SerializedName("resource_class") val resourceClass: String
    )

    data class ParallelRun(val index: Long, val status: String)

    data class LatestWorkflow(val id: String, val name: String)

    data class Pipeline(val id: String)

    data class Organization(val name: String)

    data class Context(val name: String)

    data class Message(val type: String, val message: String, val reason: String?)
}
