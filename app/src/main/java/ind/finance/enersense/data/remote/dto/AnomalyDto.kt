package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Anomaly assessment for the reading under the simulation cursor.
 *
 * Primary names match the current backend; `alternate` keeps the older v1 field names
 * working, so the app binds against either without a code change.
 */
data class AnomalyDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,

    /** NORMAL, UNUSUAL, or LEARNING while the baseline is still being built. */
    @SerializedName("status") val status: String? = null,

    @SerializedName("at", alternate = ["simulatedTimestamp", "timestamp"])
    val simulatedTimestamp: String? = null,

    @SerializedName("currentWh", alternate = ["currentConsumptionWh"])
    val currentConsumptionWh: Double? = null,

    /** Mean of previous readings at this same hour, not a daily average. */
    @SerializedName("baselineMeanWh", alternate = ["typicalConsumptionWh"])
    val typicalConsumptionWh: Double? = null,

    @SerializedName("differenceWh") val differenceWh: Double? = null,

    @SerializedName("percentAboveBaseline", alternate = ["percentageAboveTypical"])
    val percentageAboveTypical: Double? = null,

    /** True only on the reading that opens an episode, so one spike alerts once. */
    @SerializedName("newEpisode", alternate = ["isNewEpisode"])
    val isNewEpisode: Boolean? = null,

    @SerializedName("message", alternate = ["userMessage"])
    val userMessage: String? = null
) {
    /**
     * The backend no longer sends a separate severity; status already carries it.
     * Derived here so existing UI code keeps working.
     */
    val severity: String? get() = status

    /** True while the detector lacks enough same-hour history to judge anything. */
    val isLearning: Boolean
        get() = status == "LEARNING" || status == "INSUFFICIENT_BASELINE"
}
