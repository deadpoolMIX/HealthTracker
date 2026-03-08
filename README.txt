我要写一个健康记录软件
该软件为离线、本地运行软件
软件的平台为：Android apk
选用的IDE是：Android studio
系统是：windows10
软件风格：Material Design 3 (MD3)
软件的核心功能是：记录和数据分析报表
需要记录的内容有：食品的大三营养素、食品的热量、体重、体脂率、肌肉量、三围、入睡\起床时间
数据分析报表需要的图表有：堆叠柱状图、并且可选择叠加一条折线来观察总摄入热量、或者单个营养素摄入的变化（三大营养素）、平滑曲线的折线图（体重、体脂率、肌肉量、三围）、范围条形图（入睡\起床时间以及睡眠时长）
数据分析时单位周期有：天、周、月、年

特殊功能：
1、身体数据（体重、体脂率、肌肉量、三围）在以一周为一个单位点（折线图上的每一点代表一个周）时，可以选取这一周的该数据的中位数
2、用户可以自定义饮食计划（例如每天每顿吃什么、吃多少），用户可以以一顿、一天、一周、一月为单位，添加每顿的食物内容，保存成为一个饮食计划。预先设定好的饮食计划，会在每日摄入中提前显示，并且在每行食物前会有一个可以check的小方块，一旦摄入，将小方框check，即视为添加到今日的食物摄入之内，若过了当日没有check，则视为为摄入，该项食物自动忽略消失。
3、一个食物如果花费n天吃完，则在记录时，选好摄入的n个日期后，将总热量平分到每天的摄入。
4、软件数据由用户自行导出备份导入恢复，备份包内只含有文本数据
5、软件高度自定义，报表高度自由化，用户可自行选择是否展示某个报表，以及以什么周期来展示
6、由于是本地运行的离线免费软件，所以我们需要先行爬取权威食品数据库的食品营养表数据，内置在软件内部，主要针对平时没有特殊标明食品营养表的食物（类似菜市场买的菜）

软件细节：
1、可以设置目标体重、体脂率、摄入量（热量或者三大营养素）。目标身体数据需要在折线图上以虚线显示。
2、睡眠的范围条形图下面需要有一行字，显示在一个周期内，平均入睡\起床时间，该周期与睡眠的范围条形图的周期一致。
3、软件有两个页面，首页为当日数据（摄入、身体、睡眠）在左下角为一个圆形，内有+号，点开后展开三个选项（摄入、身体数据、睡眠）点击其中一个选项进入二级页面（全屏显示）进行具体数据记录；第二个页面为报表页面，报表页面右上角可导出导入数据
4、食物以简笔画或者图标的形式显示，无需具体图片，具体食物内容不用细化（例如肉类都用一种图标即可，节省内存占用）
5、首页顶部横幅显示当日可摄入的热量，以半圆形式显示，并且如果指定的摄入量大于基础代谢，则在半圆上标明基础代谢的位置
6、首次使用软件，需要填写计算所需的身体数据，然后根据数据自动计算出BMR和TDEE，计算出的两个数据，后续在首页左上角用户图标点击即可查看

注意：
1、每次输出都先输出copy，然后再输出内容
2、你指导我在Android studio里的每一步行动
3、在说studio里每个选项时，中英文同时输出（例如：class 类）
4、每写出/修改一个功能，就应该提交一次git
5、停止输出时，必须发出提示音

最终：
1、部署到GitHub，并且告诉我须知事项
2、总结软件所有功能，写一个GitHub页面的readme
3、测试所有功能

================================================================================
                              需要逐步完善的功能
================================================================================

