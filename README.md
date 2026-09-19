# 📅 DateCalculator (日期计算器) - Android v1.2.1

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v1.2.1-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高精度、全功能 **3D 新拟物视觉风格 (Neumorphism)** 日期与工作日计算器 Android 应用。

包含工作日精准推算、大小周/单双休自定义规则、13 个国家/地区法定节假日及调休数据库、倒计时与提醒闹钟/日历联动、农历公历双向互转（含干支属相与传统节日烟花粒子），并全量适配系统 **TalkBack 无障碍视觉障碍模式**。

---

## 🌟 核心功能一览 (Key Features)

### 1. 📅 日期计算 (Date Calculation)
- **工作日 / 自然日双模式**：支持根据加减天数精确推算未来或过往日期。
- **灵活工作日规则**：
  - 双休 (周六日休息)
  - 大小周 (单双休隔周轮替)
  - 单休 (仅周日 / 仅周六休息)
  - 无休 (七天工作制)
- **快捷算天**：内置 `今天`、`昨天`、`+1周`、`-1周`、`5天`、`15天`、`30天` 快捷按键，附带平滑 Pop Bounce 视觉触感。

### 2. ⏳ 日期倒计时 (Target Date Countdown)
- **常用节日倒计时**：自动根据当前国家/地区显示热门法定节日与考试倒计时（如国庆、春节、中秋、高考、中考等）。
- **+ 新增自定义倒计时**：
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
- **13 个国家/地区法定节假日与调休数据库**：
  - 🇨🇳 **中国大陆** (含国务院历年实际调休补班)
  - 🇹🇼 **台湾（中国）**
  - 🇭🇰 **中国香港**
  - 🇲🇴 **中国澳门**
  - 🇸🇬 **新加坡**
  - 🇲🇾 **马来西亚**
  - 🇻🇳 **越南**
  - 🇯🇵 **日本**
  - 🇰🇷 **韩国**
  - 🇦🇺 **澳大利亚**
  - 🇳🇿 **新西兰**
  - 🇺🇸 **美国**
  - 🇹🇭 **泰国**
- **🛰️ GPS 自动识别所在地**：支持基于网络/GPS定位自动识别并切换当前所在地节假日（默认关闭，首次开启主动提示权限授权）。
- **计算历史记录**：支持查看、单独删除与一键清空计算日志。

---

## 🎨 设计与无障碍适配 (Design & Accessibility)

- **3D 新拟物美学 (Neumorphism Design)**：柔和凹凸视觉凹陷 (`neumorphicInset`) 与悬浮凸起 (`neumorphicExtruded`)，配合 `18.dp` 圆角纯物理弹出菜单，无任何直角/方角阴影残影。
- **底栏 0 闪烁丝滑切页**：采用 `lerp` 实时颜色插值，底部导航栏在滑动切页时呈现绝对连续平滑的渐变，消除闪烁感。
- **♿ 全量 TalkBack 无障碍适配**：
  - 所有按钮与点击元素均提供无障碍朗读语义标签（`semantics` & `Role.Button`）。
  - 触控热区均大于 **`48.dp x 48.dp`** 规范，方便视障与长者用户顺畅操作。

---

## 📝 版本演进与变更日志 (Changelog Summary)

详细的版本更新与 Bug 修复日志请查阅：📜 **[CHANGELOG.md](./CHANGELOG.md)**

### 🚀 v1.2.1 变更摘要
- 🐛 修复 DatePicker 文本输入模式下修改年份时光标无法在数字 `6` 后面定位的 Bug。
- 🐛 修复 DatePicker 快速选年份网格视图与月份日面的透明重叠显示 Bug。
- 🐛 彻底消除下拉菜单弹出层边缘残留的 Android 原生灰色直角阴影。
- 🐛 彻底消除底栏滑动切页时文字/图标颜色的跳变闪烁感。
- 🌟 支持 GPS 自动定位识别所在地节假日（默认关闭，首次开启自动询问定位授权）。
- 🌟 全量支持 13 个国家/地区节假日及调休数据库，台湾更名为`台湾（中国）`。
- ♿ 全量适配系统 TalkBack 无障碍与大字号高对比度读屏模式。

---

## 📲 安装包下载 (Download APK)

可直接在仓库根目录获取编译好的 Release APK 安装包：

- 📦 **[DateCalculator-v1.2.1.apk](./DateCalculator-v1.2.1.apk)** (21.8 MB)

---

## 🛠️ 项目架构与技术栈 (Tech Stack)

```text
me.paco.datecalculator
├── data/
│   ├── Models.kt              # 数据模型 (HolidayRegion, WeekendRule, CustomEventItem)
│   └── RegionalHolidays.kt    # 13 国家/地区法定节假日与调休数据库
├── ui/
│   ├── components/            # 新拟物通用组件 (NeumorphicComponents, DatePickerModal, FireworksAnimation)
│   ├── screens/               # 主功能界面 (DateCalculationScreen, DateDiffScreen, LunarConverterScreen, SettingsScreen)
│   └── viewmodel/             # StateFlow 响应式状态管理 (DateCalculatorViewModel)
└── util/
    ├── DateCalculatorUtils.kt # 工作日/自然日核心算法
    ├── LunarCalendarUtils.kt  # 农历1900-2100万年历核心算法
    ├── LocationUtils.kt       # GPS/网络国家码识别工具
    └── NotificationUtils.kt   # 系统通知渠道与弹出工具
```

---

## 📄 开源协议 (License)

本项目基于 [MIT License](LICENSE) 协议开源。
