package com.duoc.cappucare.repository

import com.duoc.cappucare.data.model.GlobalHealthStats
import com.duoc.cappucare.data.remote.RetrofitInstance

class HealthRepository {
    suspend fun getGlobalStats(): Result<GlobalHealthStats> {
        return try {
            val stats = RetrofitInstance.healthApi.getGlobalStats()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}