1、✅ 首页应与报表同层级（已完成）
2、✅ 点击右下角加号记录每日摄入时，应当能记录的数据有：食物的重量（克/毫升）；每百克的热量、蛋白质、脂肪、碳水；食物的名称；备注；食物的单位（个/杯/瓶等等所有量词）（已完成）
3、✅ 再添加一个与首页、报表同层级的页面，该页面为食物库页面，能显示最近摄入的食物、自定义添加的食物、收藏的食物三种类型（已完成）
4、✅ 再添加第四个同层级的页面————饮食计划页面，该页面是我们添加的所有饮食计划.右下角有加号，点击即可添加饮食计划，饮食计划里可以是一天的内容（三餐）；也可以是一周的内容（每天每餐）；也可以是一顿的内容（已完成）
5、✅ 模拟并往软件里添加十天的摄入、睡眠、身体数据，模拟用户填写的方式，而不是内置进去，用作测试报表（已完成）
6、✅ 首页顶部日期，点进去后应当进入一个页面，这个页面上半部分是日历形式，这个日历的每个日期的格子应当为热力图，也就是这个格子的背景颜色的深浅应当与当日摄入的热量多少相匹配；下半部分是上半部分日历选中的日期的当日摄入的饮食记录。（已完成）
7、✅ 导入、导出备份数据，应当只包含文本数据，尽量精简。（已完成）
8、✅ 首页右下角加号记录摄入时，应当优先为搜索食物库中的食物，然后下面有个按钮为"添加自定义食物"然后再进入到我们设置的能自定义食物三大营养素、热量等等的那个界面。并且添加的自定义食物应当加入到食物库页面的自定义食物类里。食物库页面的自定义食物页面的右下角要有个加号，点击即可直接添加自定义食物，自定义的界面与刚才提到的"自定义食物"界面相同。（已完成）
9、✅ 给软件设置中加上可以切换主题颜色的功能，并且要有夜间模式，而且夜间模式能跟随系统，总体风格不能偏离Material Design 3（已完成）
10、✅ 每日的每条食物记录，点击，可以修改这个食物的内容；长按可以选择删除、编辑、批量选择的功能。（已完成）
11、✅ 每次进入软件的时候，它的主题颜色会在默认的绿色闪一下，然后才显示现在选择的主题色请把这个修复。（已完成）
12、✅ 左上角用户图标点击进去后，输入年龄身高体重后保存，但是没有计算出来的数值。（已完成）
13、✅ 我要确认一下，设置中的"食物管理"页面和食物库的"最近摄入"页面，这两个页面应当显示的是软件中所有的食物数据库里的食物，也就是软件内置的食物数据（这个暂时还没有，后期添加）和用户自定义食物二者的总和。食物库中的最近摄入，其本质应该是储存的食物库里的所有食物按照最近被记录的顺序排序。（已完成）
14、✅ 页面上下滑动时，帧率低，显得很卡顿，请将软件和设备的刷新率匹配（已完成）
15、✅ 设置页面中的"食物管理"右下角的加号点击后，按理来说是能够添加自定义食物的，但是现在点击加号无反应（已完成）
16、✅ 首页的”今日摄入”区块，里面应当按照早午晚加餐按顺序分类排放，不要堆在一块。然后分类后，去除首页每项摄入的食物克数右边的”早、午、晚、加餐”等字样（已完成）
17、✅ 日历页面，日历应当随页面一块上下滑动（也就是整个页面应当一起上下滑动），而不是只有日历以下的食物记录部分能滑动；并且，左右滑动日历可以直接切换月份；并且，点击日历上面的年月，应当弹出年月跳转窗口。（已完成）
18、✅ 首页的"身体数据""睡眠"两个区块，应当有跟主题色相同的浅一点的底色来区分两个区块。（已完成）
19、✅ 首页”今日可摄入热量”的圆圈进度，长按后应当可以设置每天的卡路里目标量。（已完成）
20、✅ 底部三个导航键（除了首页以外的三个），长按拖动可以自定义更改顺序。（已完成）
21、✅ 在添加饮食计划的时候，没有搜索食物库中食物的功能，现在只能自定义要添加的食物，请修改成和首页添加摄入一样的页面，然后保留原页面的计划类型和计划名称这两部分。（已完成）
22、✅ 修改/添加食物时，可以自定义食物的图标，图标用emoji，所以还要在软件里内置一套最新的食物emoji；然后同种类食物默认用一个对应emoji来表示即可，例如牛肉羊肉猪肉，都用肉的emoji即可。（已完成）
23、✅ “自定义食物”页面的食物，点击后，应当可以编辑数据。（已完成）
24、✅ 重写”添加摄入”页面。功能：选择餐次和日期、搜索食物库、点击食物弹出对话框输入份量和单位、可添加多个食物、底部保存按钮批量保存。（已完成）
25、✅ 重写”添加自定义食物”页面。功能：记录食物每百克的热量、碳水、蛋白质、脂肪；食物的名称；食物的图标；食物的单位（可选）；有单位时可记录单位重量；保存时直接保存进食物库。三个入口：食物库自定义食物页FAB、添加摄入页添加自定义食物按钮、设置食物管理页FAB。（已完成）
26、✅ 修复测试数据生成的食物图标显示问题。食物库和食物管理中现在正确显示 emoji 图标，而不是英文字母。（已完成）
27、✅ 日历选中日期后，首页数据同步显示选中日期的数据。（已完成）
28、✅ 首页身体数据和睡眠点击后显示当前数据并可以修改。（已完成）
29、✅ 修复”添加自定义食物”页面中图标选择弹窗滚动问题，整个页面一块滚动。（已完成）
30、✅ 设置单位布局调整：数值在左、单位在右，标签改为”数值”。（已完成）
31、✅ 食物库中”最近摄入”和”自定义食物”的所有食物点击后都能修改。（已完成）
32、✅ 编辑食物页面布局与”添加自定义食物”一致，并显示当前数据。（已完成）
33、✅ 添加摄入对话框优化：左边输入框、右边下拉选择框，使用食物库数据计算。（已完成）
34、✅ 修复食物管理首字母排序：中文食物按拼音首字母排序。（已完成）
35、✅ 首页点击食物弹出修改框：可修改餐次、数值、单位。（已完成）
36、✅ 首页食物图标使用食物库图标：IntakeRecordEntity存储foodIcon字段。（已完成）
37、✅ 修复食物管理字母导航栏滚动：点击字母正确跳转到对应位置。（已完成）
38、✅ 重写测试数据生成器：生成最近一周数据，包含图标信息。（已完成）
39、✅ 确认食物库作为统一数据源：各页面使用一致的FoodRepository数据。（已完成）
40、✅ 添加摄入使用食物库数据：FoodEntity包含完整营养和图标信息。（已完成）
41、✅ 测试数据生成器从食物库选择：使用真实食物数据生成记录。（已完成）
42、✅ 添加摄入保存后直接返回首页：修改导航逻辑。（已完成）
43、✅ 修复添加摄入保存后返回首页：saveCompleted改为StateFlow。（已完成）
44、✅ 修复食物图标显示英文：添加isEmoji检查，非emoji则根据名称推断。（已完成）
45、✅ 修复修改窗口餐次选择大小不一致：使用weight(1f)确保等宽。（已完成）
46、✅ "添加自定义食物"页面营养数据改为每n克，单位增加毫升，计算自动换算。（已完成）
47、✅ 修改窗口餐次选择在同一行显示，等高等宽，maxLines=1。（已完成）
48、✅ 首页食物图标添加isEmoji检查，非emoji则根据名称推断。（已完成）
49、✅ "添加摄入"页面搜索食物列表限制在固定高度框内滚动（280dp）。（已完成）
50、✅ "添加自定义食物"按钮移到搜索框外面，不跟搜索结果一起滚动。（已完成）
51、✅ DefaultFoods中icon改为emoji，AddIntakeViewModel保存时确保存储正确emoji。（已完成）
52、✅ EditIntakeDialog餐次选择调整字体大小，确保四个选项完整显示。（已完成）
53、✅ EditFoodScreen同步"每n克"功能，布局与AddCustomFoodScreen一致。（已完成）
54、✅ 报表页面优化 - 阶段一：营养素图表（堆叠柱状图、热量折线、切换按钮、数据汇总）。（已完成）
55、✅ 报表页面优化 - 阶段二：身体数据图表（肌肉量、三围、平滑曲线、数据切换）。（已完成）
56、✅ 报表页面优化 - 阶段三：睡眠图表（日期标签、时长列表、优化时间轴）。（已完成）
57、✅ 报表页面优化 - 阶段四：报表自定义功能（显示/隐藏图表、默认周期设置、设置对话框）。（已完成）
58、✅ 报表页面优化 - 阶段五：UI/UX优化（骨架屏加载、卡片阴影、统计卡片美化）。（已完成）
59、✅ 睡眠记录页面支持跨天记录、日期选择、滑动转轮时间选择器（iOS风格）。（已完成）
60、✅ 日历页面食物显示与首页同步，按餐次分组、图标一致。（已完成）
61、✅ 添加饮食计划页面布局与添加摄入页面一致，保留保存计划按钮。（已完成）
62、✅ 计划页面添加排序功能（名称拼音、添加时间升降序、手动排序）。（已完成）
63、✅ 睡眠记录页面精简优化，紧凑布局，一屏显示完整。（已完成）
64、✅ 日历与首页日期同步，进入日历时显示当前选中日期，重开软件重置为今天。（已完成）
65、✅ 添加饮食计划页面星期选择可左右滑动，显示完整七天，选项等宽。（已完成）
66、✅ 添加饮食计划页面搜索食物库部分与添加摄入页面一致。（已完成）
67、✅ 点击饮食计划跳转到编辑计划页面，与添加页面布局一致。（已完成）
68、✅ "数据报表"页面，只保留周、月的筛选机制。（已完成）
69、✅ "营养素摄入"、"身体数据趋势"、"睡眠记录"三个区块点击都可以跳转到对应的一个页面。（已完成）
70、✅ 营养素摄入详情页：柱状图区块、平均摄入量区块、摄入总量区块；周/月筛选；周显示7天每天数据，月显示4周每周汇总。（已完成）
71、✅ 同#70（已完成）
72、✅ 身体数据趋势详情页：自定义时间范围/以周为一点两种筛选；折线图区块；数据变化总结区块；右上角切换体重体脂/三围；周中位数计算。（已完成）
73、✅ 先不要输出代码，先确认我的需求，并给我一个计划让我确定，现在我们来指定一下”睡眠记录”的子页面的内容。我要求有两个区块，分别是睡眠图表区块和睡眠总结区块，两个区块显示的数据的时间范围是同步且一致的；睡眠总结区块要求有：平均入睡时间点和平均起床时间点和平均睡眠时长；时间范围筛选有周、月；睡眠图表在周的筛选下，显示的是七天每天的，在月的筛选下，现实的是四周每周的平均；然后睡眠图表现在是横着的，我需要它是竖着的。（已完成）
74、✅ 重写一下睡眠记录页面的时间记录功能。（已完成）
75、✅ 添加饮食计划页面的星期选项，只显示一二三四的字，不要”周”这个字了，把滑动功能删除#71（已完成）
76、✅ 把”饮食计划”页面和设置里的”饮食计划”隐藏。（已完成）
77、✅ “数据报表”页面的”营养素摄入”区块，柱状图没有和x轴对齐，并且切换成月的时候，x轴没有变为第一周、第二周，这样的四个柱状图（已完成）
78、✅ 统一一下颜色，碳水用海蓝色，蛋白质用亮黄色、脂肪用血红色（亮一点的），在任意主题颜色下都这样。（已完成）
79、✅ 把蛋白改为草绿色（已完成）
80、
================================================================================
                              Git 版本控制操作指南
