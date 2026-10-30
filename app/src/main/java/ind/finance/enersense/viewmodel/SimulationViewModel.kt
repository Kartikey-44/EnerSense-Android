package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.SimulationStateDto
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class SimulationUiState {
    object Idle : SimulationUiState()
    object Starting : SimulationUiState()
    data class Running(val state: SimulationStateDto) : SimulationUiState()
    data class Completed(val state: SimulationStateDto) : SimulationUiState()
    data class Error(val message: String) : SimulationUiState()
}

class SimulationViewModel(
    private val simulationRepository: SimulationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimulationUiState>(SimulationUiState.Idle)
    val uiState: StateFlow<SimulationUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        fetchInitialState()
    }

    fun fetchInitialState() {
        viewModelScope.launch {
            when (val result = simulationRepository.getSimulationState(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    val state = result.data
                    updateStateFromDto(state)
                }
                else -> {
                    _uiState.value = SimulationUiState.Idle
                }
            }
        }
    }

    fun startSimulation() {
        if (simulationJob?.isActive == true) return

        viewModelScope.launch {
            _uiState.value = SimulationUiState.Starting
            when (val result = simulationRepository.startSimulation(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    val state = result.data
                    updateStateFromDto(state)
                    if (state.running == true) {
                        startAdvanceLoop()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = SimulationUiState.Error(getUserFriendlyMessage(result.code))
                }
                is NetworkResult.Exception -> {
                    _uiState.value = SimulationUiState.Error("Network failure. Unable to start simulation.")
                }
                is NetworkResult.Empty -> {
                    _uiState.value = SimulationUiState.Error("Simulation start failed: empty response.")
                }
            }
        }
    }

    private fun startAdvanceLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                when (val result = simulationRepository.advanceSimulation(consumerId, meterId)) {
                    is NetworkResult.Success -> {
                        val state = result.data
                        updateStateFromDto(state)
                        if (state.running != true || state.completed == true) {
                            break
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = SimulationUiState.Error(getUserFriendlyMessage(result.code))
                        break
                    }
                    is NetworkResult.Exception -> {
                        _uiState.value = SimulationUiState.Error("Network failure during simulation advance.")
                        break
                    }
                    is NetworkResult.Empty -> {
                        // continue
                    }
                }
            }
        }
    }

    private fun updateStateFromDto(state: SimulationStateDto) {
        val isCompleted = state.completed == true || state.running == false && state.currentReading != null
        if (isCompleted) {
            _uiState.value = SimulationUiState.Completed(state)
            simulationJob?.cancel()
        } else if (state.running == true) {
            _uiState.value = SimulationUiState.Running(state)
            if (simulationJob?.isActive != true) {
                startAdvanceLoop()
            }
        } else {
            _uiState.value = SimulationUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }

    private fun getUserFriendlyMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request parameters."
            401, 403 -> "Authentication error."
            404 -> "Simulation endpoint not found."
            422 -> "Simulation state unprocessable."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Simulation operation failed."
        }
    }
}
