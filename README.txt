# HealthTracker 开发进度

## 2026-03-12: 每餐总热量显示

### 新增功能
- **每餐总热量**：在饮食记录列表的每个餐次区块（早餐、午餐、晚餐、加餐）标题栏右侧显示总热量

### 修改文件
- `HomeScreen.kt` - 修改餐次标题为 Row 布局，右侧显示热量汇总

### 实现说明
- 位置：餐次标题右侧，与餐次名称水平对齐
- 格式：`[数值] kcal`（如：900 kcal）
- 样式：使用红色半透明文字，比标题字号略小
- 计算：动态汇总该餐次下所有食物的 calories 总和

---

## 2026-03-12: 营养素目标功能完成

### 新增功能
- **营养素目标设置**：支持设置每日碳水、蛋白质、脂肪目标
- **两种模式**：
  - 自动计算：设置比例后根据热量自动计算目标克数
  - 手动设置：直接输入各营养素目标克数
- **进度条展示**：首页营养素卡片显示进度条和目标值

### 修改文件
1. `UserSettingsEntity.kt` - 添加 nutrientMode, carbsRatio, proteinRatio, fatRatio 字段
2. `HealthTrackerDatabase.kt` - 数据库版本升级到 14，添加 MIGRATION_13_14
3. `DatabaseModule.kt` - 注册新迁移
4. `UserSettingsDao.kt` - 添加营养素目标更新方法
5. `UserSettingsRepository.kt` - 添加营养素目标更新方法
6. `HomeViewModel.kt` - 添加营养素目标状态和更新方法
7. `HomeScreen.kt` - 改造 TargetCaloriesDialog 和 NutrientSummaryCard

### 功能入口
- 首页热量卡片长按 → 打开设置弹窗
- 可切换"自动计算"和"手动设置"模式
- 自动模式：拖动滑块调整比例，实时显示计算结果
- 手动模式：直接输入各营养素目标克数

### 默认比例
- 碳水：50%
- 蛋白质：20%
- 脂肪：30%

---

## 2026-03-10: 周期食物功能完成

### 新增功能
- **周期食物 (Cycle Food)**：适用于分多天吃完的食物（如一整个蛋糕）
- 输入每百克营养数据和总重量，系统自动计算总营养和每份数据
- 支持记录"吃一份"和"吃完剩余"两种操作
- **长按周期食物卡片可编辑或删除**

### 新增文件
1. `CycleFoodEntity.kt` - 周期食物数据实体
2. `CycleFoodDao.kt` - 数据访问对象
3. `CycleFoodRepository.kt` - 仓库层
4. `AddCycleFoodScreen.kt` - 添加周期食物界面（输入每百克营养+总重量）
5. `AddCycleFoodViewModel.kt` - 添加周期食物的 ViewModel
6. `EditCycleFoodScreen.kt` - 编辑周期食物界面
7. `EditCycleFoodViewModel.kt` - 编辑周期食物的 ViewModel

### 修改文件
1. `HealthTrackerDatabase.kt` - 添加 cycle_foods 表，数据库版本升级到 13
2. `DatabaseModule.kt` - 添加 CycleFoodDao 依赖注入
3. `HomeViewModel.kt` - 添加周期食物相关方法（eatCycleFoodPortion, finishCycleFood, deleteCycleFood）
4. `HomeScreen.kt` - 添加周期食物卡片显示、FAB 第4个选项、长按编辑/删除
5. `Screen.kt` - 添加 AddCycleFood 和 EditCycleFood 路由
6. `HealthTrackerNavGraph.kt` - 添加 AddCycleFood 和 EditCycleFood 页面导航
7. `MainScreen.kt` - 传递导航回调

### 功能入口
- 添加：首页右下角 FAB → 第4个选项 "周期"
- 编辑/删除：长按周期食物卡片 → 选择编辑或删除

### 使用流程
1. 点击 FAB → 选择"周期"
2. 填写食物名称、每百克营养数据、总重量、预计天数
3. 系统自动计算总营养和每份营养
4. 保存后，首页显示周期食物卡片
5. 可以"吃一份"或"吃完剩余"
6. 每次操作自动创建摄入记录并更新剩余量
7. 长按卡片可编辑或删除

---

## 待处理
- FAB 导航问题（首次点击后跳转错误，第二次正常）- 暂未解决