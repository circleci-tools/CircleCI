package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v3.request.TriggerRunRequest
import com.github.unhappychoice.circleci.v3.response.RunResponse
import com.github.unhappychoice.circleci.v3.response.TriggerRunResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CircleCIAPIClientV3 {
    @POST("runs")
    fun triggerRun(
        @Body request: TriggerRunRequest,
        @Query("filter[project_id]") projectId: String? = null
    ): Observable<TriggerRunResponse>

    @GET("runs/{id}")
    fun getRun(@Path("id") id: String): Observable<RunResponse>
}
