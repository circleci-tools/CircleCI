package com.github.unhappychoice.circleci.v2.response

import com.google.gson.annotations.SerializedName

data class RollbackProjectResponse @JvmOverloads constructor(
    val message: String? = null,
    val id: String? = null,
    @SerializedName("rollback_type") val rollbackType: String? = null
)
