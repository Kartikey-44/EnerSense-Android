package ind.finance.enersense.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.ForecastViewModel

class ForecastViewModelFactory(
    private val forecastRepository: ForecastRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForecastViewModel(forecastRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