================================================================================

【什么是 Git？】
Git 是一个"时光机"，可以记录你代码的每一次修改，随时回退到之前的版本。
备份存储在项目文件夹下的 .git 文件夹中（本地备份，不需要网络）。

【常用操作 - 自己可以在终端执行】
1. 查看当前状态
   git status
2. 查看历史版本（显示提交记录）
   git log --oneline
3. 回退到指定版本（⚠️ 谨慎操作，会丢失当前未提交的修改）
   git reset --hard <commit-hash>
   例如：git reset --hard 5b0f612
4. 撤销未提交的修改（恢复到上一次提交的状态）
   git checkout -- .          # 撤销所有文件的修改
   git checkout -- <文件名>   # 撤销单个文件的修改
5. 查看某次提交的详细内容
   git show <commit-hash>

【备份策略】
- 完成重大功能/需求后，Claude 会提醒确认是否创建备份
- 每次备份会记录在下方"项目进度"中

================================================================================
                                 项目进度记录
================================================================================

【已完成功能概览】

一、数据层 (Data Layer)
  ├── Entity 实体类 (7个)
  │   ├── FoodEntity - 食品营养数据
  │   ├── IntakeRecordEntity - 摄入记录（支持多天分摊）
  │   ├── BodyRecordEntity - 身体数据（体重/体脂/肌肉/三围）
  │   ├── SleepRecordEntity - 睡眠记录
  │   ├── MealPlanEntity - 饮食计划
  │   ├── MealPlanItemEntity - 饮食计划项目
  │   └── UserSettingsEntity - 用户设置
  ├── DAO 数据访问对象 (7个)
  │   └── 完整的 CRUD 操作和查询方法
  ├── Database
  │   └── HealthTrackerDatabase - Room 数据库配置
  └── Repository 仓库层 (6个)
      └── 数据访问封装

二、依赖注入 (Hilt)
  └── DatabaseModule - 数据库和 DAO 提供

