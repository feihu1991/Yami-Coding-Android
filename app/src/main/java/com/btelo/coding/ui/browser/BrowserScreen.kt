package com.btelo.coding.ui.browser

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.btelo.coding.domain.model.ProxyEntry
import com.btelo.coding.domain.model.ProxyStatus
import com.btelo.coding.ui.theme.AccentBlue
import com.btelo.coding.ui.theme.AppBackground
import com.btelo.coding.ui.theme.CardSurface
import com.btelo.coding.ui.theme.GreenSuccess
import com.btelo.coding.ui.theme.RedError
import com.btelo.coding.ui.theme.TextPrimary
import com.btelo.coding.ui.theme.TextSecondary
import com.btelo.coding.ui.theme.TextTertiary
import com.btelo.coding.ui.theme.ThinkingPurple

@Composable
fun BrowserScreen(viewModel: BrowserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var portInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Computer,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Web Proxy",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Access local dev servers and websites remotely",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).clickable { viewModel.showAddPortDialog() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Power, "Add Port", tint = ThinkingPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Port Proxy", color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f).clickable { viewModel.showAddWebsiteDialog() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Language, "Add Website", tint = ThinkingPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Website", color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Proxies header with auto toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Proxies", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Auto-proxy ports", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.autoProxyEnabled,
                        onCheckedChange = viewModel::setAutoProxy,
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue, checkedTrackColor = AccentBlue.copy(alpha = 0.4f))
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Proxy entries
            items(uiState.proxies) { proxy ->
                ProxyEntryRow(
                    proxy = proxy,
                    onRefresh = { viewModel.refreshProxy(proxy.id) },
                    onRetry = { viewModel.retryProxy(proxy.id) },
                    onClose = { viewModel.closeProxy(proxy.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Add Port Proxy dialog
    if (uiState.showAddPortDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Add Port Proxy", color = TextPrimary) },
            text = {
                Column {
                    Text("Enter a local port number to proxy:", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = portInput,
                        onValueChange = { portInput = it },
                        placeholder = { Text("e.g. 8080", color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addPortProxy(portInput)
                    portInput = ""
                }) {
                    Text("Add", color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissDialogs()
                    portInput = ""
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }

    // Add Website Proxy dialog
    if (uiState.showAddWebsiteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Add Website Proxy", color = TextPrimary) },
            text = {
                Column {
                    Text("Enter a website URL to proxy:", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("e.g. https://example.com", color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addWebsiteProxy(urlInput)
                    urlInput = ""
                }) {
                    Text("Add", color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissDialogs()
                    urlInput = ""
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
private fun ProxyEntryRow(
    proxy: ProxyEntry,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Main row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CardSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (proxy.status == ProxyStatus.ACTIVE) GreenSuccess.copy(alpha = 0.2f) else RedError.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Computer,
                    contentDescription = null,
                    tint = if (proxy.status == ProxyStatus.ACTIVE) GreenSuccess else RedError,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(proxy.address, color = TextPrimary, fontSize = 14.sp)
                Text(proxy.fullAddress, color = TextSecondary, fontSize = 12.sp)
            }
            Text("→", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.clickable(onClick = onRefresh))
        }

        // Error details with browser toolbar
        if (proxy.status == ProxyStatus.ERROR && !proxy.errorMessage.isNullOrBlank()) {
            Text(
                proxy.errorMessage,
                color = RedError,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable(onClick = onClose))
                Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { })
                Icon(Icons.Default.ArrowForward, "Forward", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { })
                Icon(Icons.Default.Refresh, "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable(onClick = onRetry))
                Icon(Icons.Default.Block, "Block", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { })
                Icon(Icons.Default.Keyboard, "Keyboard", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { })
            }
        }
    }
}
