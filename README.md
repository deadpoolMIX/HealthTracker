# <img src="icon.png" alt="HealthTracker" width="40" height="40"> HealthTracker

> 一款简洁纯粹的本地健康记录应用，专为健身人群打造

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

</div>

---

## ✨ 项目亮点

🎂 **零基础5天完成** — 作为编程小白，使用 Claude Code + GLM-5 模型，在5天内从零开始完成了这个完整的健康记录应用

🔒 **完全本地离线** — 所有数据存储在本地，无需联网，零广告，隐私安全

📊 **丰富的数据可视化** — 多种图表类型，直观展示您的健康数据变化趋势

---

## 📱 功能概览

### 🍽️ 饮食记录
| 功能 | 描述 |
|------|------|
| 🥗 食物库 | 内置常见食物营养数据，支持自定义添加 |
| 🔍 智能搜索 | 快速搜索食物，一键添加记录 |
| 📏 多种单位 | 支持克、毫升、个、杯、勺等多种计量单位 |
| 🕐 餐次分类 | 自动按早餐、午餐、晚餐、加餐分类显示 |
| 📆 历史记录 | 日历视图查看每日摄入，热量热力图直观展示 |
| 📤 数据导入 | 支持 JSON/CSV 格式导入食物营养数据库 |

### 📊 数据报表
| 图表类型 | 描述 |
|---------|------|
| 📊 堆叠柱状图 | 三大营养素（碳水/蛋白质/脂肪）摄入分析 |
| 📈 热量折线图 | 可叠加显示总热量变化趋势 |
| 📉 体重趋势图 | 平滑曲线展示体重、体脂率、肌肉量变化 |
| 😴 睡眠分析图 | 范围条形图展示入睡/起床时间与睡眠时长 |
| 📅 多周期筛选 | 支持周/月/年三种时间维度查看数据 |
| 🔄 历史对比 | 本周vs上周、本月vs上月一键切换 |

### 💪 身体数据
- ⚖️ 体重、体脂率、肌肉量记录
- 📏 支持设置目标值，图表中以虚线显示
- 📈 点击图表可查看当日详细数据

### 😴 睡眠追踪
- 🌙 记录入睡和起床时间
- ⏰ 支持跨天睡眠记录
- 📊 周/月/年睡眠趋势分析
- 💡 平均入睡时间、起床时间、睡眠时长统计

### ⚙️ 其他特性
- 🎨 **多主题配色** — 5种主题色可选，支持浅色/深色/跟随系统
- 📱 **Material Design 3** — 现代化 UI 设计，流畅动画
- 🔄 **高刷新率适配** — 匹配设备最高刷新率，滑动流畅
- 📤 **数据备份** — 导出/导入 JSON 格式备份文件
- 🧮 **BMR/TDEE 计算** — 根据身体数据自动计算基础代谢

---

## 📸 应用截图

<div align="center">

<details>
<summary>📱 点击展开查看截图</summary>

<div style="display: flex; overflow-x: auto; white-space: nowrap; gap: 10px; padding: 10px 0;">

<img src="app%20screenshots/首页.jpg" alt="首页" width="200"/>

<img src="app%20screenshots/日历.jpg" alt="日历" width="200"/>

<img src="app%20screenshots/营养素摄入页面.jpg" alt="营养素摄入页面" width="200"/>

<img src="app%20screenshots/体重记录页面.jpg" alt="体重记录页面" width="200"/>

<img src="app%20screenshots/睡眠记录页面.jpg" alt="睡眠记录页面" width="200"/>

</div>

</details>

</div>

---

## 📦 项目文件说明

### 食物数据文件

