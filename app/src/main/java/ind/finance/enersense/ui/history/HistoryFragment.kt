package ind.finance.enersense.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.repository.ConsumptionRepository
import ind.finance.enersense.databinding.FragmentHistoryBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.DetailUiState
import ind.finance.enersense.viewmodel.HistoryUiState
import ind.finance.enersense.viewmodel.HistoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val consumptionRepository = ConsumptionRepository(RetrofitClient.apiService)
        val factory = HistoryViewModelFactory(consumptionRepository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        historyAdapter = HistoryAdapter { item ->
            val dateStr = item.date ?: item.timestamp ?: ""
            if (dateStr.length >= 10) {
                viewModel.loadDateDetail(dateStr.substring(0, 10))
            }
        }

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        binding.etFromDate.setText(viewModel.fromDate)
        binding.etToDate.setText(viewModel.toDate)

        binding.etFromDate.setOnClickListener {
            showDatePicker(true)
        }

        binding.etToDate.setOnClickListener {
            showDatePicker(false)
        }

        binding.btnLoadHistory.setOnClickListener {
            if (validateDateRange()) {
                viewModel.loadHistory()
            }
        }

        binding.swipeRefreshHistory.setOnRefreshListener {
            viewModel.loadHistory()
        }

        binding.btnSortHistory.setOnClickListener {
            showSortDialog()
        }
        updateSortButtonText()
    }

    private fun showSortDialog() {
        val options = arrayOf("Newest to Oldest", "Oldest to Newest")
        val currentSelectedIndex = if (viewModel.sortOrder == ind.finance.enersense.viewmodel.HistorySortOrder.NEWEST_TO_OLDEST) 0 else 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort by")
            .setSingleChoiceItems(options, currentSelectedIndex) { dialog, which ->
                val selectedOrder = if (which == 0) {
                    ind.finance.enersense.viewmodel.HistorySortOrder.NEWEST_TO_OLDEST
                } else {
                    ind.finance.enersense.viewmodel.HistorySortOrder.OLDEST_TO_NEWEST
                }
                viewModel.setSortOrder(selectedOrder)
                updateSortButtonText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSortButtonText() {
        binding.tvCurrentSortOrder.text = if (viewModel.sortOrder == ind.finance.enersense.viewmodel.HistorySortOrder.NEWEST_TO_OLDEST) {
            "Newest to Oldest"
        } else {
            "Oldest to Newest"
        }
    }

    private fun showDatePicker(isFrom: Boolean) {
        val calendar = Calendar.getInstance()
        val currentStr = if (isFrom) viewModel.fromDate else viewModel.toDate
        try {
            val parts = currentStr.split("-")
            if (parts.size == 3) {
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (e: Exception) {
            // fallback to default
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
                if (isFrom) {
                    viewModel.fromDate = formatted
                    binding.etFromDate.setText(formatted)
                } else {
                    viewModel.toDate = formatted
                    binding.etToDate.setText(formatted)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateDateRange(): Boolean {
        val from = viewModel.fromDate
        val to = viewModel.toDate
        if (from > to) {
            Toast.makeText(requireContext(), "From date cannot be after To date.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.historyState.collectLatest { state ->
                        binding.swipeRefreshHistory.isRefreshing = false
                        when (state) {
                            is HistoryUiState.Loading -> {
                                binding.progressBarHistory.visibility = View.VISIBLE
                                binding.recyclerViewHistory.visibility = View.GONE
                                binding.layoutEmptyState.visibility = View.GONE
                                binding.tvHistoryStateMessage.visibility = View.GONE
                            }
                            is HistoryUiState.Success -> {
                                binding.progressBarHistory.visibility = View.GONE
                                binding.recyclerViewHistory.visibility = View.VISIBLE
                                binding.layoutEmptyState.visibility = View.GONE
                                binding.tvHistoryStateMessage.visibility = View.GONE
                                historyAdapter.submitList(state.items)

                                val totalWh = state.items.sumOf { (it.consumption ?: it.totalConsumption ?: 0.0) }
                                val totalKwh = totalWh / 1000.0
                                val avgWh = if (state.items.isNotEmpty()) totalWh / state.items.size else 0.0
                                val avgKwh = avgWh / 1000.0
                                binding.tv7DayTotal.text = String.format(Locale.US, "%.1f kWh", totalKwh)
                                binding.tv7DayAvg.text = String.format(Locale.US, "%.1f kWh", avgKwh)
                            }
                            is HistoryUiState.Empty -> {
                                binding.progressBarHistory.visibility = View.GONE
                                binding.recyclerViewHistory.visibility = View.GONE
                                binding.layoutEmptyState.visibility = View.VISIBLE
                                binding.tvHistoryStateMessage.visibility = View.VISIBLE
                                binding.tvHistoryStateMessage.text = state.message
                                binding.tv7DayTotal.text = "-- kWh"
                                binding.tv7DayAvg.text = "-- kWh"
                                historyAdapter.submitList(emptyList())
                            }
                            is HistoryUiState.Error -> {
                                binding.progressBarHistory.visibility = View.GONE
                                binding.recyclerViewHistory.visibility = View.GONE
                                binding.layoutEmptyState.visibility = View.VISIBLE
                                binding.tvHistoryStateMessage.visibility = View.VISIBLE
                                binding.tvHistoryStateMessage.text = state.message
                                binding.tv7DayTotal.text = "-- kWh"
                                binding.tv7DayAvg.text = "-- kWh"
                                historyAdapter.submitList(emptyList())
                            }
                        }
                    }
                }

                launch {
                    viewModel.detailState.collectLatest { state ->
                        when (state) {
                            is DetailUiState.Loading -> {
                                // optional loading toast or indicator
                            }
                            is DetailUiState.Success -> {
                                val item = state.data
                                val date = item.date ?: item.timestamp ?: "Selected Date"
                                val wh = item.consumption ?: item.totalConsumption ?: 0.0
                                val kwh = wh / 1000.0
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Consumption Details")
                                    .setMessage("Date: $date\nConsumption: ${String.format(Locale.US, "%.2f kWh", kwh)}\nConsumer ID: ${item.consumerId ?: viewModel.consumerId}\nMeter ID: ${item.meterId ?: viewModel.meterId}")
                                    .setPositiveButton("Close") { dialog, _ ->
                                        dialog.dismiss()
                                        viewModel.clearDetail()
                                    }
                                    .show()
                            }
                            is DetailUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.clearDetail()
                            }
                            is DetailUiState.Idle -> {}
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
