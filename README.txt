# HealthTracker 开发进度

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