三、UI 层 (Jetpack Compose)
  ├── 导航
  │   ├── Screen.kt - 路由定义 (11个页面)
  │   └── HealthTrackerNavGraph.kt - 导航图
  ├── 首页 (HomeScreen)
  │   ├── 热量半圆进度显示
  │   ├── 营养素摘要卡片
  │   ├── 今日摄入列表
  │   ├── 身体数据卡片
  │   ├── 睡眠数据卡片
  │   └── 多功能 FAB (展开式添加按钮)
  ├── 报表页 (ReportsScreen) - 基础结构
  ├── 记录页面
  │   ├── AddIntakeScreen - 添加摄入
  │   ├── AddBodyDataScreen - 添加身体数据
  │   └── AddSleepScreen - 添加睡眠
  └── 设置页面
      ├── SettingsScreen - 设置主页
      ├── UserProfileScreen - 用户资料
      ├── MealPlansScreen - 饮食计划
      ├── FoodManagerScreen - 食品管理
      ├── DataExportScreen - 数据导出
      └── DataImportScreen - 数据导入

四、工具类
  ├── DateTimeUtils - 日期时间处理
  └── HealthCalculator - 健康计算 (BMR/TDEE等)

五、内置数据
  └── DefaultFoods - 20种常见食品营养数据

六、主题
  └── Material Design 3 主题配置

================================================================================
                                 修复记录
================================================================================

【2026-03-05】初始设置
- Git 仓库初始化完成
- 创建 .gitignore 排除缓存文件
- 初始提交：78 个文件，4502 行代码
- 提交 hash：5b0f612

【2026-03-05】构建问题修复
- 问题1：AGP 9.1.0 与 Hilt 2.52 不兼容 → 降级 AGP 到 8.7.3
- 问题2：缺少 AndroidX 配置 → 添加 android.useAndroidX=true
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL，97 个任务执行完成
- APK 输出位置：app/build/outputs/apk/debug/app-debug.apk

【2026-03-05】闪退问题修复
- 问题：DatabaseModule 中初始化默认食品数据的逻辑错误
  - 原代码：foodDao.getAllFoods().toString().isEmpty()
  - 问题：getAllFoods() 返回 Flow，toString() 永远不为空
- 解决方案：
  1. 添加 getFoodCount() 方法到 FoodDao
  2. 使用 runBlocking 同步检查并初始化数据
  3. 添加异常处理防止初始化失败导致崩溃
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-05】ClassNotFoundException 修复（重大重构）
- 问题：Hilt 生成的代码未正确打包，导致 HealthTrackerApplication 类找不到
- 根本原因：
  1. 缺少 kotlin-android 插件
  2. KSP 与 Hilt 的兼容性问题
  3. JVM target 版本不一致
- 解决方案：
  1. 切换从 KSP 到 KAPT 处理 Hilt 注解
  2. 添加 kotlin-android 和 kotlin-kapt 插件
  3. 统一 JVM target 为 17
  4. 添加 material-icons-extended 依赖
  5. 修复缺失的颜色定义（Color.kt）
  6. 修复图标引用（FileDownload → Download, FileUpload → Upload）
  7. 修复导入和包名问题
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL，APK 大小 17.8 MB

【2026-03-05】导航结构重构 - 首页与报表同级
- 问题：报表页是首页的子页面，有返回键，不符合设计要求
- 解决方案：
  1. 创建 MainScreen.kt 作为主容器，包含底部导航栏
  2. 首页和报表页作为同级页面，通过底部导航栏切换
  3. 移除 HomeScreen 的底部导航栏和 onNavigateToReports 参数
  4. 移除 ReportsScreen 的返回按钮和 onNavigateBack 参数
  5. 更新 Screen.kt 添加 Main 路由
  6. 更新 HealthTrackerNavGraph 使用 MainScreen
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL，首页和报表页现在同级切换

【2026-03-05】功能 #2：完善摄入记录页面
- 需求：记录食物重量、每百克营养数据、名称、备注、单位等
- 修改内容：
  1. IntakeRecordEntity - 添加 note, unit, amountInUnit, gramsPerUnit 字段
  2. IntakeRecordEntity - 添加每百克营养数据字段（caloriesPer100g等）
  3. IntakeRecordEntity - foodId 改为可空，支持自定义输入
  4. HealthTrackerDatabase - 版本升级 1→2，添加 Migration
  5. DatabaseModule - 添加 Migration 支持
  6. AddIntakeScreen - 完整重写，支持所有输入字段和实时计算
  7. AddIntakeViewModel - 新建，处理保存逻辑
- 界面功能：
  - 餐次选择（早餐/午餐/晚餐/加餐）
  - 食物名称输入
  - 重量输入（克/毫升）
  - 单位选择（个/杯/瓶等）+ 单位换算
  - 每百克营养数据输入（热量/碳水/蛋白质/脂肪）
  - 实时计算结果预览
  - 备注输入
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-05】功能 #3：添加食物库页面
- 需求：添加与首页、报表同层级的食物库页面，显示最近摄入、自定义、收藏食物
- 修改内容：
  1. FoodEntity - 添加 isFavorite 字段
  2. FoodDao - 添加 getCustomFoods(), getFavoriteFoods(), toggleFavorite(), setFavorite() 方法
  3. IntakeRecordDao - 添加 getRecentRecords() 方法
  4. FoodRepository - 添加对应方法
  5. IntakeRecordRepository - 添加 getRecentRecords() 方法
  6. HealthTrackerDatabase - 版本升级 2→3，添加 MIGRATION_2_3
  7. DatabaseModule - 添加新 Migration
  8. FoodLibraryScreen - 新建，包含三个 Tab 页面
  9. FoodLibraryViewModel - 新建，处理数据和操作
  10. Screen.kt - 添加 FoodLibrary 路由
  11. MainScreen.kt - 添加第三个底部导航项
