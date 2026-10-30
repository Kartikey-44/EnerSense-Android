package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.enersense.data.remote.dto.ConsumptionDto
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HistorySortOrder {
    NEWEST_TO_OLDEST, OLDEST_TO_NEWEST
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val items: List<ConsumptionDto>) : HistoryUiState()
    data class Empty(val message: String) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

sealed class DetailUiState {
    object Idle : DetailUiState()
    object Loading : DetailUiState()
    data class Success(val data: ConsumptionDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class HistoryViewModel(
    private val consumptionRepository: ConsumptionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _historyState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    var fromDate: String = "2016-01-01"
    var toDate: String = "2018-01-31"

    var sortOrder: HistorySortOrder = HistorySortOrder.NEWEST_TO_OLDEST
        private set

    private var rawHistoryList: List<ConsumptionDto> = emptyList()

    val consumerId: String get() = sessionManager.consumerId
    val meterId: String get() = sessionManager.meterId

    init {
        loadHistory()
    }

    fun setSortOrder(newOrder: HistorySortOrder) {
        if (sortOrder != newOrder) {
            sortOrder = newOrder
            if (rawHistoryList.isNotEmpty()) {
                _historyState.value = HistoryUiState.Success(applySort(rawHistoryList))
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            when (val result = consumptionRepository.getConsumptionHistory(consumerId, meterId, fromDate, toDate)) {
                is NetworkResult.Success -> {
                    val list = result.data
                    rawHistoryList = list
                    if (list.isEmpty()) {
                        _historyState.value = HistoryUiState.Empty("No consumption data available for this period.")
                    } else {
                        _historyState.value = HistoryUiState.Success(applySort(list))
                    }
                }
                is NetworkResult.Empty -> {
                    rawHistoryList = emptyList()
                    _historyState.value = HistoryUiState.Empty("No consumption data available for this period.")
                }
                is NetworkResult.Error -> {
                    _historyState.value = HistoryUiState.Error(getUserFriendlyMessage(result.code))
                }
                is NetworkResult.Exception -> {
                    _historyState.value = HistoryUiState.Error("Network failure. Please check your connection.")
                }
            }
        }
    }

    fun loadDateDetail(date: String) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            when (val result = consumptionRepository.getConsumptionByDate(date, consumerId, meterId)) {
                is NetworkResult.Success -> {
                    _detailState.value = DetailUiState.Success(result.data)
                }
                is NetworkResult.Empty -> {
                    _detailState.value = DetailUiState.Error("No details available for $date.")
                }
                is NetworkResult.Error -> {
                    _detailState.value = DetailUiState.Error(getUserFriendlyMessage(result.code))
                }
                is NetworkResult.Exception -> {
                    _detailState.value = DetailUiState.Error("Network failure.")
                }
            }
        }
    }

    fun clearDetail() {
        _detailState.value = DetailUiState.Idle
    }

    private fun getUserFriendlyMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request parameters."
            401, 403 -> "Authentication error."
            404 -> "History records not found."
            422 -> "Unprocessable data."
            in 500..599 -> "Server unavailable. Please try again later."
            else -> "Error ($code): Unable to load history."
        }
    }

    private fun applySort(list: List<ConsumptionDto>): List<ConsumptionDto> {
        return when (sortOrder) {
            HistorySortOrder.NEWEST_TO_OLDEST -> list.sortedByDescending { it.date ?: it.timestamp ?: "" }
            HistorySortOrder.OLDEST_TO_NEWEST -> list.sortedBy { it.date ?: it.timestamp ?: "" }
        }
    }
}
