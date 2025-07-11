package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class ProjectSettings(
    val advanced: AdvancedSettings
) {
    data class AdvancedSettings(
        @SerializedName("autocancel_builds")
        val autocancelBuilds: Boolean?,
        @SerializedName("build_fork_prs")
        val buildForkPrs: Boolean?,
        @SerializedName("build_prs_only")
        val buildPrsOnly: Boolean?,
        @SerializedName("disable_ssh")
        val disableSsh: Boolean?,
        @SerializedName("forks_receive_secret_env_vars")
        val forksReceiveSecretEnvVars: Boolean?,
        val oss: Boolean?,
        @SerializedName("set_github_status")
        val setGithubStatus: Boolean?,
        @SerializedName("setup_workflows")
        val setupWorkflows: Boolean?,
        @SerializedName("write_settings_requires_admin")
        val writeSettingsRequiresAdmin: Boolean?,
        @SerializedName("pr_only_branch_overrides")
        val prOnlyBranchOverrides: List<String>?
    )
}