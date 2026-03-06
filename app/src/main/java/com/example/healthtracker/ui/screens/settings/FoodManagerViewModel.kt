package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodManagerUiState(
    val searchText: String = ""
)

@HiltViewModel
class FoodManagerViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodManagerUiState())
    val uiState = _uiState.asStateFlow()

    // 所有食物，按名称排序
    val allFoods = foodRepository.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 搜索过滤后的食物
    val filteredFoods = combine(_uiState, allFoods) { state, foods ->
        val sortedFoods = foods.sortedBy { it.name }
        if (state.searchText.isBlank()) {
            sortedFoods
        } else {
            sortedFoods.filter {
                it.name.contains(state.searchText, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 获取首字母列表（用于字母导航）
    val indexLetters: List<String>
        get() {
            val letters = mutableSetOf<String>()
            filteredFoods.value.forEach { food ->
                val firstChar = food.name.firstOrNull() ?: return@forEach
                // 中文按拼音首字母，英文直接取首字母
                val letter = getFirstLetter(firstChar)
                letters.add(letter)
            }
            return letters.sorted()
        }

    fun setSearchText(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text)
    }

    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch {
            if (food.isCustom) {
                foodRepository.deleteCustomFood(food.id)
            }
        }
    }

    fun toggleFavorite(food: FoodEntity) {
        viewModelScope.launch {
            foodRepository.toggleFavorite(food.id)
        }
    }

    // 获取字符的首字母（拼音首字母）
    fun getFirstLetter(char: Char): String {
        return when {
            char in 'A'..'Z' -> char.toString()
            char in 'a'..'z' -> char.uppercaseChar().toString()
            // 中文字符 - 简化处理，按实际首字母
            else -> {
                // 常见食物首字母映射
                when (char) {
                    '米' -> "M"
                    '面' -> "M"
                    '馒' -> "M"
                    '饭' -> "F"
                    '猪' -> "Z"
                    '牛' -> "N"
                    '羊' -> "Y"
                    '鸡' -> "J"
                    '鸭' -> "Y"
                    '鱼' -> "Y"
                    '虾' -> "X"
                    '蛋' -> "D"
                    '奶' -> "N"
                    '豆' -> "D"
                    '花' -> "H"
                    '坚' -> "J"
                    '蔬' -> "S"
                    '白' -> "B"
                    '西' -> "X"
                    '黄' -> "H"
                    '土' -> "T"
                    '苹' -> "P"
                    '香' -> "X"
                    '橙' -> "C"
                    '植' -> "Z"
                    '油' -> "Y"
                    else -> char.uppercase().take(1)
                }
            }
        }
    }
}