# 📅 DateCalculator (日期计算器) - Android v2.0.0

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-green.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/Material%203-Neumorphic-purple.svg)](https://m3.material.io)
[![Android SDK](https://img.shields.io/badge/API-26%2B%20(Android%208.0%2B)-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v2.0.0-brightgreen.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**日期计算器** 是一款基于 **Kotlin** 与 **Jetpack Compose** 开发的 Android 应用，采用 3D 新拟物视觉风格 (Neumorphism)。

应用提供工作日推算、多段排期连算、区间天数拆算、远期节假日预测、倒计时提醒以及农历公历互转等功能，方便日常生活、出行计划与工期排期。

---

## 🌟 主要功能

### 1. 📅 日期计算与多段模式
- **工作日 / 自然日推算**：支持按加减天数推算目标日期，自动扣除法定节假日与调休补班。
- **多段模式**：
  - 支持多时间段连续加减连算。
  - 支持为排期方案与各个时间段添加自定义备注。
  - **总时间安排示意图**：按时间先后顺序呈现工作日、周末双休与法定节假日的比例条。
  - 支持导出为标准 CSV 电子表格。
- **区间拆算**：选择起始与终止日期，自动拆算包含的自然日、工作日、周末双休与法定节假日天数。
- **股市模式**：针对金融交易需求，调休补班日可设为按正常周末休息/休市计算。
- **远期节假日预测 (2027+)**：针对未公布放假安排的远期年份，根据传统历法算法自动推算节假日。

### 2. ⏳ 目标日期倒计时
- **常用节日倒计时**：内置国庆、春节、清明、劳动节、端午、中秋等热门节日与考试节点。
- **自定义倒计时**：支持选择 Emoji 分类图标并设置周/月/年重复推算。
- **提醒设置**：支持跳转系统闹钟、日历或应用本地通知提醒。

### 3. 🌙 农历公历转换
- **双向互转**：支持 1900 ~ 2100 年公历与农历精准转换。
- **干支属相**：自动显示年份干支与生肖属相。
- **节日烟花**：传统节日支持点击触发烟花粒子效果。

### 4. ⚙️ 多地区支持与设置
- **19 个国家和地区**：支持中国大陆、台湾、香港、澳门、新加坡、马来西亚、越南、日本、韩国、英国、德国、法国、意大利、印度、印尼、澳大利亚、新西兰、美国、泰国等地区的法定节假日数据。
- **GPS 自动定位**：可根据当前位置自动识别所在地节假日。
- **启动静默同步**：启动应用时自动静默同步最新节假日安排。
- **历史记录编辑**：历史卡片提供编辑按钮，支持自由重命名。

---

## 📝 变更日志

详细更新说明请参阅：📜 **[CHANGELOG.md](./CHANGELOG.md)**

---

## 📲 安装包下载

- 📦 **[DateCalculator-v2.0.0.apk](./DateCalculator-v2.0.0.apk)** (22.0 MB 直接安装包)
- 🚀 **[DateCalculator-v2.0.0.aab](./DateCalculator-v2.0.0.aab)** (14.9 MB Google Play 上架包)

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。
