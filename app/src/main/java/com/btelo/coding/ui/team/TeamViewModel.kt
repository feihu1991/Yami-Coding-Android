package com.btelo.coding.ui.team

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TeamMember(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val isOnline: Boolean,
    val role: String = ""
)

data class SharedSession(
    val id: String,
    val name: String,
    val owner: String,
    val memberCount: Int,
    val lastActive: Long
)

data class TeamUiState(
    val members: List<TeamMember> = emptyList(),
    val sharedSessions: List<SharedSession> = emptyList(),
    val inviteCode: String = "BTELO-TEAM-42X7"
)

@HiltViewModel
class TeamViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _state.value = TeamUiState(
            members = listOf(
                TeamMember("1", "You", 0xFF3B82F6, true, "Owner"),
                TeamMember("2", "Alice", 0xFF22C55E, true, "Developer"),
                TeamMember("3", "Bob", 0xFFF59E0B, true, "Developer"),
                TeamMember("4", "Carol", 0xFF8B5CF6, false, "Reviewer"),
                TeamMember("5", "Dave", 0xFFEF4444, false, "Viewer")
            ),
            sharedSessions = listOf(
                SharedSession("s1", "api-refactor", "Alice", 3, System.currentTimeMillis() - 1800000),
                SharedSession("s2", "fix-auth-bug", "Bob", 2, System.currentTimeMillis() - 7200000),
                SharedSession("s3", "frontend-redesign", "You", 4, System.currentTimeMillis() - 14400000)
            )
        )
    }

    fun refreshInviteCode() {
        val suffix = (1000..9999).random()
        _state.value = _state.value.copy(inviteCode = "BTELO-TEAM-$suffix")
    }
}
