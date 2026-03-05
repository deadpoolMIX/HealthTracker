package com.example.healthtracker.data.local.database

import com.example.healthtracker.data.local.entity.FoodEntity

/**
 * 内置食品营养数据（参考中国食物成分表）
 * 数据为每100g可食部含量
 */
object DefaultFoods {
    val foods = listOf(
        // 主食类
        FoodEntity(
            name = "米饭",
            category = "主食",
            calories = 116.0,
            carbohydrates = 25.9,
            protein = 2.6,
            fat = 0.3,
            icon = "rice"
        ),
        FoodEntity(
            name = "馒头",
            category = "主食",
            calories = 223.0,
            carbohydrates = 47.0,
            protein = 7.0,
            fat = 1.1,
            icon = "bread"
        ),
        FoodEntity(
            name = "面条",
            category = "主食",
            calories = 137.0,
            carbohydrates = 28.6,
            protein = 4.5,
            fat = 0.5,
            icon = "noodle"
        ),
        // 肉类
        FoodEntity(
            name = "猪肉（瘦）",
            category = "肉类",
            calories = 143.0,
            carbohydrates = 0.2,
            protein = 20.3,
            fat = 6.2,
            icon = "meat"
        ),
        FoodEntity(
            name = "牛肉（瘦）",
            category = "肉类",
            calories = 106.0,
            carbohydrates = 0.1,
            protein = 20.2,
            fat = 2.3,
            icon = "meat"
        ),
        FoodEntity(
            name = "鸡肉",
            category = "肉类",
            calories = 167.0,
            carbohydrates = 0.0,
            protein = 19.3,
            fat = 9.4,
            icon = "chicken"
        ),
        // 蔬菜类
        FoodEntity(
            name = "白菜",
            category = "蔬菜",
            calories = 13.0,
            carbohydrates = 2.4,
            protein = 1.2,
            fat = 0.2,
            icon = "vegetable"
        ),
        FoodEntity(
            name = "西红柿",
            category = "蔬菜",
            calories = 15.0,
            carbohydrates = 3.3,
            protein = 0.9,
            fat = 0.2,
            icon = "vegetable"
        ),
        FoodEntity(
            name = "黄瓜",
            category = "蔬菜",
            calories = 15.0,
            carbohydrates = 2.9,
            protein = 0.8,
            fat = 0.2,
            icon = "vegetable"
        ),
        FoodEntity(
            name = "土豆",
            category = "蔬菜",
            calories = 76.0,
            carbohydrates = 17.2,
            protein = 2.0,
            fat = 0.2,
            icon = "vegetable"
        ),
        // 水果类
        FoodEntity(
            name = "苹果",
            category = "水果",
            calories = 52.0,
            carbohydrates = 13.5,
            protein = 0.2,
            fat = 0.2,
            icon = "fruit"
        ),
        FoodEntity(
            name = "香蕉",
            category = "水果",
            calories = 93.0,
            carbohydrates = 20.7,
            protein = 1.2,
            fat = 0.2,
            icon = "fruit"
        ),
        FoodEntity(
            name = "橙子",
            category = "水果",
            calories = 48.0,
            carbohydrates = 11.8,
            protein = 0.8,
            fat = 0.2,
            icon = "fruit"
        ),
        // 蛋奶类
        FoodEntity(
            name = "鸡蛋",
            category = "蛋奶",
            calories = 144.0,
            carbohydrates = 0.1,
            protein = 13.3,
            fat = 8.8,
            icon = "egg"
        ),
        FoodEntity(
            name = "牛奶",
            category = "蛋奶",
            calories = 54.0,
            carbohydrates = 3.4,
            protein = 3.0,
            fat = 3.2,
            icon = "milk"
        ),
        // 豆类
        FoodEntity(
            name = "豆腐",
            category = "豆类",
            calories = 76.0,
            carbohydrates = 1.8,
            protein = 8.1,
            fat = 3.7,
            icon = "bean"
        ),
        FoodEntity(
            name = "黄豆",
            category = "豆类",
            calories = 359.0,
            carbohydrates = 18.6,
            protein = 35.0,
            fat = 16.0,
            icon = "bean"
        ),
        // 坚果类
        FoodEntity(
            name = "花生",
            category = "坚果",
            calories = 574.0,
            carbohydrates = 13.0,
            protein = 24.8,
            fat = 48.0,
            icon = "nut"
        ),
        // 海鲜类
        FoodEntity(
            name = "虾",
            category = "海鲜",
            calories = 87.0,
            carbohydrates = 0.0,
            protein = 18.6,
            fat = 0.8,
            icon = "seafood"
        ),
        FoodEntity(
            name = "鱼（鲤鱼）",
            category = "海鲜",
            calories = 109.0,
            carbohydrates = 0.0,
            protein = 17.6,
            fat = 4.1,
            icon = "seafood"
        ),
        // 油脂类
        FoodEntity(
            name = "植物油",
            category = "油脂",
            calories = 899.0,
            carbohydrates = 0.0,
            protein = 0.0,
            fat = 99.9,
            icon = "oil"
        )
    )
}