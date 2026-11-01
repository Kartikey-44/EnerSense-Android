package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * One hour of the predicted day.
 *
 * The 24 values sum exactly to the day's prediction — the final hour absorbs whatever
 * the other 23 lose to rounding, so a chart of these never disagrees with the headline.
 */
data class HourlyForecastDto(
    @SerializedName("timestamp") val timestamp: String? = null,

    @SerializedName("predictedWh", alternate = ["predictedConsumptionWh"])
    val predictedWh: Double? = null,

    /** How this hour was derived: historical share, or an even split when no profile exists. */
    @SerializedName("method") val method: String? = null
)
