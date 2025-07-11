package com.github.unhappychoice.circleci.v1.response

data class Artifact(
  val nodeIndex: Int,
  val path: String,
  val prettyPath: String,
  val url: String
)