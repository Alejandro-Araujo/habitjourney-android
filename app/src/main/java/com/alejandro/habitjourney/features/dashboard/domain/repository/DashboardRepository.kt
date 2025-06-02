package com.alejandro.habitjourney.features.dashboard.domain.repository

import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardData(): Flow<Result<DashboardData>>
    suspend fun refreshDashboardData()
}