package com.github.unhappychoice.circleci.v1.response

import com.github.unhappychoice.circleci.Redacted
import com.github.unhappychoice.circleci.redactedToString
import java.util.*

data class BuildAction(
  val bashCommand: String?,
  val canceled: Boolean?,
  val endTime: Date?,
  val exitCode: Int?,
  val failed: Boolean?,
  val hasOutput: Boolean?,
  val infrastructureFail: Boolean?,
  val index: Int?,
  val name: String?,
  @Redacted val outputUrl: String?,
  val parallel: Boolean?,
  val runTimeMillis: Int?,
  val startTime: Date?,
  val status: String?,
  val step: Int?,
  val timedout: Boolean?,
  val truncated: Boolean?,
  val type: String?
) {
  override fun toString() = redactedToString(this)
}
