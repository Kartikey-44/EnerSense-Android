package ind.finance.enersense.ui.analytics

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.remote.dto.AnalyticsBucketDto
import ind.finance.enersense.data.repository.AnalyticsRepository
import ind.finance.enersense.databinding.FragmentAnalyticsBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.AnalyticsScale
import ind.finance.enersense.viewmodel.AnalyticsUiState
import ind.finance.enersense.viewmodel.AnalyticsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AnalyticsViewModel

    private var currentBuckets: List<AnalyticsBucketDto> = emptyList()
    private var currentLabels: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyticsBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val repository = AnalyticsRepository(RetrofitClient.apiService)
        val factory = AnalyticsViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[AnalyticsViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etAnalyticsFrom.setText(viewModel.fromDate)
        binding.etAnalyticsTo.setText(viewModel.toDate)

        binding.etAnalyticsFrom.setOnClickListener { showDatePicker(true) }
        binding.etAnalyticsTo.setOnClickListener { showDatePicker(false) }

        binding.btnLoadAnalytics.setOnClickListener {
            if (validateDateRange()) {
                viewModel.loadAnalytics()
            }
        }

        binding.swipeRefreshAnalytics.setOnRefreshListener {
            viewModel.loadAnalytics()
        }

        binding.tvDismissSelection.setOnClickListener {
            binding.cardChartSelection.visibility = View.GONE
            binding.lineChartAnalytics.highlightValues(null)
            binding.barChartAnalytics.highlightValues(null)
        }

        binding.toggleGroupScale.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.selectedScale = when (checkedId) {
                    R.id.btnScaleHourly -> AnalyticsScale.HOURLY
                    R.id.btnScaleDaily -> AnalyticsScale.DAILY
                    R.id.btnScaleWeekly -> AnalyticsScale.WEEKLY
                    R.id.btnScaleMonthly -> AnalyticsScale.MONTHLY
                    R.id.btnScaleYearly -> AnalyticsScale.YEARLY
                    else -> AnalyticsScale.DAILY
                }
                binding.cardChartSelection.visibility = View.GONE
                viewModel.loadAnalytics()
            }
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
        } catch (_: Exception) {
            // fallback
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
                if (isFrom) {
                    viewModel.fromDate = formatted
                    binding.etAnalyticsFrom.setText(formatted)
                } else {
                    viewModel.toDate = formatted
                    binding.etAnalyticsTo.setText(formatted)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateDateRange(): Boolean {
        if (viewModel.fromDate > viewModel.toDate) {
            Toast.makeText(requireContext(), "From date cannot be after To date.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.analyticsState.collectLatest { state ->
                    binding.swipeRefreshAnalytics.isRefreshing = false
                    when (state) {
                        is AnalyticsUiState.Loading -> {
                            binding.progressBarAnalytics.visibility = View.VISIBLE
                            binding.lineChartAnalytics.visibility = View.GONE
                            binding.barChartAnalytics.visibility = View.GONE
                            binding.layoutAnalyticsEmptyState.visibility = View.GONE
                            binding.cardChartSelection.visibility = View.GONE
                            binding.cardConsumerInsight.visibility = View.GONE
                            resetSummary()
                        }
                        is AnalyticsUiState.Success -> {
                            binding.progressBarAnalytics.visibility = View.GONE
                            binding.layoutAnalyticsEmptyState.visibility = View.GONE
                            updateSummaryAndChart(state.buckets, viewModel.selectedScale)
                        }
                        is AnalyticsUiState.Empty -> {
                            binding.progressBarAnalytics.visibility = View.GONE
                            binding.lineChartAnalytics.visibility = View.GONE
                            binding.barChartAnalytics.visibility = View.GONE
                            binding.layoutAnalyticsEmptyState.visibility = View.VISIBLE
                            binding.cardChartSelection.visibility = View.GONE
                            binding.cardConsumerInsight.visibility = View.GONE
                            binding.tvAnalyticsEmptyTitle.text = "No energy data available yet."
                            binding.tvAnalyticsStateMessage.text = state.message
                            resetSummary()
                        }
                        is AnalyticsUiState.Error -> {
                            binding.progressBarAnalytics.visibility = View.GONE
                            binding.lineChartAnalytics.visibility = View.GONE
                            binding.barChartAnalytics.visibility = View.GONE
                            binding.layoutAnalyticsEmptyState.visibility = View.VISIBLE
                            binding.cardChartSelection.visibility = View.GONE
                            binding.cardConsumerInsight.visibility = View.GONE
                            binding.tvAnalyticsEmptyTitle.text = "Unable to load energy data"
                            binding.tvAnalyticsStateMessage.text = state.message
                            resetSummary()
                        }
                    }
                }
            }
        }
    }

    private fun resetSummary() {
        binding.tvSummaryTotal.text = "0.00"
        binding.tvSummaryTotalUnit.text = "kWh"
        binding.tvSummaryPeak.text = "0.00"
        binding.tvSummaryPeakUnit.text = "kWh"
        binding.tvSummaryAverage.text = "0.00"
        binding.tvSummaryAverageUnit.text = "kWh"
    }

    private fun updateSummaryAndChart(buckets: List<AnalyticsBucketDto>, scale: AnalyticsScale) {
        currentBuckets = buckets
        currentLabels = buckets.map { formatAxisLabel(it.period, scale) }

        var totalWh = 0.0
        var highestWh = 0.0

        buckets.forEach { bucket ->
            val consumption = bucket.consumption ?: bucket.totalConsumption ?: 0.0
            totalWh += consumption
            if (consumption > highestWh) highestWh = consumption
        }
        val averageWh = if (buckets.isNotEmpty()) totalWh / buckets.size else 0.0

        // Consumer-friendly units: use kWh when total is high, Wh when appropriate
        val (formattedTotal, totalUnit) = formatEnergy(totalWh)
        val (formattedHighest, highestUnit) = formatEnergy(highestWh)
        val (formattedAvg, avgUnit) = formatEnergy(averageWh)

        binding.tvSummaryTotal.text = formattedTotal
        binding.tvSummaryTotalUnit.text = totalUnit

        binding.tvSummaryPeak.text = formattedHighest
        binding.tvSummaryPeakUnit.text = highestUnit

        binding.tvSummaryAverage.text = formattedAvg
        binding.tvSummaryAverageUnit.text = avgUnit

        // Consumer-oriented header wording
        val (title, subtitle) = when (scale) {
            AnalyticsScale.HOURLY -> Pair("When you use the most", "Hourly energy consumption")
            AnalyticsScale.DAILY -> Pair("Your daily usage", "Day-by-day energy consumption")
            AnalyticsScale.WEEKLY -> Pair("Your weekly usage", "Weekly aggregated energy consumption")
            AnalyticsScale.MONTHLY -> Pair("Your monthly usage", "Monthly energy consumption")
            AnalyticsScale.YEARLY -> Pair("Your yearly usage", "Year-by-year energy consumption")
        }
        binding.tvChartHeaderTitle.text = title
        binding.tvChartHeaderSubtitle.text = "$subtitle • ${viewModel.fromDate} to ${viewModel.toDate}"

        // Update consumer insights
        updateConsumerInsights(buckets, scale)

        val useLineChart = (scale == AnalyticsScale.HOURLY || scale == AnalyticsScale.DAILY)
        val primaryColor = Color.parseColor("#0B533E")
        val gridColor = Color.parseColor("#EDEFF2")
        val textColor = Color.parseColor("#6B7280")

        val labelCount = minOf(currentLabels.size, 6)

        if (useLineChart) {
            binding.lineChartAnalytics.visibility = View.VISIBLE
            binding.barChartAnalytics.visibility = View.GONE

            val entries = ArrayList<Entry>()
            buckets.forEachIndexed { index, bucket ->
                val y = bucket.consumption ?: bucket.totalConsumption ?: 0.0
                entries.add(Entry(index.toFloat(), y.toFloat()))
            }

            val dataSet = LineDataSet(entries, "Energy (Wh)").apply {
                color = primaryColor
                setCircleColor(primaryColor)
                lineWidth = 2.5f
                circleRadius = 3.5f
                setDrawCircleHole(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                fillColor = primaryColor
                fillAlpha = 35
                setDrawFilled(true)
                highLightColor = Color.parseColor("#14B87A")
                setDrawHighlightIndicators(true)
            }

            binding.lineChartAnalytics.apply {
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    setDrawGridLines(false)
                    this.textColor = textColor
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setLabelCount(labelCount, false)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in currentLabels.indices) currentLabels[idx] else ""
                        }
                    }
                }
                axisLeft.apply {
                    this.textColor = textColor
                    setGridColor(gridColor)
                    axisMinimum = 0f
                    resetAxisMaximum()
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value >= 1000f) {
                                String.format(Locale.US, "%.1fk", value / 1000f)
                            } else {
                                value.toInt().toString()
                            }
                        }
                    }
                }
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let { showPointDetails(it.x.toInt(), it.y.toDouble(), scale) }
                    }

                    override fun onNothingSelected() {
                        binding.cardChartSelection.visibility = View.GONE
                    }
                })
                fitScreen()
                invalidate()
            }
        } else {
            binding.lineChartAnalytics.visibility = View.GONE
            binding.barChartAnalytics.visibility = View.VISIBLE

            val entries = ArrayList<BarEntry>()
            buckets.forEachIndexed { index, bucket ->
                val y = bucket.consumption ?: bucket.totalConsumption ?: 0.0
                entries.add(BarEntry(index.toFloat(), y.toFloat()))
            }

            val dataSet = BarDataSet(entries, "Energy (Wh)").apply {
                color = primaryColor
                setDrawValues(false)
                highLightColor = Color.parseColor("#14B87A")
            }

            binding.barChartAnalytics.apply {
                data = BarData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    setDrawGridLines(false)
                    this.textColor = textColor
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setLabelCount(labelCount, false)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in currentLabels.indices) currentLabels[idx] else ""
                        }
                    }
                }
                axisLeft.apply {
                    this.textColor = textColor
                    setGridColor(gridColor)
                    axisMinimum = 0f
                    resetAxisMaximum()
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value >= 1000f) {
                                String.format(Locale.US, "%.1fk", value / 1000f)
                            } else {
                                value.toInt().toString()
                            }
                        }
                    }
                }
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let { showPointDetails(it.x.toInt(), it.y.toDouble(), scale) }
                    }

                    override fun onNothingSelected() {
                        binding.cardChartSelection.visibility = View.GONE
                    }
                })
                fitScreen()
                invalidate()
            }
        }
    }

    private fun showPointDetails(index: Int, consumptionWh: Double, scale: AnalyticsScale) {
        if (index !in currentBuckets.indices) return
        val rawPeriod = currentBuckets[index].period
        val readablePeriod = formatDetailPeriod(rawPeriod, scale)

        val (formattedValue, unit) = formatEnergy(consumptionWh)
        binding.tvSelectedPeriod.text = readablePeriod
        binding.tvSelectedConsumption.text = "Energy used: $formattedValue $unit"
        binding.cardChartSelection.visibility = View.VISIBLE
    }

    private fun updateConsumerInsights(buckets: List<AnalyticsBucketDto>, scale: AnalyticsScale) {
        if (buckets.isEmpty()) {
            binding.cardConsumerInsight.visibility = View.GONE
            return
        }

        // Find highest consumption period
        var maxIndex = 0
        var maxWh = -1.0
        var minIndex = 0
        var minWh = Double.MAX_VALUE

        buckets.forEachIndexed { idx, bucket ->
            val consumption = bucket.consumption ?: bucket.totalConsumption ?: 0.0
            if (consumption > maxWh) {
                maxWh = consumption
                maxIndex = idx
            }
            if (consumption < minWh) {
                minWh = consumption
                minIndex = idx
            }
        }

        val highestPeriodLabel = formatDetailPeriod(buckets[maxIndex].period, scale)
        val lowestPeriodLabel = formatDetailPeriod(buckets[minIndex].period, scale)
        val (formattedHighest, highestUnit) = formatEnergy(maxWh)

        val primaryInsight = when (scale) {
            AnalyticsScale.HOURLY -> "Your highest usage was at $highestPeriodLabel ($formattedHighest $highestUnit)."
            AnalyticsScale.DAILY -> "Your highest usage was on $highestPeriodLabel ($formattedHighest $highestUnit)."
            AnalyticsScale.WEEKLY -> "Your highest usage was during the week of $highestPeriodLabel ($formattedHighest $highestUnit)."
            AnalyticsScale.MONTHLY -> "Your highest usage was in $highestPeriodLabel ($formattedHighest $highestUnit)."
            AnalyticsScale.YEARLY -> "Your highest usage was in $highestPeriodLabel ($formattedHighest $highestUnit)."
        }

        // Determine usage trend if we have at least 2 points
        var secondaryInsight = ""
        var trendBadge = "Stable"
        var trendColor = Color.parseColor("#0B533E")

        if (buckets.size >= 2) {
            val half = buckets.size / 2
            val firstHalfAvg = buckets.take(half).map { it.consumption ?: 0.0 }.average()
            val secondHalfAvg = buckets.drop(half).map { it.consumption ?: 0.0 }.average()

            if (secondHalfAvg > firstHalfAvg * 1.05) {
                trendBadge = "Increasing"
                trendColor = Color.parseColor("#DC2626")
                secondaryInsight = "Your usage increased over this period. Lowest use was $lowestPeriodLabel."
            } else if (secondHalfAvg < firstHalfAvg * 0.95) {
                trendBadge = "Decreasing"
                trendColor = Color.parseColor("#0B533E")
                secondaryInsight = "Your usage decreased over this period. Lowest use was $lowestPeriodLabel."
            } else {
                trendBadge = "Consistent"
                trendColor = Color.parseColor("#2563EB")
                secondaryInsight = "Your usage remained steady. Lowest use was $lowestPeriodLabel."
            }
        } else {
            secondaryInsight = "Only 1 period of readings recorded so far."
        }

        binding.tvInsightTrendBadge.text = trendBadge
        binding.tvInsightTrendBadge.setTextColor(trendColor)
        binding.tvConsumerInsightPrimary.text = primaryInsight
        binding.tvConsumerInsightSecondary.text = secondaryInsight
        binding.cardConsumerInsight.visibility = View.VISIBLE
    }

    private fun formatAxisLabel(periodStr: String?, scale: AnalyticsScale): String {
        if (periodStr == null) return ""
        return try {
            val dateTime = parseIsoDateTime(periodStr)
            when (scale) {
                AnalyticsScale.HOURLY -> dateTime.format(DateTimeFormatter.ofPattern("h a", Locale.US))
                AnalyticsScale.DAILY -> dateTime.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
                AnalyticsScale.WEEKLY -> {
                    val endWeek = dateTime.plusDays(6)
                    if (dateTime.month == endWeek.month) {
                        "${dateTime.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))}–${endWeek.dayOfMonth}"
                    } else {
                        "${dateTime.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))}–${endWeek.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))}"
                    }
                }
                AnalyticsScale.MONTHLY -> dateTime.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
                AnalyticsScale.YEARLY -> dateTime.format(DateTimeFormatter.ofPattern("yyyy", Locale.US))
            }
        } catch (_: Exception) {
            periodStr.take(10)
        }
    }

    private fun formatDetailPeriod(periodStr: String?, scale: AnalyticsScale): String {
        if (periodStr == null) return "Unknown"
        return try {
            val dateTime = parseIsoDateTime(periodStr)
            when (scale) {
                AnalyticsScale.HOURLY -> dateTime.format(DateTimeFormatter.ofPattern("h:mm a, MMM d", Locale.US))
                AnalyticsScale.DAILY -> dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US))
                AnalyticsScale.WEEKLY -> {
                    val endWeek = dateTime.plusDays(6)
                    "${dateTime.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))} – ${endWeek.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))}"
                }
                AnalyticsScale.MONTHLY -> dateTime.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
                AnalyticsScale.YEARLY -> dateTime.format(DateTimeFormatter.ofPattern("yyyy", Locale.US))
            }
        } catch (_: Exception) {
            periodStr
        }
    }

    private fun parseIsoDateTime(periodStr: String): LocalDateTime {
        return if (periodStr.contains("T")) {
            val cleaned = if (periodStr.length > 19) periodStr.substring(0, 19) else periodStr
            LocalDateTime.parse(cleaned)
        } else if (periodStr.length == 10) {
            LocalDate.parse(periodStr).atStartOfDay()
        } else {
            LocalDateTime.parse(periodStr)
        }
    }

    private fun formatEnergy(wh: Double): Pair<String, String> {
        return if (wh >= 1000.0) {
            Pair(String.format(Locale.US, "%.2f", wh / 1000.0), "kWh")
        } else {
            Pair(String.format(Locale.US, "%.1f", wh), "Wh")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
