package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SimulationStateDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,
    @SerializedName("simulatedDate", alternate = ["currentDate", "date"]) val currentDate: String? = null,
    @SerializedName("simulatedTimestamp", alternate = ["time"]) val time: String? = null,
    @SerializedName("currentConsumptionWh", alternate = ["currentReading", "consumption", "value"]) val currentReading: Double? = null,
    @SerializedName("position") val position: Int? = null,
    @SerializedName("readingId") val readingId: Long? = null,
    @SerializedName("endOfSimulation", alternate = ["completed"]) val completed: Boolean? = null
) {
    val running: Boolean? get() = completed?.not()
    val consumption: Double? get() = currentReading
    val readingIndex: Int? get() = position
}
