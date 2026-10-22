# 📅 DateCalculator (日期计算器) - Android v3.0.7

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v3.0.7-brightgreen.svg)](CHANGELOG.md)
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
- **100% 多语言动态绑定**：阶段名 (`Stage 1 / Stage 2`)、单段计算、周数 (`Sep 27, 2026 (Wk 40)`)、12 星座 (`Virgo`, `Libra`)、生肖 (`Dragon`)、天气第 4 天 (`3 Days Later`) 实现全量多语言翻译。

### 3. 🌟 “首页”全能看板与 3D 卫星地球背景
- **7×6 扩展月历视图**：显示公历、农历月日、二十四节气与法定节假日放假（**“休”**）与调休补班（**“班”**）角标。单元格 44dp 高度确保文字完整无压缩。非中文模式下自动隐藏农历文字。
- **当日老黄历与节气 Card**：仅中文模式下展示正统老黄历“宜”与“忌”事项。
- **📍 GPS 实时天气推算 Card**：动态读取 GPS 定位或城市数据库，推算城市当日及未来三日天气状况，支持点击天数进入二级 6 小时精细时段预报（包含今天/明天时段标注与降水概率）。
- **🌍 3D 卫星视角地球背景模式**：支持背景模式 3 选 1（空白 / 月历水印 / 3D卫星地球）。3D 地球视角精准对准当前 GPS 城市，支持 1,000km~25,000km 视距缩放，并根据当地实时昼夜自动切换浅色/深色主题。

### 4. ❤️ “重要纪念日”与 📍 GPS 精准打卡
- **专属底栏导航**：底栏正式新增“纪念日”Tab 选项。单个设备最高支持存储 999 个纪念日卡片。
- **📍 GPS 精准打卡**：点击“打卡”自动记录当前城市名称 + GPS 经纬度坐标 + 毫秒级打卡时刻，可自动同步联动至倒数日按键。

### 5. 📅 核心日期计算与多段拖拽模式
- **工作日 / 自然日推算**：支持按加减天数推算目标日期，内置多国家/地区法定节假日与调休补班逻辑。
- **阶段截止日期“选择日期”联动**：多段加减每行配备 3D 拟物“选择日期”按钮，可输入天数自动推算截止日期，也可直接在日历中选择目标截止日期并反向倒推所需天数。
- **总时间安排主时间轴**：直观展示工作日、周末双休与法定节假日的真实时间比例条。支持导出为 **原生 `.xls` 电子表格（包含 1:1 单元格背景颜色填充甘特图）**。

### 6. 🎂 年龄计算器与生日倒计时
- **精确推算多维年龄**：选择出生日期后，自动计算精确的实岁年龄（X岁 X个月 X天）、总生活自然日天数、总月数、总周数。
- **生肖与星座卡片**：展示生肖 Emoji（支持英文/日文/韩文翻译）、星座名称与日期范围。已删除运势展示，界面纯粹干练。

---

## 🛠️ v3.0.6 ➔ v3.0.7 更新明细

更详细的变更历史与缺陷日志请参阅：变更日志 **[CHANGELOG.md](./CHANGELOG.md)** / 🐛 **[BUGLIST.md](./BUGLIST.md)**

1. **R8 Full Mode 极致全量代码优化**：开启 R8 Full Mode，消除了 Google Play Console “DEX 代码优化低”警告，优化比例提升至 85%+。
2. **三向背景模式与 3D 卫星地球**：支持“空白 / 月历水印 / 3D卫星地球”模式，3D 地球视角对准 GPS 定位，支持 1,000km~25,000km 视距拉近与远眺。
3. **昼夜实时重写主题色**：处于 3D 地球模式时，系统根据用户所在位置的太阳高度角自动强切主题（白昼浅色新拟物，黑夜深色夜景）。
4. **二级天气卡片 UI 风格 1:1 像素级统一**：二角弹窗的标头、圆角（22dp）、3D 沉降槽以及 4 栏并发排版与主页 Weather Card 彻底统一。
5. **修正温度重复符号与降水概率高亮**：格式化温度为 `23°C~34°C`，并在指示栏右侧增加 `💧 降水概率: 15%` 高亮。
6. **底栏虚拟导航按键防遮挡**：导航栏增加 `navigationBarsPadding()` 避让，彻底解决系统三按键虚拟导航栏（`≡ ▢ <`）遮挡底部 Tab 的问题。

---

## 📲 最新安装包下载 (v3.0.7)

- 📦 **[DateCalculator-v3.0.7-R8-Optimized.apk](./DateCalculator-v3.0.7-R8-Optimized.apk)** (6.3 MB R8 混淆极速测试包)
- 🚀 **[DateCalculator-v3.0.7-R8-Optimized.aab](./DateCalculator-v3.0.7-R8-Optimized.aab)** (18.2 MB R8 混淆正式发布包)

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
