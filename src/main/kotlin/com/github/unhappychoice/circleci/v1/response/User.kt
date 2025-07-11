package com.github.unhappychoice.circleci.v1.response

data class User(
  val avatarUrl: String,
  val login: String,
  val name: String,
  val projects: Map<String, Map<String, Any>>,
  val selectedEmail: String,
  val pusherId: String
)
