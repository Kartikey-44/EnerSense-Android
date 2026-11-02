package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.ForecastResponseDto
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ForecastUiState {
    object Loading : ForecastUiState()
    data class Success(val forecast: ForecastResponseDto) : ForecastUiState()
    data class Empty(val message: String) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

class ForecastViewModel(
    private val forecastRepository: ForecastRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState: StateFlow<ForecastUiState> = _forecastState.asStateFlow()

    private var forecastJob: Job? = null

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        loadForecast()
        startForecastAutoRefresh()
    }

    fun loadForecast() {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            fetchForecastInternal()
        }
    }

    private fun startForecastAutoRefresh() {
        forecastJob?.cancel()
        forecastJob = viewModelScope.launch {
            while (isActive) {
                delay(2000L)
                fetchForecastSilent()
            }
        }
    }

    private suspend fun fetchForecastInternal() {
        when (val result = forecastRepository.getTomorrowForecast(consumerId, meterId)) {
            is NetworkResult.Success -> {
                _forecastState.value = ForecastUiState.Success(result.data)
            }
            is NetworkResult.Empty -> {
                _forecastState.value = ForecastUiState.Empty("Forecast unavailable for the current simulation window.")
            }
            is NetworkResult.Error -> {
                if (result.code == 422 || result.code == 404 || result.code == 400 || result.message.contains("NO_HISTORICAL_READINGS", ignoreCase = true)) {
                    _forecastState.value = ForecastUiState.Empty("Forecast unavailable for the current simulation window.")
                } else {
                    _forecastState.value = ForecastUiState.Error(getUserFriendlyMessage(result.code))
                }
            }
            is NetworkResult.Exception -> {
                _forecastState.value = ForecastUiState.Error("Network failure. Please check your connection.")
            }
        }
    }

    private suspend fun fetchForecastSilent() {
        when (val result = forecastRepository.getTomorrowForecast(consumerId, meterId)) {
            is NetworkResult.Success -> {
                _forecastState.value = ForecastUiState.Success(result.data)
            }
            is NetworkResult.Empty -> {
                if (_forecastState.value !is ForecastUiState.Success) {
                    _forecastState.value = ForecastUiState.Empty("Forecast unavailable for the current simulation window.")
                }
            }
            is NetworkResult.Error -> {
                if ((result.code == 422 || result.code == 404 || result.code == 400 || result.message.contains("NO_HISTORICAL_READINGS", ignoreCase = true)) && _forecastState.value !is ForecastUiState.Success) {
                    _forecastState.value = ForecastUiState.Empty("Forecast unavailable for the current simulation window.")
                }
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        forecastJob?.cancel()
    }

    private fun getUserFriendlyMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request parameters."
            401, 403 -> "Authentication error."
            404 -> "Forecast data not found."
            422 -> "Forecast unavailable for the current simulation window."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Unable to load forecast."
        }
    }
}
