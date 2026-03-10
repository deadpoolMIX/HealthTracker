#!/usr/bin/env python3
"""
从 USDA FoodData Central Foundation Foods 数据中提取食物营养信息
数据来源可靠，只提取确定的数据
"""
import json

# 读取 USDA Foundation Foods 数据
with open('FoodData_Central_foundation_food_json_2025-12-18.json', 'r', encoding='utf-8') as f:
    usda_data = json.load(f)

# 已有食物名称（去重用）
existing_foods = set()

# 读取现有食物数据库
def load_existing_foods():
    files = [
        'app/src/main/assets/preset_foods_usda.json',
        'app/src/main/assets/preset_foods_chinese.json'
    ]
    for file in files:
        try:
            with open(file, 'r', encoding='utf-8') as f:
                data = json.load(f)
                for food in data.get('foods', []):
                    existing_foods.add(food.get('name', '').lower())
        except:
            pass

load_existing_foods()
print(f"已有食物数量: {len(existing_foods)}")

# 食物分类映射（英文 -> 中文分类）
category_mapping = {
    # 主食/谷物
    'bread': '主食', 'rice': '主食', 'pasta': '主食', 'noodle': '主食',
    'flour': '主食', 'cereal': '主食', 'oat': '主食', 'grain': '主食',
    'wheat': '主食', 'barley': '主食', 'quinoa': '主食', 'bulgur': '主食',
    'tortilla': '主食', 'pancake': '主食', 'bagel': '主食', 'cracker': '主食',

    # 肉类
    'beef': '肉类', 'pork': '肉类', 'lamb': '肉类', 'veal': '肉类',
    'chicken': '肉类', 'turkey': '肉类', 'duck': '肉类', 'goose': '肉类',
    'bacon': '肉类', 'sausage': '肉类', 'ham': '肉类', 'steak': '肉类',

    # 海鲜
    'fish': '海鲜', 'salmon': '海鲜', 'tuna': '海鲜', 'cod': '海鲜',
    'shrimp': '海鲜', 'crab': '海鲜', 'lobster': '海鲜', 'clam': '海鲜',
    'oyster': '海鲜', 'mussel': '海鲜', 'scallop': '海鲜', 'squid': '海鲜',
    'sardine': '海鲜', 'mackerel': '海鲜', 'anchovy': '海鲜', 'trout': '海鲜',

    # 蛋奶
    'egg': '蛋奶', 'milk': '蛋奶', 'cheese': '蛋奶', 'yogurt': '蛋奶',
    'cream': '蛋奶', 'butter': '蛋奶', 'cottage': '蛋奶',

    # 蔬菜
    'vegetable': '蔬菜', 'broccoli': '蔬菜', 'carrot': '蔬菜', 'spinach': '蔬菜',
    'tomato': '蔬菜', 'onion': '蔬菜', 'garlic': '蔬菜', 'pepper': '蔬菜',
    'cabbage': '蔬菜', 'lettuce': '蔬菜', 'cucumber': '蔬菜', 'celery': '蔬菜',
    'zucchini': '蔬菜', 'eggplant': '蔬菜', 'cauliflower': '蔬菜', 'asparagus': '蔬菜',
    'kale': '蔬菜', 'mushroom': '蔬菜', 'corn': '蔬菜', 'potato': '蔬菜',
    'bean': '蔬菜', 'pea': '蔬菜', 'lentil': '蔬菜', 'chickpea': '蔬菜',

    # 水果
    'fruit': '水果', 'apple': '水果', 'banana': '水果', 'orange': '水果',
    'grape': '水果', 'strawberry': '水果', 'blueberry': '水果', 'raspberry': '水果',
    'mango': '水果', 'pineapple': '水果', 'peach': '水果', 'pear': '水果',
    'watermelon': '水果', 'cantaloupe': '水果', 'honeydew': '水果', 'kiwi': '水果',
    'plum': '水果', 'cherry': '水果', 'apricot': '水果', 'lemon': '水果',
    'lime': '水果', 'grapefruit': '水果', 'papaya': '水果', 'pomegranate': '水果',

    # 坚果
    'nut': '坚果', 'almond': '坚果', 'walnut': '坚果', 'cashew': '坚果',
    'pistachio': '坚果', 'pecan': '坚果', 'hazelnut': '坚果', 'macadamia': '坚果',
    'peanut': '坚果', 'seed': '坚果', 'sunflower': '坚果', 'pumpkin': '坚果',

    # 油脂
    'oil': '油脂', 'olive oil': '油脂', 'coconut oil': '油脂',

    # 豆类
    'tofu': '豆类', 'soy': '豆类', 'edamame': '豆类', 'tempeh': '豆类',

    # 饮料
    'juice': '饮料', 'coffee': '饮料', 'tea': '饮料', 'soda': '饮料',

    # 零食/甜点
    'snack': '零食', 'chip': '零食', 'cookie': '零食', 'chocolate': '零食',
    'candy': '零食', 'ice cream': '零食', 'cake': '零食', 'pie': '零食',
    'dessert': '零食', 'donut': '零食',
}

