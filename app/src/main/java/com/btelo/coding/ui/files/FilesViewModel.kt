package com.btelo.coding.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btelo.coding.domain.model.GitRepoInfo
import com.btelo.coding.domain.repository.SessionRepository
import com.btelo.coding.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilesUiState(
    val gitRepos: List<GitRepoInfo> = emptyList(),
    val currentPath: String = "/opt",
    val directories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _uiState.value = FilesUiState(
            gitRepos = listOf(
                GitRepoInfo("her", "/opt/her", "main", System.currentTimeMillis()),
                GitRepoInfo("next-dashboard", "/opt/next-dashboard", "main", System.currentTimeMillis()),
                GitRepoInfo("shopify-store", "/opt/shopify-store", "main", System.currentTimeMillis()),
                GitRepoInfo("go-api", "/opt/go-api", "main", System.currentTimeMillis()),
                GitRepoInfo("react-native-app", "/opt/react-native-app", "main", System.currentTimeMillis()),
                GitRepoInfo("btelo-server", "/opt/btelo-server", "main", System.currentTimeMillis())
            ),
            currentPath = "/opt",
            directories = listOf("homebrew", "source", "workspace", "projects")
        )
    }

    fun navigateToPath(path: String) {
        _uiState.value = _uiState.value.copy(currentPath = path)
        Logger.i("FilesVM", "Navigate to: $path")
    }

    fun createSessionAtPath(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dirName = path.substringAfterLast("/").ifBlank { "session" }
                sessionRepository.createSession(dirName, "claude")
                Logger.i("FilesVM", "Session created at: $path")
            } catch (e: Exception) {
                Logger.e("FilesVM", "Failed to create session: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearch() {
        _uiState.value = _uiState.value.copy(
            isSearchVisible = !_uiState.value.isSearchVisible,
            searchQuery = ""
        )
    }

    fun getFilteredRepos(): List<GitRepoInfo> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) return _uiState.value.gitRepos
        return _uiState.value.gitRepos.filter {
            it.name.lowercase().contains(query) || it.path.lowercase().contains(query)
        }
    }

    fun getFilteredDirs(): List<String> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) return _uiState.value.directories
        return _uiState.value.directories.filter { it.lowercase().contains(query) }
    }

    fun refreshCurrentPath() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        Logger.i("FilesVM", "Refreshing: ${_uiState.value.currentPath}")
        // TODO: call server API to list directories at current path
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
