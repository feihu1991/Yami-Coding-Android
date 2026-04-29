package com.btelo.coding.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.btelo.coding.ui.agents.AgentsScreen
import com.btelo.coding.ui.browser.BrowserScreen
import com.btelo.coding.ui.chat.ChatScreen
import com.btelo.coding.ui.files.FilesScreen
import com.btelo.coding.ui.git.GitPanelScreen
import com.btelo.coding.ui.device.DevicesScreen
import com.btelo.coding.ui.notification.NotificationSettingsScreen
import com.btelo.coding.ui.scan.ScanScreen
import com.btelo.coding.ui.session.SessionListScreen
import com.btelo.coding.ui.settings.ProviderSettingsScreen
import com.btelo.coding.ui.team.TeamScreen

sealed class Screen(val route: String) {
    // Auth
    object Scan : Screen("auth/scan")

    // Main (tab container)
    object Main : Screen("main")

    // Tab destinations
    object Agents : Screen("main/agents")
    object Team : Screen("main/team")
    object Files : Screen("main/files")
    object Browser : Screen("main/browser")

    // Nested within Agents tab
    object Chat : Screen("main/agents/chat/{sessionId}") {
        fun createRoute(sessionId: String) = "main/agents/chat/$sessionId"
    }
    object SessionList : Screen("main/agents/sessions")
    object NotificationSettings : Screen("main/agents/notifications")
    object ProviderSettings : Screen("main/agents/settings")
    object Devices : Screen("main/agents/devices")
    object GitPanel : Screen("main/files/git/{repoPath}") {
        fun createRoute(repoPath: String) = "main/files/git/$repoPath"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Scan.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable(Screen.Scan.route) {
            ScanScreen(
                onConnected = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }

        // Main flow with tab navigation
        composable(Screen.Main.route) {
            MainScreen(
                onDisconnect = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onDisconnect: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Agents.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Agents tab — main chat interface
            composable(Screen.Agents.route) {
                AgentsScreen(
                    onSessionListOpen = {
                        navController.navigate(Screen.SessionList.route)
                    },
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.Chat.createRoute(sessionId))
                    },
                    onDisconnect = onDisconnect,
                    onNotificationClick = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onProviderSettingsClick = {
                        navController.navigate(Screen.ProviderSettings.route)
                    },
                    onDevicesClick = {
                        navController.navigate(Screen.Devices.route)
                    }
                )
            }

            // Team tab — placeholder
            composable(Screen.Team.route) {
                TeamScreen()
            }

            // Files tab — file browser
            composable(Screen.Files.route) {
                FilesScreen(
                    onGitClick = { repoPath ->
                        navController.navigate(Screen.GitPanel.createRoute(repoPath))
                    }
                )
            }

            // Git panel (nested within files)
            composable(
                route = Screen.GitPanel.route,
                arguments = listOf(
                    navArgument("repoPath") { type = NavType.StringType }
                )
            ) {
                GitPanelScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Browser tab — web proxy
            composable(Screen.Browser.route) {
                BrowserScreen()
            }

            // Chat screen (nested within agents)
            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                ChatScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Session list (nested within agents)
            composable(Screen.SessionList.route) {
                SessionListScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.Chat.createRoute(sessionId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Notification settings (nested within agents)
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Provider settings (nested within agents)
            composable(Screen.ProviderSettings.route) {
                ProviderSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Devices (nested within agents)
            composable(Screen.Devices.route) {
                DevicesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
