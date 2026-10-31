package ind.finance.enersense.ui.simulation

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ind.finance.enersense.MainActivity
import ind.finance.enersense.R
import ind.finance.enersense.data.remote.RetrofitClient
import ind.finance.enersense.data.remote.dto.AnomalyDto
import ind.finance.enersense.data.remote.dto.SimulationStateDto
import ind.finance.enersense.data.repository.AnomalyRepository
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
    private var anomalyDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimulationBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val apiService = RetrofitClient.apiService
        val simulationRepo = SimulationRepository(apiService)
        val anomalyRepo = AnomalyRepository(apiService)
        val factory = SimulationViewModelFactory(simulationRepo, anomalyRepo, sessionManager)
        viewModel = ViewModelProvider(this, factory)[SimulationViewModel::class.java]

        setupUI()
        observeViewModel()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun setupUI() {
        binding.swipeRefreshSimulation.setOnRefreshListener {
            viewModel.fetchInitialState()
        }

        binding.btnStartSimulation.setOnClickListener {
            viewModel.startSimulation()
        }

        binding.btnStop.setOnClickListener {
            viewModel.toggleStopResume()
        }

        binding.btnRestart.setOnClickListener {
            viewModel.restartSimulation()
        }

        binding.btnAdvance.setOnClickListener {
            viewModel.advanceSimulation()
        }

        binding.btnSimulationRetry.setOnClickListener {
            viewModel.resumeSimulation()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.swipeRefreshSimulation.isRefreshing = false
                    when (state) {
                        is SimulationUiState.Idle -> {
                            dismissAnomalyDialog()
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.visibility = View.VISIBLE
                            binding.layoutPlaybackControls.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = true
                            binding.btnStartSimulation.text = "Start Simulation"
                            binding.tvSimulationStatus.text = "NOT STARTED"
                            binding.tvSimulationConsumption.text = "-- Wh"
                            binding.tvSimulationDateTime.text = "Simulation not started"
                            binding.tvSimulationPosition.text = "Press Start Simulation to begin auto replay"
                        }
                        is SimulationUiState.Starting -> {
                            dismissAnomalyDialog()
                            binding.progressBarSimulation.visibility = View.VISIBLE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.visibility = View.VISIBLE
                            binding.layoutPlaybackControls.visibility = View.GONE
                            binding.btnStartSimulation.isEnabled = false
                            binding.btnStartSimulation.text = "Starting..."
                            binding.tvSimulationStatus.text = "STARTING"
                        }
                        is SimulationUiState.Running -> {
                            dismissAnomalyDialog()
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.visibility = View.GONE
                            binding.layoutPlaybackControls.visibility = View.VISIBLE

                            val isPlaying = state.isPlaying
                            val isAdvancing = state.isAdvancing

                            binding.btnAdvance.isEnabled = !isAdvancing
                            binding.btnAdvance.text = if (isAdvancing) "Advancing..." else "Advance"

                            binding.btnStop.isEnabled = true
                            binding.btnStop.text = if (isPlaying) "Stop" else "Resume"
                            binding.btnRestart.isEnabled = true

                            binding.tvSimulationStatus.text = if (isPlaying) "● RUNNING (Auto)" else "⏹ STOPPED"

                            val s = state.state
                            val wh = s.currentReading ?: s.consumption ?: 0.0
                            binding.tvSimulationConsumption.text = String.format(Locale.US, "%.3f Wh", wh)

                            val date = s.currentDate ?: ""
                            val time = s.time ?: ""
                            binding.tvSimulationDateTime.text = if (time.isNotEmpty()) "$date $time" else (date.ifEmpty { "Active Simulation" })

                            val pos = s.position ?: s.readingIndex
                            binding.tvSimulationPosition.text = if (pos != null) "Reading Position #$pos" else "Smart Meter Replay"
                        }
                        is SimulationUiState.AnomalyDetected -> {
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.visibility = View.GONE
                            binding.layoutPlaybackControls.visibility = View.VISIBLE

                            binding.btnStop.isEnabled = true
                            binding.btnStop.text = "Resume"
                            binding.btnRestart.isEnabled = true
                            binding.btnRestart.text = "Restart"
                            binding.btnAdvance.isEnabled = true

                            binding.tvSimulationStatus.text = "⚠️ ANOMALY DETECTED"

                            val s = state.state
                            val wh = s.currentReading ?: s.consumption ?: state.anomaly.currentConsumptionWh ?: 0.0
                            binding.tvSimulationConsumption.text = String.format(Locale.US, "%.3f Wh", wh)

                            val date = state.anomaly.simulatedTimestamp ?: s.currentDate ?: ""
                            val time = s.time ?: ""
                            binding.tvSimulationDateTime.text = if (time.isNotEmpty() && !date.contains(time)) "$date $time" else date

                            val pos = s.position ?: s.readingIndex
                            binding.tvSimulationPosition.text = if (pos != null) "Reading Position #$pos (Anomaly)" else "Anomaly Detected"

                            showSystemNotification(state.anomaly, state.state)
                            showAnomalyDialog(state)
                        }
                        is SimulationUiState.Completed -> {
                            dismissAnomalyDialog()
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.GONE
                            binding.btnStartSimulation.visibility = View.GONE
                            binding.layoutPlaybackControls.visibility = View.VISIBLE
                            binding.btnAdvance.isEnabled = false
                            binding.btnAdvance.text = "Completed"
                            binding.btnStop.isEnabled = false
                            binding.btnStop.text = "Stop"
                            binding.btnRestart.isEnabled = true
                            binding.btnRestart.text = "Restart"
                            binding.tvSimulationStatus.text = "COMPLETED"

                            val s = state.state
                            val wh = s.currentReading ?: s.consumption ?: 0.0
                            binding.tvSimulationConsumption.text = String.format(Locale.US, "%.3f Wh", wh)
                            binding.tvSimulationDateTime.text = "Finished at ${s.currentDate ?: ""} ${s.time ?: ""}"
                            binding.tvSimulationPosition.text = "Simulation complete (End of dataset)"
                        }
                        is SimulationUiState.Error -> {
                            dismissAnomalyDialog()
                            binding.progressBarSimulation.visibility = View.GONE
                            binding.layoutSimulationError.visibility = View.VISIBLE
                            binding.tvSimulationErrorMessage.text = state.message

                            if (state.lastState != null) {
                                binding.btnStartSimulation.visibility = View.GONE
                                binding.layoutPlaybackControls.visibility = View.VISIBLE
                                binding.btnStop.isEnabled = true
                                binding.btnStop.text = "Resume"
                                binding.btnRestart.isEnabled = true
                                binding.btnRestart.text = "Restart"
                                binding.btnAdvance.isEnabled = true
                            } else {
                                binding.btnStartSimulation.visibility = View.VISIBLE
                                binding.layoutPlaybackControls.visibility = View.GONE
                                binding.btnStartSimulation.isEnabled = true
                                binding.btnStartSimulation.text = "Start Simulation"
                            }
                            binding.tvSimulationStatus.text = "STOPPED / ERROR"
                        }
                    }
                }
            }
        }
    }

    private fun showSystemNotification(anomaly: AnomalyDto, state: SimulationStateDto) {
        val context = context ?: return
        val channelId = "enersense_anomaly_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Energy Anomalies",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for unusual energy consumption anomalies"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val timeStr = anomaly.simulatedTimestamp ?: state.currentDate ?: "Unknown Time"
        val consumedWh = anomaly.currentConsumptionWh ?: state.currentReading ?: 0.0
        val typicalWh = anomaly.typicalConsumptionWh ?: 0.0
        val contentText = "Time: $timeStr | Consumed: ${String.format(Locale.US, "%.3f Wh", consumedWh)} (Typical usage: ${String.format(Locale.US, "%.3f Wh", typicalWh)})"

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bolt)
            .setLargeIcon(largeIcon)
            .setContentTitle("⚠️ Unusual Energy Usage Detected")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\n${anomaly.userMessage ?: ""}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2001, notification)
        } catch (_: SecurityException) {}
    }

    private fun showAnomalyDialog(state: SimulationUiState.AnomalyDetected) {
        if (anomalyDialog?.isShowing == true) return

        val anomaly = state.anomaly
        val timeStr = anomaly.simulatedTimestamp ?: state.state.currentDate ?: "Unknown Time"
        val consumedWh = anomaly.currentConsumptionWh ?: state.state.currentReading ?: 0.0
        val typicalWh = anomaly.typicalConsumptionWh ?: 0.0
        val diffWh = anomaly.differenceWh ?: (consumedWh - typicalWh)
        val pct = anomaly.percentageAboveTypical ?: 0.0
        val userMsg = anomaly.userMessage ?: "Unusual energy usage detected."

        val messageText = buildString {
            append("Timestamp: ").append(timeStr).append("\n")
            append("Consumed: ").append(String.format(Locale.US, "%.3f Wh", consumedWh)).append("\n")
            append("Typical usage: ").append(String.format(Locale.US, "%.3f Wh", typicalWh)).append("\n")
            append("Difference: ").append(String.format(Locale.US, "%.3f Wh", diffWh)).append("\n")
            append("Above Typical: ").append(String.format(Locale.US, "%.1f%%", pct)).append("\n\n")
            append(userMsg)
        }

        anomalyDialog = AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Unusual Energy Usage Detected")
            .setMessage(messageText)
            .setPositiveButton("Acknowledge and Continue") { dialog, _ ->
                dialog.dismiss()
                anomalyDialog = null
                viewModel.resumeSimulation()
            }
            .setCancelable(false)
            .create()

        anomalyDialog?.show()
    }

    private fun dismissAnomalyDialog() {
        anomalyDialog?.dismiss()
        anomalyDialog = null
    }

    override fun onDestroyView() {
        dismissAnomalyDialog()
        super.onDestroyView()
        _binding = null
    }
}
