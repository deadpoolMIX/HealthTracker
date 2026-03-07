package com.example.healthtracker.util

/**
 * 食物 emoji 工具类
 * 提供食物 emoji 列表和默认映射
 */
object FoodEmojiUtils {

    // 所有食物 emoji 列表（按分类组织）
    val foodEmojisByCategory = mapOf(
        "主食" to listOf(
            "🍚" to "米饭", "🍜" to "面条", "🍝" to "意面", "🍞" to "面包",
            "🥟" to "饺子", "🥡" to "外卖", "🍲" to "火锅", "🥣" to "粥",
            "🍘" to "米饼", "🍙" to "饭团", "🍛" to "咖喱饭", "🥙" to "卷饼",
            "🥖" to "法棍", "🌮" to "墨西哥卷", "🌯" to "卷饼", "🥞" to "煎饼"
        ),
        "肉类" to listOf(
            "🥩" to "牛排", "🍖" to "烤肉", "🍗" to "鸡肉", "🥓" to "培根",
            "🌭" to "热狗", "🍔" to "汉堡"
        ),
        "海鲜" to listOf(
            "🐟" to "鱼", "🦐" to "虾", "🦀" to "螃蟹", "🦞" to "龙虾",
            "🦑" to "鱿鱼", "🐙" to "章鱼", "🦪" to "生蚝", "🍣" to "寿司"
        ),
        "蔬菜" to listOf(
            "🥬" to "蔬菜", "🥦" to "西兰花", "🥒" to "黄瓜", "🍆" to "茄子",
            "🥕" to "胡萝卜", "🌽" to "玉米", "🌶️" to "辣椒", "🧅" to "洋葱",
            "🥔" to "土豆", "🍠" to "红薯", "🧄" to "大蒜", "🍅" to "番茄"
        ),
        "水果" to listOf(
            "🍎" to "苹果", "🍊" to "橙子", "🍋" to "柠檬", "🍇" to "葡萄",
            "🍉" to "西瓜", "🍓" to "草莓", "🍑" to "桃子", "🍒" to "樱桃",
            "🍌" to "香蕉", "🥝" to "猕猴桃", "🥭" to "芒果", "🍍" to "菠萝",
            "🥥" to "椰子", "🍈" to "哈密瓜", "🍐" to "梨", "🫐" to "蓝莓"
        ),
        "蛋奶" to listOf(
            "🥚" to "鸡蛋", "🥛" to "牛奶", "🧀" to "奶酪", "🥧" to "蛋挞",
            "🍳" to "煎蛋", "🧈" to "黄油"
        ),
        "豆类" to listOf(
            "🫘" to "豆子", "🥜" to "花生", "🌰" to "栗子"
        ),
        "坚果" to listOf(
            "🥜" to "花生", "🌰" to "坚果", "🫒" to "橄榄"
        ),
        "饮品" to listOf(
            "☕" to "咖啡", "🍵" to "茶", "🥤" to "饮料", "🧃" to "果汁",
            "🥤" to "奶茶", "🧋" to "珍珠奶茶", "🍷" to "红酒", "🍺" to "啤酒",
            "🥂" to "香槟", "🍹" to "鸡尾酒", "🥃" to "威士忌", "🫖" to "茶壶"
        ),
        "甜点" to listOf(
            "🍰" to "蛋糕", "🧁" to "杯子蛋糕", "🍩" to "甜甜圈", "🍪" to "饼干",
            "🍫" to "巧克力", "🍬" to "糖果", "🍭" to "棒棒糖", "🍮" to "布丁",
            "🍦" to "冰淇淋", "🍧" to "刨冰", "🍡" to "麻薯", "🧇" to "华夫饼"
        ),
        "油脂" to listOf(
            "🫒" to "橄榄油", "🧈" to "黄油"
        ),
        "其他" to listOf(
            "🍽️" to "食物", "🥗" to "沙拉", "🥘" to "炖菜", "🍜" to "汤面",
            "🫕" to "火锅", "🍱" to "便当", "🍘" to "零食"
        )
    )

    // 扁平化的 emoji 列表（用于选择器）
    val allFoodEmojis: List<Pair<String, String>> = foodEmojisByCategory.flatMap { (category, emojis) ->
        emojis
    }.distinctBy { it.first }

    // 根据 emoji 获取描述
    fun getEmojiDescription(emoji: String): String {
        return allFoodEmojis.find { it.first == emoji }?.second ?: "食物"
    }

