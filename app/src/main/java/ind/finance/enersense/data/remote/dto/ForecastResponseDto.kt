package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,
    @SerializedName("forecastDate", alternate = ["date"]) val forecastDate: String? = null,
    @SerializedName("predictedConsumptionWh", alternate = ["predictedConsumption", "forecastValue"]) val predictedConsumption: Double? = null,
    @SerializedName("modelName", alternate = ["method"]) val modelName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    val forecastValue: Double? get() = predictedConsumption
    val date: String? get() = forecastDate
    val method: String? get() = modelName
}
