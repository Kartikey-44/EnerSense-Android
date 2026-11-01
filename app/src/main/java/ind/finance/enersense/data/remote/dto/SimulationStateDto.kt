package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Where the replay cursor currently sits.
 *
 * Primary names match the current backend; `alternate` keeps v1 names working.
 */
data class SimulationStateDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,

    @SerializedName("currentDate", alternate = ["simulatedDate", "date"])
    val currentDate: String? = null,

    @SerializedName("currentTimestamp", alternate = ["simulatedTimestamp", "time"])
    val time: String? = null,

    @SerializedName("currentWh", alternate = ["currentConsumptionWh", "currentReading", "consumption", "value"])
    val currentReading: Double? = null,

    @SerializedName("position") val position: Int? = null,
    @SerializedName("readingId") val readingId: Long? = null,

    /** No further readings exist; the replay has run to the end of the series. */
    @SerializedName("endOfSeries", alternate = ["endOfSimulation", "completed"])
    val completed: Boolean? = null
) {
    val running: Boolean? get() = completed?.not()
    val consumption: Double? get() = currentReading
    val readingIndex: Int? get() = position
}
