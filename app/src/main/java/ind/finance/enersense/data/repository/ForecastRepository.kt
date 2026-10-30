package ind.finance.enersense.data.repository

import ind.finance.enersense.data.remote.ApiService
import ind.finance.enersense.data.remote.dto.ForecastResponseDto
import ind.finance.enersense.utils.NetworkResult
import ind.finance.enersense.utils.safeApiCall

class ForecastRepository(private val apiService: ApiService) {

    suspend fun getTomorrowForecast(consumerId: String, meterId: String): NetworkResult<ForecastResponseDto> {
        return safeApiCall { apiService.getTomorrowForecast(consumerId, meterId) }
    }
}
