# 🐛 DateCalculator 缺陷与解决记录 (Bug Resolution Log)

---

## 📋 v3.0.1 ➔ v3.0.2 缺陷修复汇总

### Bug 1: 首页今天日期的节气图标丢失
- **现象**：在首页“当日农历、节气与老黄历宜忌 Card”中，特定日期（如秋分等）的节气 SuggestionChip 偶发不展示。
- **原因**：`LunarCalendarUtils.solarToLunar` 中的节气计算逻辑在某些月份边界未精准覆盖当天。
- **解决**：修补了 `LunarCalendarUtils.getSolarTerm` 的日期区间推算，确保 24 节气在当月当天 100% 准确显示。

### Bug 2: 非简体中文语言（含繁体中文、英文、日文、韩文）下首页仍展示老黄历（宜/忌）
- **现象**：当设置语言为繁体中文、英文、日文或韩文时，首页老黄历宜忌面板依然渲染展示。
- **原因**：此前代码误将繁体中文归类为展示黄历的区域（`isChineseLocale` 包含了 `TRADITIONAL_CHINESE`）。
- **解决**：重构黄历展示规则：仅在 **简体中文 (`SIMPLIFIED_CHINESE`)** 模式下展示老黄历（宜/忌），所有非简体中文模式下 100% 隐藏老黄历面板。

### Bug 3: 非简体中文模式下月历网格单元格展示了农历
- **现象**：在英文、日文、韩文或繁体中文下，月历网格单元格下方仍残留展示“廿一”、“初一”等农历文字。
- **原因**：`HomeScreen.kt` 月历单元格未对非简体中文语言做农历文字置空处理。
- **解决**：在非简体中文模式下将 `lunarText` 强制置空，月历网格仅展示清晰的公历数字与放假调休角标。

### Bug 4: 主页地名、历史记录顶栏及历史内容未跟随语言翻译
- **现象**：切换语言后，主页天气地名（如 *深圳市*）、历史记录弹窗顶栏标题以及历史卡片的类别、详情文案依然显示为简体中文。
- **原因**：`HomeScreen.kt`、`HistoryBottomSheet.kt` 及 `HistoryScreen.kt` 存在部分硬编码字符串，未透传 `appLanguage` 状态。
- **解决**：
  1. 增加 `LanguageUtils.getLocalizedCityName()` 地名翻译函数（如 *深圳市 ➔ Shenzhen / 深セン / 선전 / 深圳市*）。
  2. 绑定 `HistoryOverlayDialog` 顶栏标题为 `LanguageUtils.getString("history_title", lang)`。
  3. 增加 `LanguageUtils.getLocalizedHistoryCategory()` 与 `LanguageUtils.getLocalizedHistoryDetail()`，实现历史记录详情全量翻译。

### Bug 5: 设置界面与倒数日页面部分控件未跟随语言翻译
- **现象**：非中文前提下，设置界面的“自定义色彩”、“调色盘”、倒数日“起始日期/目标日期”以及公农历切换开关（`SolarLunarSwitch`）仍显示中文。
- **原因**：`SolarLunarSwitch` 调用时未透传 `language` 参数（默认使用了 `SIMPLIFIED_CHINESE`），且设置界面部分 Label 遗漏了 `getString` 映射。
- **解决**：
  1. 为 `SolarLunarSwitch` 传入 ViewModel 中的 `language = uiState.appLanguage`。
  2. 绑定设置界面“自定义色彩” (`Custom Color`) 与“调色盘” (`Theme Color Palette`) 翻译。
  3. 增加 `DateCalculatorUtils.formatDateLocalized()` 格式化函数，非中文下采用本地化日期格式（如 *Sep 25, 2026* / *2026年9月25日* / *2026년 9월 25일*）。

### Bug 6: 年龄计算页面仍残存运势卡片
- **现象**：年龄计算页面底部依然展示星座每日运势卡片。
- **原因**：之前重构时遗留了 `AgeCalculatorScreen.kt` 中的运势 Card 渲染块。
- **解决**：彻底移除 `AgeCalculatorScreen.kt` 中的运势 Card 模块，使年龄计算界面更加紧凑与专注。

### Bug 7: 桌面小组件无交互功能与实时计算
- **现象**：桌面小组件仅能点击唤醒 App，无法在桌面直接查看实时计算结果或进行快捷操作。
- **解决**：
  1. **`CountdownWidgetProvider`**：支持点击“切换”按键在桌面实时轮播置顶倒数日事件与剩余天数。
  2. **`AlmanacWidgetProvider`**：桌面实时推算并展示当日公历、农历、干支纪年及建除十二神老黄历宜/忌。
  3. **`AnniversaryWidgetProvider`**：桌面实时计算已陪伴天数与下个周年剩余天数，支持按键轮播卡片。
  4. **`CheckInWidgetProvider`**：新增 **`📍 1键打卡`** 桌面按键，点击直接抓取 GPS 城市 + 毫秒级时刻并保存打卡记录。
  5. **`QuickCalcWidgetProvider`**：新增 **`+15天`**, **`+30天`**, **`+100天`** 桌面实时计算按键，点击直接在桌面显示目标日期。
