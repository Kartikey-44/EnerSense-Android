package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Next-day prediction with its 24-hour profile.
 *
 * Primary names match the current backend; `alternate` keeps v1 names working.
 */
data class ForecastResponseDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,

    @SerializedName("forecastDate", alternate = ["date"])
    val forecastDate: String? = null,

    @SerializedName("predictedWh", alternate = ["predictedConsumptionWh", "predictedConsumption", "forecastValue"])
    val predictedConsumption: Double? = null,

    /** Human-readable model name, e.g. "Persistence (previous day)". */
    @SerializedName("model", alternate = ["modelName"])
    val modelName: String? = null,

    @SerializedName("generatedAt", alternate = ["createdAt"])
    val createdAt: String? = null,

    @SerializedName("hourly") val hourly: List<HourlyForecastDto>? = null
) {
    val forecastValue: Double? get() = predictedConsumption
    val date: String? get() = forecastDate
    val method: String? get() = modelName
}
