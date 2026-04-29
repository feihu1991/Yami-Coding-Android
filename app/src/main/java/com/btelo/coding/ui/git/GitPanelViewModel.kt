package com.btelo.coding.ui.git

import androidx.lifecycle.ViewModel
import com.btelo.coding.domain.model.GitCommit
import com.btelo.coding.domain.model.GitDiffFile
import com.btelo.coding.domain.model.GitFileChange
import com.btelo.coding.domain.model.GitFileStatus
import com.btelo.coding.domain.model.GitStash
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class GitTab { CHANGES, STASH, COMMITS, DIFF, TREE }

data class GitPanelState(
    val repoName: String = "btelo-server",
    val currentBranch: String = "main",
    val aheadCount: Int = 2,
    val behindCount: Int = 0,
    val selectedTab: GitTab = GitTab.CHANGES,
    val changes: List<GitFileChange> = emptyList(),
    val stashes: List<GitStash> = emptyList(),
    val commits: List<GitCommit> = emptyList(),
    val diffFiles: List<GitDiffFile> = emptyList(),
    val selectedDiff: GitDiffFile? = null,
    val treeFiles: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GitPanelViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(GitPanelState())
    val state: StateFlow<GitPanelState> = _state.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _state.value = GitPanelState(
            changes = listOf(
                GitFileChange("src/main/kotlin/Main.kt", GitFileStatus.MODIFIED, 12, 3),
                GitFileChange("src/main/kotlin/Utils.kt", GitFileStatus.MODIFIED, 5, 1),
                GitFileChange("build.gradle.kts", GitFileStatus.MODIFIED, 2, 0),
                GitFileChange("README.md", GitFileStatus.MODIFIED, 8, 2),
                GitFileChange("src/test/Test.kt", GitFileStatus.ADDED, 45, 0),
                GitFileChange("config/old.yaml", GitFileStatus.DELETED, 0, 32)
            ),
            stashes = listOf(
                GitStash(0, "WIP: refactor auth module", "main", System.currentTimeMillis() - 3600000),
                GitStash(1, "temp: debug logging", "feature/api", System.currentTimeMillis() - 86400000)
            ),
            commits = listOf(
                GitCommit("a1b2c3d", "feat: add user authentication", "dev", System.currentTimeMillis() - 3600000, 5),
                GitCommit("e4f5g6h", "fix: resolve null pointer in parser", "dev", System.currentTimeMillis() - 7200000, 2),
                GitCommit("i7j8k9l", "refactor: extract common utilities", "dev", System.currentTimeMillis() - 14400000, 8),
                GitCommit("m0n1o2p", "docs: update API documentation", "dev", System.currentTimeMillis() - 28800000, 1),
                GitCommit("q3r4s5t", "test: add integration tests", "dev", System.currentTimeMillis() - 43200000, 3)
            ),
            diffFiles = listOf(
                GitDiffFile(
                    "src/main/kotlin/Main.kt",
                    "fun main() {\n    println(\"Hello\")\n}",
                    "fun main() {\n    println(\"Hello World\")\n    startServer()\n}"
                )
            ),
            treeFiles = listOf(
                "src/", "src/main/", "src/main/kotlin/", "src/main/kotlin/Main.kt",
                "src/main/kotlin/Utils.kt", "src/test/", "src/test/Test.kt",
                "build.gradle.kts", "README.md", "config/"
            )
        )
    }

    fun selectTab(tab: GitTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun selectDiff(diff: GitDiffFile) {
        _state.value = _state.value.copy(selectedDiff = diff)
    }

    fun pushChanges() {
        _state.value = _state.value.copy(isLoading = true)
        // TODO: call server git push endpoint
        _state.value = _state.value.copy(isLoading = false, aheadCount = 0)
    }
}
