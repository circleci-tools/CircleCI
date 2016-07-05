package com.github.unhappychoice.circleci.response

data class User(
  val selectedEmail: String,
  val login: String,
  val name: String,
  val avatarUrl: String,
  val projects: Map<String, Map<String, Any>>
)
