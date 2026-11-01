package ind.finance.enersense.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsResponseDto(
    @SerializedName("consumerId") val consumerId: String? = null,
    @SerializedName("meterId") val meterId: String? = null,

    /** Echoed back by the backend: hourly, daily, weekly, monthly or yearly. */
    @SerializedName("granularity") val granularity: String? = null,

    @SerializedName("from") val from: String? = null,
    @SerializedName("to") val to: String? = null,
    @SerializedName("buckets") val buckets: List<AnalyticsBucketDto>? = null
)
