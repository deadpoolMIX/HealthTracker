package com.example.healthtracker.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthtracker.ui.screens.home.HomeScreen
import com.example.healthtracker.ui.screens.ReportsScreen

/**
 * 主容器页面
 * 包含首页和报表两个同级页面，通过底部导航栏切换
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToAddIntake: () -> Unit,
    onNavigateToAddBodyData: () -> Unit,
    onNavigateToAddSleep: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToDataExport: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        if (currentRoute != Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Filled.Home, contentDescription = "首页")
                    },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Reports.route,
                    onClick = {
                        if (currentRoute != Screen.Reports.route) {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Outlined.BarChart, contentDescription = "报表")
                    },
                    label = { Text("报表") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddIntake = onNavigateToAddIntake,
                    onNavigateToAddBodyData = onNavigateToAddBodyData,
                    onNavigateToAddSleep = onNavigateToAddSleep,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToUserProfile = onNavigateToUserProfile
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(
                    onNavigateToDataExport = onNavigateToDataExport
                )
            }
        }
    }
}