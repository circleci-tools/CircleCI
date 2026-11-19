package com.github.unhappychoice.circleci.v3.response

import com.google.gson.annotations.SerializedName

data class RunResponse(val data: Data) {
    data class Data(val id: String, val attributes: Attributes)

    data class Attributes(
        val phase: String,
        @SerializedName("current_outcome") val currentOutcome: String?
    )
}
