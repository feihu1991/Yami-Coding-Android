package com.btelo.coding.ui.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btelo.coding.data.remote.websocket.factory.ConnectionState
import com.btelo.coding.domain.model.Message
import com.btelo.coding.domain.model.SkillTag
import com.btelo.coding.domain.model.SkillTagType
import com.btelo.coding.domain.repository.AuthRepository
import com.btelo.coding.domain.repository.MessageRepository
import com.btelo.coding.data.local.DataStoreManager
import com.btelo.coding.domain.repository.SessionRepository
import com.btelo.coding.domain.voice.VoiceInputManager
import com.btelo.coding.domain.voice.VoiceInputState
import com.btelo.coding.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionInfo(
    val id: String,
    val name: String,
    val isConnected: Boolean = false,
    val unreadCount: Int = 0,
    val tokenCount: Int = 0
)

data class AgentsUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val currentSessionId: String? = null,
    val currentSessionName: String = "BTELO Coding",
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val streamingContent: String = "",
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    val tokenCount: Int = 0,
    val currentCommandTag: String? = null,
    val showSlashPanel: Boolean = false,
    val completionExpanded: Boolean = false,
    val skillTags: List<SkillTag> = listOf(
        SkillTag("1", "vibe-remote", SkillTagType.PATH, "/opt/vibe-remote"),
        SkillTag("2", "fix-auth", SkillTagType.FEATURE),
        SkillTag("3", "api-refactor", SkillTagType.FEATURE)
    ),
    val showTaskComplete: Boolean = false,
    val completedTaskName: String = "",
    val completedTaskDesc: String = "",
    val pinnedMessageIds: Set<String> = emptySet(),
    val quickActions: List<String> = listOf(
        "Build feature",
        "Fix bug",
        "Run tests",
        "Deploy"
    )
)

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager,
    private val voiceInputManager: VoiceInputManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    private val coroutineJobs = mutableListOf<Job>()

    init {
        loadSessions()
        observeConnectionState()
        observeVoiceInput()
    }

    private fun observeVoiceInput() {
        viewModelScope.launch {
            voiceInputManager.state.collect { state ->
                when (state) {
                    is VoiceInputState.Result -> {
                        _uiState.value = _uiState.value.copy(
                            inputText = _uiState.value.inputText + state.text
                        )
                        voiceInputManager.resetState()
                    }
                    is VoiceInputState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "语音: ${state.message}"
                        )
                        voiceInputManager.resetState()
                    }
                    else -> { /* Listening / Processing / Idle */ }
                }
            }
        }
    }

    private fun loadSessions() {
        val job = viewModelScope.launch {
            sessionRepository.getSessions().collect { sessions ->
                val sessionInfos = sessions.map { session ->
                    SessionInfo(
                        id = session.id,
                        name = session.name,
                        isConnected = session.isConnected,
                        tokenCount = session.tokenCount
                    )
                }
                _uiState.value = _uiState.value.copy(sessions = sessionInfos)

                // Auto-select first session if none selected
                if (_uiState.value.currentSessionId == null && sessionInfos.isNotEmpty()) {
                    switchSession(sessionInfos.first().id)
                }
            }
        }
        coroutineJobs.add(job)
    }

    private fun observeConnectionState() {
        val job = viewModelScope.launch {
            messageRepository.connectionState.collect { state ->
                val isConnected = state is ConnectionState.Connected
                val errorMessage = when (state) {
                    is ConnectionState.Error -> state.message
                    else -> null
                }
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    isConnected = isConnected,
                    errorMessage = errorMessage
                )
            }
        }
        coroutineJobs.add(job)
    }

    fun switchSession(sessionId: String) {
        // Cancel previous session jobs
        coroutineJobs.forEach { if (isActive(it)) it.cancel() }
        coroutineJobs.clear()

        _uiState.value = _uiState.value.copy(
            currentSessionId = sessionId,
            messages = emptyList(),
            streamingContent = "",
            isStreaming = false
        )

        // Find session name and token count
        val session = _uiState.value.sessions.find { it.id == sessionId }
        _uiState.value = _uiState.value.copy(
            currentSessionName = session?.name ?: "Claude",
            tokenCount = session?.tokenCount ?: 0
        )

        // Connect to session
        connectToSession(sessionId)
        loadMessages(sessionId)
        observeOutput(sessionId)
        observeConnectionState()
    }

    private fun connectToSession(sessionId: String) {
        val job = viewModelScope.launch {
            val serverAddress = authRepository.getServerAddress().firstOrNull() ?: ""
            val wsToken = authRepository.getWsTokenSync()
            val authToken = authRepository.getTokenSync()
            val token = wsToken ?: authToken ?: ""
            if (serverAddress.isNotBlank() && token.isNotBlank()) {
                messageRepository.connect(serverAddress, token, sessionId)
            }
        }
        coroutineJobs.add(job)
    }

    private fun loadMessages(sessionId: String) {
        val job = viewModelScope.launch {
            messageRepository.getMessages(sessionId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
        coroutineJobs.add(job)
    }

    private fun observeOutput(sessionId: String) {
        val job = viewModelScope.launch {
            messageRepository.observeOutput(sessionId).collect { message ->
                val current = _uiState.value.streamingContent
                val newContent = if (current.isEmpty()) {
                    message.content
                } else {
                    current + message.content
                }
                _uiState.value = _uiState.value.copy(
                    streamingContent = newContent,
                    isStreaming = true
                )

                // Auto-clear streaming after 2 seconds of inactivity
                kotlinx.coroutines.delay(2000)
                if (_uiState.value.isStreaming && _uiState.value.streamingContent == newContent) {
                    _uiState.value = _uiState.value.copy(
                        streamingContent = "",
                        isStreaming = false
                    )
                }
            }
        }
        coroutineJobs.add(job)
    }

    fun updateInputText(text: String) {
        val showSlashPanel = text.startsWith("/")
        val commandTag = if (showSlashPanel) {
            text.substringBefore(" ")
        } else null
        _uiState.value = _uiState.value.copy(
            inputText = text,
            showSlashPanel = showSlashPanel,
            currentCommandTag = commandTag
        )
    }

    fun dismissSlashPanel() {
        _uiState.value = _uiState.value.copy(showSlashPanel = false, currentCommandTag = null)
    }

    fun selectSlashCommand(command: com.btelo.coding.domain.model.SlashCommand) {
        _uiState.value = _uiState.value.copy(
            inputText = command.displayName + " ",
            showSlashPanel = false,
            currentCommandTag = command.displayName
        )
    }

    fun toggleCompletionExpanded() {
        _uiState.value = _uiState.value.copy(
            completionExpanded = !_uiState.value.completionExpanded
        )
    }

    fun toggleAiMode() {
        Logger.i("AgentsVM", "Toggle AI mode")
        // TODO: cycle through AI modes (Claude, GPT, etc.)
    }

    fun toggleFavorite() {
        Logger.i("AgentsVM", "Toggle favorite")
        // TODO: bookmark current session
    }

    fun insertCodeBlock() {
        val current = _uiState.value.inputText
        _uiState.value = _uiState.value.copy(inputText = "$current\n```\n\n```")
        Logger.i("AgentsVM", "Insert code block")
    }

    fun showToolsMenu() {
        Logger.i("AgentsVM", "Show tools menu")
        // TODO: show tools/actions menu
    }

    fun startVoiceInput() {
        voiceInputManager.startListening()
    }

    fun stopVoiceInput() {
        voiceInputManager.stopListening()
    }

    // --- Skill Tags ---

    fun addSkillTag(name: String, type: SkillTagType, path: String? = null) {
        if (name.isBlank()) return
        val tag = SkillTag(
            id = "tag-${System.currentTimeMillis()}",
            name = name.trim(),
            type = type,
            path = path
        )
        _uiState.value = _uiState.value.copy(
            skillTags = _uiState.value.skillTags + tag
        )
    }

    fun removeSkillTag(id: String) {
        _uiState.value = _uiState.value.copy(
            skillTags = _uiState.value.skillTags.filter { it.id != id }
        )
    }

    fun onSkillTagClick(tag: SkillTag) {
        when (tag.type) {
            SkillTagType.PATH -> {
                tag.path?.let { p ->
                    _uiState.value = _uiState.value.copy(
                        inputText = "$p "
                    )
                }
            }
            SkillTagType.FEATURE -> {
                _uiState.value = _uiState.value.copy(
                    inputText = "/${tag.name} "
                )
            }
        }
    }

    // --- Task Complete ---

    fun showTaskCompleteDialog(taskName: String, description: String = "") {
        _uiState.value = _uiState.value.copy(
            showTaskComplete = true,
            completedTaskName = taskName,
            completedTaskDesc = description
        )
    }

    fun dismissTaskCompleteDialog() {
        _uiState.value = _uiState.value.copy(
            showTaskComplete = false,
            completedTaskName = "",
            completedTaskDesc = ""
        )
    }

    // --- Message operations ---

    fun retryMessage(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        _uiState.value = _uiState.value.copy(inputText = message.content)
        sendMessage()
    }

    fun togglePinMessage(messageId: String) {
        val current = _uiState.value.pinnedMessageIds
        _uiState.value = _uiState.value.copy(
            pinnedMessageIds = if (messageId in current) current - messageId else current + messageId
        )
    }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        if (content.isBlank()) return

        val sessionId = _uiState.value.currentSessionId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                inputText = "",
                streamingContent = "",
                isStreaming = true
            )

            messageRepository.sendMessage(sessionId, content)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isStreaming = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun createSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.createSession("Claude", "claude")
                _uiState.value = _uiState.value.copy(
                    currentSessionId = session.id,
                    currentSessionName = session.name
                )
                switchSession(session.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _uiState.value.currentSessionId?.let { sessionId ->
                messageRepository.disconnect(sessionId)
            }
            dataStoreManager.clearConnection()
            _uiState.value = AgentsUiState()
        }
    }

    private fun isActive(job: Job): Boolean = job.isActive

    override fun onCleared() {
        super.onCleared()
        voiceInputManager.destroy()
        coroutineJobs.forEach { if (it.isActive) it.cancel() }
        coroutineJobs.clear()
    }
}
