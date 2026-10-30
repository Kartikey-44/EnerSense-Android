package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.enersense.data.repository.AnomalyRepository
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.session.SessionManager

class HomeViewModelFactory(
    private val consumptionRepository: ConsumptionRepository,
    private val forecastRepository: ForecastRepository,
    private val simulationRepository: SimulationRepository,
    private val anomalyRepository: AnomalyRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                consumptionRepository,
                forecastRepository,
                simulationRepository,
                anomalyRepository,
                sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
