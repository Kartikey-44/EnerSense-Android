package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnomalyDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("simulatedTimestamp") val simulatedTimestamp: String? = null,
    @SerializedName("currentConsumptionWh") val currentConsumptionWh: Double? = null,
    @SerializedName("typicalConsumptionWh") val typicalConsumptionWh: Double? = null,
    @SerializedName("differenceWh") val differenceWh: Double? = null,
    @SerializedName("percentageAboveTypical") val percentageAboveTypical: Double? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("isNewEpisode") val isNewEpisode: Boolean? = null,
    @SerializedName("userMessage") val userMessage: String? = null
)
