package com.github.unhappychoice.circleci.v1.response

data class Commit(
  val authorDate: String?,
  val authorEmail: String?,
  val authorLogin: String?,
  val authorName: String?,
  val body: String?,
  val branch: String?,
  val commit: String?,
  val commitUrl: String?,
  val committerDate: String?,
  val committerEmail: String?,
  val committerLogin: String?,
  val committerName: String?,
  val subject: String?
)