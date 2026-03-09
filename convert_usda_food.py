#!/usr/bin/env python3
"""
USDA FoodData Central JSON 转换脚本 v3
转换所有可用的食物数据，仅移除明显不合适的
"""

import json
import re

# USDA 营养素 ID 映射
NUTRIENT_IDS = {
    'energy_kcal': 1008,   # Energy (kcal)
    'energy_kj': 1062,     # Energy (kJ)
    'protein': 1003,
    'fat': 1004,
    'carbs': 1005,
}

# 食物名称翻译
TRANSLATIONS = {
    # 肉类
    'chicken': '鸡肉', 'beef': '牛肉', 'pork': '猪肉', 'turkey': '火鸡肉',
    'lamb': '羊肉', 'duck': '鸭肉', 'goose': '鹅肉', 'veal': '小牛肉',
    'chicken breast': '鸡胸肉', 'chicken thigh': '鸡腿肉', 'chicken leg': '鸡腿',
    'chicken wing': '鸡翅', 'pork chop': '猪排', 'pork loin': '猪里脊',
    'pork tenderloin': '猪里脊', 'beef steak': '牛排', 'steak': '牛排',
    'ground beef': '牛肉末', 'ground pork': '猪肉末', 'ground chicken': '鸡肉末',
    'ground turkey': '火鸡肉末', 'beef rib': '牛肋排', 'pork rib': '猪肋排',
    'beef liver': '牛肝', 'pork liver': '猪肝', 'chicken liver': '鸡肝',
    'beef tongue': '牛舌', 'pork belly': '五花肉', 'bacon': '培根',
    'ham': '火腿', 'sausage': '香肠', 'hot dog': '热狗', 'frankfurter': '法兰克福香肠',
    'ribeye': '肋眼牛排', 'sirloin': '西冷牛排', 'tenderloin': '里脊',
    'brisket': '牛腩', 'flank': '牛腩', 'chuck': '牛肩肉',
    'rump': '牛臀肉', 'round': '牛后腿肉', 'shank': '牛小腿',
    'meat': '肉', 'poultry': '家禽',

    # 海鲜
    'salmon': '三文鱼', 'tuna': '金枪鱼', 'shrimp': '虾', 'cod': '鳕鱼',
    'crab': '螃蟹', 'lobster': '龙虾', 'tilapia': '罗非鱼', 'trout': '鳟鱼',
    'sardine': '沙丁鱼', 'mackerel': '鲭鱼', 'catfish': '鲶鱼',
    'clam': '蛤蜊', 'oyster': '牡蛎', 'mussel': '贻贝', 'scallop': '扇贝',
    'squid': '鱿鱼', 'octopus': '章鱼', 'crawfish': '小龙虾',
    'halibut': '大比目鱼', 'pollock': '狭鳕鱼', 'haddock': '黑线鳕',
    'perch': '鲈鱼', 'snapper': '鲷鱼', 'sole': '比目鱼', 'flounder': '比目鱼',
    'white fish': '白鱼', 'fish': '鱼肉', 'prawn': '对虾',
    'crayfish': '小龙虾', 'anchovy': '凤尾鱼', 'herring': '鲱鱼',
    'sea bass': '海鲈鱼', 'eel': '鳗鱼', 'roe': '鱼籽', 'seafood': '海鲜',

    # 蔬菜
    'broccoli': '西兰花', 'carrot': '胡萝卜', 'carrots': '胡萝卜',
    'spinach': '菠菜', 'lettuce': '生菜', 'romaine': '罗马生菜',
    'tomato': '番茄', 'tomatoes': '番茄', 'potato': '土豆', 'potatoes': '土豆',
    'sweet potato': '红薯', 'yam': '山药', 'onion': '洋葱', 'onions': '洋葱',
    'garlic': '大蒜', 'ginger': '生姜', 'cucumber': '黄瓜', 'cucumbers': '黄瓜',
    'cabbage': '卷心菜', 'celery': '芹菜', 'corn': '玉米',
    'peas': '豌豆', 'green pea': '豌豆', 'kale': '羽衣甘蓝',
    'zucchini': '西葫芦', 'eggplant': '茄子', 'cauliflower': '花菜',
    'asparagus': '芦笋', 'mushroom': '蘑菇', 'mushrooms': '蘑菇',
    'bell pepper': '甜椒', 'green pepper': '青椒', 'red pepper': '红椒',
    'green bean': '四季豆', 'green beans': '四季豆', 'snap bean': '四季豆',
    'pumpkin': '南瓜', 'squash': '南瓜', 'bok choy': '小白菜',
    'napa cabbage': '大白菜', 'chinese cabbage': '大白菜',
    'radish': '萝卜', 'daikon': '白萝卜', 'turnip': '芜菁',
    'beet': '甜菜', 'artichoke': '洋蓟', 'leek': '韭葱',
    'shallot': '红葱头', 'chive': '韭菜', 'scallion': '葱',
    'watercress': '西洋菜', 'arugula': '芝麻菜', 'endive': '菊苣',
    'brussels sprout': '抱子甘蓝', 'okra': '秋葵', 'bamboo shoot': '竹笋',
    'bean sprout': '豆芽', 'snow pea': '荷兰豆', 'edamame': '毛豆',
    'water chestnut': '马蹄', 'lotus root': '莲藕', 'bitter melon': '苦瓜',
    'winter melon': '冬瓜', 'luffa': '丝瓜', 'chayote': '佛手瓜',
    'olive': '橄榄', 'olives': '橄榄', 'vegetable': '蔬菜',

    # 水果
    'apple': '苹果', 'apples': '苹果', 'banana': '香蕉', 'bananas': '香蕉',
    'orange': '橙子', 'oranges': '橙子', 'grape': '葡萄', 'grapes': '葡萄',
    'strawberry': '草莓', 'strawberries': '草莓', 'blueberry': '蓝莓', 'blueberries': '蓝莓',
    'raspberry': '覆盆子', 'raspberries': '覆盆子', 'blackberry': '黑莓', 'blackberries': '黑莓',
    'mango': '芒果', 'mangoes': '芒果', 'pineapple': '菠萝', 'pineapples': '菠萝',
    'watermelon': '西瓜', 'watermelons': '西瓜', 'cantaloupe': '哈密瓜', 'cantaloupes': '哈密瓜',
    'honeydew': '蜜瓜', 'peach': '桃子', 'peaches': '桃子',
    'nectarine': '油桃', 'nectarines': '油桃', 'pear': '梨', 'pears': '梨',
    'plum': '李子', 'plums': '李子', 'cherry': '樱桃', 'cherries': '樱桃',
    'lemon': '柠檬', 'lemons': '柠檬', 'lime': '青柠', 'limes': '青柠',
    'grapefruit': '葡萄柚', 'grapefruits': '葡萄柚', 'kiwi': '猕猴桃', 'kiwis': '猕猴桃',
    'kiwifruit': '猕猴桃', 'papaya': '木瓜', 'papayas': '木瓜',
    'coconut': '椰子', 'coconuts': '椰子', 'avocado': '牛油果', 'avocados': '牛油果',
    'pomegranate': '石榴', 'apricot': '杏', 'apricots': '杏',
    'fig': '无花果', 'figs': '无花果', 'date': '枣', 'dates': '枣',
    'persimmon': '柿子', 'durian': '榴莲', 'mangosteen': '山竹',
    'lychee': '荔枝', 'lychees': '荔枝', 'longan': '龙眼',
    'passion fruit': '百香果', 'guava': '番石榴', 'dragon fruit': '火龙果',
    'star fruit': '杨桃', 'fruit': '水果', 'berry': '浆果', 'berries': '浆果',

    # 主食谷物
    'rice': '大米', 'brown rice': '糙米', 'white rice': '大米',
    'wild rice': '野米', 'jasmine rice': '香米', 'basmati rice': '印度香米',
    'pasta': '意面', 'spaghetti': '意大利面', 'macaroni': '通心粉',
    'noodle': '面条', 'noodles': '面条', 'egg noodle': '鸡蛋面', 'rice noodle': '米粉',
    'bread': '面包', 'white bread': '白面包', 'whole wheat bread': '全麦面包',
    'wheat bread': '小麦面包', 'bagel': '贝果', 'tortilla': '玉米饼',
    'pita': '皮塔饼', 'oat': '燕麦', 'oats': '燕麦', 'oatmeal': '燕麦片',
    'wheat': '小麦', 'barley': '大麦', 'rye': '黑麦',
    'quinoa': '藜麦', 'buckwheat': '荞麦', 'millet': '小米',
    'cornmeal': '玉米面', 'polenta': '玉米糊', 'cereal': '谷物',
    'flour': '面粉', 'all-purpose flour': '普通面粉', 'whole wheat flour': '全麦面粉',
    'couscous': '古斯古斯', 'cracker': '饼干', 'crackers': '饼干',
    'pretzel': '椒盐卷饼', 'granola': '格兰诺拉麦片', 'grain': '谷物',

    # 蛋奶
    'milk': '牛奶', 'whole milk': '全脂牛奶', 'skim milk': '脱脂牛奶',
    'low fat milk': '低脂牛奶', 'buttermilk': '酪乳', 'cheese': '奶酪',
    'cheddar': '切达奶酪', 'mozzarella': '马苏里拉奶酪',
    'parmesan': '帕玛森奶酪', 'cream cheese': '奶油奶酪',
    'cottage cheese': '农家奶酪', 'swiss cheese': '瑞士奶酪',
    'gouda': '豪达奶酪', 'brie': '布里奶酪', 'feta': '菲达奶酪',
    'yogurt': '酸奶', 'greek yogurt': '希腊酸奶',
    'cream': '奶油', 'heavy cream': '淡奶油', 'sour cream': '酸奶油',
    'whipped cream': '打发的奶油', 'half and half': '半对半奶油',
    'butter': '黄油', 'margarine': '人造黄油',
    'egg': '鸡蛋', 'eggs': '鸡蛋', 'whole egg': '全蛋', 'egg white': '蛋白',
    'egg yolk': '蛋黄', 'duck egg': '鸭蛋', 'quail egg': '鹌鹑蛋',
    'ice cream': '冰淇淋', 'frozen yogurt': '冻酸奶', 'dairy': '乳制品',

    # 坚果种子
    'almond': '杏仁', 'almonds': '杏仁', 'walnut': '核桃', 'walnuts': '核桃',
    'pecan': '碧根果', 'pecans': '碧根果', 'cashew': '腰果', 'cashews': '腰果',
    'pistachio': '开心果', 'peanut': '花生', 'peanuts': '花生',
    'hazelnut': '榛子', 'hazelnuts': '榛子', 'macadamia': '夏威夷果',
    'brazil nut': '巴西坚果', 'pine nut': '松子', 'chestnut': '栗子', 'chestnuts': '栗子',
    'sunflower seed': '葵花籽', 'pumpkin seed': '南瓜子',
    'sesame seed': '芝麻', 'chia seed': '奇亚籽', 'flax seed': '亚麻籽',
    'hemp seed': '火麻仁', 'poppy seed': '罂粟籽',
    'tahini': '芝麻酱', 'peanut butter': '花生酱', 'almond butter': '杏仁酱',
    'nut': '坚果', 'nuts': '坚果', 'seed': '种子', 'seeds': '种子',

    # 油脂
    'olive oil': '橄榄油', 'vegetable oil': '植物油', 'canola oil': '菜籽油',
    'corn oil': '玉米油', 'soybean oil': '大豆油', 'sunflower oil': '葵花籽油',
    'coconut oil': '椰子油', 'sesame oil': '芝麻油', 'peanut oil': '花生油',
    'grape seed oil': '葡萄籽油', 'avocado oil': '牛油果油',
    'lard': '猪油', 'tallow': '牛油', 'ghee': '酥油',
    'mayonnaise': '蛋黄酱', 'oil': '油', 'fat': '脂肪',

    # 豆类
    'tofu': '豆腐', 'soybean': '黄豆', 'soybeans': '黄豆', 'soy milk': '豆浆',
    'lentil': '扁豆', 'lentils': '扁豆', 'chickpea': '鹰嘴豆', 'chickpeas': '鹰嘴豆',
    'garbanzo': '鹰嘴豆', 'black bean': '黑豆', 'kidney bean': '芸豆',
    'pinto bean': '斑豆', 'navy bean': '海军豆', 'black-eyed pea': '黑眼豆',
    'adzuki bean': '红豆', 'mung bean': '绿豆', 'red bean': '红豆',
    'tempeh': '豆豉', 'soy protein': '大豆蛋白',
    'bean curd': '豆腐皮', 'dried tofu': '豆干', 'bean': '豆', 'beans': '豆类',

    # 调味品和其他
    'honey': '蜂蜜', 'sugar': '糖', 'brown sugar': '红糖',
    'maple syrup': '枫糖浆', 'agave': '龙舌兰蜜', 'molasses': '糖蜜',
    'salt': '盐', 'black pepper': '黑胡椒', 'soy sauce': '酱油',
    'vinegar': '醋', 'wine': '酒', 'cooking wine': '料酒',
    'ketchup': '番茄酱', 'mustard': '芥末', 'hot sauce': '辣酱',
    'worcestershire sauce': '伍斯特酱', 'fish sauce': '鱼露',
    'oyster sauce': '蚝油', 'curry': '咖喱', 'curry paste': '咖喱酱',
    'vanilla': '香草', 'cinnamon': '肉桂', 'cumin': '孜然',
    'turmeric': '姜黄', 'oregano': '牛至', 'basil': '罗勒',
    'thyme': '百里香', 'rosemary': '迷迭香', 'parsley': '欧芹',
    'cilantro': '香菜', 'dill': '莳萝', 'mint': '薄荷',
    'yeast': '酵母', 'baking powder': '泡打粉', 'baking soda': '小苏打',
    'gelatin': '明胶', 'agar': '琼脂', 'chocolate': '巧克力',
    'cocoa': '可可', 'coffee': '咖啡', 'tea': '茶',
    'juice': '果汁', 'apple juice': '苹果汁', 'orange juice': '橙汁',

    # 零食和甜品
    'cookie': '饼干', 'cookies': '饼干', 'cake': '蛋糕',
    'pie': '派', 'donut': '甜甜圈', 'pastry': '糕点',
    'candy': '糖果', 'chip': '薯片', 'chips': '薯片',
    'popcorn': '爆米花', 'snack': '零食', 'dessert': '甜点',

    # 饮料
    'soda': '汽水', 'soft drink': '软饮料', 'sports drink': '运动饮料',
    'energy drink': '能量饮料', 'beer': '啤酒', 'wine': '葡萄酒',
    'alcohol': '酒精', 'cocktail': '鸡尾酒', 'beverage': '饮料', 'drink': '饮料',

    # 其他常见词
    'sauce': '酱汁', 'soup': '汤', 'salad': '沙拉',
    'sandwich': '三明治', 'burger': '汉堡', 'pizza': '披萨',
    'french fries': '薯条', 'fried': '油炸', 'baked': '烘烤',
    'boiled': '水煮', 'steamed': '蒸', 'grilled': '烤',
    'roasted': '烤', 'raw': '生', 'cooked': '熟',
    'dried': '干', 'fresh': '新鲜', 'frozen': '冷冻',
    'canned': '罐装', 'pack': '包装', 'instant': '速食',
    'whole': '全', 'sliced': '切片', 'chopped': '切碎',
    'minced': '剁碎', 'ground': '磨碎', 'powder': '粉',
    'syrup': '糖浆', 'paste': '酱', 'extract': '提取物',
    'mix': '混合', 'blend': '混合', 'prepared': '预制',
    'homemade': '自制', 'commercial': '商业', 'brand': '品牌',
    'fast food': '快餐', 'restaurant': '餐厅',
}