- 界面功能：
  - 三个 Tab：最近摄入、自定义食物、收藏食物
  - 最近摄入：显示最近吃过的食物（去重）
  - 自定义食物：显示用户添加的食物，可删除、收藏
  - 收藏食物：显示收藏的食物，可取消收藏
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-05】功能 #4：添加饮食计划页面
- 需求：添加第四个同级页面，显示所有饮食计划，支持添加单餐/单日/周计划
- 修改内容：
  1. MealPlanItemEntity - 移除 foodId 外键约束，改为可空，添加营养数据字段
  2. HealthTrackerDatabase - 版本升级 3→4，添加 MIGRATION_3_4
  3. DatabaseModule - 添加新 Migration
  4. MealPlanScreen - 新建，显示所有饮食计划列表
  5. MealPlanViewModel - 新建，处理计划和状态
  6. AddMealPlanScreen - 新建，添加/编辑饮食计划
  7. AddMealPlanViewModel - 新建，处理添加计划逻辑
  8. Screen.kt - 添加 MealPlan, AddMealPlan 路由
  9. MainScreen.kt - 添加第四个底部导航项"计划"
  10. HealthTrackerNavGraph.kt - 添加 AddMealPlan 导航
- 界面功能：
  - 饮食计划列表，可展开查看详情
  - 计划类型：单餐、单日（三餐）、周计划（每天三餐）
  - 每个计划可激活/停用、删除
  - 添加计划时可设置名称、类型，选择餐次和星期
  - 添加食物对话框支持输入名称、份量、营养数据
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-05】功能 #5：生成测试数据
- 需求：模拟用户填写方式生成10天测试数据，用于测试报表
- 修改内容：
  1. TestDataGenerator - 新建，生成模拟的摄入、身体、睡眠数据
  2. TestDataViewModel - 新建，处理数据生成逻辑
  3. SettingsScreen - 添加"生成测试数据"选项和确认对话框
- 功能特点：
  - 摄入记录：每天3-5顿，包含常见食物（米饭、鸡蛋、牛奶等15种）
  - 身体数据：体重轻微波动、体脂率、肌肉量、三围
  - 睡眠记录：入睡时间21:00-01:00，睡眠时长5-9小时
  - 所有数据随机化，模拟真实用户行为
- 操作方式：设置 → 开发者选项 → 生成测试数据
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #9：主题切换与夜间模式
- 需求：设置中添加主题颜色切换和夜间模式，支持跟随系统
- 修改内容：
  1. UserSettingsEntity - 添加 themeMode, themeColor 字段
  2. HealthTrackerDatabase - 版本升级 4→5，添加 MIGRATION_4_5
  3. UserSettingsDao - 添加 updateThemeMode(), updateThemeColor(), upsertThemeMode(), upsertThemeColor() 方法
  4. UserSettingsRepository - 添加主题相关方法
  5. Color.kt - 添加5种主题颜色（绿/蓝/紫/橙/红）
  6. Theme.kt - 完全重写，支持多主题和多模式
  7. MainActivity - 观察主题设置并实时应用
  8. ThemeSettingsScreen - 新建主题设置界面
  9. ThemeSettingsViewModel - 新建主题设置 ViewModel
  10. SettingsScreen - 添加主题设置入口
- 功能特点：
  - 主题模式：跟随系统、浅色、深色三种
  - 主题颜色：绿色、蓝色、紫色、橙色、红色五种
  - 设置页面有实时预览效果
  - 完全遵循 Material Design 3
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #8 优化：拆分摄入记录页面
- 需求：首页加号进入的页面只显示搜索，点击食物或"添加自定义食物"后再进入详细录入页面
- 修改内容：
  1. AddIntakeScreen - 重写为搜索页面（餐次选择 + 搜索框 + 搜索结果 + 按钮）
  2. CustomFoodInputScreen - 新建详细录入页面
  3. HealthTrackerNavGraph - 添加带参数导航
  4. MainScreen - 添加 onNavigateToCustomFood 参数
- 页面流程：
  - 首页加号 → 搜索食物页 → 点击食物 → 详细录入页
  - 首页加号 → 搜索食物页 → 点击"+自定义食物" → 详细录入页
  - 食物库自定义食物tab FAB → 详细录入页
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #10：食物记录编辑与批量操作
- 需求：点击修改食物内容，长按删除/编辑/批量选择
- 修改内容：
  1. IntakeRecordDao - 添加 deleteRecordsByIds() 批量删除
  2. IntakeRecordRepository - 添加 deleteRecordsByIds()
  3. HomeViewModel - 添加 deleteRecord(), deleteRecordsByIds(), updateRecord()
  4. HomeScreen - 添加点击编辑、长按菜单、批量选择模式
  5. EditIntakeViewModel - 新建编辑 ViewModel
  6. EditIntakeScreen - 新建编辑页面
  7. Screen.kt - 添加 EditIntake 路由
  8. HealthTrackerNavGraph - 添加编辑页面导航
- 功能特点：
  - 点击食物 → 进入编辑页面修改
  - 长按食物 → 显示菜单（编辑、删除、批量选择）
  - 批量选择模式 → 全选、批量删除
  - 删除操作有确认对话框
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

本次开发时间：3.5 14:30 - 3.6 1:30（约 11 小时）
Token 使用量：15.7m（模型：glm-5）
会话数：8 | 最长会话：10h 12m 20s

【2026-03-06】功能 #11：修复主题颜色启动闪烁问题
- 问题：每次进入软件时，主题颜色会先显示默认绿色，然后才切换到用户选择的主题色
- 根本原因：
  1. 用户设置是异步从数据库加载的（Flow）
  2. 在设置加载完成前，themeColorIndex 默认为 0（绿色）
  3. 加载完成后才切换到用户选择的主题色，造成视觉闪烁
