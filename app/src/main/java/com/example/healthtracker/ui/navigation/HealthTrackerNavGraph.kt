package com.example.healthtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthtracker.ui.screens.intake.AddIntakeScreen
import com.example.healthtracker.ui.screens.body.AddBodyDataScreen
import com.example.healthtracker.ui.screens.sleep.AddSleepScreen
import com.example.healthtracker.ui.screens.settings.SettingsScreen
import com.example.healthtracker.ui.screens.settings.UserProfileScreen
import com.example.healthtracker.ui.screens.MealPlansScreen
import com.example.healthtracker.ui.screens.FoodManagerScreen
import com.example.healthtracker.ui.screens.settings.DataExportScreen
import com.example.healthtracker.ui.screens.settings.DataImportScreen
import com.example.healthtracker.ui.screens.settings.ThemeSettingsScreen
import com.example.healthtracker.ui.screens.settings.FoodDataImportScreen
import com.example.healthtracker.ui.screens.mealplan.AddMealPlanScreen
import com.example.healthtracker.ui.screens.calendar.CalendarScreen
import com.example.healthtracker.ui.screens.food.AddCustomFoodScreen
import com.example.healthtracker.ui.screens.food.EditFoodScreen
import com.example.healthtracker.ui.screens.intake.CustomFoodInputScreen
import com.example.healthtracker.ui.screens.intake.EditIntakeScreen
import com.example.healthtracker.ui.screens.reports.NutritionDetailScreen
import com.example.healthtracker.ui.screens.reports.BodyDataDetailScreen
import com.example.healthtracker.ui.screens.reports.SleepDetailScreen
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
                onNavigateToCustomFood = { navController.navigate(Screen.AddCustomFood.route) },
                onNavigateToEditFood = { foodId ->
                    navController.navigate("edit_food/$foodId")
                },
                onNavigateToEditIntake = { recordId ->
                    navController.navigate("edit_intake/$recordId")
                },
                onNavigateToEditMealPlan = { planId ->
                    navController.navigate("edit_meal_plan/$planId")
                },
                onNavigateToNutritionDetail = { navController.navigate(Screen.NutritionDetail.route) },
                onNavigateToBodyDataDetail = { navController.navigate(Screen.BodyDataDetail.route) },
                onNavigateToSleepDetail = { navController.navigate(Screen.SleepDetail.route) }
            )
        }

        // 报表详情页面
        composable(Screen.NutritionDetail.route) {
            NutritionDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BodyDataDetail.route) {
            BodyDataDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SleepDetail.route) {
            SleepDetailScreen(
                onNavigateBack = { navController.popBackStack() }
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
                onNavigateBack = {
                    // 保存后直接返回首页（清除返回栈）
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToCustomFood = { _ ->
                    navController.navigate(Screen.AddCustomFood.route)
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

        // 编辑自定义食物页面
        composable(
            route = "edit_food/{foodId}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getLong("foodId") ?: 0L
            com.example.healthtracker.ui.screens.food.EditFoodScreen(
                foodId = foodId,
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomFood = { navController.navigate(Screen.AddCustomFood.route) }
            )
        }

        // 编辑饮食计划
        composable(
            route = Screen.EditMealPlan.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) {
            AddMealPlanScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomFood = { navController.navigate(Screen.AddCustomFood.route) }
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
                onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings.route) },
                onNavigateToFoodDataImport = { navController.navigate(Screen.FoodDataImport.route) }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCustomFood = { navController.navigate(Screen.AddCustomFood.route) }
            )
        }

        // 添加自定义食物页面
        composable(Screen.AddCustomFood.route) {
            AddCustomFoodScreen(
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

        // 食物数据导入
        composable(Screen.FoodDataImport.route) {
            FoodDataImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}