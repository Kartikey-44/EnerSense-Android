package ind.finance.enersense.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.repository.AnomalyRepository
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.databinding.FragmentHomeBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.HomeViewModel
import ind.finance.enersense.viewmodel.HomeViewModelFactory
import ind.finance.enersense.viewmodel.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val apiService = RetrofitClient.apiService
        val viewModelFactory = HomeViewModelFactory(
            ConsumptionRepository(apiService),
            ForecastRepository(apiService),
            SimulationRepository(apiService),
            AnomalyRepository(apiService),
            sessionManager
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvSessionInfo.text = "Consumer #${viewModel.consumerId} • Meter #${viewModel.meterId}"

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadHomeData()
        }

        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.btnAnalytics.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_analytics)
        }

        binding.btnForecast.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_forecast)
        }

        binding.btnSimulation.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_simulation)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.consumptionState.collectLatest { state ->
                        binding.swipeRefreshLayout.isRefreshing = false
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBarConsumption.visibility = View.VISIBLE
                                binding.tvConsumptionValue.visibility = View.GONE
                                binding.tvConsumptionSubtitle.visibility = View.GONE
                                binding.tvConsumptionError.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressBarConsumption.visibility = View.GONE
                                binding.tvConsumptionValue.visibility = View.VISIBLE
                                binding.tvConsumptionSubtitle.visibility = View.GONE
                                binding.tvConsumptionError.visibility = View.GONE
                                val wh = state.data.consumption ?: state.data.totalConsumption ?: 0.0
                                val kwh = wh / 1000.0
                                binding.tvConsumptionValue.text = String.format(Locale.US, "%.2f kWh", kwh)
                            }
                            is UiState.Empty -> {
                                binding.progressBarConsumption.visibility = View.GONE
                                binding.tvConsumptionValue.visibility = View.GONE
                                binding.tvConsumptionError.visibility = View.GONE
                                binding.tvConsumptionSubtitle.visibility = View.VISIBLE
                                binding.tvConsumptionSubtitle.text = state.message
                            }
                            is UiState.Error -> {
                                binding.progressBarConsumption.visibility = View.GONE
                                binding.tvConsumptionValue.visibility = View.GONE
                                binding.tvConsumptionSubtitle.visibility = View.GONE
                                binding.tvConsumptionError.visibility = View.VISIBLE
                                binding.tvConsumptionError.text = state.message
                            }
                        }
                    }
                }

                launch {
                    viewModel.forecastState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBarForecast.visibility = View.VISIBLE
                                binding.tvForecastValue.visibility = View.GONE
                                binding.tvForecastDate.visibility = View.GONE
                                binding.tvForecastError.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressBarForecast.visibility = View.GONE
                                binding.tvForecastValue.visibility = View.VISIBLE
                                binding.tvForecastDate.visibility = View.VISIBLE
                                binding.tvForecastError.visibility = View.GONE
                                val predWh = state.data.predictedConsumption ?: state.data.forecastValue ?: 0.0
                                val predKwh = predWh / 1000.0
                                binding.tvForecastValue.text = String.format(Locale.US, "%.2f kWh", predKwh)
                                binding.tvForecastDate.text = state.data.forecastDate ?: state.data.date ?: ""
                                val model = state.data.modelName ?: state.data.method ?: "7-Day Moving Average"
                                binding.tvForecastModel.text = model
                            }
                            is UiState.Empty -> {
                                binding.progressBarForecast.visibility = View.GONE
                                binding.tvForecastValue.visibility = View.GONE
                                binding.tvForecastDate.visibility = View.GONE
                                binding.tvForecastError.visibility = View.VISIBLE
                                binding.tvForecastError.text = state.message
                            }
                            is UiState.Error -> {
                                binding.progressBarForecast.visibility = View.GONE
                                binding.tvForecastValue.visibility = View.GONE
                                binding.tvForecastDate.visibility = View.GONE
                                binding.tvForecastError.visibility = View.VISIBLE
                                binding.tvForecastError.text = state.message
                            }
                        }
                    }
                }

                launch {
                    viewModel.simulationState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBarSimulation.visibility = View.VISIBLE
                                binding.tvSimulationStatus.visibility = View.GONE
                                binding.tvSimulationDate.visibility = View.GONE
                                binding.tvSimulationError.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressBarSimulation.visibility = View.GONE
                                binding.tvSimulationStatus.visibility = View.VISIBLE
                                binding.tvSimulationDate.visibility = View.VISIBLE
                                binding.tvSimulationError.visibility = View.GONE
                                val sim = state.data
                                val isRunning = sim.running == true
                                binding.tvSimulationStatus.text = if (isRunning) "● Running" else "Not Started"
                                val dateStr = sim.currentDate ?: sim.time ?: ""
                                val readingWh = sim.currentReading ?: sim.consumption ?: 0.0
                                val readingKwh = readingWh / 1000.0
                                val readingStr = if (sim.currentReading != null || sim.consumption != null) " • ${String.format(Locale.US, "%.2f kWh", readingKwh)}" else ""
                                binding.tvSimulationDate.text = "$dateStr$readingStr"
                            }
                            is UiState.Empty -> {
                                binding.progressBarSimulation.visibility = View.GONE
                                binding.tvSimulationStatus.visibility = View.VISIBLE
                                binding.tvSimulationDate.visibility = View.VISIBLE
                                binding.tvSimulationError.visibility = View.GONE
                                binding.tvSimulationStatus.text = "Not Started"
                                binding.tvSimulationDate.text = "Simulation not started"
                            }
                            is UiState.Error -> {
                                binding.progressBarSimulation.visibility = View.GONE
                                binding.tvSimulationStatus.visibility = View.GONE
                                binding.tvSimulationDate.visibility = View.GONE
                                binding.tvSimulationError.visibility = View.VISIBLE
                                binding.tvSimulationError.text = state.message
                            }
                        }
                    }
                }

                launch {
                    viewModel.anomalyState.collectLatest { _ ->
                        // Phase 2A state collected; UI rendering in Phase 2B/UI phase
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
