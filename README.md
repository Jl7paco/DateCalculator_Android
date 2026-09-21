# 📅 DateCalculator (日期计算器) - Android v2.1.0

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v2.1.0-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**日期计算器** 是一款基于 **Kotlin** 与 **Jetpack Compose** 开发的 Android 日常计算应用，采用新拟物 (Neumorphism) 界面风格。

应用提供工作日推算、多段天数计算、老黄历宜忌、区间天数拆算、节假日预测、倒计时提醒以及农历公历转换等功能。

---

## 📋 主要功能

### 1. 📅 日期计算与多段模式
- **工作日 / 自然日计算**：按加减天数推算目标日期，自动扣除法定节假日与调休补班。
- **多段模式**：
  - 支持多时间段连续加减连算，点击“保存到记录”保存方案。
  - 各时间段可添加备注说明（如 *第一段时间*、*装修进度*）。
  - **总时间安排示意图**：按时间先后顺序展示工作日、周末双休与法定节假日的比例条。
  - 支持导出为标准 CSV 电子表格文件。
  - **历史记录详情**：历史记录支持点击查看完整排期图表并导出 CSV。
- **区间拆算**：选择起始与终止日期，拆算包含的自然日、工作日、周末双休与法定节假日天数。
- **股市模式**：调休补班日可设为按正常周末休市计算。
- **远期节假日预测 (2027+)**：针对未公布放假安排的远期年份，按算法推算节假日。

### 2. 📜 老黄历“宜”与“忌”
- **每日更新**：根据日期计算当日老黄历的“宜”和“忌”事项，数据随日期切换更新。
- **界面配色**：“宜”采用绿底白字，“忌”采用冷灰色调，对比直观。
- **同步历史**：转换结果与宜忌内容同步保存到历史记录。

### 3. ⏳ 目标日期倒计时
- **常用节日倒计时**：内置国庆、春节、清明、劳动节、端午、中秋等节日。
- **自定义倒计时**：支持选择分类图标，并支持按周/月/年重复推算。
- **提醒设置**：支持跳转系统闹钟、日历或应用本地通知提醒。

### 4. ⚙️ 多地区与设置
- **多地区支持**：支持中国大陆、台湾、香港、澳门、新加坡、马来西亚、日本、韩国、英国、德国、美国等地区的节假日数据。
- **GPS 自动识别**：可根据位置识别所在地节假日。
- **历史记录编辑**：历史卡片支持重命名与清理。

---

## 📝 变更日志

详细更新说明请参阅：📜 **[CHANGELOG.md](./CHANGELOG.md)**

---

## 📲 安装包下载

- 📦 **[DateCalculator-v2.1.0.apk](./DateCalculator-v2.1.0.apk)** (22.8 MB 安装包)
- 🚀 **[DateCalculator-v2.1.0.aab](./DateCalculator-v2.1.0.aab)** (14.9 MB 上架包)

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。
