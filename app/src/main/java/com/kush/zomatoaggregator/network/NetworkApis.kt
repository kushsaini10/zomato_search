package com.kush.zomatoaggregator.network

import com.kush.zomatoaggregator.network.models.Model
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkApis {
    @GET(ApiEndpoints.SEARCH)
    fun search(
        @Query(ApiRequestParams.QUERY) query: String = "",
        @Query(ApiRequestParams.ENTITY_ID) entityId: Int = 1,
        @Query(ApiRequestParams.ENTITY_TYPE) entityType: String = "city",
        @Query(ApiRequestParams.START) start: Int = 0,
        @Query(ApiRequestParams.COUNT) count: Int = 20
    ): Observable<Model.SearchResponse>
}