    // 根据食物名称获取默认 emoji
    fun getDefaultEmojiForFood(name: String): String {
        return when {
            // 主食
            name.contains("饭") -> "🍚"
            name.contains("粥") -> "🥣"
            name.contains("面") && !name.contains("便面") -> "🍜"
            name.contains("粉") -> "🍜"
            name.contains("馒头") || name.contains("包子") -> "🥟"
            name.contains("面包") -> "🍞"
            name.contains("饺子") -> "🥟"
            name.contains("饼") -> "🥙"
            name.contains("意面") || name.contains("意大利面") -> "🍝"

            // 肉类
            name.contains("猪") -> "🥩"
            name.contains("牛") && !name.contains("奶") -> "🥩"
            name.contains("羊") -> "🍖"
            name.contains("鸡") || name.contains("鸭") -> "🍗"
            name.contains("排骨") -> "🍖"
            name.contains("火腿") || name.contains("培根") -> "🥓"
            name.contains("香肠") -> "🌭"
            name.contains("汉堡") -> "🍔"

            // 海鲜
            name.contains("鱼") -> "🐟"
            name.contains("虾") -> "🦐"
            name.contains("蟹") -> "🦀"
            name.contains("龙虾") -> "🦞"
            name.contains("鱿鱼") -> "🦑"
            name.contains("章鱼") -> "🐙"
            name.contains("生蚝") || name.contains("牡蛎") -> "🦪"
            name.contains("寿司") -> "🍣"
            name.contains("海鲜") -> "🦐"

            // 蔬菜
            name.contains("蔬菜") || name.contains("白菜") || name.contains("青菜") -> "🥬"
            name.contains("西兰花") -> "🥦"
            name.contains("黄瓜") -> "🥒"
            name.contains("茄子") -> "🍆"
            name.contains("西红柿") || name.contains("番茄") -> "🍅"
            name.contains("土豆") -> "🥔"
            name.contains("红薯") || name.contains("地瓜") -> "🍠"
            name.contains("胡萝卜") -> "🥕"
            name.contains("玉米") -> "🌽"
            name.contains("辣椒") -> "🌶️"
            name.contains("洋葱") -> "🧅"
            name.contains("大蒜") -> "🧄"
            name.contains("蘑菇") || name.contains("菌") -> "🍄"

            // 水果
            name.contains("苹果") -> "🍎"
            name.contains("香蕉") -> "🍌"
            name.contains("橙") || name.contains("橘子") -> "🍊"
            name.contains("葡萄") -> "🍇"
            name.contains("草莓") -> "🍓"
            name.contains("西瓜") -> "🍉"
            name.contains("桃") -> "🍑"
            name.contains("樱桃") -> "🍒"
            name.contains("猕猴桃") -> "🥝"
            name.contains("芒果") -> "🥭"
            name.contains("菠萝") -> "🍍"
            name.contains("椰子") -> "🥥"
            name.contains("哈密瓜") -> "🍈"
            name.contains("梨") -> "🍐"
            name.contains("柠檬") -> "🍋"
            name.contains("蓝莓") -> "🫐"
            name.contains("水果") -> "🍇"

            // 蛋奶
            name.contains("蛋") && !name.contains("蛋糕") && !name.contains("蛋挞") -> "🥚"
            name.contains("牛奶") || name.contains("奶") && !name.contains("奶茶") -> "🥛"
            name.contains("奶酪") || name.contains("芝士") -> "🧀"
            name.contains("蛋挞") -> "🥧"
            name.contains("煎蛋") -> "🍳"
            name.contains("黄油") -> "🧈"

            // 豆类
            name.contains("豆") || name.contains("豆腐") -> "🫘"
            name.contains("花生") -> "🥜"

            // 坚果
            name.contains("坚果") || name.contains("核桃") || name.contains("杏仁") -> "🌰"
            name.contains("橄榄") -> "🫒"

            // 饮品
            name.contains("咖啡") -> "☕"
            name.contains("茶") && !name.contains("奶茶") -> "🍵"
            name.contains("奶茶") -> "🧋"
            name.contains("可乐") || name.contains("汽水") || name.contains("饮料") -> "🥤"
            name.contains("果汁") -> "🧃"
            name.contains("酒") && !name.contains("酒精") -> "🍺"
            name.contains("红酒") -> "🍷"
            name.contains("啤酒") -> "🍺"

            // 甜点
            name.contains("蛋糕") -> "🍰"
            name.contains("杯子蛋糕") -> "🧁"
            name.contains("甜甜圈") -> "🍩"
            name.contains("饼干") -> "🍪"
            name.contains("巧克力") -> "🍫"
            name.contains("糖果") -> "🍬"
            name.contains("冰淇淋") -> "🍦"
            name.contains("布丁") -> "🍮"
            name.contains("麻薯") -> "🍡"
            name.contains("零食") -> "🍪"
            name.contains("甜点") -> "🍰"

            // 其他
            name.contains("油") && !name.contains("橄榄油") -> "🫒"
            name.contains("汤") -> "🥣"
            name.contains("沙拉") || name.contains("沙拉") -> "🥗"
            name.contains("火锅") -> "🫕"
            name.contains("寿司") -> "🍣"
            name.contains("便当") -> "🍱"
            name.contains("水") && !name.contains("水果") && !name.contains("水果") -> "💧"

            else -> "🍽️"
        }
    }

    // 根据分类获取默认 emoji
    fun getDefaultEmojiForCategory(category: String): String {
        return when (category) {
            "主食" -> "🍚"
            "肉类" -> "🥩"
            "海鲜" -> "🦐"
            "蔬菜" -> "🥬"
            "水果" -> "🍇"
            "蛋奶" -> "🥚"
            "豆类" -> "🫘"
            "坚果" -> "🌰"
            "饮品" -> "🥤"
            "甜点" -> "🍰"
            "油脂" -> "🫒"
            else -> "🍽️"
        }
    }
}