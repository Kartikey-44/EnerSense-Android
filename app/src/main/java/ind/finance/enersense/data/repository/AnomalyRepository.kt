package ind.finance.enersense.data.repository

import ind.finance.enersense.data.remote.ApiService
import ind.finance.enersense.data.remote.dto.AnomalyDto
import ind.finance.enersense.utils.NetworkResult
import ind.finance.enersense.utils.safeApiCall

class AnomalyRepository(private val apiService: ApiService) {

    suspend fun getCurrentAnomaly(consumerId: String, meterId: String): NetworkResult<AnomalyDto> {
        return safeApiCall { apiService.getCurrentAnomaly(consumerId, meterId) }
    }
}