# 分类关键词
CATEGORY_KEYWORDS = {
    '海鲜': ['fish', 'salmon', 'tuna', 'cod', 'shrimp', 'crab', 'lobster',
              'clam', 'oyster', 'mussel', 'scallop', 'squid', 'octopus',
              'trout', 'sardine', 'mackerel', 'catfish', 'tilapia', 'halibut',
              'pollock', 'haddock', 'perch', 'snapper', 'sole', 'flounder',
              'prawn', 'crayfish', 'anchovy', 'herring', 'sea bass', 'eel',
              'roe', 'seafood'],

    '肉类': ['beef', 'pork', 'lamb', 'veal', 'mutton', 'meat',
              'chicken', 'turkey', 'duck', 'goose', 'poultry',
              'bacon', 'ham', 'sausage', 'hot dog', 'frankfurter',
              'steak', 'rib', 'liver', 'tongue', 'belly'],

    '蔬菜': ['vegetable', 'broccoli', 'carrot', 'spinach', 'lettuce',
              'tomato', 'potato', 'onion', 'garlic', 'ginger', 'cucumber',
              'cabbage', 'celery', 'corn', 'peas', 'kale', 'zucchini',
              'eggplant', 'cauliflower', 'asparagus', 'mushroom', 'pepper',
              'bean', 'pumpkin', 'squash', 'radish', 'turnip', 'beet',
              'artichoke', 'leek', 'shallot', 'chive', 'scallion',
              'watercress', 'arugula', 'endive', 'okra', 'bamboo',
              'edamame', 'water chestnut', 'lotus root', 'bitter melon',
              'winter melon', 'luffa', 'chayote', 'olive'],

    '水果': ['fruit', 'apple', 'banana', 'orange', 'grape', 'strawberry',
              'blueberry', 'raspberry', 'blackberry', 'mango', 'pineapple',
              'watermelon', 'cantaloupe', 'honeydew', 'peach', 'nectarine',
              'pear', 'plum', 'cherry', 'lemon', 'lime', 'grapefruit',
              'kiwi', 'papaya', 'coconut', 'avocado', 'pomegranate',
              'apricot', 'fig', 'date', 'persimmon', 'durian', 'mangosteen',
              'lychee', 'longan', 'passion', 'guava', 'dragon fruit', 'berry'],

    '主食': ['rice', 'pasta', 'spaghetti', 'macaroni', 'noodle', 'bread',
              'bagel', 'tortilla', 'pita', 'oat', 'wheat', 'barley', 'rye',
              'quinoa', 'buckwheat', 'millet', 'cornmeal', 'flour', 'couscous',
              'cereal', 'grain', 'cracker', 'pretzel', 'granola'],

    '蛋奶': ['milk', 'cheese', 'yogurt', 'cream', 'butter', 'egg',
              'ice cream', 'dairy'],

    '坚果': ['almond', 'walnut', 'pecan', 'cashew', 'pistachio', 'peanut',
              'hazelnut', 'macadamia', 'brazil nut', 'pine nut', 'chestnut',
              'sunflower seed', 'pumpkin seed', 'sesame seed', 'chia seed',
              'flax seed', 'hemp seed', 'tahini', 'peanut butter', 'almond butter',
              'nut', 'seed'],

    '油脂': ['olive oil', 'vegetable oil', 'canola oil', 'corn oil',
              'soybean oil', 'sunflower oil', 'coconut oil', 'sesame oil',
              'peanut oil', 'grape seed oil', 'avocado oil', 'lard', 'tallow',
              'ghee', 'mayonnaise', 'margarine'],

    '豆类': ['tofu', 'soybean', 'soy milk', 'lentil', 'chickpea', 'garbanzo',
              'tempeh', 'edamame'],

    '调味品': ['sugar', 'honey', 'syrup', 'sauce', 'spice', 'herb', 'salt',
                'vinegar', 'ketchup', 'mustard', 'curry', 'vanilla', 'cinnamon',
                'cumin', 'turmeric', 'oregano', 'basil', 'thyme', 'rosemary',
                'parsley', 'cilantro', 'dill', 'mint', 'yeast', 'baking',
                'gelatin', 'agar', 'chocolate', 'cocoa', 'coffee', 'tea'],

    '饮料': ['juice', 'coffee', 'tea', 'soda', 'drink', 'beverage',
              'beer', 'wine', 'alcohol', 'cocktail'],

    '零食': ['snack', 'chip', 'popcorn', 'pretzel', 'candy', 'cookie',
              'cake', 'donut', 'pastry', 'pie', 'dessert', 'chocolate'],
}

