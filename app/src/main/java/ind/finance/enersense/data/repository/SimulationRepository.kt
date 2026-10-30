package ind.finance.enersense.data.repository

import ind.finance.enersense.data.remote.ApiService
import ind.finance.enersense.data.remote.dto.SimulationStateDto
import ind.finance.enersense.utils.NetworkResult
import ind.finance.enersense.utils.safeApiCall

class SimulationRepository(private val apiService: ApiService) {

    suspend fun startSimulation(consumerId: String, meterId: String): NetworkResult<SimulationStateDto> {
        return safeApiCall { apiService.startSimulation(consumerId, meterId) }
    }

    suspend fun advanceSimulation(consumerId: String, meterId: String): NetworkResult<SimulationStateDto> {
        return safeApiCall { apiService.advanceSimulation(consumerId, meterId) }
    }

    suspend fun getSimulationState(consumerId: String, meterId: String): NetworkResult<SimulationStateDto> {
        return safeApiCall { apiService.getSimulationState(consumerId, meterId) }
    }
}
