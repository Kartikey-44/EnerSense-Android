package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.AnomalyDto
import ind.finance.enersense.data.remote.dto.SimulationStateDto
import ind.finance.enersense.data.repository.AnomalyRepository
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class SimulationUiState {
    object Idle : SimulationUiState()
    object Starting : SimulationUiState()
    data class Running(val state: SimulationStateDto, val isPlaying: Boolean, val isAdvancing: Boolean = false) : SimulationUiState()
    data class AnomalyDetected(val state: SimulationStateDto, val anomaly: AnomalyDto) : SimulationUiState()
    data class Completed(val state: SimulationStateDto) : SimulationUiState()
    data class Error(val message: String, val lastState: SimulationStateDto? = null) : SimulationUiState()
}

class SimulationViewModel(
    private val simulationRepository: SimulationRepository,
    private val anomalyRepository: AnomalyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimulationUiState>(SimulationUiState.Idle)
    val uiState: StateFlow<SimulationUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private val advanceMutex = Mutex()

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
                    updateStateFromDto(state, startPlaying = false)
                }
                else -> {
                    _uiState.value = SimulationUiState.Idle
                }
            }
        }
    }

    fun startSimulation() {
        stopAutoAdvanceLoop()

        viewModelScope.launch {
            _uiState.value = SimulationUiState.Starting
            when (val result = simulationRepository.startSimulation(consumerId, meterId)) {
                is NetworkResult.Success -> {
                    val state = result.data
                    if (state.completed == true) {
                        _uiState.value = SimulationUiState.Completed(state)
                    } else {
                        checkAnomalyAndDispatch(state, startPlaying = true)
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

    fun advanceSimulation() {
        val currentState = _uiState.value
        val currentDto = when (currentState) {
            is SimulationUiState.Running -> currentState.state
            is SimulationUiState.Error -> currentState.lastState
            is SimulationUiState.AnomalyDetected -> currentState.state
            else -> null
        }

        viewModelScope.launch {
            if (currentDto != null) {
                _uiState.value = SimulationUiState.Running(currentDto, isPlaying = isPlayingState(currentState), isAdvancing = true)
            }
            val result = advanceMutex.withLock {
                simulationRepository.advanceSimulation(consumerId, meterId)
            }
            when (result) {
                is NetworkResult.Success -> {
                    val state = result.data
                    val isPlaying = isPlayingState(currentState)
                    checkAnomalyAndDispatch(state, startPlaying = isPlaying)
                }
                is NetworkResult.Error -> {
                    _uiState.value = SimulationUiState.Error(getUserFriendlyMessage(result.code), currentDto)
                }
                is NetworkResult.Exception -> {
                    _uiState.value = SimulationUiState.Error("Network failure during simulation advance.", currentDto)
                }
                is NetworkResult.Empty -> {
                    if (currentDto != null) {
                        _uiState.value = SimulationUiState.Running(currentDto, isPlaying = isPlayingState(currentState), isAdvancing = false)
                    }
                }
            }
        }
    }

    fun stopSimulation() {
        val currentState = _uiState.value
        if (currentState is SimulationUiState.Running) {
            stopAutoAdvanceLoop()
            _uiState.value = currentState.copy(isPlaying = false)
        } else if (currentState is SimulationUiState.AnomalyDetected) {
            stopAutoAdvanceLoop()
            _uiState.value = SimulationUiState.Running(currentState.state, isPlaying = false)
        }
    }

    fun restartSimulation() {
        startSimulation()
    }

    fun resumeSimulation() {
        val currentState = _uiState.value
        val currentDto = when (currentState) {
            is SimulationUiState.Running -> currentState.state
            is SimulationUiState.AnomalyDetected -> currentState.state
            is SimulationUiState.Error -> currentState.lastState
            else -> null
        }
        if (currentDto != null) {
            _uiState.value = SimulationUiState.Running(currentDto, isPlaying = true, isAdvancing = false)
            startAutoAdvanceLoop()
        }
    }

    fun toggleStopResume() {
        val currentState = _uiState.value
        if (currentState is SimulationUiState.Running) {
            if (currentState.isPlaying) {
                stopSimulation()
            } else {
                resumeSimulation()
            }
        } else if (currentState is SimulationUiState.Error) {
            resumeSimulation()
        }
    }

    private suspend fun checkAnomalyAndDispatch(state: SimulationStateDto, startPlaying: Boolean) {
        if (state.completed == true) {
            stopAutoAdvanceLoop()
            _uiState.value = SimulationUiState.Completed(state)
            return
        }

        // Check current anomaly - only trigger for new anomaly episodes (isNewEpisode == true)
        when (val anomalyResult = anomalyRepository.getCurrentAnomaly(consumerId, meterId)) {
            is NetworkResult.Success -> {
                val anomaly = anomalyResult.data
                if (anomaly.status == "UNUSUAL" && anomaly.isNewEpisode == true) {
                    stopAutoAdvanceLoop()
                    _uiState.value = SimulationUiState.AnomalyDetected(state, anomaly)
                    return
                }
            }
            else -> {}
        }

        _uiState.value = SimulationUiState.Running(state, startPlaying, isAdvancing = false)
        if (startPlaying) {
            startAutoAdvanceLoop()
        } else {
            stopAutoAdvanceLoop()
        }
    }

    private fun startAutoAdvanceLoop() {
        stopAutoAdvanceLoop()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                val currentState = _uiState.value
                val currentDto = when (currentState) {
                    is SimulationUiState.Running -> currentState.state
                    else -> null
                }

                val result = advanceMutex.withLock {
                    simulationRepository.advanceSimulation(consumerId, meterId)
                }

                when (result) {
                    is NetworkResult.Success -> {
                        val state = result.data
                        if (state.completed == true) {
                            stopAutoAdvanceLoop()
                            _uiState.value = SimulationUiState.Completed(state)
                            break
                        }

                        // Check anomaly on auto advance - only trigger for new anomaly episodes (isNewEpisode == true)
                        when (val anomalyResult = anomalyRepository.getCurrentAnomaly(consumerId, meterId)) {
                            is NetworkResult.Success -> {
                                val anomaly = anomalyResult.data
                                if (anomaly.status == "UNUSUAL" && anomaly.isNewEpisode == true) {
                                    stopAutoAdvanceLoop()
                                    _uiState.value = SimulationUiState.AnomalyDetected(state, anomaly)
                                    break
                                }
                            }
                            else -> {}
                        }

                        _uiState.value = SimulationUiState.Running(state, isPlaying = true, isAdvancing = false)
                    }
                    is NetworkResult.Error -> {
                        stopAutoAdvanceLoop()
                        _uiState.value = SimulationUiState.Error(getUserFriendlyMessage(result.code), currentDto)
                        break
                    }
                    is NetworkResult.Exception -> {
                        stopAutoAdvanceLoop()
                        _uiState.value = SimulationUiState.Error("Network failure during automatic advance.", currentDto)
                        break
                    }
                    is NetworkResult.Empty -> {
                        // continue loop
                    }
                }
                delay(10L)
            }
        }
    }

    private fun stopAutoAdvanceLoop() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun updateStateFromDto(state: SimulationStateDto, startPlaying: Boolean) {
        if (state.completed == true) {
            stopAutoAdvanceLoop()
            _uiState.value = SimulationUiState.Completed(state)
        } else if (state.currentReading != null || state.currentDate != null || state.running == true || state.position != null) {
            viewModelScope.launch {
                checkAnomalyAndDispatch(state, startPlaying)
            }
        } else {
            stopAutoAdvanceLoop()
            _uiState.value = SimulationUiState.Idle
        }
    }

    private fun isPlayingState(state: SimulationUiState): Boolean {
        return when (state) {
            is SimulationUiState.Running -> state.isPlaying
            is SimulationUiState.AnomalyDetected -> false
            else -> false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoAdvanceLoop()
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
