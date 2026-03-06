package com.example.healthtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthtracker.ui.screens.AddIntakeScreen
import com.example.healthtracker.ui.screens.AddBodyDataScreen
import com.example.healthtracker.ui.screens.AddSleepScreen
import com.example.healthtracker.ui.screens.settings.SettingsScreen
import com.example.healthtracker.ui.screens.settings.UserProfileScreen
import com.example.healthtracker.ui.screens.MealPlansScreen
import com.example.healthtracker.ui.screens.FoodManagerScreen
import com.example.healthtracker.ui.screens.settings.DataExportScreen
import com.example.healthtracker.ui.screens.settings.DataImportScreen
import com.example.healthtracker.ui.screens.settings.ThemeSettingsScreen
import com.example.healthtracker.ui.screens.mealplan.AddMealPlanScreen
import com.example.healthtracker.ui.screens.calendar.CalendarScreen
import com.example.healthtracker.ui.screens.intake.CustomFoodInputScreen
import com.example.healthtracker.ui.screens.intake.EditIntakeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HealthTrackerNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 主容器（包含首页、食物库、饮食计划、报表）
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAddIntake = { navController.navigate(Screen.AddIntake.route) },
                onNavigateToAddBodyData = { navController.navigate(Screen.AddBodyData.route) },
                onNavigateToAddSleep = { navController.navigate(Screen.AddSleep.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToUserProfile = { navController.navigate(Screen.UserProfile.route) },
                onNavigateToDataExport = { navController.navigate(Screen.DataExport.route) },
                onNavigateToAddMealPlan = { navController.navigate(Screen.AddMealPlan.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToCustomFood = { navController.navigate("custom_food_input?isFromFoodLibrary=true") },
                onNavigateToEditIntake = { recordId ->
                    navController.navigate("edit_intake/$recordId")
                }
            )
        }

        // 日历页面
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 记录页面 - 搜索食物
        composable(Screen.AddIntake.route) {
            AddIntakeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomFood = { _ ->
                    navController.navigate("custom_food_input?isFromFoodLibrary=true")
                },
                onNavigateToFoodDetail = { foodName, calories, carbs, protein, fat, mealType ->
                    val encodedName = URLEncoder.encode(foodName, StandardCharsets.UTF_8.toString())
                    navController.navigate(
                        "custom_food_input?foodName=$encodedName&calories=$calories&carbs=$carbs&protein=$protein&fat=$fat&mealType=$mealType&isFromFoodLibrary=false"
                    )
                }
            )
        }

        // 详细录入页面 - 带参数
        composable(
            route = "custom_food_input?foodName={foodName}&calories={calories}&carbs={carbs}&protein={protein}&fat={fat}&mealType={mealType}&isFromFoodLibrary={isFromFoodLibrary}",
            arguments = listOf(
                navArgument("foodName") { type = NavType.StringType; defaultValue = "" },
                navArgument("calories") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("carbs") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("protein") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("fat") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("mealType") { type = NavType.IntType; defaultValue = 0 },
                navArgument("isFromFoodLibrary") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val calories = backStackEntry.arguments?.getString("calories")?.toDoubleOrNull() ?: 0.0
            val carbs = backStackEntry.arguments?.getString("carbs")?.toDoubleOrNull() ?: 0.0
            val protein = backStackEntry.arguments?.getString("protein")?.toDoubleOrNull() ?: 0.0
            val fat = backStackEntry.arguments?.getString("fat")?.toDoubleOrNull() ?: 0.0
            val mealType = backStackEntry.arguments?.getInt("mealType") ?: 0
            val isFromFoodLibrary = backStackEntry.arguments?.getBoolean("isFromFoodLibrary") ?: false

            CustomFoodInputScreen(
                onNavigateBack = { navController.popBackStack() },
                initialFoodName = foodName,
                initialCalories = calories,
                initialCarbs = carbs,
                initialProtein = protein,
                initialFat = fat,
                initialMealType = mealType,
                isFromFoodLibrary = isFromFoodLibrary
            )
        }

        // 编辑摄入记录页面
        composable(
            route = "edit_intake/{recordId}",
            arguments = listOf(
                navArgument("recordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
            EditIntakeScreen(
                recordId = recordId,
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

        // 饮食计划
        composable(Screen.AddMealPlan.route) {
            AddMealPlanScreen(
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
                onNavigateToDataExport = { navController.navigate(Screen.DataExport.route) },
                onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings.route) }
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

        composable(Screen.ThemeSettings.route) {
            ThemeSettingsScreen(
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