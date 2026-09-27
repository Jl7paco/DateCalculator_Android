# 📅 DateCalculator (日期计算器) - Android v3.0.2

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v3.0.2-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**日期计算器** 是一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高颜值、全功能 Android 日期算法应用。界面采用新拟物 (Neumorphism) 3D 光影设计，内置工作日计算、多段推算、建除十二神正统老黄历、星体行运每日运势、倒数日推算、重要纪念日与 GPS 精准打卡、农历公历双向转换、年龄精准推算及 **5 款桌面小组件 (AppWidgets)**。

---

## 📋 功能特性概览

### 1. 📱 5 款 3D 新拟物桌面小组件 (AppWidgets - 支持 previewLayout 缩略图预览)
- ⏳ **目标倒数日小组件 (`CountdownWidgetProvider`)**：
  - 展示倒数日名称、目标日期与实时剩余天数，支持 **`切换 ➔`** 按键在桌面轮播置顶事件。
- 📜 **今日黄历小组件 (`AlmanacWidgetProvider`)**：
  - 桌面实时推算公历、农历、干支纪年及建除十二神老黄历宜/忌（仅中文界面显示）。
- ❤️ **重要纪念日小组件 (`AnniversaryWidgetProvider`)**：
  - 展示纪念日已陪伴天数与下个周年剩余天数，支持按键轮播卡片。
- 📍 **纪念日打卡小组件 (`CheckInWidgetProvider`)**：
  - 桌面提供 **`📍 1键打卡`** 按键，点击直接抓取当前 GPS 定位城市 + 精准时刻保存打卡记录。
- 📅 **日期快速计算小组件 (`QuickCalcWidgetProvider`)**：
  - 桌面提供 **`+15天`**, **`+30天`**, **`+100天`** 实时计算按键，点击直接在桌面显示推算目标日期。

### 2. 🌐 多语言 i18n 体系与华人/非华人农历规则
- **新增【跟随系统 (System Default)】语言选项**：设为全应用默认语言，自动读取系统 `Locale.getDefault()` 并适配中/英/日/韩。
- **农历与老黄历语言统一规则**：仅在 **【简体中文】** 或 **【繁體中文】** 界面设置下展示农历月日与老黄历（宜/忌）。非中文界面（English, 日本語, 한국어）下 **100% 彻底隐藏农历月日与老黄历卡片**。
- **100% 多语言动态绑定**：阶段名 (`Stage 1 / Stage 2`)、单段计算、周数 (`Sep 27, 2026 (Wk 40)`）、12 星座 (`Virgo`, `Libra`)、生肖 (`Dragon`)、天气第 4 天 (`3 Days Later`) 实现全量多语言翻译。

### 3. 🌟 “首页”全能看板
- **7×6 扩展月历视图**：显示公历、农历月日、二十四节气与法定节假日放假（**“休”**）与调休补班（**“班”**）角标。单元格 44dp 高度确保文字完整无压缩。非中文模式下自动隐藏农历文字。
- **当日老黄历与节气 Card**：仅中文模式下展示正统老黄历“宜”与“忌”事项。
- **📍 GPS 实时天气推算 Card**：动态读取 GPS 定位或城市数据库，推算城市当日及未来三日天气状况，城市名称支持多语言翻译。
- **⭐ 星座每日运势 Card**：结合天体行运相位推算每日综合星级评分、幸运数字、幸运颜色与开运指南。

### 4. ❤️ “重要纪念日”与 📍 GPS 精准打卡
- **专属底栏导航**：底栏正式新增“纪念日”Tab 选项。单个设备最高支持存储 999 个纪念日卡片。
- **📍 GPS 精准打卡**：点击“打卡”自动记录当前城市名称 + GPS 精准经纬度坐标 + 毫秒级打卡时刻，可自动同步联动至倒数日按键。

### 5. 📅 核心日期计算与多段拖拽模式
- **工作日 / 自然日推算**：支持按加减天数推算目标日期，内置多国家/地区法定节假日与调休补班逻辑。
- **多段加减 (`Multi-Stage Calc`) 自由拖拽调换**：支持添加多段时间段，每个时间段配备 **`DragHandle` 拖拽句柄图标**，按住即可上下自由拖拽调换阶段顺序。
- **总时间安排主时间轴**：直观展示工作日、周末双休与法定节假日的真实时间比例条。支持导出为标准 CSV 电子表格文件。

### 6. 🎂 年龄计算器与生日倒计时
- **精确推算多维年龄**：选择出生日期后，自动计算精确的实岁年龄（X岁 X个月 X天）、总生活自然日天数、总月数、总周数。
- **生肖与星座卡片**：展示生肖 Emoji（支持英文/日文/韩文翻译）、星座名称与日期范围。已删除运势展示，界面纯粹干练。

---

## 🛠️ v3.0.1 ➔ v3.0.2 更新与 Bug 修复明细

更详细的缺陷日志请参阅：🐛 **[BUGLIST.md](./BUGLIST.md)**

1. **新增【跟随系统】语言选项**：设为全应用默认语言，自动适配系统 Language。
2. **农历与老黄历统一规则**：仅在简体中文/繁体中文界面下显示农历与黄历，非中文界面下 100% 彻底隐藏。
3. **补齐全量遗漏翻译**：阶段名 (`Stage 1 / Stage 2`)、单段计算、周数 (`Wk 40`)、12 星座 (`Virgo`)、生肖 (`Dragon`)、天气第 4 天 (`3 Days Later`) 实现全量翻译。
4. **小组件配置 previewLayout 预览缩略图**：可在桌面添加小组件选单中直接预览 3D 新拟物原生外观。
5. **UI 精细微调**：英文“Yesterday”缩写为 **`Yest`**；拓宽 `Workday / Calendar Day` 胶囊开关宽度至 150dp 居中；彻底去除了模式切换按钮底部的凹陷椭圆阴影，更名为 **“多段加减”** (`Multi-Stage Calc`)。

---

## 📲 最新安装包下载 (v3.0.2)

- 📦 **[DateCalculator-v3.0.2.apk](./DateCalculator-v3.0.2.apk)** (22.9 MB 测试安装包)
- 🚀 **[DateCalculator-v3.0.2.aab](./DateCalculator-v3.0.2.aab)** (15.0 MB 官方 App Bundle)

---

## 🛠️ 技术栈 (Tech Stack)

- **语言**: Kotlin 2.2.10
- **UI 框架**: Jetpack Compose (Material 3 + 3D Neumorphism Design)
- **桌面组件**: Android AppWidgetProvider + RemoteViews
- **架构**: Clean Architecture / MVVM (`StateFlow` + `ViewModel`)
- **存储**: SharedPreferences (JSON 序列化存储高达 999 个纪念日卡片)
- **定位**: Android GPS Location API + Geocoder 城市推算

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。
