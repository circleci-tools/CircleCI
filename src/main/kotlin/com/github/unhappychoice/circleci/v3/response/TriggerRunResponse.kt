package com.github.unhappychoice.circleci.v3.response

data class TriggerRunResponse(val data: Data) {
    data class Data(val id: String)
}
