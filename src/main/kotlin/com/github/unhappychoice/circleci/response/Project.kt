package com.github.unhappychoice.circleci.response

data class Project(
  val parallel: Int,
  val reponame: String,
  val username: String,
  val vcsUrl: String,
  val branches: Map<String, Any>
)
