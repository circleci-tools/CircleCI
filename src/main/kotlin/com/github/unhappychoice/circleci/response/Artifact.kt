package com.github.unhappychoice.circleci.response

data class Artifact(
  val path: String,
  val prettyPath: String,
  val nodeIndex: Int,
  val url: String
)