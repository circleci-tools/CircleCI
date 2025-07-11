package com.github.unhappychoice.circleci.v2.response

data class FlakyTests(
    val `flaky-tests`: List<FlakyTest>
) {
    data class FlakyTest(
        val `time-wasted`: Long,
        val `workflow-created-at`: String,
        val `workflow-id`: String,
        val classname: String?,
        val `pipeline-number`: Long,
        val `workflow-name`: String,
        val `test-name`: String,
        val `job-name`: String,
        val `job-number`: Long,
        val `pipeline-id`: String,
        val `rerun-workflow-id`: String?,
        val `test-result`: String,
        val `workflow-rerun-number`: Long?
    )
}