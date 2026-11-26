package com.github.unhappychoice.circleci.v1.request

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class TriggerNewBuildWithBranchRequest(
  val revision: String? = null,
  val parallel: Int? = null,
  @Redacted val buildParameters: Map<String, Any?>? = null
) {
  override fun toString() = redactedToString(this)
}
