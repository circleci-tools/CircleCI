package com.github.unhappychoice.circleci.v1.response

data class Project(
  val branches: Map<String, Any>?,
  val parallel: Int?,
  val reponame: String?,
  val username: String?,
  val vcsUrl: String?
)
