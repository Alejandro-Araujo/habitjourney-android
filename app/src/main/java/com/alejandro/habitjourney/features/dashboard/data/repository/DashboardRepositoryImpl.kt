package com.alejandro.habitjourney.features.dashboard.data.repository

import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import com.alejandro.habitjourney.features.dashboard.domain.repository.DashboardRepository
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * Implementación concreta del repositorio de dashboard.
 *
 * Esta implementación actúa como una capa de abstracción sobre el use case
 * principal, proporcionando beneficios como:
 * - Separación clara entre lógica de negocio y acceso a datos
 * - Cumple con el principio de inversión de dependencias
 *
 * @param getDashboardDataUseCase Use case principal que contiene toda la lógica
 */
class DashboardRepositoryImpl @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : DashboardRepository {

    override fun getDashboardData(): Flow<Result<DashboardData>> {
        return getDashboardDataUseCase()
    }

    override suspend fun refreshDashboardData() {
    }
}