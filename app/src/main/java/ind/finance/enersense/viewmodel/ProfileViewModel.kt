package ind.finance.enersense.viewmodel

import androidx.lifecycle.ViewModel
import ind.finance.enersense.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileState(
    val consumerId: String,
    val meterId: String,
    val isConfigured: Boolean
)

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _profileState = MutableStateFlow(loadState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private fun loadState(): ProfileState {
        val cid = sessionManager.consumerId
        val mid = sessionManager.meterId
        val configured = (cid.isNotBlank() && mid.isNotBlank())
        return ProfileState(cid, mid, configured)
    }

    fun refreshState() {
        _profileState.value = loadState()
    }

    fun clearSession() {
        sessionManager.clearSession()
        refreshState()
    }
}
