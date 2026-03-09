package com.example.healthtracker.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCustomFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun saveCustomFood(
        name: String,
        icon: String,
        calories: Double,
        carbs: Double,
        protein: Double,
        fat: Double,
        unit: String?,
        gramsPerUnit: Double?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val food = FoodEntity(
                    name = name,
                    category = getCategoryFromName(name),
                    calories = calories,
                    carbohydrates = carbs,
                    protein = protein,
                    fat = fat,
                    icon = icon,
                    isCustom = true,
                    unit = unit,
                    gramsPerUnit = gramsPerUnit
                )
                foodRepository.insertFood(food)
                _saveSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun getCategoryFromName(name: String): String {
        val nameLower = name.lowercase()
        return when {
            nameLower.contains("米") || nameLower.contains("面") ||
            nameLower.contains("饭") || nameLower.contains("粥") ||
            nameLower.contains("面包") || nameLower.contains("馒头") -> "主食"

            nameLower.contains("猪") || nameLower.contains("牛") ||
            nameLower.contains("羊") || nameLower.contains("鸡") ||
            nameLower.contains("鸭") || nameLower.contains("肉") -> "肉类"

            nameLower.contains("鱼") || nameLower.contains("虾") ||
            nameLower.contains("蟹") || nameLower.contains("海鲜") -> "海鲜"

            nameLower.contains("蛋") -> "蛋类"

            nameLower.contains("奶") || nameLower.contains("牛乳") ||
            nameLower.contains("酸奶") || nameLower.contains("乳") -> "奶制品"

            nameLower.contains("豆") || nameLower.contains("豆腐") -> "豆类"

            nameLower.contains("蔬") || nameLower.contains("菜") ||
            nameLower.contains("白菜") || nameLower.contains("萝卜") ||
            nameLower.contains("番茄") || nameLower.contains("黄瓜") -> "蔬菜"

            nameLower.contains("果") || nameLower.contains("苹果") ||
            nameLower.contains("香蕉") || nameLower.contains("橙") -> "水果"

            nameLower.contains("油") || nameLower.contains("酱") -> "调味品"

            nameLower.contains("饮") || nameLower.contains("茶") ||
            nameLower.contains("咖啡") || nameLower.contains("果汁") -> "饮品"

            else -> "其他"
        }
    }
}