# DateCalculator (日期计算器) - Android v3.0.8

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v3.0.8-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

[ 🇨🇳 简体中文 ](#-简体中文) | [ 🇺🇸 English ](#-english) | [ 🇯🇵 日本語 ](#-日本語)

---

## 🇨🇳 简体中文

### 关于项目
**DateCalculator (日期计算器)** 是一款基于 **Kotlin** 和 **Jetpack Compose** 开发的 Android 日期与时间计算工具。界面采用了新拟物 (Neumorphism) 风格设计，主要用于日常日期加减、工作日推算、农历公历转换、倒数日管理、打卡水印相机以及桌面小组件。

---

### 主要功能

#### 1. 日期与工作日计算
- **日期加减与间隔计算**：按天数推算未来或过去的日期，或计算两个日期之间的自然日与工作日天数。
- **休息日规则**：支持双休（周六日）、单休（仅周日/仅周六）、大小周（单双周交替）及无休模式。
- **中国节假日调休**：内置中国法定节假日放假与调休补班数据。
- **多段计算与导出**：支持多阶段连贯日期加减，可展示时间排期并导出为 `.xls` 电子表格。

#### 2. 时间计算器
- **时间单位换算**：支持年、月、天、小时、分钟、秒等单位的混合加减与换算。
- **单位随语言切换**：单位名称（如 `年/月/天`、`Year/Mon/Day`、`年/ヶ月/日`）随应用和系统语言自动更变。
- **标量乘除**：支持时间跨度除以数值（例：`10天 ÷ 2 = 5天`）或求时间跨度比值（例：`10天 ÷ 5天 = 2`）。

#### 3. 打卡水印相机
- **4:3 取景**：采用 `FIT_CENTER` 缩放，保持摄像头原始拍摄视野。
- **焦段切换**：根据手机硬件自动识别并显示焦段倍率（如 `0.5x` / `0.6x` 超广角、`1x`、`2x`、`3x`、`5x`、`10x`）。
- **水印卡片**：在取景框右上角展示含定位城市、地标与时间的水印卡片，横屏旋转时自动进行位置补偿，防止超出取景框。
- **预览与保存**：拍完照片或视频后可进行全屏预览（视频自动循环播放），满意后再选择保存至系统相册（照片保存至 `DCIM/DateCalculator`，视频保存至 `Movies/DateCalculator`）。

#### 4. 节假日与倒数日
- **全球 19 个地区节假日**：支持中国大陆、港澳台、日本、韩国、新加坡、马来西亚、欧美等 19 个国家和地区的节假日数据。
- **矢量背景匹配**：根据节日名称自动匹配对应的 SVG 矢量背景图。

#### 5. 农历转换与黄历
- **公农历互转**：支持 1900–2100 年公历与农历互相转换。
- **黄历与节气**：仅在中文模式下展示老黄历宜忌和二十四节气，非中文模式下自动隐藏。

#### 6. 桌面小组件
- 提供倒数日、今日黄历、纪念日、GPS打卡和日期快算 5 款桌面小组件。

---

### 构建与测试
项目包含 17 项 JUnit 单元测试与集成测试，涵盖日期计算、公农历转换、19 地区节假日和多语言文本校验。可通过以下命令运行测试：
```bash
./gradlew testDebugUnitTest
```

---

## 🇺🇸 English

### Overview
**DateCalculator** is an Android application for date and time calculations, built using **Kotlin** and **Jetpack Compose**. It features a Neumorphic UI design and provides practical tools for date offsets, workday tracking, lunar calendar conversions, time duration math, a watermark camera, and desktop widgets.

---

### Key Features

#### 1. Date & Workday Calculations
- **Date Offset & Span**: Calculate target dates by adding or subtracting days, or compute natural days and workdays between two dates.
- **Custom Rest Schedules**: Supports 5-day workweeks (Sat/Sun off), 6-day workweeks (Sunday off / Saturday off), alternating weeks, and no rest days.
- **Statutory Holidays**: Includes China statutory holiday and adjusted workday data.
- **Multi-Step Scheduling**: Chain multiple date calculations and export schedule timelines to `.xls` spreadsheets.

#### 2. Time Duration Calculator
- **Unit Conversions**: Supports math across years, months, days, hours, minutes, and seconds.
- **Localized Units**: Unit labels adapt automatically to system and app language settings (e.g., `Year/Mon/Day`, `年/月/天`, `年/ヶ月/日`).
- **Scalar Math**: Supports dividing time durations by numbers (e.g., `10 Days ÷ 2 = 5 Days`) or calculating ratios (`10 Days ÷ 5 Days = 2`).

#### 3. Watermark Camera
- **4:3 Aspect Ratio**: Uses `FIT_CENTER` scaling to preserve the camera sensor's full field of view.
- **Hardware Zoom Ratios**: Automatically detects available camera lenses (e.g., `0.5x`/`0.6x` ultra-wide, `1x`, `2x`, `3x`, `5x`, `10x`).
- **Watermark Badge**: Displays location, landmark, and timestamp in a card positioned at the top-right of the viewfinder, with geometric compensation when rotated into landscape.
- **Preview & Export**: Preview captured photos or looping videos before saving to system gallery (`DCIM/DateCalculator` for photos, `Movies/DateCalculator` for videos).

#### 4. Global Holidays & Countdowns
- **19 Country/Region Presets**: Preloaded holiday datasets for Mainland China, Hong Kong, Macao, Taiwan, Japan, South Korea, Singapore, Malaysia, US, UK, Germany, France, Italy, Australia, New Zealand, Vietnam, Thailand, Indonesia, and India.
- **Vector Backgrounds**: Matches holidays with corresponding SVG vector artwork.

#### 5. Lunar Calendar & Almanac
- **Solar/Lunar Conversion**: Two-way conversion between solar and lunar dates for 1900–2100.
- **Traditional Almanac**: Displays traditional Chinese almanac data (Yi/Ji) and solar terms when in Chinese language mode.

#### 6. AppWidgets
- Includes 5 desktop widgets: Countdown, Almanac, Anniversary, GPS Check-in, and Quick Date Calculation.

---

### Build & Testing
The project includes 17 unit and integration tests covering calculation engines, lunar conversions, holiday datasets, and string localization. Run tests using:
```bash
./gradlew testDebugUnitTest
```

---

## 🇯🇵 日本語

### 概要
**DateCalculator（万能日付電卓）**は、**Kotlin**と**Jetpack Compose**で開発されたAndroid向けの日付・時間計算アプリです。ニューモフィズム（Neumorphic）デザインを採用し、日付加減算、稼働日計算、旧暦変換、時間単位計算、ウォーターマークカメラ、ウィジェットなどの機能を提供します。

---

### 主な機能

#### 1. 日付・稼働日計算
- **加減算・期間計算**: 指定日数の加減算、または2つの日付間の暦日数・稼働日数を計算。
- **休日ルール設定**: 週休2日（土日休み）、週休1日（日曜休み/土曜休み）、隔週週休2日、年中無休に対応。
- **祝日・振替休日**: 祝日および振替出勤日ルールに対応。
- **複数ステップ計算**: 段階的なスケジュール計算と`.xls`形式での出力。

#### 2. 時間・単位電卓
- **単位換算**: 年、月、日、時間、分、秒の混合計算。
- **言語別単位表示**: アプリと言語設定に応じて単位ラベルを自動切り替え（例: `年/ヶ月/日`、`Year/Mon/Day`、`年/月/天`）。
- **時間除算・比率計算**: 時間の除算（例: `10日 ÷ 2 = 5日`）および比率計算（例: `10日 ÷ 5日 = 2`）に対応。

#### 3. ウォーターマークカメラ
- **4:3画角**: `FIT_CENTER`表示でカメラ本来の画角を保持。
- **倍率切り替え**: デバイスがサポートする画角（`0.5x`/`0.6x`超広角、`1x`、`2x`、`3x`、`5x`、`10x`）を自動検出。
- **透かしカード**: 現在地・日時入りのカードをプレビュー右上に配置。画面回転時も枠内に収まるよう位置補正。
- **プレビューと保存**: 撮影後に写真やループ動画をプレビュー確認し、端末のアルバム（`DCIM/DateCalculator`または`Movies/DateCalculator`）に保存。

#### 4. 祝日・カウントダウン
- **19の国・地域**: 日本、中国、台湾、香港、マカオ、韓国、シンガポール、マレーシア、アメリカ、イギリス、ドイツ、フランス、イタリア、豪州、NZ、ベトナム、タイ、インドネシア、インドの祝日データを収録。
- **ベクター背景**: 祝日名に応じたSVG背景を表示。

#### 5. 旧暦変換・六曜
- 1900年〜2100年の旧暦・新暦相互変換。
- 日本語・中国語モード時に旧暦および関連情報を表示。

#### 6. ホーム画面ウィジェット
- カウントダウン、旧暦、記念日、GPSチェックイン、日付計算の5種類のウィジェットを搭載。

---

### ビルドとテスト
日付計算、旧暦変換、祝日データ、多言語表示を検証する17項目の単体・結合テストが含まれています。
```bash
./gradlew testDebugUnitTest
```

---

## 🛠️ 技术栈 (Tech Stack)

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material 3 + 3D Neumorphism Design)
- **Camera & Media**: CameraX (1.3.x) + MediaStore + MediaMetadataRetriever
- **Widgets**: Android AppWidgetProvider + RemoteViews
- **Architecture**: MVVM (`StateFlow` + `ViewModel`)
- **Storage**: SharedPreferences (JSON)
- **Location**: Android GPS Location API + Offline Geocoding + Esri World Street Map Tiles

---

## 📄 开源协议 (License)

本项目基于 [MIT License](LICENSE) 协议开源。