- 解决方案：
  1. 添加 AndroidX SplashScreen 库依赖
  2. 创建 Splash Screen 主题（Theme.HealthTracker.Splash）
  3. 使用 installSplashScreen() API 保持启动画面直到设置加载完成
  4. 设置 setKeepOnScreenCondition 等待 settings 不为 null
- 修改文件：
  - app/build.gradle.kts - 添加 splashscreen 依赖
  - res/values/themes.xml - 添加 Splash Screen 主题
  - AndroidManifest.xml - Activity 使用 Splash 主题
  - MainActivity.kt - 使用 installSplashScreen() 等待设置加载
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL，启动时不再有主题闪烁

【2026-03-06】功能 #12：用户资料页面 BMR/TDEE 计算
- 问题：用户资料页面输入数据后保存，但没有显示计算出的 BMR 和 TDEE 数值
- 根本原因：
  1. UserProfileScreen 是静态 UI，没有 ViewModel
  2. 没有实际保存数据到数据库
  3. 没有调用 HealthCalculator 计算 BMR/TDEE
- 解决方案：
  1. UserSettingsEntity - 添加 age 和 weight 字段
  2. HealthTrackerDatabase - 版本升级 5→6，添加 MIGRATION_5_6
  3. UserSettingsDao - 添加 updateUserInfo() 方法
  4. UserSettingsRepository - 添加 updateUserInfo() 方法
  5. UserProfileViewModel - 新建 ViewModel 处理数据和计算
  6. UserProfileScreen - 重写，使用 ViewModel，实时计算并显示结果
- 功能特点：
  - 加载时自动填充已保存的数据
  - 输入身高、体重、年龄时实时计算并预览 BMR/TDEE
  - 保存时计算并存储 BMR/TDEE 到数据库
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #12：用户资料页面 BMR/TDEE 计算
- 问题：用户资料页面输入数据后保存，但没有显示计算出的 BMR 和 TDEE 数值
- 根本原因：
  1. UserProfileScreen 是静态 UI，没有 ViewModel
  2. 没有实际保存数据到数据库
  3. 没有调用 HealthCalculator 计算 BMR/TDEE
- 解决方案：
  1. UserSettingsEntity - 添加 age 和 weight 字段
  2. HealthTrackerDatabase - 版本升级 5→6，添加 MIGRATION_5_6
  3. UserSettingsDao - 添加 updateUserInfo() 方法
  4. UserSettingsRepository - 添加 updateUserInfo() 方法
  5. UserProfileViewModel - 新建 ViewModel 处理数据和计算
  6. UserProfileScreen - 重写，使用 ViewModel，实时计算并显示结果
- 功能特点：
  - 加载时自动填充已保存的数据
  - 输入身高、体重、年龄时实时计算并预览 BMR/TDEE
  - 保存时计算并存储 BMR/TDEE 到数据库
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL
【2026-03-06】功能 #13：食物库"最近摄入"显示逻辑
- 问题：食物库的"最近摄入"页面应显示食物库中所有食物（内置+自定义），按最近被记录的顺序排序
- 根本原因：
  1. 原实现显示的是 IntakeRecordEntity（摄入记录），而不是 FoodEntity（食物）
  2. 没有按照"最近被记录的顺序"排序
- 解决方案：
  1. IntakeRecordDao - 添加 getFoodLastRecordTimes() 获取每个食物的最近记录时间
  2. IntakeRecordRepository - 添加对应方法
  3. FoodLibraryViewModel - 使用 combine 组合所有食物和最近记录时间，按时间排序
  4. FoodLibraryScreen - 修改 RecentFoodsTab 显示 FoodEntity 列表，支持收藏功能
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL，最近摄入页面现在显示所有食物并按最近记录时间排序

【2026-03-06】功能 #15：食物管理页面完善
- 问题：设置页面的"食物管理"应显示所有食物，按名称排序，右侧有字母导航栏
- 解决方案：
  1. FoodManagerViewModel - 新建 ViewModel 管理食物数据，支持搜索和首字母分组
  2. FoodManagerScreen - 重写，添加字母导航栏，按首字母分组显示食物
  3. 支持搜索、收藏、删除（仅自定义食物可删除）
  4. FAB 点击跳转到添加自定义食物页面
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #14：高刷新率支持
- 问题：页面滑动时帧率低，显得卡顿
- 解决方案：
  1. MainActivity 添加 setHighRefreshRate() 方法
  2. 获取设备支持的最高刷新率并设置
  3. 使滑动更流畅
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #17：日历页面完善
- 需求：日历随页面一起滑动、左右滑动切换月份、点击年月弹出选择窗口
- 修改内容：
  1. CalendarScreen - 整个页面使用 verticalScroll 一起滑动
  2. 添加 HorizontalDragGestures 检测左右滑动
  3. 使用 AnimatedContent 添加月份切换动画
  4. 简化 MonthYearPickerDialog 使用 ExposedDropdownMenuBox
  5. 修复滑动方向：从左向右 = 上个月，从右向左 = 下个月
  6. 移除 enableEdgeToEdge() 修复顶部空白问题
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #18：首页身体数据和睡眠区块底色
- 需求：身体数据和睡眠两个区块添加主题色浅色底色
- 修改内容：
  1. BodyDataCard - 使用 primaryContainer.copy(alpha = 0.5f) 作为背景色
  2. SleepDataCard - 使用相同的主题色背景
  3. 添加圆角 RoundedCornerShape(12.dp)
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #21：添加饮食计划搜索食物库
- 需求：添加饮食计划时可以搜索食物库中的食物，并支持添加自定义食物
- 修改内容：
  1. AddMealPlanScreen - 添加 onNavigateToCustomFood 参数
  2. HealthTrackerNavGraph - 配置导航到自定义食物页面
  3. "添加自定义食物"按钮现在可以正确跳转到自定义食物输入页面
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #22：食物自定义 emoji 图标
- 需求：修改/添加食物时可以自定义食物的 emoji 图标
- 修改内容：
  1. FoodEmojiUtils - 新建工具类，包含食物 emoji 列表和默认映射
  2. EditFoodScreen - 添加 emoji 选择器对话框
  3. EditFoodViewModel - 支持 icon 参数
  4. CustomFoodInputScreen - 添加 emoji 选择器（仅在保存到食物库时显示）
  5. AddIntakeViewModel - saveRecord 支持 icon 参数
  6. 更新多个文件的 getFoodEmoji 函数优先使用食物自带的 icon
