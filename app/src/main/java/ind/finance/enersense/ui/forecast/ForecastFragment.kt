package ind.finance.enersense.ui.forecast

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.repository.ForecastRepository
import ind.finance.enersense.databinding.FragmentForecastBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.ForecastUiState
import ind.finance.enersense.viewmodel.ForecastViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ForecastFragment : Fragment(R.layout.fragment_forecast) {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForecastViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForecastBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val repository = ForecastRepository(RetrofitClient.apiService)
        val factory = ForecastViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.swipeRefreshForecast.setOnRefreshListener {
            viewModel.loadForecast()
        }
        binding.btnForecastRetry.setOnClickListener {
            viewModel.loadForecast()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forecastState.collectLatest { state ->
                    binding.swipeRefreshForecast.isRefreshing = false
                    when (state) {
                        is ForecastUiState.Loading -> {
                            binding.progressBarForecast.visibility = View.VISIBLE
                            binding.layoutForecastContent.visibility = View.GONE
                            binding.layoutForecastError.visibility = View.GONE
                        }
                        is ForecastUiState.Success -> {
                            binding.progressBarForecast.visibility = View.GONE
                            binding.layoutForecastContent.visibility = View.VISIBLE
                            binding.layoutForecastError.visibility = View.GONE

                            val forecast = state.forecast
                            val predWh = forecast.predictedConsumption ?: forecast.forecastValue ?: 0.0
                            val (formattedVal, unit) = formatEnergy(predWh)

                            binding.tvForecastValue.text = formattedVal
                            binding.tvForecastUnit.text = unit

                            val rawDate = forecast.forecastDate ?: forecast.date
                            val formattedDate = formatDate(rawDate)
                            binding.tvForecastDate.text = if (formattedDate.isNotEmpty()) "For $formattedDate" else ""

                            // Consumer insight: e.g. "Your energy use is expected to be around 6.380 Wh tomorrow."
                            binding.tvConsumerInsight.text = "Your energy use is expected to be around $formattedVal $unit tomorrow."

                            binding.tvForecastModel.text = forecast.modelName ?: forecast.method ?: "7-Day Simple Moving Average"
                        }
                        is ForecastUiState.Empty -> {
                            binding.progressBarForecast.visibility = View.GONE
                            binding.layoutForecastContent.visibility = View.GONE
                            binding.layoutForecastError.visibility = View.VISIBLE
                            binding.tvForecastEmptyTitle.text = "No forecast available yet"
                            binding.tvForecastErrorMessage.text = state.message
                        }
                        is ForecastUiState.Error -> {
                            binding.progressBarForecast.visibility = View.GONE
                            binding.layoutForecastContent.visibility = View.GONE
                            binding.layoutForecastError.visibility = View.VISIBLE
                            binding.tvForecastEmptyTitle.text = "Unable to load forecast"
                            binding.tvForecastErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun formatEnergy(wh: Double): Pair<String, String> {
        return if (wh >= 1000.0) {
            Pair(String.format(Locale.US, "%.2f", wh / 1000.0), "kWh")
        } else {
            Pair(String.format(Locale.US, "%.3f", wh), "Wh")
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return try {
            val date = LocalDate.parse(dateStr.take(10))
            date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))
        } catch (_: Exception) {
            dateStr
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
