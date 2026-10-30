package ind.finance.enersense.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.SimulationViewModel

class SimulationViewModelFactory(
    private val simulationRepository: SimulationRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimulationViewModel(simulationRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
