package com.github.unhappychoice.circleci.v1.response

data class BuildStep(
  val name: String,
  val actions: List<BuildAction>
)
