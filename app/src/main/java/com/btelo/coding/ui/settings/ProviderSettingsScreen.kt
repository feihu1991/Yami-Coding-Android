package com.btelo.coding.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.btelo.coding.ui.theme.AccentBlue
import com.btelo.coding.ui.theme.AppBackground
import com.btelo.coding.ui.theme.CardSurface
import com.btelo.coding.ui.theme.BorderSubtle
import com.btelo.coding.ui.theme.TextPrimary
import com.btelo.coding.ui.theme.TextSecondary
import com.btelo.coding.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    onBack: () -> Unit,
    viewModel: ProviderSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- Manage Providers ---
            SectionHeader("Manage Providers")
            Spacer(modifier = Modifier.height(8.dp))

            AiProvider.entries.forEach { provider ->
                ProviderOptionRow(
                    name = provider.displayName,
                    isSelected = state.selectedProvider == provider,
                    onClick = { viewModel.selectProvider(provider) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- AI Settings ---
            SectionHeader("AI Settings")
            Spacer(modifier = Modifier.height(8.dp))

            // Model selector
            var showModelDropdown by remember { mutableStateOf(false) }
            DropdownSelector(
                label = "Model",
                value = state.selectedModel.displayName,
                expanded = showModelDropdown,
                onToggle = { showModelDropdown = true },
                onDismiss = { showModelDropdown = false }
            ) {
                AiModel.entries
                    .filter { it.provider == state.selectedProvider }
                    .forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.selectModel(model)
                                showModelDropdown = false
                            }
                        )
                    }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Effort level
            Text("Effort", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EffortLevel.entries.forEach { level ->
                    val selected = state.effortLevel == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) AccentBlue.copy(alpha = 0.15f) else CardSurface)
                            .border(1.dp, if (selected) AccentBlue else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectEffort(level) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            level.displayName,
                            color = if (selected) AccentBlue else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Provider Details ---
            SectionHeader("Provider Details")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("CLI Version", state.cliVersion)
            DetailRow("Settings Scope", state.settingsScope)
            DetailRow("Adapter Version", state.adapterVersion)

            Spacer(modifier = Modifier.height(20.dp))

            // --- Usage Statistics ---
            SectionHeader("Usage Statistics")
            Spacer(modifier = Modifier.height(8.dp))
            StatCard("Sessions", state.totalSessions.toString())
            Spacer(modifier = Modifier.height(8.dp))
            StatCard("Messages", state.totalMessages.toString())
            Spacer(modifier = Modifier.height(8.dp))
            StatCard("Tokens", formatNumber(state.totalTokens))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProviderOptionRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentBlue.copy(alpha = 0.1f) else CardSurface)
            .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            color = if (isSelected) AccentBlue else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = TextPrimary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("▼", color = TextTertiary, fontSize = 12.sp)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatNumber(n: Long): String {
    return when {
        n >= 1_000_000 -> "${n / 1_000_000}.${(n % 1_000_000) / 100_000}M"
        n >= 1_000 -> "${n / 1_000}.${(n % 1_000) / 100}K"
        else -> n.toString()
    }
}
