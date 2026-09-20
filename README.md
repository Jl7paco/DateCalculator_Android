# 📅 DateCalculator (日期计算器) - Android v1.3.0

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v1.3.0-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高精度、全功能 **3D 新拟物视觉风格 (Neumorphism)** 日期与工作日计算器 Android 应用。

包含工作日精准推算、股市/金融忽略调休模式、大小周/单双休自定义规则、**19 个国家/地区**法定节假日及调休数据库、倒计时与提醒闹钟/日历联动、农历公历双向互转（含干支属相与传统节日烟花粒子），并全量适配系统 **TalkBack 无障碍视觉障碍模式**。

---

## 🌟 核心功能一览 (Key Features)

### 1. 📅 日期计算 (Date Calculation)
- **工作日 / 自然日双模式**：支持根据加减天数精确推算未来或过往日期。
- **📈 股市模式 (强制双休/忽略调休补班)**：针对 A 股与金融交易场景，调休补班日强制按正常周末休市/休息算。
- **灵活工作日规则**：
  - 双休 (周六日休息)
  - 大小周 (单双休隔周轮替)
  - 单休 (仅周日 / 仅周六休息)
  - 无休 (七天工作制)
- **快捷算天**：内置 `今天`、`昨天`、`+1周`、`-1周`、`5天`、`15天`、`30天` 快捷按键，结果平滑下滑完整展示。

### 2. ⏳ 目标日期倒计时 (Target Date Countdown)
- **常用目标日期**：自动根据当前国家/地区显示全量热门法定节日与考试倒计时（国庆、春节、清明、劳动节、端午、中秋、高考、中考等）。
- **+ 自定义倒计时**：
  - 12 种自选 **Emoji 拟物分类图标**（`📌`, `🎂`, `💍`, `❤️`, `🚀`, `✈️`, `🎓`, `🏠`, `💰`, `🎁`, `⚽`, `🎮`）。
  - **周期性自动滚动**（`不重复`、`每周`、`每月`、`每年`，自动推算下一个周期的目标日期）。
  - 支持长按或小 `x` 键轻松管理与删除。
- **一键添加系统提醒**：
  - 🔔 **App 弹出通知提醒**（配合 Android 13+ 运行时通知权限）。
  - ⏰ **跳转系统闹钟**（自动带入事件专属名称，可自由设置响铃时间）。
  - 📅 **跳转系统日历**（自动创建日程）。

### 3. 🌙 农历公历转换 (Lunar & Solar Converter)
- **公历 ⇄ 农历双向转换**：支持 1900 ~ 2100 年超长跨度精确互转。
- **干支属相与闰月**：自动显示年份干支（如 *丙午 (马) 年*）与闰月推算。
- **🎉 传统节日烟花动画**：选中传统节日时可点击触发 `FireworksAnimation` 动态烟花粒子散落视效。

### 4. ⚙️ 历史与设置 (Multi-Region Holiday Database & Settings)
- **19 个国家/地区法定节假日与调休数据库**：
  - 🇨🇳 中国大陆、🇹🇼 台湾（中国）、🇭🇰 中国香港、🇲🇴 中国澳门、🇸🇬 新加坡、🇲🇾 马来西亚、🇻🇳 越南、🇯🇵 日本、🇰🇷 韩国、🇬🇧 英国、🇩🇪 德国、🇫🇷 法国、🇮🇹 意大利、🇮🇳 印度、🇮🇩 印尼、🇦🇺 澳大利亚、🇳🇿 新西兰、🇺🇸 美国、🇹🇭 泰国。
- **🛰️ GPS 自动识别所在地**：支持基于网络/GPS定位自动识别并切换当前所在地节假日（默认开启）。
- **常用倒计时节日配置**：可自由开启或隐藏任意预设节日按键。
- **结构化历史记录**：R 角圆角卡片，分类型色彩标签，结构化展示计算过程与时间页脚。

---

## 🎨 设计与无障碍适配 (Design & Accessibility)

- **3D 新拟物美学 (Neumorphism Design)**：柔和凹凸视觉凹陷 (`neumorphicInset`) 与悬浮凸起 (`neumorphicExtruded`)，配合 `18.dp` 圆角纯物理弹出菜单，无任何直角/方角阴影残影。
- **底栏 0 闪烁丝滑切页**：采用 `lerp` 实时颜色插值，底部导航栏在滑动切页时呈现绝对连续平滑的渐变，消除闪烁感。
- **♿ 全量 TalkBack 无障碍适配**：所有按钮与点击元素均提供无障碍朗读语义标签（`semantics` & `Role.Button`），触控热区均大于 **`48.dp x 48.dp`** 规范。

---

## 📝 版本演进与变更日志 (Changelog Summary)

详细的版本更新与 Bug 修复日志请查阅：📜 **[CHANGELOG.md](./CHANGELOG.md)**

### 🚀 v1.3.0 重磅变更摘要
- 🌟 新增英国 🇬🇧、德国 🇩🇪、法国 🇫🇷、意大利 🇮🇹、印度 🇮🇳、印尼 🇮🇩，总计支持全球 19 国/地区全量法定节假日。
- 🌟 新增 **📈 股市模式**（针对 A股与金融交易，忽略调休补班，强制按正常双休/休市算）。
- 🌟 设置全面支持持久化存储记忆（退出重进自动记忆地区、规则与开关状态）。
- 🎨 日期倒计时功能区结构调整与平民化重命名（`常用目标日期`、`+ 自定义`、`目标日期`）。
- 🎨 重构结构化历史记录卡片（`18.dp` 圆角，分类彩色标签，计算过程清晰拆解）。
- 🎨 “计算历史”与“工作日与休假规则”标题规格全局统一（`36.dp` 统一高度）。

---

## 📲 安装包下载 (Download APK)

可直接在仓库根目录获取编译好的 Release 产物：

- 📦 **[DateCalculator-v1.3.0.apk](./DateCalculator-v1.3.0.apk)** (21.9 MB Direct Testing APK)
- 🚀 **[DateCalculator-v1.3.0.aab](./DateCalculator-v1.3.0.aab)** (14.8 MB Google Play Bundle)

---

## 🛠️ 项目架构与技术栈 (Tech Stack)

```text
me.paco.datecalculator
├── data/
│   ├── Models.kt              # 数据模型 (HolidayRegion, WeekendRule, CustomEventItem)
│   └── RegionalHolidays.kt    # 19 国家/地区法定节假日与调休数据库
├── ui/
│   ├── components/            # 新拟物通用组件 (NeumorphicComponents, DatePickerModal, FireworksAnimation)
│   ├── screens/               # 主功能界面 (DateCalculationScreen, DateDiffScreen, LunarConverterScreen, SettingsScreen)
│   └── viewmodel/             # StateFlow 响应式状态管理 (DateCalculatorViewModel)
└── util/
    ├── DateCalculatorUtils.kt # 工作日/自然日核心算法
    ├── LunarCalendarUtils.kt  # 农历1900-2100万年历核心算法
    ├── LocationUtils.kt       # GPS/网络国家码识别工具
    ├── PreferenceUtils.kt     # SharedPreferences 持久化存储工具
    └── NotificationUtils.kt   # 系统通知渠道与弹出工具
```

---

## 📄 开源协议 (License)

本项目基于 [MIT License](LICENSE) 协议开源。
