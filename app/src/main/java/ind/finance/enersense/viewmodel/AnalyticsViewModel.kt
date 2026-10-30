package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.AnalyticsBucketDto
import ind.finance.enersense.data.repository.AnalyticsRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AnalyticsScale {
    HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val buckets: List<AnalyticsBucketDto>) : AnalyticsUiState()
    data class Empty(val message: String) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _analyticsState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val analyticsState: StateFlow<AnalyticsUiState> = _analyticsState.asStateFlow()

    var selectedScale: AnalyticsScale = AnalyticsScale.DAILY
    var fromDate: String = "2016-01-01"
    var toDate: String = "2018-01-31"

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = AnalyticsUiState.Loading
            val result = when (selectedScale) {
                AnalyticsScale.HOURLY -> analyticsRepository.getHourlyAnalytics(consumerId, meterId, fromDate, toDate)
                AnalyticsScale.DAILY -> analyticsRepository.getDailyAnalytics(consumerId, meterId, fromDate, toDate)
                AnalyticsScale.WEEKLY -> analyticsRepository.getWeeklyAnalytics(consumerId, meterId, fromDate, toDate)
                AnalyticsScale.MONTHLY -> analyticsRepository.getMonthlyAnalytics(consumerId, meterId, fromDate, toDate)
                AnalyticsScale.YEARLY -> analyticsRepository.getYearlyAnalytics(consumerId, meterId, fromDate, toDate)
            }

            when (result) {
                is NetworkResult.Success -> {
                    val list = result.data
                    if (list.isEmpty()) {
                        _analyticsState.value = AnalyticsUiState.Empty("Start Live Simulation to begin receiving meter readings.")
                    } else {
                        _analyticsState.value = AnalyticsUiState.Success(list)
                    }
                }
                is NetworkResult.Empty -> {
                    _analyticsState.value = AnalyticsUiState.Empty("Start Live Simulation to begin receiving meter readings.")
                }
                is NetworkResult.Error -> {
                    _analyticsState.value = AnalyticsUiState.Error(getUserFriendlyMessage(result.code))
                }
                is NetworkResult.Exception -> {
                    _analyticsState.value = AnalyticsUiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    private fun getUserFriendlyMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request parameters."
            401, 403 -> "Authentication error."
            404 -> "Analytics records not found."
            422 -> "Unprocessable data."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Unable to load analytics."
        }
    }
}
