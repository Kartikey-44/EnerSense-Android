package ind.finance.enersense.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.HistoryViewModel

class HistoryViewModelFactory(
    private val consumptionRepository: ConsumptionRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(consumptionRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
