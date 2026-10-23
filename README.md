# 📅 DateCalculator (万能日期计算器) - Android v3.0.7

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v3.0.7-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**万能日期计算器** 是一款基于 **Kotlin** 与 **Jetpack Compose** 打造的高颜值、全功能 Android 综合日期与时间算法工具。界面采用新拟物 (Neumorphism) 3D 光影设计，内置工作日计算、多段推算、建除十二神正统老黄历、星体行运每日运势、倒数日推算、防伪水印相机、多语言时间跨度计算器、重要纪念日打卡、农历公历双向转换、年龄精准推算及 **5 款 3D 桌面小组件 (AppWidgets)**。

---

## 📋 功能特性概览

### 1. 📷 全能防伪水印相机 (Watermark Camera)
- **4:3 不裁剪全物理视角取景**：取景画面严格采用 `FIT_CENTER` 比例，保持摄像头传感器原始视野，绝不盲目裁剪画面。
- **全硬件焦段倍率切换**：自动读取手机摄像头硬件支持的真实倍率（支持 `0.5x` / `0.6x` 超广角、`1x` 主摄、`2x`、`3x` 中长焦及 `5x`、`10x` 潜望长焦），黑透小胶囊悬浮于顶栏，不遮挡取景画面。
- **1:1 防伪水印卡片（横竖屏精确几何落点）**：水印卡片放置于取景框内部右上角。在手机横屏旋转（90°/270°）时，以右上角为轴心顺畅旋转并应用动态几何补偿，保证横竖屏水印均 100% 锁死在取景框内部，绝不上穿黑边或超出屏幕。
- **照片与视频双模式 + 全屏预览确认（↺ 重拍 / ✔ 满意使用）**：
  - 拍照/录视频完成后弹出全屏预览确认浮层，视频使用 `VideoView` 自动无缝循环播放。
  - 确认按钮采用极简纯图标悬浮圆形按键（`↺` 重拍 / `✔` 满意使用），取消多余文字。
  - 满意确认后，照片自动保存至 `DCIM/DateCalculator`，视频自动导出保存至 `Movies/DateCalculator`，并自动抽取视频首帧水印图作为打卡卡片背景底图。
- **极速响应与全屏遮罩**：开启水印相机时采用全屏 Dialog 浮层，自动遮罩/隐藏主界面底栏 NavigationBar，底栏快门与模式按键自然下移至屏幕极底端；关闭相机时异步解绑 CameraX 资源，响应速度提升 60%。

### 2. ⏱️ 跨度与单位时间计算器 (Time Calculator)
- **多语言单位按键与展示**：消除了原本抽象的 `y, M, d` 字符，时间单位与计算结果随 App 及系统语言设置动态自动翻译：
  - **简体中文**：`年`、`月`、`天`、`时`、`分`、`秒`（例：`2年 8月 29天 5小时 30分 12秒`）
  - **繁體中文**：`年`、`月`、`天`、`時`、`分`、`秒`
  - **English**：`Year`、`Mon`、`Day`、`Hour`、`Min`、`Sec`
  - **日本語**：`年`、`ヶ月`、`日`、`時間`、`分`、`秒`
  - **한국어**：`년`、`개월`、`일`、`시간`、`분`、`초`
- **智能乘除法与时间标量算术**：
  - 支持时间跨度除以数值 scalar（例：`10天 ÷ 2 = 5天`）。
  - 支持时间跨度相除求比值（例：`10天 ÷ 5天 = 2`）。
  - 支持时间跨度乘法（例：`5天 × 3 = 15天`）。

### 3. 🎨 19 个国家与地区 68 款 SVG 矢量背景图匹配
- **解耦精细匹配引擎**：打破地区参数拦截限制，基于节日关键词全量匹配 68 款高精 VectorDrawable SVG 矢量素材（清明节 `bg_olive.svg`、高考 `bg_books.svg`、中秋节 `bg_autumn_moon.svg`、春节 `bg_lunar_festival.svg`、端午节 `bg_zongzi.svg`、元旦 `bg_fireworks.svg`、香港叮叮车 `bg_hk_tram.svg`、澳门瓷砖 `bg_macau_tiles.svg`、日本樱花 `bg_sakura.svg` / 竹林 `bg_bamboo.svg`、德国扭结饼 `bg_germany_pretzel.svg` 等）。
- **`ContentScale.Crop` 1:1 无变形平铺**：按键与倒数日卡片采用 `ContentScale.Crop`，保证 SVG 素材按原始宽高比裁剪，绝对不拉伸变形。

### 4. 📱 5 款 3D 新拟物桌面小组件 (AppWidgets)
- ⏳ **目标倒数日小组件 (`CountdownWidgetProvider`)**：展示倒数日名称、目标日期与实时剩余天数，支持按键轮播事件。
- 📜 **今日黄历小组件 (`AlmanacWidgetProvider`)**：桌面实时推算公历、农历、干支及老黄历宜/忌。
- ❤️ **重要纪念日小组件 (`AnniversaryWidgetProvider`)**：展示纪念日累计陪伴天数与下个周年倒计时。
- 📍 **纪念日打卡小组件 (`CheckInWidgetProvider`)**：桌面一键抓取当前 GPS 定位城市 + 精准时刻保存打卡记录。
- 📅 **日期快速计算小组件 (`QuickCalcWidgetProvider`)**：桌面提供 `+15天`, `+30天`, `+100天` 实时计算按键。

### 5. 🌐 多语言 i18n 体系与华人/非华人农历规则
- **支持【跟随系统 (System Default)】语言选项**：自动适配中/英/日/韩系统语言。
- **农历与老黄历语言统一规则**：仅在 **【简体中文】** 或 **【繁體中文】** 界面设置下展示农历月日与老黄历（宜/忌）。非中文界面下 100% 自动隐藏农历与黄历卡片。

### 6. 🚀 iOS 级阻尼弹簧动效与 GPU 零重绘性能优化
- **iOS 级阻尼弹簧模型 (`Spring Physics`)**：全量按键、卡片与弹窗套用 `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)` 物理曲线。
- **GPU 硬件直通渲染 (`Modifier.graphicsLayer`)**：将动画渲染全部封包至 GPU 绘图层，跳过 CPU 重构（Recomposition）与 Layout 测量，CPU 占用率降低 80%。
- **地图 Tile 内存 LruCache 缓存**：离线地图切片优先读取内存 LruCache，加载延迟降至 **0ms**。

---

## 📲 最新安装包下载 (v3.0.7)

- 📦 **[DateCalculator-v3.0.7-R8-Optimized.apk](./DateCalculator-v3.0.7-R8-Optimized.apk)** (8.6 MB R8 混淆极速测试包)
- 🚀 **[DateCalculator-v3.0.7-R8-Optimized.aab](./DateCalculator-v3.0.7-R8-Optimized.aab)** (20.9 MB R8 混淆正式发布包)

---

## 🛠️ 技术栈 (Tech Stack)

- **语言**: Kotlin 2.2.10
- **UI 框架**: Jetpack Compose (Material 3 + 3D Neumorphism Design)
- **相机与媒体**: CameraX (1.3.x) + MediaStore + MediaMetadataRetriever
- **桌面组件**: Android AppWidgetProvider + RemoteViews
- **架构**: Clean Architecture / MVVM (`StateFlow` + `ViewModel`)
- **存储**: SharedPreferences (JSON 序列化)
- **定位**: Android GPS Location API + 离线逆地理编码 + Esri World Street Map 切片

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。
