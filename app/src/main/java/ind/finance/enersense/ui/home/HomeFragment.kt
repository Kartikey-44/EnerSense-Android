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

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.loadHomeData()
        }
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

        binding.btnLaunchSimulation.setOnClickListener {
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
                                binding.tvConsumptionSubtitle.visibility = View.VISIBLE
                                binding.tvConsumptionError.visibility = View.GONE
                                val wh = state.data.consumption ?: state.data.totalConsumption ?: 0.0
                                val kwh = wh / 1000.0
                                binding.tvConsumptionValue.text = String.format(Locale.US, "%.2f", kwh)
                                binding.tvConsumptionUnit.text = "kWh"
                                binding.tvConsumptionSubtitle.text = "Recorded through today"
                            }
                            is UiState.Empty -> {
                                binding.progressBarConsumption.visibility = View.GONE
                                binding.tvConsumptionValue.visibility = View.VISIBLE
                                binding.tvConsumptionError.visibility = View.GONE
                                binding.tvConsumptionSubtitle.visibility = View.VISIBLE
                                binding.tvConsumptionValue.text = "0.00"
                                binding.tvConsumptionUnit.text = "kWh"
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
                                binding.tvForecastValue.text = String.format(Locale.US, "%.2f", predKwh)
                                binding.tvForecastUnit.text = "kWh"
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
                                val isRunning = sim.running == true || sim.currentReading != null
                                binding.tvSimulationStatus.text = if (isRunning) "● Active" else "Not Started"
                                val dateStr = sim.currentDate ?: sim.time ?: ""
                                val readingWh = sim.currentReading ?: sim.consumption ?: 0.0
                                val readingStr = if (sim.currentReading != null || sim.consumption != null) " • ${String.format(Locale.US, "%.3f Wh", readingWh)}" else ""
                                binding.tvSimulationDate.text = if (dateStr.isNotEmpty()) "$dateStr$readingStr" else "Simulation ready"
                                binding.btnLaunchSimulation.text = "Open Simulation Deck"
                            }
                            is UiState.Empty -> {
                                binding.progressBarSimulation.visibility = View.GONE
                                binding.tvSimulationStatus.visibility = View.VISIBLE
                                binding.tvSimulationDate.visibility = View.VISIBLE
                                binding.tvSimulationError.visibility = View.GONE
                                binding.tvSimulationStatus.text = "Not Started"
                                binding.tvSimulationDate.text = "Simulation not started"
                                binding.btnLaunchSimulation.text = "Launch Simulation"
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
                    viewModel.anomalyState.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBarAnomaly.visibility = View.VISIBLE
                                binding.tvAnomalyStatus.visibility = View.GONE
                                binding.tvAnomalyMessage.visibility = View.GONE
                                binding.tvAnomalyDetails.visibility = View.GONE
                                binding.tvAnomalyError.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressBarAnomaly.visibility = View.GONE
                                binding.tvAnomalyStatus.visibility = View.VISIBLE
                                binding.tvAnomalyMessage.visibility = View.VISIBLE
                                binding.tvAnomalyDetails.visibility = View.VISIBLE
                                binding.tvAnomalyError.visibility = View.GONE

                                val anomaly = state.data
                                val status = anomaly.status ?: "NORMAL"
                                binding.tvAnomalyStatus.text = status

                                when (status) {
                                    "INSUFFICIENT_BASELINE" -> {
                                        binding.tvAnomalyStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                                        binding.tvAnomalyMessage.text = anomaly.userMessage ?: "We're still learning your typical energy usage."
                                        binding.tvAnomalyDetails.visibility = View.GONE
                                    }
                                    "NORMAL" -> {
                                        binding.tvAnomalyStatus.setTextColor(resources.getColor(R.color.brand_primary, null))
                                        binding.tvAnomalyMessage.text = anomaly.userMessage ?: "Energy consumption is normal."
                                        binding.tvAnomalyDetails.visibility = View.GONE
                                    }
                                    "UNUSUAL" -> {
                                        binding.tvAnomalyStatus.setTextColor(resources.getColor(R.color.status_error_red, null))
                                        binding.tvAnomalyMessage.text = anomaly.userMessage ?: "Unusual energy usage detected!"

                                        val current = anomaly.currentConsumptionWh ?: 0.0
                                        val typical = anomaly.typicalConsumptionWh ?: 0.0
                                        val diff = anomaly.differenceWh ?: (current - typical)
                                        val pct = anomaly.percentageAboveTypical ?: 0.0
                                        val severity = anomaly.severity ?: "UNUSUAL"
                                        val isNew = anomaly.isNewEpisode == true

                                        val detailsText = StringBuilder().apply {
                                            append("Current: ").append(String.format(Locale.US, "%.3f Wh", current)).append("\n")
                                            append("Typical: ").append(String.format(Locale.US, "%.3f Wh", typical)).append("\n")
                                            append("Difference: ").append(String.format(Locale.US, "%.3f Wh", diff)).append("\n")
                                            append(String.format(Locale.US, "%.1f%% above typical", pct)).append("\n")
                                            append("Severity: ").append(severity)
                                            if (isNew) {
                                                append("\nNew anomaly detected")
                                            }
                                        }.toString()

                                        binding.tvAnomalyDetails.text = detailsText
                                        binding.tvAnomalyDetails.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        binding.tvAnomalyStatus.setTextColor(resources.getColor(R.color.text_primary, null))
                                        binding.tvAnomalyMessage.text = anomaly.userMessage ?: status
                                        binding.tvAnomalyDetails.visibility = View.GONE
                                    }
                                }
                            }
                            is UiState.Empty -> {
                                binding.progressBarAnomaly.visibility = View.GONE
                                binding.tvAnomalyStatus.visibility = View.VISIBLE
                                binding.tvAnomalyMessage.visibility = View.VISIBLE
                                binding.tvAnomalyDetails.visibility = View.GONE
                                binding.tvAnomalyError.visibility = View.GONE
                                binding.tvAnomalyStatus.text = "READY"
                                binding.tvAnomalyMessage.text = state.message
                            }
                            is UiState.Error -> {
                                binding.progressBarAnomaly.visibility = View.GONE
                                binding.tvAnomalyStatus.visibility = View.GONE
                                binding.tvAnomalyMessage.visibility = View.GONE
                                binding.tvAnomalyDetails.visibility = View.GONE
                                binding.tvAnomalyError.visibility = View.VISIBLE
                                binding.tvAnomalyError.text = state.message
                            }
                        }
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