# 分类 Emoji
CATEGORY_EMOJI = {
    '海鲜': '🐟', '肉类': '🥩', '蔬菜': '🥬', '水果': '🍎',
    '主食': '🍚', '蛋奶': '🥛', '坚果': '🌰', '油脂': '🫒',
    '豆类': '🫘', '调味品': '🧂', '饮料': '🥤', '零食': '🍪',
    '其他': '🍽️',
}

def get_category(description):
    """根据描述判断食物分类"""
    desc_lower = description.lower()

    for category, keywords in CATEGORY_KEYWORDS.items():
        for keyword in keywords:
            if keyword in desc_lower:
                return category

    return '其他'

def translate_description(description):
    """翻译食物名称"""
    desc_lower = description.lower()

    # 尝试完全匹配
    if desc_lower in TRANSLATIONS:
        return TRANSLATIONS[desc_lower]

    # 尝试部分匹配（从长到短）
    sorted_translations = sorted(TRANSLATIONS.items(), key=lambda x: -len(x[0]))
    for eng, chn in sorted_translations:
        if eng in desc_lower:
            return chn

    # 无法翻译时返回简化名称
    # 取逗号前的部分作为名称
    name = description.split(',')[0].strip()
    return name

def get_nutrient_value(food, nutrient_id):
    """从食物数据中提取营养素值"""
    for nutrient in food.get('foodNutrients', []):
        nutrient_info = nutrient.get('nutrient', {})
        if nutrient_info.get('id') == nutrient_id:
            return nutrient.get('amount', 0)
    return 0

