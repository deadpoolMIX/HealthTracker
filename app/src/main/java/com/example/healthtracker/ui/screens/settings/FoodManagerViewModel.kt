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

    // 获取字符的首字母（拼音首字母）
    fun getFirstLetter(char: Char): String {
        return when {
            char in 'A'..'Z' -> char.toString()
            char in 'a'..'z' -> char.uppercaseChar().toString()
            // 中文字符 - 按拼音首字母映射
            else -> {
                // 常见食物首字母映射
                when (char) {
                    // A
                    '阿' -> "A"
                    '艾' -> "A"
                    // B
                    '白' -> "B"
                    '包' -> "B"
                    '冰' -> "B"
                    '菠' -> "B"
                    // C
                    '草' -> "C"
                    '茶' -> "C"
                    '橙' -> "C"
                    '春' -> "C"
                    '葱' -> "C"
                    '醋' -> "C"
                    // D
                    '大' -> "D"
                    '蛋' -> "D"
                    '豆' -> "D"
                    '冬' -> "D"
                    // E
                    '鹅' -> "E"
                    // F
                    '番' -> "F"
                    '饭' -> "F"
                    '蜂' -> "F"
                    '腐' -> "F"
                    // G
                    '甘' -> "G"
                    '瓜' -> "G"
                    '果' -> "G"
                    // H
                    '海' -> "H"
                    '核' -> "H"
                    '红' -> "H"
                    '花' -> "H"
                    '黄' -> "H"
                    '火' -> "H"
                    // J
                    '鸡' -> "J"
                    '坚' -> "J"
                    '酱' -> "J"
                    '桔' -> "J"
                    '饺' -> "J"
                    // K
                    '咖' -> "K"
                    '可' -> "K"
                    '苦' -> "K"
                    // L
                    '辣' -> "L"
                    '蓝' -> "L"
                    '梨' -> "L"
                    '李' -> "L"
                    '莲' -> "L"
                    '绿' -> "L"
                    '萝' -> "L"
                    // M
                    '馒' -> "M"
                    '面' -> "M"
                    '米' -> "M"
                    '蘑' -> "M"
                    // N
                    '奶' -> "N"
                    '南' -> "N"
                    '牛' -> "N"
                    // O
                    '藕' -> "O"
                    // P
                    '排' -> "P"
                    '苹' -> "P"
                    '葡' -> "P"
                    // Q
                    '芹' -> "Q"
                    '青' -> "Q"
                    // R
                    '肉' -> "R"
                    // S
                    '三' -> "S"
                    '山' -> "S"
                    '生' -> "S"
                    '柿' -> "S"
                    '蔬' -> "S"
                    '水' -> "S"
                    '蒜' -> "S"
                    // T
                    '桃' -> "T"
                    '甜' -> "T"
                    '土' -> "T"
                    // W
                    '味' -> "W"
                    // X
                    '西' -> "X"
                    '香' -> "X"
                    '小' -> "X"
                    '杏' -> "X"
                    '虾' -> "X"
                    '雪' -> "X"
                    // Y
                    '鸭' -> "Y"
                    '羊' -> "Y"
                    '腰' -> "Y"
                    '椰' -> "Y"
                    '银' -> "Y"
                    '樱' -> "Y"
                    '油' -> "Y"
                    '鱼' -> "Y"
                    '玉' -> "Y"
                    '元' -> "Y"
                    '饮' -> "Y"
                    '燕' -> "Y"
                    '洋' -> "Y"
                    // Z
                    '枣' -> "Z"
                    '芝' -> "Z"
                    '紫' -> "Z"
                    '猪' -> "Z"
                    '植' -> "Z"
                    // 其他常见字
                    else -> {
                        // 使用汉字 Unicode 范围判断拼音首字母
                        getPinyinLetter(char)
                    }
                }
            }
        }
    }

    /**
     * 根据汉字 Unicode 编码获取拼音首字母
     */
    private fun getPinyinLetter(char: Char): String {
        val code = char.code
        return when {
            // 使用 GB2312 汉字拼音首字母区间
            code in 0xB0A1..0xB0C4 -> "A"
            code in 0xB0C5..0xB2C0 -> "B"
            code in 0xB2C1..0xB4ED -> "C"
            code in 0xB4EE..0xB6E9 -> "D"
            code in 0xB6EA..0xB7A1 -> "E"
            code in 0xB7A2..0xB8C0 -> "F"
            code in 0xB8C1..0xB9FD -> "G"
            code in 0xB9FE..0xBBF6 -> "H"
            code in 0xBBF7..0xBFA5 -> "J"
            code in 0xBFA6..0xC0AB -> "K"
            code in 0xC0AC..0xC2E7 -> "L"
            code in 0xC2E8..0xC4C2 -> "M"
            code in 0xC4C3..0xC5B5 -> "N"
            code in 0xC5B6..0xC5BD -> "O"
            code in 0xC5BE..0xC6D9 -> "P"
            code in 0xC6DA..0xC8BA -> "Q"
            code in 0xC8BB..0xC8F5 -> "R"
            code in 0xC8F6..0xCBF0 -> "S"
            code in 0xCBF1..0xCDD9 -> "T"
            code in 0xCDDA..0xCEF3 -> "W"
            code in 0xCEF4..0xD188 -> "X"
            code in 0xD189..0xD4D0 -> "Y"
            code in 0xD4D1..0xD7F9 -> "Z"
            // 不在拼音范围内，返回 #
            else -> "#"
        }
    }
}