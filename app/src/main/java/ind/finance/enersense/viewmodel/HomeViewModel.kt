package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.AnomalyDto
import ind.finance.enersense.data.remote.dto.ConsumptionDto
import ind.finance.enersense.data.remote.dto.ForecastResponseDto
import ind.finance.enersense.data.remote.dto.SimulationStateDto
import ind.finance.enersense.data.repository.AnomalyRepository
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String) : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}

class HomeViewModel(
    private val consumptionRepository: ConsumptionRepository,
    private val forecastRepository: ForecastRepository,
    private val simulationRepository: SimulationRepository,
    private val anomalyRepository: AnomalyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _consumptionState = MutableStateFlow<UiState<ConsumptionDto>>(UiState.Loading)
    val consumptionState: StateFlow<UiState<ConsumptionDto>> = _consumptionState.asStateFlow()

    private val _forecastState = MutableStateFlow<UiState<ForecastResponseDto>>(UiState.Loading)
    val forecastState: StateFlow<UiState<ForecastResponseDto>> = _forecastState.asStateFlow()

    private val _simulationState = MutableStateFlow<UiState<SimulationStateDto>>(UiState.Loading)
    val simulationState: StateFlow<UiState<SimulationStateDto>> = _simulationState.asStateFlow()

    private val _anomalyState = MutableStateFlow<UiState<AnomalyDto>>(UiState.Loading)
    val anomalyState: StateFlow<UiState<AnomalyDto>> = _anomalyState.asStateFlow()

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        fetchTodayConsumption()
        fetchTomorrowForecast()
        fetchSimulationState()
        fetchAnomaly()
    }

    fun fetchTodayConsumption() {
        viewModelScope.launch {
            _consumptionState.value = UiState.Loading
            when (val result = consumptionRepository.getTodayConsumption(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val wh = data.consumption ?: data.totalConsumption ?: 0.0
                    if (wh <= 0.0) {
                        _consumptionState.value = UiState.Empty("No consumption data available for today.\nThe available dataset contains historical consumption data.")
                    } else {
                        _consumptionState.value = UiState.Success(data)
                    }
                }
                is NetworkResult.Empty -> {
                    _consumptionState.value = UiState.Empty("No consumption data available for today.\nThe available dataset contains historical consumption data.")
                }
                is NetworkResult.Error -> {
                    _consumptionState.value = UiState.Error(getUserFriendlyMessage(result.code, result.message))
                }
                is NetworkResult.Exception -> {
                    _consumptionState.value = UiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    fun fetchTomorrowForecast() {
        viewModelScope.launch {
            _forecastState.value = UiState.Loading
            when (val result = forecastRepository.getTomorrowForecast(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    _forecastState.value = UiState.Success(result.data)
                }
                is NetworkResult.Empty -> {
                    _forecastState.value = UiState.Empty("No forecast available.")
                }
                is NetworkResult.Error -> {
                    _forecastState.value = UiState.Error(getUserFriendlyMessage(result.code, result.message))
                }
                is NetworkResult.Exception -> {
                    _forecastState.value = UiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    fun fetchSimulationState() {
        viewModelScope.launch {
            _simulationState.value = UiState.Loading
            when (val result = simulationRepository.getSimulationState(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    _simulationState.value = UiState.Success(result.data)
                }
                is NetworkResult.Empty -> {
                    _simulationState.value = UiState.Empty("Simulation not started")
                }
                is NetworkResult.Error -> {
                    if (result.code == 404) {
                        _simulationState.value = UiState.Empty("Simulation not started")
                    } else {
                        _simulationState.value = UiState.Error(getUserFriendlyMessage(result.code, result.message))
                    }
                }
                is NetworkResult.Exception -> {
                    _simulationState.value = UiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    fun fetchAnomaly() {
        viewModelScope.launch {
            _anomalyState.value = UiState.Loading
            when (val result = anomalyRepository.getCurrentAnomaly(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    _anomalyState.value = UiState.Success(result.data)
                }
                is NetworkResult.Empty -> {
                    _anomalyState.value = UiState.Empty("No anomaly data available.")
                }
                is NetworkResult.Error -> {
                    _anomalyState.value = UiState.Error(getUserFriendlyMessage(result.code, result.message))
                }
                is NetworkResult.Exception -> {
                    _anomalyState.value = UiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    fun refreshAnomaly() {
        fetchAnomaly()
    }

    private fun getUserFriendlyMessage(code: Int, message: String): String {
        return when (code) {
            400 -> "Invalid request. Please check parameters."
            401, 403 -> "Authentication error."
            404 -> "Requested information not found."
            422 -> "Unprocessable data."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Unable to load data."
        }
    }
}
