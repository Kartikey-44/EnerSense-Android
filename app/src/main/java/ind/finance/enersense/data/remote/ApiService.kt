package ind.finance.enersense.data.remote

import ind.finance.enersense.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/consumption/today")
    suspend fun getTodayConsumption(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<ConsumptionDto>

    @GET("api/consumption/history")
    suspend fun getConsumptionHistory(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ConsumptionHistoryResponseDto>

    @GET("api/consumption/{date}")
    suspend fun getConsumptionByDate(
        @Path("date") date: String,
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<ConsumptionDto>

    @GET("api/analytics/hourly")
    suspend fun getHourlyAnalytics(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<AnalyticsResponseDto>

    @GET("api/analytics/daily")
    suspend fun getDailyAnalytics(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<AnalyticsResponseDto>

    @GET("api/analytics/weekly")
    suspend fun getWeeklyAnalytics(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<AnalyticsResponseDto>

    @GET("api/analytics/monthly")
    suspend fun getMonthlyAnalytics(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<AnalyticsResponseDto>

    @GET("api/analytics/yearly")
    suspend fun getYearlyAnalytics(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<AnalyticsResponseDto>

    @GET("api/forecast/tomorrow")
    suspend fun getTomorrowForecast(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<ForecastResponseDto>

    @POST("api/simulation/start")
    suspend fun startSimulation(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<SimulationStateDto>

    @POST("api/simulation/advance")
    suspend fun advanceSimulation(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<SimulationStateDto>

    @GET("api/simulation/state")
    suspend fun getSimulationState(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<SimulationStateDto>

    @GET("api/anomaly/current")
    suspend fun getCurrentAnomaly(
        @Query("consumerId") consumerId: String,
        @Query("meterId") meterId: String
    ): Response<AnomalyDto>
}
