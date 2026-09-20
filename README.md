# 📅 DateCalculator (日期计算器) - Android v2.0.0

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v2.0.0-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高精度、全功能 **3D 新拟物视觉风格 (Neumorphism)** 日期与工作日计算器 Android 应用。

包含工作日精准推算、科学计算器式`多段模式`算法、交替时间轴图表与 CSV 导出、远期节假日智能算法预测、📈 股市/金融忽略调休模式、大小周/单双休自定义规则、**19 个国家/地区**法定节假日及调休数据库、倒计时与提醒闹钟/日历联动、农历公历双向互转（含干支属相与传统节日烟花粒子），并全量适配系统 **TalkBack 无障碍视觉障碍模式**。

---

## 🌟 核心功能一览 (Key Features)

### 1. 📅 日期计算与多段模式 (Date Calculation & Multi-Stage Pipeline)
- **工作日 / 自然日双模式**：支持根据加减天数精确推算未来或过往日期。
- **📈 股市模式 (强制双休/忽略调休补班)**：针对 A 股与金融交易场景，调休补班日强制按正常周末休市/休息算。
- **🧮 科学计算器式 `多段模式` 拓展**：
  - 点击标题旁小型模式按键自由展开/折叠。
  - 支持多时间段连续累加/累减排期（*例如: 阶段1 +15工作日 ➔ 阶段2 +15工作日*）。
  - 支持给整段计划自定义命名（如 *`毕业旅行` / `装修进度` / `减脂计划`*）及各阶段备注。
  - **总时间安排示意图**：呈现 `----工作日---- 休息日----工作日---休息日` 交替色块比例图，精准区分蓝色工作日、琥珀金周末双休与玫瑰红法定节假日。
  - **📊 一键导出 CSV**：将排期表导出为标准 CSV 电子表格。
- **智能远期节假日预测 (2027+)**：超长跨度推算时自动按照传统历法推算远期节日与调休，并附带智能预测提示。
- **灵活工作日规则**：双休、大小周（单双休轮替）、单休（周日/周六）、无休。

### 2. ⏳ 目标日期倒计时 (Target Date Countdown)
- **常用目标日期**：自动根据当前国家/地区显示全量热门法定节日与考试倒计时（国庆、春节、清明、劳动节、端午、中秋、高考、中考等）。
- **+ 自定义倒计时**：
  - 12 种自选 **Emoji 拟物分类图标**（`📌`, `🎂`, `💍`, `❤️`, `🚀`, `✈️`, `🎓`, `🏠`, `💰`, `🎁`, `⚽`, `🎮`）。
  - **周期性自动滚动**（`不重复`、`每周`、`每月`、`每年`，自动推算下一个周期的目标日期）。
- **一键添加系统提醒**：
  - 🔔 **App 弹出通知提醒**（配合 Android 13+ 运行时通知权限）。
  - ⏰ **跳转系统闹钟**（自动带入事件专属名称）。
  - 📅 **跳转系统日历**（自动创建日程）。

### 3. 🌙 农历公历转换 (Lunar & Solar Converter)
- **公历 ⇄ 农历双向转换**：支持 1900 ~ 2100 年超长跨度精确互转。
- **干支属相与闰月**：自动显示年份干支（如 *丙午 (马) 年*）与闰月推算。
- **🎉 传统佳节烟花动画**：选中传统节日时可点击触发 `FireworksAnimation` 动态烟花粒子散落视效。

### 4. ⚙️ 历史与设置 (Multi-Region Holiday Database & Settings)
- **19 个国家/地区法定节假日与调休数据库**：
  - 🇨🇳 中国大陆、🇹🇼 台湾（中国）、🇭🇰 中国香港、🇲🇴 中国澳门、🇸🇬 新加坡、🇲🇾 马来西亚、🇻🇳 越南、🇯🇵 日本、🇰🇷 韩国、🇬🇧 英国、🇩🇪 德国、🇫🇷 法国、🇮🇹 意大利、🇮🇳 印度、🇮🇩 印尼、🇦🇺 澳大利亚、🇳🇿 新西兰、🇺🇸 美国、🇹🇭 泰国。
