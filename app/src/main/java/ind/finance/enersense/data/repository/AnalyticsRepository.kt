package ind.finance.enersense.data.repository

import ind.finance.enersense.data.remote.ApiService
import ind.finance.enersense.data.remote.dto.AnalyticsBucketDto
import ind.finance.enersense.utils.NetworkResult
import ind.finance.enersense.utils.safeApiCall

class AnalyticsRepository(private val apiService: ApiService) {

    suspend fun getHourlyAnalytics(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<AnalyticsBucketDto>> {
        val result = safeApiCall { apiService.getHourlyAnalytics(consumerId, meterId, from, to) }
        return unwrapBuckets(result)
    }

    suspend fun getDailyAnalytics(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<AnalyticsBucketDto>> {
        val result = safeApiCall { apiService.getDailyAnalytics(consumerId, meterId, from, to) }
        return unwrapBuckets(result)
    }

    suspend fun getWeeklyAnalytics(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<AnalyticsBucketDto>> {
        val result = safeApiCall { apiService.getWeeklyAnalytics(consumerId, meterId, from, to) }
        return unwrapBuckets(result)
    }

    suspend fun getMonthlyAnalytics(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<AnalyticsBucketDto>> {
        val result = safeApiCall { apiService.getMonthlyAnalytics(consumerId, meterId, from, to) }
        return unwrapBuckets(result)
    }

    suspend fun getYearlyAnalytics(
        consumerId: String,
        meterId: String,
        from: String? = null,
        to: String? = null
    ): NetworkResult<List<AnalyticsBucketDto>> {
        val result = safeApiCall { apiService.getYearlyAnalytics(consumerId, meterId, from, to) }
        return unwrapBuckets(result)
    }

    private fun unwrapBuckets(result: NetworkResult<ind.finance.enersense.data.remote.dto.AnalyticsResponseDto>): NetworkResult<List<AnalyticsBucketDto>> {
        return when (result) {
            is NetworkResult.Success -> {
                val list = result.data.buckets ?: emptyList()
                if (list.isEmpty()) NetworkResult.Empty else NetworkResult.Success(list)
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
            is NetworkResult.Empty -> NetworkResult.Empty
        }
    }
}
