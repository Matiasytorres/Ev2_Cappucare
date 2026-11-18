package com.duoc.cappucare.data.remote

import com.duoc.cappucare.data.model.GlobalHealthStats
import retrofit2.http.GET

interface HealthApiService {
    @GET("v3/covid-19/all")
    suspend fun getGlobalStats(): GlobalHealthStats
}