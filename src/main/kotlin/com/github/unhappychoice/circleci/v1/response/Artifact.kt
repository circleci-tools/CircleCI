package com.github.unhappychoice.circleci.v1.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString

data class Artifact(
  val nodeIndex: Int,
  val path: String,
  val prettyPath: String,
  @Redacted val url: String
) {
  override fun toString() = redactedToString(this)
}
