package com.example.healthtracker.ui.navigation

sealed class Screen(val route: String) {
    // 主容器（包含首页、报表、食物库、饮食计划）
    object Main : Screen("main")

    object Home : Screen("home")
    object FoodLibrary : Screen("food_library")
    object MealPlan : Screen("meal_plan")
    object Reports : Screen("reports")
    object Calendar : Screen("calendar")

    // 报表详情页面
    object NutritionDetail : Screen("nutrition_detail")
    object BodyDataDetail : Screen("body_data_detail")
    object SleepDetail : Screen("sleep_detail")

    // 记录页面
    object AddIntake : Screen("add_intake")
    object EditIntake : Screen("edit_intake/{recordId}")
    object CustomFoodInput : Screen("custom_food_input?foodName={foodName}&calories={calories}&carbs={carbs}&protein={protein}&fat={fat}&mealType={mealType}&isFromFoodLibrary={isFromFoodLibrary}")
    object AddCustomFood : Screen("add_custom_food")
    object AddBodyData : Screen("add_body_data")
    object AddSleep : Screen("add_sleep")
    object AddCycleFood : Screen("add_cycle_food")
    object EditCycleFood : Screen("edit_cycle_food/{cycleFoodId}")

    // 饮食计划
    object AddMealPlan : Screen("add_meal_plan")
    object EditMealPlan : Screen("edit_meal_plan/{planId}")

    // 设置页面
    object Settings : Screen("settings")
    object UserProfile : Screen("user_profile")
    object MealPlans : Screen("meal_plans")
    object FoodManager : Screen("food_manager")
    object ThemeSettings : Screen("theme_settings")
    object About : Screen("about")

    // 数据管理
    object DataExport : Screen("data_export")
    object DataImport : Screen("data_import")

    // 食物数据导入
    object FoodDataImport : Screen("food_data_import")

    // 测试数据生成
    object TestData : Screen("test_data")
}