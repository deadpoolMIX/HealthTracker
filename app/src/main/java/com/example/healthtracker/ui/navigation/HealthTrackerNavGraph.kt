package com.example.healthtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthtracker.ui.screens.home.HomeScreen
import com.example.healthtracker.ui.screens.ReportsScreen
import com.example.healthtracker.ui.screens.AddIntakeScreen
import com.example.healthtracker.ui.screens.AddBodyDataScreen
import com.example.healthtracker.ui.screens.AddSleepScreen
import com.example.healthtracker.ui.screens.settings.SettingsScreen
import com.example.healthtracker.ui.screens.UserProfileScreen
import com.example.healthtracker.ui.screens.MealPlansScreen
import com.example.healthtracker.ui.screens.FoodManagerScreen
import com.example.healthtracker.ui.screens.DataExportScreen
import com.example.healthtracker.ui.screens.DataImportScreen

@Composable
fun HealthTrackerNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 主页面
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToAddIntake = { navController.navigate(Screen.AddIntake.route) },
                onNavigateToAddBodyData = { navController.navigate(Screen.AddBodyData.route) },
                onNavigateToAddSleep = { navController.navigate(Screen.AddSleep.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToUserProfile = { navController.navigate(Screen.UserProfile.route) }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDataExport = { navController.navigate(Screen.DataExport.route) }
            )
        }

        // 记录页面
        composable(Screen.AddIntake.route) {
            AddIntakeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddBodyData.route) {
            AddBodyDataScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddSleep.route) {
            AddSleepScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 设置页面
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { navController.navigate(Screen.UserProfile.route) },
                onNavigateToMealPlans = { navController.navigate(Screen.MealPlans.route) },
                onNavigateToFoodManager = { navController.navigate(Screen.FoodManager.route) },
                onNavigateToDataExport = { navController.navigate(Screen.DataExport.route) }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MealPlans.route) {
            MealPlansScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FoodManager.route) {
            FoodManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 数据管理
        composable(Screen.DataExport.route) {
            DataExportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDataImport = { navController.navigate(Screen.DataImport.route) }
            )
        }

        composable(Screen.DataImport.route) {
            DataImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}