# 食物名称中文翻译
food_translations = {
    # 肉类
    'beef': '牛肉', 'pork': '猪肉', 'chicken': '鸡肉', 'turkey': '火鸡肉',
    'lamb': '羊肉', 'duck': '鸭肉', 'bacon': '培根', 'sausage': '香肠',
    'ham': '火腿', 'steak': '牛排', 'veal': '小牛肉', 'goose': '鹅肉',
    'ground beef': '牛肉馅', 'pork chop': '猪排', 'pork loin': '猪里脊',
    'chicken breast': '鸡胸肉', 'chicken thigh': '鸡腿肉', 'chicken wing': '鸡翅',
    'ribs': '排骨', 'liver': '肝', 'heart': '心', 'kidney': '肾',

    # 海鲜
    'salmon': '三文鱼', 'tuna': '金枪鱼', 'cod': '鳕鱼', 'shrimp': '虾',
    'crab': '螃蟹', 'lobster': '龙虾', 'clam': '蛤蜊', 'oyster': '牡蛎',
    'mussel': '贻贝', 'scallop': '扇贝', 'squid': '鱿鱼', 'sardine': '沙丁鱼',
    'mackerel': '鲭鱼', 'anchovy': '凤尾鱼', 'trout': '鳟鱼', 'catfish': '鲶鱼',
    'halibut': '大比目鱼', 'flounder': '比目鱼', 'tilapia': '罗非鱼',

    # 蔬菜
    'broccoli': '西兰花', 'carrot': '胡萝卜', 'spinach': '菠菜', 'tomato': '番茄',
    'onion': '洋葱', 'garlic': '大蒜', 'cabbage': '卷心菜', 'lettuce': '生菜',
    'cucumber': '黄瓜', 'celery': '芹菜', 'zucchini': '西葫芦', 'eggplant': '茄子',
    'cauliflower': '花菜', 'asparagus': '芦笋', 'kale': '羽衣甘蓝', 'mushroom': '蘑菇',
    'potato': '土豆', 'sweet potato': '红薯', 'yam': '山药',
    'green bean': '四季豆', 'pea': '豌豆', 'corn': '玉米',
    'bell pepper': '甜椒', 'chili': '辣椒', 'artichoke': '朝鲜蓟',
    'beet': '甜菜', 'radish': '萝卜', 'turnip': '芜菁', 'okra': '秋葵',
    'leek': '韭葱', 'brussels sprout': '孢子甘蓝', 'bok choy': '小白菜',

    # 水果
    'apple': '苹果', 'banana': '香蕉', 'orange': '橙子', 'grape': '葡萄',
    'strawberry': '草莓', 'blueberry': '蓝莓', 'raspberry': '树莓', 'blackberry': '黑莓',
    'mango': '芒果', 'pineapple': '菠萝', 'peach': '桃子', 'pear': '梨',
    'watermelon': '西瓜', 'cantaloupe': '哈密瓜', 'honeydew': '蜜瓜', 'kiwi': '猕猴桃',
    'plum': '李子', 'cherry': '樱桃', 'apricot': '杏', 'lemon': '柠檬',
    'lime': '青柠', 'grapefruit': '葡萄柚', 'papaya': '木瓜', 'pomegranate': '石榴',
    'avocado': '牛油果', 'coconut': '椰子', 'fig': '无花果', 'date': '枣',
    'cranberry': '蔓越莓', 'guava': '番石榴', 'passion fruit': '百香果',
    'dragon fruit': '火龙果', 'lychee': '荔枝', 'persimmon': '柿子',

    # 蛋奶
    'egg': '鸡蛋', 'milk': '牛奶', 'cheese': '奶酪', 'yogurt': '酸奶',
    'cream': '奶油', 'butter': '黄油', 'cottage cheese': '农家干酪',
    'cream cheese': '奶油奶酪', 'parmesan': '帕玛森奶酪', 'cheddar': '切达奶酪',
    'mozzarella': '马苏里拉奶酪', 'feta': '羊乳酪', 'swiss cheese': '瑞士奶酪',
    'goat cheese': '山羊奶酪', 'greek yogurt': '希腊酸奶', 'whole milk': '全脂牛奶',
    'skim milk': '脱脂牛奶', 'buttermilk': '酪乳', 'evaporated milk': '淡奶',
    'condensed milk': '炼乳', 'sour cream': '酸奶油', 'half and half': '半对半奶油',
    'egg white': '蛋白', 'egg yolk': '蛋黄', 'duck egg': '鸭蛋', 'quail egg': '鹌鹑蛋',

    # 主食/谷物
    'bread': '面包', 'rice': '米饭', 'pasta': '意面', 'noodle': '面条',
    'flour': '面粉', 'cereal': '麦片', 'oat': '燕麦', 'quinoa': '藜麦',
    'bagel': '贝果', 'tortilla': '玉米饼', 'pancake': '煎饼', 'waffle': '华夫饼',
    'cracker': '饼干', 'pretzel': '椒盐脆饼', 'white rice': '白米饭',
    'brown rice': '糙米饭', 'wild rice': '野米', 'couscous': '古斯米',
    'barley': '大麦', 'bulgur': '碾碎干小麦', 'millet': '小米',
    'spaghetti': '意大利面', 'macaroni': '通心粉', 'lasagna': '千层面',
    'whole wheat bread': '全麦面包', 'white bread': '白面包', 'rye bread': '黑麦面包',
    'sourdough': '酸面包', 'croissant': '羊角面包', 'pita': '皮塔饼',

    # 坚果和种子
    'almond': '杏仁', 'walnut': '核桃', 'cashew': '腰果', 'pistachio': '开心果',
    'pecan': '碧根果', 'hazelnut': '榛子', 'macadamia': '夏威夷果',
    'peanut': '花生', 'sunflower seed': '葵花籽', 'pumpkin seed': '南瓜子',
    'chia seed': '奇亚籽', 'flaxseed': '亚麻籽', 'sesame seed': '芝麻',
    'pine nut': '松子', 'brazil nut': '巴西坚果',

    # 豆类
    'tofu': '豆腐', 'soybean': '黄豆', 'edamame': '毛豆', 'tempeh': '天贝',
    'chickpea': '鹰嘴豆', 'lentil': '扁豆', 'kidney bean': '红腰豆',
    'black bean': '黑豆', 'navy bean': '白豆', 'pinto bean': '花豆',
    'soy milk': '豆浆', 'miso': '味噌', 'natto': '纳豆',

    # 油脂
    'olive oil': '橄榄油', 'vegetable oil': '植物油', 'coconut oil': '椰子油',
    'canola oil': '菜籽油', 'sesame oil': '芝麻油', 'peanut oil': '花生油',
    'sunflower oil': '葵花籽油', 'corn oil': '玉米油', 'avocado oil': '牛油果油',
    'lard': '猪油', 'ghee': '酥油', 'margarine': '人造黄油',

    # 饮料
    'orange juice': '橙汁', 'apple juice': '苹果汁', 'grape juice': '葡萄汁',
    'tomato juice': '番茄汁', 'carrot juice': '胡萝卜汁', 'lemonade': '柠檬水',
    'coffee': '咖啡', 'espresso': '浓缩咖啡', 'tea': '茶', 'green tea': '绿茶',
    'black tea': '红茶', 'herbal tea': '花草茶',

    # 其他
    'honey': '蜂蜜', 'sugar': '糖', 'maple syrup': '枫糖浆', 'molasses': '糖蜜',
    'chocolate': '巧克力', 'dark chocolate': '黑巧克力', 'cocoa': '可可',
    'vanilla': '香草', 'cinnamon': '肉桂', 'ginger': '姜',
    'vinegar': '醋', 'soy sauce': '酱油', 'ketchup': '番茄酱', 'mayonnaise': '蛋黄酱',
    'mustard': '芥末', 'hot sauce': '辣酱', 'salsa': '莎莎酱', 'hummus': '鹰嘴豆泥',
    'peanut butter': '花生酱', 'jam': '果酱', 'jelly': '果冻',
    'popcorn': '爆米花', 'chips': '薯片', 'pretzels': '椒盐卷饼',
    'granola': '格兰诺拉麦片', 'trail mix': '混合坚果干果',
    'ice cream': '冰淇淋', 'frozen yogurt': '冻酸奶', 'sorbet': '冰糕',
    'gelatin': '明胶', 'pudding': '布丁', 'custard': '蛋奶冻',

    # 更多肉类
    'pork ribs': '猪肋排', 'beef rib': '牛肋排', 'pork belly': '五花肉',
    'beef liver': '牛肝', 'chicken liver': '鸡肝', 'pork liver': '猪肝',
    'ground pork': '猪肉馅', 'ground turkey': '火鸡肉馅', 'ground chicken': '鸡肉馅',
    'beef tongue': '牛舌', 'pork tongue': '猪舌', 'beef tripe': '牛肚',
    'chicken drumstick': '鸡腿', 'chicken leg': '鸡腿', 'chicken tender': '鸡柳',

    # 更多海鲜
    'pollock': '狭鳕鱼', 'haddock': '黑线鳕', 'snapper': '鲷鱼',
    'swordfish': '剑鱼', 'sea bass': '海鲈鱼', 'monkfish': '鮟鱇鱼',
    'crawfish': '小龙虾', 'prawn': '对虾', 'langostino': '长额虾',
    'sea urchin': '海胆', 'abalone': '鲍鱼', 'geoduck': '象拔蚌',
    'whelk': '海螺', 'conch': '海螺', 'periwinkle': '玉黍螺',

    # 更多蔬菜
    'arugula': '芝麻菜', 'endive': '菊苣', 'watercress': '豆瓣菜',
    'collard': '羽衣甘蓝', 'chard': '牛皮菜', 'bok choy': '青菜',
    'napa cabbage': '大白菜', 'savoy cabbage': '皱叶甘蓝',
    'snow pea': '荷兰豆', 'snap pea': '甜豆', 'bean sprout': '豆芽',
    'bamboo shoot': '竹笋', 'water chestnut': '荸荠', 'lotus root': '莲藕',
    'bitter melon': '苦瓜', 'winter melon': '冬瓜', 'luffa': '丝瓜',
    'chayote': '佛手瓜', 'bitter gourd': '苦瓜',
    'daikon': '白萝卜', 'jicama': '豆薯', 'taro': '芋头',
    'cassava': '木薯', 'plantain': '大蕉',

    # 更多水果
    'acerola': '针叶樱桃', 'durian': '榴莲', 'mangosteen': '山竹',
    'rambutan': '红毛丹', 'jackfruit': '菠萝蜜', 'starfruit': '杨桃',
    'longan': '龙眼', 'langsat': '兰撒果', 'santol': '山陀儿',
    'tamarind': '罗望子', 'breadfruit': '面包果', 'soursop': '刺果番荔枝',
    'cherimoya': '番荔枝', 'atemoya': '凤梨释迦', 'sapodilla': '人心果',
    'carambola': '杨桃', 'physalis': '灯笼果', 'gooseberry': '醋栗',
    'currant': '醋栗', 'boysenberry': '博伊森莓', 'marionberry': '马里昂莓',
    'elderberry': '接骨木莓', 'cloudberry': '云莓', 'lingonberry': '越橘',
    'mulberry': '桑葚', 'medlar': '欧楂', 'quince': '榅桲',
    'feijoa': '费约果', 'loquat': '枇杷', 'jabuticaba': '嘉宝果',
    'surinam cherry': '苏里南樱桃', 'pawpaw': '巴婆果',
    'ugli fruit': '丑橘', 'tangelo': '橘子柚', 'minneola': '蜜诺卡橘',
    'pomelo': '柚子', 'yuzu': '柚子', 'satsuma': '萨摩蜜橘',
    'clementine': '克莱门氏小柑橘', 'mandarin': '橘子', 'tangerine': '橘子',
    'blood orange': '血橙', 'cara cara': '红肉脐橙',
    'plantain': '大蕉', 'burro banana': '牛角香蕉', 'red banana': '红香蕉',

    # 更多坚果
    'chestnut': '栗子', 'ginkgo nut': '白果', 'lotus seed': '莲子',
    'watermelon seed': '西瓜子', 'hemp seed': '火麻仁', 'safflower seed': '红花籽',

    # 更多豆类
    'adzuki bean': '红豆', 'mung bean': '绿豆', 'fava bean': '蚕豆',
    'lima bean': '利马豆', 'garbanzo bean': '鹰嘴豆', 'soybean': '黄豆',
    'hyacinth bean': '扁豆', 'hyacinth': '扁豆',

    # 调味料和酱料
    'worcestershire': '伍斯特酱', 'bbq sauce': '烧烤酱', 'teriyaki': '照烧酱',
    'hoisin sauce': '海鲜酱', 'oyster sauce': '蚝油', 'fish sauce': '鱼露',
    'sriracha': '是拉差辣椒酱', 'tahini': '芝麻酱', 'tartar sauce': '塔塔酱',
    'relish': '调味酱', 'chutney': '酸辣酱',
}

