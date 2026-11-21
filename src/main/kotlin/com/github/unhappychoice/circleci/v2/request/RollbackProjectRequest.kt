package com.github.unhappychoice.circleci.v2.request

import com.google.gson.annotations.SerializedName

data class RollbackProjectRequest(
    @SerializedName("environment_name") val environmentName: String,
    @SerializedName("component_name") val componentName: String,
    @SerializedName("current_version") val currentVersion: String,
    @SerializedName("target_version") val targetVersion: String,
    val reason: String? = null,
    val namespace: String? = null,
    val parameters: Map<String, Any>? = null
)
