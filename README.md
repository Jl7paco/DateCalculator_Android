# 📅 DateCalculator (日期计算器) - Android v3.0.1

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v3.0.1-brightgreen.svg)](README.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**日期计算器** 是一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高颜值、全功能 Android 日期算法应用。界面采用新拟物 (Neumorphism) 3D 光影设计，内置工作日计算、多段推算、建除十二神正统老黄历、星体行运每日运势、倒数日推算、重要纪念日与 GPS 精准打卡、农历公历双向转换及年龄精准推算等功能。

---

## 📋 功能特性概览

### 1. 📅 核心日期计算与多段拖拽模式
- **工作日 / 自然日推算**：支持按加减天数推算目标日期，内置多国家/地区法定节假日与调休补班逻辑。
- **多段推算与自由拖拽调换**：
  - 支持添加多段时间段连续加减算。
  - 每个时间段配备 **`DragHandle` 拖拽句柄图标**，按住即可上下自由拖拽调换阶段顺序。
  - **总时间安排主时间轴**：直观展示工作日、周末双休与法定节假日的真实时间比例条。
  - 支持导出为标准 CSV 电子表格文件。
- **单段推算精简呈现**：单段推算结果隐藏子时间轴拆解，仅保留总时间安排主时间轴，界面清晰简洁。

### 2. ❤️ 重要纪念日与 📍 GPS 精准打卡（v3.0.1 全新重磅上线）
- **独立功能页面**：底栏提供专属“纪念日”导航，页面纯粹聚焦于纪念日与打卡，**不显示任何星座与运势信息**。
- **高容量与全量列表**：单个设备**最多可存储 999 个**重要纪念日卡片，并在页面直观呈现全量历史卡片。
- **独立存储不同步**：纪念日卡片与打卡数据独立于历史记录表，不增加历史记录负担。
- **自动双向联动**：保存纪念日或完成打卡后，**100% 自动同步联动**至倒数日页面的自定义快捷按键区。
- **📍 GPS 精准打卡**：
  - 点击“📍 打卡”可自动获取当前所在**城市/地区名称 + 准确 GPS 经纬度坐标 + 毫秒级打卡时刻**。
  - 支持手动输入当时做了什么或去了哪里（如：*在深圳湾公园看海*），记录美好时光。

### 3. 📜 建除十二星神老黄历与星体行运运势
- **正统建除十二值星神算法**：基于千年干支历法月令（月支）与日干支，精准推算建、除、满、平、定、执、破、危、成、收、开、闭等正统老黄历“宜”与“忌”事项。
- **星体相位每日运势**：结合黄道十二宫主照行星行运轨迹，推算事业、财运、感情、健康分项评分与每日开运指南。

### 4. ⏳ 目标倒数日推算
- **公农历双历同框**：起始日期与目标日期均配备**同行精细单选切换开关**（尺寸缩小 30%）。
- **推栈卡片位置重构**：推算结果卡片与置顶卡片精准排列在“常用倒数日”快捷图标区域正下方。
- **醒目按键**：目标日期推算等于号按键 **`=`** 采用醒目红色（`#EF4444`）强调设计。

### 5. 🌙 深色模式（Dark Theme）全局强行应用
- 搭建 `LocalDarkTheme` 组合局部上下文（CompositionLocal），彻底解决 Android 系统设置干扰。
- 黑暗模式选择 **`“开启”`** 时，应用会**忽略手机系统设置，0毫秒瞬间将整个 App、底栏、所有功能卡片与弹窗切换为纯正深色模式** (`#1B232A`)。
- 水印不透明度在深色模式下提升至 28%，纹理立体清晰。

### 6. 🎨 14 款精选配色与主题调色盘
- 扩充调色盘至 **14 款绚丽主色调**（宝石蓝、翡翠绿、极光紫、樱花粉、蔚蓝海、夕阳橘、珊瑚红、深松绿、星钻紫、古铜金、绿松石、沉稳灰、青青草、魅惑紫）。
- 移除了调色盘最右侧的橙色竖条。
- 主题配色卡片调整至“周末休息模式”卡片正下方。

---

## 🛠️ v2.1.0 ➔ v3.0.1 代码更新变动与 Bug 修复日志

### 变更明细 (Changelog)

| 模块 / 文件 | 更新类型 | 详细代码变动说明 |
| :--- | :--- | :--- |
| **`app/build.gradle.kts`** | 版本升级 | 更新 `versionCode = 301`, `versionName = "3.0.1"`。 |
| **`MainActivity.kt`** | 主架构 | 给 `DateCalculatorTheme` 传入 ViewModel 响应式 `darkTheme = isDark` 状态，确保全局主题更新。 |
| **`MainScreen.kt`** | 导航与主题 | 修正内部 `DateCalculatorTheme` 遗漏传入 `darkTheme` 的问题；在底栏新增 **“纪念日”** 专属 Tab。 |
| **`Theme.kt`** | 主题引擎 | 引入 `LocalDarkTheme` CompositionLocal；修复 Dialog 内部 `view.context` 强转 Activity 的崩溃 bug（新增 `findActivity()` 安全扩展）。 |
| **`Neumorphic.kt`** / **`NeumorphicComponents.kt`** | 新拟物组件 | 降级 `isSystemInDarkTheme()` 为 `LocalDarkTheme.current` 感知；精简 `SolarLunarSwitch` 与 `WorkdayNaturalSwitch` 尺寸（30dp 高度）。 |
| **`AnniversaryScreen.kt`** | 新增页面 | 打造“重要纪念日”专属页面，包含全量卡片列表（上限999）、无运势无历史同步、3D 边框空白卡、📍 GPS 打卡与自定义纪念日自动联动。 |
| **`DateCalculationScreen.kt`** | 日期计算 | 在多段模式卡片中加入 `Icons.Default.DragHandle` 与 `detectDragGestures` 手势，支持上下自由拖拽调换阶段顺序。 |
| **`DateDiffScreen.kt`** | 倒数日 | 将“选择起始日期”与“选择目标日期”的公农历开关提升至同行展示；结果卡片后置于“常用倒数日”图标下方；等于号设为红色 (`#EF4444`)。 |
| **`HomeScreen.kt`** | 首页布局 | 顶栏删掉“主页”字样；月历行高调整为 44dp（农历月日显示完整）；调整垂直间距使运势卡片 Header 正好露头；恢复天气预报卡片。 |
| **`LunarCalendarUtils.kt`** | 历法算法 | 重构黄历宜忌为正统**建除十二值星神算法**；升级每日运势为**天体行运相位算法**。 |
| **`DynamicCalendarWatermarkBg.kt`** | 3D 水印 | 切换为 `LocalDarkTheme.current` 感知；深色模式下水印不透明度提升至 0.28f，清晰易读。 |
| **`SettingsScreen.kt`** | 系统设置 | 主题配色卡片调整至休息模式卡片下方；扩充至 14 款精选配色；深色模式更名为“深色模式”，选项简化为“开启”与“关闭”。 |

---

### Bug 修复列表 (Bug Fix List)

1. **Bug 1: 深色模式开关点击后只有设置弹窗变黑，主界面无效果**
   - **原因**：`MainScreen.kt` 内部调用的 `DateCalculatorTheme` 遗漏了 `darkTheme` 参数，默认回退到了系统设置 `isSystemInDarkTheme()`。
   - **解决**：在 `MainScreen.kt` 中准确解析并传入 `darkTheme = isDark`，并建立了全局 `LocalDarkTheme` 上下文。

2. **Bug 2: 深色模式下点击设置按钮闪退崩溃 (`ClassCastException`)**
   - **原因**：`Theme.kt` 侧边效应中使用了 `(view.context as Activity)`，而在 Dialog 内部 `context` 实际类型为 `ContextThemeWrapper`。
   - **解决**：编写了 `tailrec fun Context.findActivity(): Activity?` 递归安全解包，彻底解决类型转换崩溃。

3. **Bug 3: 首页月历部分日期与农历文字被压缩截断**
   - **原因**：网格行高仅为 38dp，双行文字在特定字体缩放下空间不足。
   - **解决**：行高扩展至 44dp，并对农历文字加上 `maxLines = 1` 与 `TextOverflow.Ellipsis` 保护。

4. **Bug 4: 深色模式下月历 3D 水印不清晰**
   - **原因**：深色背景下仅 10% 的白色不透明度导致对比度过低。
   - **解决**：深色模式下提升不透明度至 28% (`0.28f`)，纹理清晰立体。

5. **Bug 5: 倒数日生成的卡片遮挡了输入框与常用节日**
   - **原因**：推栈卡片在布局层次中放置在“常用倒数日”区域上方。
   - **解决**：重构 `DateDiffScreen.kt` 布局顺序，将推算卡片精准移动到“常用倒数日”快捷图标区正下方。

6. **Bug 6: 多段计算卡片无法直观调整前后顺序**
   - **解决**：引入 `detectDragGestures` 垂直拖拽手势，配合 `DragHandle` 图标，实现流畅拖拽换位。

7. **Bug 7: 重要纪念日无记录空白卡无边框**
   - **解决**：为空白卡加入 1.2dp `NeumorphicAccent` 3D 边框，与正式卡片样式完美统一。

---

## 📲 最新安装包下载 (v3.0.1)

- 📦 **[DateCalculator-v3.0.1.apk](./DateCalculator-v3.0.1.apk)** (22.4 MB 测试安装包)
- 🚀 **[DateCalculator-v3.0.1.aab](./DateCalculator-v3.0.1.aab)** (15.0 MB 官方 App Bundle)

---

## 🛠️ 技术栈 (Tech Stack)

- **语言**: Kotlin 2.2.10
- **UI 框架**: Jetpack Compose (Material 3 + 3D Neumorphism Design)
- **架构**: Clean Architecture / MVVM (`StateFlow` + `ViewModel`)
- **存储**: SharedPreferences (JSON 序列化存储高达 999 个纪念日卡片)
- **定位**: Android GPS Location API + Geocoder 城市推算

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。
