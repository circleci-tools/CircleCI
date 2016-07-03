package com.github.unhappychoice.circleci.request

data class TriggerNewBuildWithBranchRequest(
  val revision: String? = null,
  val parallel: Int? = null,
  val buildParameters: Map<String, Any?>? = null
)