def get_calories(food):
    """获取热量数据，优先 kcal，其次从 kJ 转换"""
    # 先尝试获取 kcal
    kcal = get_nutrient_value(food, NUTRIENT_IDS['energy_kcal'])
    if kcal > 0:
        return kcal

    # 如果没有 kcal，从 kJ 转换 (1 kcal = 4.184 kJ)
    kj = get_nutrient_value(food, NUTRIENT_IDS['energy_kj'])
    if kj > 0:
        return round(kj / 4.184, 1)

    return 0

def process_foods(input_file, output_file):
    """处理 USDA 数据并转换为 HealthTracker 格式"""

    print(f"正在读取文件: {input_file}")

    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    foundation_foods = data.get('FoundationFoods', [])
    print(f"找到 {len(foundation_foods)} 种食物")

    processed_foods = []
    seen_names = set()
    stats = {'no_calories': 0, 'duplicate': 0}

    for food in foundation_foods:
        description = food.get('description', '')

        # 提取营养素
        calories = get_calories(food)
        protein = get_nutrient_value(food, NUTRIENT_IDS['protein'])
        fat = get_nutrient_value(food, NUTRIENT_IDS['fat'])
        carbs = get_nutrient_value(food, NUTRIENT_IDS['carbs'])

        # 跳过没有热量数据的
        if calories == 0:
            stats['no_calories'] += 1
            continue

        # 用原始名称去重
        if description in seen_names:
            stats['duplicate'] += 1
            continue
        seen_names.add(description)

        # 翻译名称
        translated_name = translate_description(description)

        # 获取分类和 emoji
        category = get_category(description)
        emoji = CATEGORY_EMOJI.get(category, '🍽️')

        # 构建食物项
        food_item = {
            'name': translated_name,
            'category': category,
            'calories': round(calories, 1),
            'carbohydrates': round(carbs, 1),
            'protein': round(protein, 1),
            'fat': round(fat, 1),
            'emoji': emoji,
            'originalName': description  # 保留原名供参考
        }

        processed_foods.append(food_item)

    # 按分类和名称排序
    processed_foods.sort(key=lambda x: (x['category'], x['name']))

    # 创建输出数据
    output_data = {
        'dataSource': 'USDA FoodData Central',
        'note': '数据来自美国农业部食物数据库',
        'foods': processed_foods
    }

    # 写入输出文件
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"\n处理完成!")
    print(f"成功转换: {len(processed_foods)} 种食物")
    print(f"跳过 (无热量): {stats['no_calories']}")
    print(f"跳过 (重复): {stats['duplicate']}")
    print(f"输出文件: {output_file}")

    # 统计分类
    category_counts = {}
    for food in processed_foods:
        cat = food['category']
        category_counts[cat] = category_counts.get(cat, 0) + 1

    print(f"\n分类统计:")
    for cat, count in sorted(category_counts.items(), key=lambda x: -x[1]):
        print(f"  {cat}: {count}")

if __name__ == '__main__':
    input_file = 'FoodData_Central_foundation_food_json_2025-12-18.json'
    output_file = 'usda-fooddata.json'

    process_foods(input_file, output_file)