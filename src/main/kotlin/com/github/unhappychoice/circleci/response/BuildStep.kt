package com.github.unhappychoice.circleci.response

data class BuildStep(
  val name: String,
  val actions: List<BuildAction>
)