- **🛰️ GPS 自动识别所在地**：支持基于网络/GPS定位自动识别并切换当前所在地节假日（默认开启）。
- **启动静默同步**：启动 App 时静默同步最新放假通知，无打扰无 Toast。
- **结构化历史记录**：R 角圆角卡片，分类型色彩标签，带 **✏️ 自由重命名铅笔按键**。

---

## 🎨 设计与无障碍适配 (Design & Accessibility)

- **3D 新拟物美学 (Neumorphism Design)**：柔和凹凸视觉凹陷 (`neumorphicInset`) 与悬浮凸起 (`neumorphicExtruded`)，全套 4 个页面字号完全统一规范。
- **底栏 0 闪烁丝滑切页**：采用 `lerp` 实时颜色插值，底部导航栏在滑动切页时呈现绝对连续平滑的渐变，消除闪烁感。
- **♿ 全量 TalkBack 无障碍适配**：所有按钮与点击元素均提供无障碍朗读语义标签（`semantics` & `Role.Button`），触控热区均大于 **`48.dp x 48.dp`** 规范。

---

## 📝 版本演进与变更日志 (Changelog Summary)

详细的版本更新与 Bug 修复日志请查阅：📜 **[CHANGELOG.md](./CHANGELOG.md)**

### 🚀 v2.0.0 重磅变更摘要
- 🌟 科学计算器式模式切换按键（`多段模式`）。
- 🌟 多段加/多段减全量连算与全局模式统一。
- 🌟 真实比例交替时间轴图表（`总时间安排示意`，精确区分工作日、双休与法定节日）。
- 🌟 远期节假日智能算法预测模型（2027+）。
- 🌟 排期方案自定义命名与历史记录卡片自由重命名（✏️ 铅笔按键）。
- 🌟 启动静默同步放假安排（无打扰）。
- 🌟 全量生活化温馨文案重构与四页面字号完全统一。

---

## 📲 安装包下载 (Download APK)

可直接在仓库根目录获取编译好的 Release 产物：

- 📦 **[DateCalculator-v2.0.0.apk](./DateCalculator-v2.0.0.apk)** (22.0 MB Direct Testing APK)
- 🚀 **[DateCalculator-v2.0.0.aab](./DateCalculator-v2.0.0.aab)** (14.9 MB Google Play Bundle)

---

## 🛠️ 项目架构与技术栈 (Tech Stack)

```text
me.paco.datecalculator
├── data/
│   ├── Models.kt              # 数据模型 (HolidayRegion, WeekendRule, CalculationStage, CustomEventItem)
│   └── RegionalHolidays.kt    # 19 国家/地区法定节假日数据库与智能远期预测算法
├── ui/
│   ├── components/            # 新拟物通用组件 (NeumorphicComponents, DatePickerModal, TimelineDiagram, FireworksAnimation)
│   ├── screens/               # 主功能界面 (DateCalculationScreen, DateDiffScreen, LunarConverterScreen, SettingsScreen)
│   └── viewmodel/             # StateFlow 响应式状态管理 (DateCalculatorViewModel)
└── util/
    ├── DateCalculatorUtils.kt # 工作日/自然日核心算法与时间轴 Block 拆解
    ├── CsvExporter.kt         # Timeline 导出 CSV 电子表格工具
    ├── LunarCalendarUtils.kt  # 农历1900-2100万年历核心算法
    ├── LocationUtils.kt       # GPS/网络国家码识别工具
    ├── PreferenceUtils.kt     # SharedPreferences 持久化存储工具
    └── NotificationUtils.kt   # 系统通知渠道与弹出工具
```

---

## 📄 开源协议 (License)

本项目基于 [MIT License](LICENSE) 协议开源。
