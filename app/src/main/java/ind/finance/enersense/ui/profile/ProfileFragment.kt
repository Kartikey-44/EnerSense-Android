package ind.finance.enersense.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ind.finance.enersense.R
import ind.finance.enersense.databinding.FragmentProfileBinding
import ind.finance.enersense.session.SessionManager
import ind.finance.enersense.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val factory = ProfileViewModelFactory(sessionManager)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val clearSessionAction = {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Session")
                .setMessage("Are you sure you want to clear the local session IDs?")
                .setPositiveButton("Clear") { dialog, _ ->
                    viewModel.clearSession()
                    Toast.makeText(requireContext(), "Session cleared successfully.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnClearSession.setOnClickListener { clearSessionAction() }
        binding.cardSignOut.setOnClickListener { clearSessionAction() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collectLatest { state ->
                    if (state.isConfigured) {
                        binding.tvConsumerId.text = "Consumer ID: ${state.consumerId}"
                        binding.tvMeterId.text = "Meter ID: ${state.meterId}"
                        binding.tvSessionStatus.text = "Session: Active & Configured"
                        binding.tvSessionStatus.setTextColor(resources.getColor(R.color.brand_primary, null))
                        binding.cardSignOut.visibility = View.VISIBLE
                    } else {
                        binding.tvConsumerId.text = "Consumer ID: Not Configured"
                        binding.tvMeterId.text = "Meter ID: Not Configured"
                        binding.tvSessionStatus.text = "Session: No Active Session"
                        binding.tvSessionStatus.setTextColor(resources.getColor(R.color.status_error_red, null))
                        binding.cardSignOut.visibility = View.GONE
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