def get_category(description):
    """根据食物描述确定分类"""
    desc_lower = description.lower()
    for key, category in category_mapping.items():
        if key in desc_lower:
            return category
    return '其他'

def translate_food(description):
    """翻译食物名称"""
    desc_lower = description.lower()

    # 直接匹配
    best_match = None
    best_len = 0
    for eng, chn in food_translations.items():
        if eng in desc_lower:
            # 找最长的匹配
            if len(eng) > best_len:
                best_match = chn
                best_len = len(eng)

    if best_match:
        return best_match

    # 尝试处理一些常见格式
    # 例如 "Chicken, broilers or fryers, breast, meat only, raw"
    for eng, chn in food_translations.items():
        if desc_lower.startswith(eng + ',') or desc_lower.startswith(eng + ' '):
            return chn

    return None  # 无法翻译的返回 None

def extract_nutrients(food):
    """从食物数据中提取营养信息"""
    nutrients = {}
    for nutrient in food.get('foodNutrients', []):
        nutrient_info = nutrient.get('nutrient', {})
        name = nutrient_info.get('name', '')
        amount = nutrient.get('amount')

        if amount is None:
            continue

        # 热量 (kcal)
        if 'Energy' in name and nutrient_info.get('unitName') == 'kcal':
            nutrients['calories'] = round(amount, 0)
        # 蛋白质
        elif 'Protein' in name:
            nutrients['protein'] = round(amount, 1)
        # 脂肪
        elif 'Total lipid' in name or name == 'Total lipid (fat)':
            nutrients['fat'] = round(amount, 1)
        # 碳水化合物
        elif 'Carbohydrate, by difference' in name or 'Carbohydrate' in name:
            nutrients['carbohydrates'] = round(amount, 1)

    return nutrients

