package ind.finance.enersense.data.repository

import ind.finance.enersense.data.remote.ApiService
import ind.finance.enersense.data.remote.dto.ConsumptionDto
import ind.finance.enersense.utils.NetworkResult
import ind.finance.enersense.utils.safeApiCall

class ConsumptionRepository(private val apiService: ApiService) {

    suspend fun getTodayConsumption(consumerId: String, meterId: String): NetworkResult<ConsumptionDto> {
        return safeApiCall { apiService.getTodayConsumption(consumerId, meterId) }
    }

    suspend fun getConsumptionHistory(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<ConsumptionDto>> {
        val result = safeApiCall { apiService.getConsumptionHistory(consumerId, meterId, from, to) }
        return when (result) {
            is NetworkResult.Success -> {
                val list = result.data.days ?: emptyList()
                if (list.isEmpty()) NetworkResult.Empty else NetworkResult.Success(list)
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
            is NetworkResult.Empty -> NetworkResult.Empty
        }
    }

    suspend fun getConsumptionByDate(date: String, consumerId: String, meterId: String): NetworkResult<ConsumptionDto> {
        return safeApiCall { apiService.getConsumptionByDate(date, consumerId, meterId) }
    }
}