- 功能特点：
  - 食物 emoji 按分类组织（主食、肉类、海鲜、蔬菜、水果等）
  - 自动根据食物名称推断默认 emoji
  - 保存自定义食物时可选择 emoji 图标
  - 编辑食物时可更换 emoji 图标
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-06】功能 #23：自定义食物点击编辑
- 需求：自定义食物页面点击食物可以编辑数据
- 状态：功能已存在
  - EditFoodScreen 已存在，支持编辑食物名称、分类、营养数据
  - 导航已配置，FoodLibraryScreen 的 CustomFoodsTab 点击可跳转编辑
  - 现在新增了 emoji 图标编辑功能
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #24：重写"添加摄入"页面
- 需求：重写添加摄入页面，支持选择餐次和日期、搜索食物库、弹出对话框输入份量、批量保存
- 修改内容：
  1. AddIntakeScreen - 完全重写
     - 添加日期选择器（DatePickerDialog）
     - 点击食物弹出 AddFoodDialog 对话框
     - 对话框包含：数值输入框、单位选择器（克/毫升/个/杯/勺/份）、营养预览
     - 添加临时食物列表（PendingFoodItem），支持移除
     - 底部保存按钮显示已添加数量
  2. AddIntakeViewModel - 重写
     - 添加 PendingFoodItem 数据类
     - 添加 pendingItems 状态列表
     - 添加 addPendingItem() 和 removePendingItem() 方法
     - 添加 saveAllRecords() 批量保存方法
  3. IntakeRecordDao - 添加 insertRecords() 批量插入方法
  4. IntakeRecordRepository - 添加 insertRecords() 方法
  5. HealthTrackerNavGraph - 更新导航，移除 onNavigateToFoodDetail 参数
- 功能特点：
  - 日期选择：点击日期卡片弹出日期选择器
  - 餐次选择：快速切换早餐/午餐/晚餐/加餐
  - 搜索食物：搜索食物库中的所有食物
  - 添加对话框：输入数值、选择单位、实时预览营养数据
  - 临时列表：已添加的食物显示在列表中，可移除
  - 批量保存：一次性保存所有添加的食物到指定日期和餐次
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #25：重写"添加自定义食物"页面
- 需求：创建独立的添加自定义食物页面，专门用于添加食物到食物库
- 修改内容：
  1. AddCustomFoodScreen - 新建页面，包含：
     - 食物名称输入
     - 图标选择器（emoji）
     - 每百克营养数据（热量、碳水、蛋白质、脂肪）
     - 可选单位设置（单位名称 + 每单位克数）
     - 每单位营养值预览
  2. AddCustomFoodViewModel - 新建 ViewModel
     - 保存自定义食物到食物库
     - 自动根据食物名称推断分类
  3. Screen.kt - 添加 AddCustomFood 路由
  4. HealthTrackerNavGraph - 添加页面导航
  5. 更新三个入口的导航：
     - 食物库自定义食物页 FAB
     - 添加摄入页"添加自定义食物"按钮
     - 设置食物管理页 FAB
- 结果：BUILD SUCCESSFUL
【2026-03-07】功能 #29 #30 #31 #32：食物编辑相关优化
- #29：修复选择图标弹窗滚动问题
  - 问题：每个类别下有固定的 LazyVerticalGrid 高度，导致只能在类别内滚动
  - 解决：将 LazyVerticalGrid 改为普通 Row，整个对话框内容使用 verticalScroll 一起滚动
- #30：设置单位布局调整
  - 问题：单位输入框布局是单位在左、数值在右，标签是"每单位克/毫升"
  - 解决：调整为数值在左、单位在右，标签改为"数值"
- #31：食物库所有食物点击可编辑
  - 问题：只有"自定义食物"Tab 的食物可以点击编辑
  - 解决：RecentFoodsTab 和 FavoriteFoodsTab 也添加 onFoodClick 参数，点击后跳转编辑页面
- #32：重写编辑食物页面
  - 问题：编辑页面布局与添加自定义食物页面不一致
  - 解决：
    1. FoodEntity 添加 unit 和 gramsPerUnit 字段
    2. 数据库版本升级 7→8，添加 MIGRATION_7_8
    3. EditFoodScreen 完全重写，布局与 AddCustomFoodScreen 一致
    4. 支持单位设置功能
    5. 加载时显示当前数据（包括单位数据）
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #33 #34 #35：摄入记录和食物管理优化
- #33：重写添加摄入对话框
  - 问题：对话框布局不合理，计算逻辑与食物库数据不匹配
  - 解决：
    1. 改为左边输入框、右边下拉选择框的布局
    2. 单位下拉框支持滚动选择
    3. 使用食物库中每百克的营养数据计算
    4. 如果食物有自定义单位，优先显示并使用对应的克数
- #34：修复食物管理首字母排序
  - 问题：中文食物在右侧字母导航栏显示为汉字本身
  - 解决：
    1. 扩展 getFirstLetter 函数，添加更多汉字的拼音首字母映射
    2. 添加 getPinyinLetter 函数，使用 GB2312 汉字拼音首字母区间
    3. 未匹配的汉字返回 "#" 而非汉字本身
