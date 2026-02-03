package com.github.unhappychoice.circleci.v1.response

data class TestMetadata(
    val message: String?,
    val file: String?,
    val source: String?,
    val run_time: Double?,
    val result: String?,
    val name: String?,
    val classname: String?
)
