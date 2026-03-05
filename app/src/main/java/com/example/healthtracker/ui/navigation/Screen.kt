package com.example.healthtracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reports : Screen("reports")

    // 记录页面
    object AddIntake : Screen("add_intake")
    object AddBodyData : Screen("add_body_data")
    object AddSleep : Screen("add_sleep")

    // 设置页面
    object Settings : Screen("settings")
    object UserProfile : Screen("user_profile")
    object MealPlans : Screen("meal_plans")
    object FoodManager : Screen("food_manager")

    // 数据管理
    object DataExport : Screen("data_export")
    object DataImport : Screen("data_import")
}