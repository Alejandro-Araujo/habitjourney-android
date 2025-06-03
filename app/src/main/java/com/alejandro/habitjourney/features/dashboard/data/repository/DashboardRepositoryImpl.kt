package com.alejandro.habitjourney.features.dashboard.data.repository

import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import com.alejandro.habitjourney.features.dashboard.domain.repository.DashboardRepository
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : DashboardRepository {

    override fun getDashboardData(): Flow<Result<DashboardData>> {
        return getDashboardDataUseCase()
    }

    override suspend fun refreshDashboardData() {
    }
}