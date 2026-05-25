package com.example.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ScanSuiteApplication
import com.example.presentation.generate.GenerateViewModel
import com.example.presentation.scan.ScanViewModel
import com.example.presentation.sessions.SessionsViewModel
import com.example.ui.generate.GenerateScreen
import com.example.ui.scan.ScanScreen
import com.example.ui.sessions.SessionsScreen

sealed class AppScreen(val route: String, val title: String) {
    object Scan : AppScreen("scan", "Scanner")
    object Sessions : AppScreen("sessions", "Batches")
    object Generate : AppScreen("generate", "Generator")
}

@Composable
fun AppNavigation(
    isExpandedScreen: Boolean = false
) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as ScanSuiteApplication
    val container = context.container

    // Set up ViewModel providers with clean manual injection
    val scanViewModel: ScanViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScanViewModel.provideFactory(
            container.saveScannedCodeUseCase,
            container.getSessionCodesUseCase,
            container.exportSessionUseCase,
            container.scanRepository
        )
    )

    val sessionsViewModel: SessionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SessionsViewModel.provideFactory(
            container.getAllSessionsUseCase,
            container.getSessionCodesUseCase,
            container.deleteSessionUseCase,
            container.exportSessionUseCase
        )
    )

    val generateViewModel: GenerateViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = GenerateViewModel.provideFactory(
            container.generateQrCodeUseCase
        )
    )

    if (isExpandedScreen) {
        // Landscape or Tablet Layout: Use side NavigationRail
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(navController)
            
            NavHost(
                navController = navController,
                startDestination = AppScreen.Scan.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(AppScreen.Scan.route) {
                    ScanScreen(viewModel = scanViewModel)
                }
                composable(AppScreen.Sessions.route) {
                    SessionsScreen(viewModel = sessionsViewModel)
                }
                composable(AppScreen.Generate.route) {
                    GenerateScreen(viewModel = generateViewModel)
                }
            }
        }
    } else {
        // Standard Portrait Mobile Layout: Use Bottom NavigationBar
        Scaffold(
            bottomBar = {
                AppNavigationBar(navController)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppScreen.Scan.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppScreen.Scan.route) {
                    ScanScreen(viewModel = scanViewModel)
                }
                composable(AppScreen.Sessions.route) {
                    SessionsScreen(viewModel = sessionsViewModel)
                }
                composable(AppScreen.Generate.route) {
                    GenerateScreen(viewModel = generateViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Triple(AppScreen.Scan, Icons.Default.PlayArrow, "Active scanning beam"),
        Triple(AppScreen.Sessions, Icons.Default.List, "Historical batches list"),
        Triple(AppScreen.Generate, Icons.Default.Build, "Custom QR parameters")
    )

    NavigationBar {
        navItems.forEach { (screen, icon, desc) ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = desc) },
                label = { Text(screen.title) }
            )
        }
    }
}

@Composable
fun AppNavigationRail(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Triple(AppScreen.Scan, Icons.Default.PlayArrow, "Active scanning beam"),
        Triple(AppScreen.Sessions, Icons.Default.List, "Historical batches list"),
        Triple(AppScreen.Generate, Icons.Default.Build, "Custom QR parameters")
    )

    NavigationRail(
        modifier = Modifier.fillMaxHeight().width(80.dp)
    ) {
        navItems.forEach { (screen, icon, desc) ->
            NavigationRailItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = desc) },
                label = { Text(screen.title) }
            )
        }
    }
}
