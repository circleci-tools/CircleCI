package com.github.unhappychoice.circleci.v1.request

data class TriggerNewBuildRequest(
  val tag: String? = null,
  val revision: String? = null,
  val parallel: Int? = null,
  val buildParameters: Map<String, Any?>? = null
)