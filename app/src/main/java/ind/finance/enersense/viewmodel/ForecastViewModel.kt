package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.ForecastResponseDto
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        loadForecast()
    }

    fun loadForecast() {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            when (val result = forecastRepository.getTomorrowForecast(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    _forecastState.value = ForecastUiState.Success(result.data)
                }
                is NetworkResult.Empty -> {
                    _forecastState.value = ForecastUiState.Empty("Start Live Simulation and collect enough consumption data to generate a forecast.")
                }
                is NetworkResult.Error -> {
                    if (result.code == 422 || result.code == 404) {
                        _forecastState.value = ForecastUiState.Empty("Start Live Simulation and collect enough consumption data to generate a forecast.")
                    } else {
                        _forecastState.value = ForecastUiState.Error(getUserFriendlyMessage(result.code))
                    }
                }
                is NetworkResult.Exception -> {
                    _forecastState.value = ForecastUiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    private fun getUserFriendlyMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request parameters."
            401, 403 -> "Authentication error."
            404 -> "Forecast data not found."
            422 -> "Forecast is not available because there is not enough historical consumption data."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Unable to load forecast."
        }
    }
}
