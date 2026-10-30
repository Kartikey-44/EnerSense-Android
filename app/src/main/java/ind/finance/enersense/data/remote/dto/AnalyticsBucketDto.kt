package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsBucketDto(
    @SerializedName("periodStart", alternate = ["period", "time", "date"]) val period: String? = null,
    @SerializedName("totalWh", alternate = ["consumption", "totalConsumption"]) val consumption: Double? = null,
    @SerializedName("readingCount") val readingCount: Long? = null,
    @SerializedName("averageDemand") val averageDemand: Double? = null,
    @SerializedName("peakDemand") val peakDemand: Double? = null
) {
    val totalConsumption: Double? get() = consumption
}
