package ind.finance.enersense.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.enersense.data.repository.AnalyticsRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.AnalyticsViewModel

class AnalyticsViewModelFactory(
    private val analyticsRepository: AnalyticsRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(analyticsRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