# emoji 映射
category_emoji = {
    '主食': '🍚', '肉类': '🥩', '海鲜': '🐟', '蛋奶': '🥛',
    '蔬菜': '🥬', '水果': '🍎', '坚果': '🌰', '油脂': '🫒',
    '豆类': '🫘', '饮料': '🥤', '零食': '🍪', '其他': '🍽️'
}

# 提取食物数据
foods = []
seen_descriptions = set()

for food in usda_data.get('FoundationFoods', []):
    description = food.get('description', '')
    if not description:
        continue

    # 检查是否已处理
    if description.lower() in seen_descriptions:
        continue
    seen_descriptions.add(description.lower())

    # 提取营养信息
    nutrients = extract_nutrients(food)

    # 必须有热量数据
    if 'calories' not in nutrients:
        continue

    # 翻译食物名称
    chinese_name = translate_food(description)
    if chinese_name is None:
        continue  # 无法翻译的跳过

    # 检查是否已存在
    if chinese_name.lower() in existing_foods:
        continue

    # 获取分类
    category = get_category(description)

    # 创建食物条目
    food_entry = {
        'name': chinese_name,
        'category': category,
        'calories': nutrients.get('calories', 0),
        'carbohydrates': nutrients.get('carbohydrates', 0.0),
        'protein': nutrients.get('protein', 0.0),
        'fat': nutrients.get('fat', 0.0),
        'emoji': category_emoji.get(category, '🍽️')
    }

    foods.append(food_entry)

    if len(foods) >= 300:
        break

print(f"提取到 {len(foods)} 种食物")

# 输出结果
result = {
    'dataSource': 'USDA FoodData Central Foundation Foods',
    'note': '数据来自美国农业部食物数据库，经过翻译和整理',
    'foods': foods
}

with open('ai-generated-food-supplement.json', 'w', encoding='utf-8') as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print("数据已保存到 ai-generated-food-supplement.json")