| 文件 | 说明 |
|------|------|
| `FoodData_Central_foundation_food_json_2025-12-18.json` | 📥 [USDA](https://fdc.nal.usda.gov/download-datasets/) 官方原始食物数据库（6.8MB），包含美国农业部收录的所有基础食物营养数据 |
| `convert_usda_food.py` | 🐍 Python 转换脚本，将 USDA 原始 JSON 转换为应用可用的格式，支持自动翻译和中文名称映射 |
| `97-complete-foods.json` | 🍽️ 从 USDA 数据转换出的 97 种常见食物数据，可直接导入应用 |
| `ai-generated-food-supplement.json` | 🤖 AI 生成的中国特有食物补充数据（101 种），已去除与 USDA 重复的食物 |
| `food-data-crawler-prompt.md` | 📝 食物数据爬虫提示词模板，指导 AI 如何生成符合格式要求的食物营养数据 |

> 💡 **使用方法**：应用已内置预设食物数据，在「设置 → 导入食物数据」页面点击「一键导入预设数据」即可导入 97 种 USDA 食物和 101 种中国常见食物。如需自定义数据，也可导入 JSON/CSV 文件。
>
> ⚠️ **注意事项**：
> - 清空食物数据库不会影响已记录的饮食记录，记录已保存食物名称和营养数据的快照
> - AI 生成的食物数据可能存在误差，请根据实际情况甄别使用

### 开发相关文件

| 文件 | 说明 |
|------|------|
| `DEV_NOTES.txt` | 📋 开发笔记，记录了所有功能需求和修复历史 |

---

## 🛠️ 技术栈

| 技术 | 说明 |
|------|------|
| Kotlin | 主要开发语言 |
| Jetpack Compose | 现代 UI 框架 |
| Material Design 3 | UI 设计规范 |
| Room Database | 本地数据存储 |
| Hilt | 依赖注入 |
| MVVM Architecture | 架构模式 |
| Kotlin Flow | 响应式数据流 |

---

## 🚀 开发故事

### 🌱 起源

作为一个 **0 编程基础的小白**，我一直想要一个简洁纯粹的饮食、睡眠、体重记录软件：
- 📱 给想要控制饮食的健身人群使用
- 🔒 本地离线运行，没有广告的困扰
- 📊 丰富的数据分析页面，直观展示日积月累的记录

市面上找不到完全符合我需求的应用，于是我决定自己做一个！

### 🔧 开发工具

| 工具 | 说明 |
|------|------|
| Android Studio | IDE |
| Claude Code 插件 | AI 辅助编程 |
| GLM-5 模型 | 主要使用的 AI 模型 |

### 📈 开发统计

| 指标 | 数据 |
|------|------|
| ⏱️ 总 Token 消耗 | 40.1M |
| 📅 开发天数 | 5 天 |
| 🕐 最长会话 | 10小时20分 |
| 📊 会话总数 | 27 次 |

> 💡 最长会话时长约等于连续观看 28 集《办公室》！

### 🙏 未完成的梦想

**饮食计划功能** 是我一直想添加的功能：
- 📝 可以添加一顿/一天/一周的饮食计划
- 📋 执行计划时，未来日期会提前显示需要吃的食物
- ✅ 每项食物前有方框可以 check，打勾后正式记录到当天

奈何能力有限，暂时未能实现。欢迎有能力的大佬帮忙完善！

---

## 📥 安装使用

1. 从 [Releases](../../releases) 下载最新 APK
2. 安装到 Android 设备（需开启未知来源安装）
3. 首次使用填写基本身体信息
4. 开始记录您的健康数据！

---

## 🔧 编译项目

```bash
# 克隆仓库
git clone https://github.com/你的用户名/HealthTracker.git

# 进入项目目录
cd HealthTracker

# 使用 Android Studio 打开项目，或命令行编译
./gradlew assembleDebug
```

---

## 🤝 贡献指南

欢迎各位大佬：
- 🐛 报告 Bug
- 💡 提出新功能建议
- 🔧 提交 Pull Request
- ⭐ Star 支持

---

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

## 🙏 特别鸣谢

感谢我的好兄弟 **[@turnflase](https://github.com/turnflase)**：
- 🎨 帮助处理应用图标
- 👂 耐心倾听我关于 Vibe Coding with Claude 的心得分享和开发过程中的碎碎念

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ and Claude Code

</div>