- #35：首页点击食物弹出修改框
  - 问题：点击食物跳转到编辑页面，不够便捷
  - 解决：
    1. 在 HomeScreen 添加 EditIntakeDialog 组件
    2. 点击食物弹出对话框，显示当前餐次、数值、单位
    3. 可修改餐次（早餐/午餐/晚餐/加餐）
    4. 可修改数值和单位
    5. 实时预览营养数据，保存后同步更新
- 结果：BUILD SUCCESSFUL
    4. 修改后实时计算营养值预览
    5. 保存后同步到数据库
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #36 #37 #38：图标一致性和导航修复
- #36：首页食物图标使用食物库图标
  - 问题：首页显示的食物图标是根据名称推断的，与食物库中实际图标不一致
  - 解决：
    1. IntakeRecordEntity 添加 foodIcon 字段存储食物图标
    2. 数据库版本升级 8→9，添加 MIGRATION_8_9
    3. AddIntakeViewModel 保存时记录食物图标
    4. HomeScreen 显示时优先使用存储的图标
- #37：修复食物管理字母导航栏滚动
  - 问题：点击右侧字母导航栏（如 Y、Z），列表不能正确滚动到对应位置
  - 解决：重新计算滚动位置，累计前面所有组的 item 数量
- #38：重写测试数据生成器
  - 问题：原来生成10天数据，函数签名复杂
  - 解决：
    1. 重写 TestDataGenerator，生成最近一周（7天）的数据
    2. 每天三餐 + 随机加餐，包含食物图标
    3. 身体数据和睡眠数据从今天往前推7天
    4. 更新 TestDataViewModel 和 SettingsScreen 调用
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #39 #40 #41 #42：食物库统一性和导航优化
- #39：确认食物库作为统一数据源
  - 验证：食物库（内置食物+自定义食物）是底层统一数据源
  - "食物管理"页面、"食物库"页面、"添加摄入"页面搜索都使用相同的 FoodRepository
- #40：添加摄入页面使用食物库数据
  - AddIntakeScreen 搜索结果使用 FoodEntity 完整数据
  - 包含图标、热量、碳水、蛋白质、脂肪等完整信息
- #41：测试数据生成器从食物库选择
  - TestDataGenerator.generateIntakeRecords() 接收 FoodEntity 列表参数
  - TestDataViewModel 从 FoodRepository.getAllFoodsOnce() 获取真实食物
  - 生成的摄入记录包含 foodId、foodIcon 和完整营养数据
  - 添加 FoodDao.getAllFoodsOnce() 和 FoodRepository.getAllFoodsOnce() 方法
- #42：添加摄入保存后直接返回首页
  - 修改 HealthTrackerNavGraph 导航逻辑
  - 使用 popUpTo 清除返回栈，直接返回 Main 页面
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #43 #44 #45：导航和显示优化
- #43：修复添加摄入保存后返回首页
  - 问题：保存后回到初始的添加摄入页面而非首页
  - 解决：将 saveCompleted 从普通属性改为 StateFlow，确保 Compose 正确响应状态变化
  - 修改 AddIntakeViewModel 使用 _saveCompleted MutableStateFlow
  - 修改 AddIntakeScreen 使用 collectAsState() 观察状态
- #44：修复食物图标显示英文问题
  - 问题：首页和添加摄入页面的食物显示英文（如 "rice"）而非 emoji
  - 解决：在 getFoodEmoji 函数中添加 isEmoji 检查
  - 只有当 icon 是有效的 emoji 时才使用，否则根据名称推断
- #45：修复修改窗口餐次选择大小不一致
  - 问题：EditIntakeDialog 中"加餐"选项大小与其他餐次不一致
  - 解决：为每个 FilterChip 添加 weight(1f) 修饰符，确保等宽显示
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #46 #47 #48 #49：营养输入和显示优化
- #46：添加自定义食物页面营养数据改为每n克
  - 问题：固定"每百克"不够灵活，缺少毫升单位
  - 解决：
    1. 添加"每n克/毫升"输入行，支持设置数值和单位（克/毫升）
    2. 营养数据根据输入的n值自动计算每百克的值
    3. "设置单位"下的标签改为"克/毫升"
- #47：修改窗口餐次选择在同一行显示
  - 问题：餐次选项太宽，导致两字分行显示
  - 解决：为 FilterChip 添加 maxLines = 1，调整间距为6dp
- #48：首页食物图标显示修复
  - 问题：foodIcon 存储的英文字符串直接显示，而非推断emoji
  - 解决：在 getFoodEmoji 函数中添加 isEmoji 检查，非emoji则根据名称推断
- #49：添加摄入页面搜索食物列表限制高度
  - 问题：搜索结果列表占据整个页面，不便操作
  - 解决：将列表放入固定高度的 Card 中（280dp，约4行食物），内部滚动
- 结果：BUILD SUCCESSFUL

【2026-03-07】功能 #50 #51 #52 #53：界面和显示优化
- #50：添加摄入页面"添加自定义食物"按钮移到框外
  - 问题：按钮在搜索结果列表内一起滚动，不便操作
  - 解决：将按钮移出 Card，放在搜索框下方单独显示
- #51：首页食物图标与食物库保持一致
  - 问题：DefaultFoods 中 icon 存储的是英文单词（rice, bread等），而非emoji
  - 解决：
    1. 修改 DefaultFoods 中所有食物的 icon 为对应 emoji
    2. AddIntakeViewModel 保存记录时检查 icon 是否为 emoji，非 emoji 则根据名称推断
- #52：修改窗口餐次选择显示问题
  - 问题：午餐、晚餐、加餐只显示第一个字
  - 解决：调整 FilterChip 使用 labelMedium 字体样式，减小间距为 4dp
- #53：编辑食物页面同步"每n克"功能
  - 问题：EditFoodScreen 未同步 AddCustomFoodScreen 的"每n克"布局
  - 解决：完全重写 EditFoodScreen，与 AddCustomFoodScreen 布局一致
- 结果：BUILD SUCCESSFUL
