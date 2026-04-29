package com.btelo.coding.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class AiProvider(val displayName: String) {
    CLAUDE("Claude Code"),
    CODEX("Codex"),
    GEMINI("Gemini")
}

enum class AiModel(val displayName: String, val provider: AiProvider) {
    CLAUDE_OPUS("Claude Opus 4.7", AiProvider.CLAUDE),
    CLAUDE_SONNET("Claude Sonnet 4.6", AiProvider.CLAUDE),
    CLAUDE_HAIKU("Claude Haiku 4.5", AiProvider.CLAUDE),
    CODEX_GPT5("GPT-5 Codex", AiProvider.CODEX),
    GEMINI_ULTRA("Gemini Ultra 2.5", AiProvider.GEMINI)
}

enum class EffortLevel(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

data class ProviderSettingsState(
    val selectedProvider: AiProvider = AiProvider.CLAUDE,
    val selectedModel: AiModel = AiModel.CLAUDE_OPUS,
    val effortLevel: EffortLevel = EffortLevel.MEDIUM,
    val cliVersion: String = "2.1.3",
    val settingsScope: String = "Project (.claude/settings.json)",
    val adapterVersion: String = "0.4.2",
    val totalSessions: Int = 28,
    val totalMessages: Int = 847,
    val totalTokens: Long = 1_250_000
)

@HiltViewModel
class ProviderSettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ProviderSettingsState())
    val state: StateFlow<ProviderSettingsState> = _state.asStateFlow()

    fun selectProvider(provider: AiProvider) {
        val defaultModel = AiModel.entries.first { it.provider == provider }
        _state.value = _state.value.copy(
            selectedProvider = provider,
            selectedModel = defaultModel
        )
    }

    fun selectModel(model: AiModel) {
        _state.value = _state.value.copy(selectedModel = model)
    }

    fun selectEffort(level: EffortLevel) {
        _state.value = _state.value.copy(effortLevel = level)
    }
}
