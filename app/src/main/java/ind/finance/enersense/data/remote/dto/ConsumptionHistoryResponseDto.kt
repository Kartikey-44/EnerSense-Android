package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConsumptionHistoryResponseDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,
    @SerializedName("from") val from: String? = null,
    @SerializedName("to") val to: String? = null,
    @SerializedName("days") val days: List<ConsumptionDto>? = null
)
