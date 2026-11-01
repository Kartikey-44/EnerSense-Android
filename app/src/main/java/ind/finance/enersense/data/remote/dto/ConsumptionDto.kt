package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * One day's consumption.
 *
 * `complete` matters: a day the cursor has only partly reached holds fewer than 24
 * readings, and its total is not comparable with a whole day's.
 */
data class ConsumptionDto(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("consumption") val consumption: Double? = null,

    @SerializedName("totalWh", alternate = ["totalConsumption"])
    val totalConsumption: Double? = null,

    @SerializedName("readingCount") val readingCount: Int? = null,

    /** True when all 24 hours of this day have been released by the cursor. */
    @SerializedName("complete") val complete: Boolean? = null,

    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null
)
