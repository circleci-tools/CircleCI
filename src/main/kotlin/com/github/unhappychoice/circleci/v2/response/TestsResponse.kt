package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class TestsResponse(
    val items: List<Test>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class Test(
        val message: String,
        val source: String,
        @SerializedName("run_time") val runTime: Double,
        val file: String,
        val result: String,
        val name: String,
        val classname: String
    )
}
