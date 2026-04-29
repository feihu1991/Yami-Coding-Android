package com.btelo.coding.ui.git

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.btelo.coding.domain.model.GitFileChange
import com.btelo.coding.domain.model.GitFileStatus
import com.btelo.coding.domain.model.GitCommit
import com.btelo.coding.domain.model.GitStash
import com.btelo.coding.domain.model.GitDiffFile
import com.btelo.coding.ui.theme.AccentBlue
import com.btelo.coding.ui.theme.AppBackground
import com.btelo.coding.ui.theme.CardSurface
import com.btelo.coding.ui.theme.CodeBlockBg
import com.btelo.coding.ui.theme.GreenSuccess
import com.btelo.coding.ui.theme.RedError
import com.btelo.coding.ui.theme.TextPrimary
import com.btelo.coding.ui.theme.TextSecondary
import com.btelo.coding.ui.theme.TextTertiary
import com.btelo.coding.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitPanelScreen(
    onBack: () -> Unit,
    viewModel: GitPanelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.repoName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text(state.currentBranch, fontSize = 12.sp, color = AccentBlue)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    if (state.aheadCount > 0) {
                        Button(
                            onClick = viewModel::pushChanges,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push ${state.aheadCount}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            GitTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            // Tab content
            when (state.selectedTab) {
                GitTab.CHANGES -> ChangesTab(state.changes) { }
                GitTab.STASH -> StashTab(state.stashes)
                GitTab.COMMITS -> CommitsTab(state.commits)
                GitTab.DIFF -> DiffTab(state.diffFiles, state.selectedDiff, viewModel::selectDiff)
                GitTab.TREE -> TreeTab(state.treeFiles)
            }
        }
    }
}

@Composable
private fun GitTabRow(selectedTab: GitTab, onTabSelected: (GitTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GitTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.15f) else CardSurface)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                    color = if (isSelected) AccentBlue else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

// --- Changes Tab ---

@Composable
private fun ChangesTab(changes: List<GitFileChange>, onFileClick: (GitFileChange) -> Unit) {
    if (changes.isEmpty()) {
        EmptyState("No changes")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(changes) { change -> ChangeItem(change, onFileClick) }
        }
    }
}

@Composable
private fun ChangeItem(change: GitFileChange, onClick: (GitFileChange) -> Unit) {
    val statusColor = when (change.status) {
        GitFileStatus.MODIFIED -> WarningAmber
        GitFileStatus.ADDED -> GreenSuccess
        GitFileStatus.DELETED -> RedError
        GitFileStatus.RENAMED -> AccentBlue
        GitFileStatus.UNTRACKED -> TextTertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(change) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status badge
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(change.status.label, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // File path
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = change.path.substringAfterLast("/"),
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = change.path,
                color = TextTertiary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Additions / deletions
        if (change.additions > 0) {
            Text("+${change.additions}", color = GreenSuccess, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
        }
        if (change.deletions > 0) {
            Text("-${change.deletions}", color = RedError, fontSize = 12.sp)
        }
    }
}

// --- Stash Tab ---

@Composable
private fun StashTab(stashes: List<GitStash>) {
    if (stashes.isEmpty()) {
        EmptyState("No stashed changes")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(stashes) { stash ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurface)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("stash@{${stash.index}}: ${stash.message}", color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("on ${stash.branch}", color = AccentBlue, fontSize = 12.sp)
                    }
                    Text(formatTimeAgo(stash.timestamp), color = TextTertiary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// --- Commits Tab ---

@Composable
private fun CommitsTab(commits: List<GitCommit>) {
    if (commits.isEmpty()) {
        EmptyState("No commits")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(commits) { commit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Timeline dot
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(AccentBlue)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(TextTertiary.copy(alpha = 0.3f))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(commit.message, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            Text(commit.hash.take(7), color = AccentBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(commit.author, color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${commit.filesChanged} files", color = TextTertiary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(formatTimeAgo(commit.timestamp), color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- Diff Tab ---

@Composable
private fun DiffTab(
    diffFiles: List<GitDiffFile>,
    selectedDiff: GitDiffFile?,
    onSelectDiff: (GitDiffFile) -> Unit
) {
    if (selectedDiff != null) {
        // Diff detail view
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .clickable { onSelectDiff(selectedDiff) } // deselect
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("◀", color = AccentBlue, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDiff.path, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CodeBlockBg)
                    .padding(8.dp)
            ) {
                item {
                    Text(
                        text = buildDiffPreview(selectedDiff),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    } else if (diffFiles.isEmpty()) {
        EmptyState("No diffs available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(diffFiles) { diff ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDiff(diff) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(diff.path, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("→", color = TextTertiary, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- Tree Tab ---

@Composable
private fun TreeTab(files: List<String>) {
    if (files.isEmpty()) {
        EmptyState("No files")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(files) { path ->
                val isDir = path.endsWith("/")
                val indent = path.count { it == '/' } * 16
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(start = (indent + 16).dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDir) "📁" else "📄",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = path.removeSuffix("/").substringAfterLast("/"),
                        color = if (isDir) AccentBlue else TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- shared ---

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = TextTertiary, fontSize = 14.sp)
    }
}

private fun buildDiffPreview(diff: GitDiffFile): String {
    val sb = StringBuilder()
    sb.appendLine("--- a/${diff.path}")
    sb.appendLine("+++ b/${diff.path}")
    sb.appendLine("@@ -1,${diff.oldContent.lines().size} +1,${diff.newContent.lines().size} @@")
    diff.oldContent.lines().forEach { sb.appendLine("- $it") }
    diff.newContent.lines().forEach { sb.appendLine("+ $it") }
    return sb.toString()
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> dateFormat.format(Date(timestamp))
    }
}
