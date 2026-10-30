package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConsumptionDto(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("consumption") val consumption: Double? = null,
    @SerializedName("totalWh", alternate = ["totalConsumption"]) val totalConsumption: Double? = null,
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null
)
