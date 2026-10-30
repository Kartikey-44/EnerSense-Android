package ind.finance.enersense.ui.simulation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.repository.SimulationRepository
import ind.finance.enersense.databinding.FragmentSimulationBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.SimulationUiState
import ind.finance.enersense.viewmodel.SimulationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class SimulationFragment : Fragment(R.layout.fragment_simulation) {

    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SimulationViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimulationBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val repository = SimulationRepository(RetrofitClient.apiService)
        val factory = SimulationViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[SimulationViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.swipeRefreshSimulation.setOnRefreshListener {
            viewModel.fetchInitialState()
        }

        binding.btnStartSimulation.setOnClickListener {
            viewModel.startSimulation()
        }

        binding.btnSimulationRetry.setOnClickListener {
            viewModel.startSimulation()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.swipeRefreshSimulation.isRefreshing = false
                    when (state) {
                        is SimulationUiState.Idle -> {
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = true
                            binding.btnStartSimulation.text = "Start Simulation"
                            binding.tvSimulationStatus.text = "NOT STARTED"
                            binding.tvSimulationConsumption.text = "-- kWh"
                            binding.tvSimulationDateTime.text = "Simulation ready to start"
                            binding.tvSimulationPosition.text = ""
                        }
                        is SimulationUiState.Starting -> {
                            binding.progressBarSimulation.visibility = View.VISIBLE
                            binding.btnStartSimulation.isEnabled = false
                            binding.btnStartSimulation.text = "Starting..."
                            binding.tvSimulationStatus.text = "STARTING"
                        }
                        is SimulationUiState.Running -> {
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = false
                            binding.btnStartSimulation.text = "Running..."
                            binding.tvSimulationStatus.text = "● RUNNING"

                            val s = state.state
                            val kwh = s.currentReading ?: s.consumption ?: 0.0
                            binding.tvSimulationConsumption.text = String.format(Locale.US, "%.2f kWh", kwh)

                            val date = s.currentDate ?: ""
                            val time = s.time ?: ""
                            binding.tvSimulationDateTime.text = if (time.isNotEmpty()) "$date $time" else date

                            val pos = s.position ?: s.readingIndex
                            binding.tvSimulationPosition.text = if (pos != null) "Reading #$pos" else ""
                        }
                        is SimulationUiState.Completed -> {
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = false
                            binding.btnStartSimulation.text = "Completed"
                            binding.tvSimulationStatus.text = "COMPLETED"

                            val s = state.state
                            val kwh = s.currentReading ?: s.consumption ?: 0.0
                            binding.tvSimulationConsumption.text = String.format(Locale.US, "%.2f kWh", kwh)
                            binding.tvSimulationDateTime.text = "Simulation finished at ${s.currentDate ?: ""} ${s.time ?: ""}"
                            binding.tvSimulationPosition.text = "Final Reading"
                        }
                        is SimulationUiState.Error -> {
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = true
                            binding.btnStartSimulation.text = "Start Simulation"
                            binding.tvSimulationStatus.text = "ERROR"
                            binding.layoutSimulationError.visibility = View.VISIBLE
                            binding.tvSimulationErrorMessage.text = state.message
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
