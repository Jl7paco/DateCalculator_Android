# 🐛 DateCalculator 缺陷与解决记录 (Bug Resolution Log)

---

## 📋 v3.0.1 ➔ v3.0.2 缺陷修复全量对比

### Bug 1: 语言未新增【跟随系统】选项
- **现象**：系统语言为英文、日文或韩文时，应用无法自动跟随系统语言。
- **原因**：`AppLanguage` 枚举遗漏了 `SYSTEM` 选项。
- **解决**：在 `AppLanguage` 中新增 `SYSTEM("system", "跟随系统")`，并通过 `getEffectiveLanguage()` 智能动态读取 Android 系统 `Locale.getDefault()`。

### Bug 2: 农历与老黄历（宜/忌）未根据语言统一规则
- **现象**：之前农历/黄历过度依赖 `HolidayRegion` 地区判断，导致选成英文语言时部分卡片依然渲染老黄历。
- **原因**：黄历与农历展示逻辑未完全与 `appLanguage` 绑定。
- **解决**：重构农历与黄历规则：**仅在简体中文 (`SIMPLIFIED_CHINESE`) 或 繁体中文 (`TRADITIONAL_CHINESE`) 界面下展示农历与老黄历（宜/忌）**。英文、日文、韩文下 100% 强制隐藏农历月日与老黄历卡片。

### Bug 3: 多段模式阶段名称未随语言转换
- **现象**：在多段加减计算模式中，新增阶段或默认生成的阶段显示为简体中文“第一段时间”、“第二段时间”。
- **原因**：`DateCalculatorViewModel.kt` 的 `addCalculationStage` 硬编码了中文。
- **解决**：重构 ViewModel 与 View，调用 `LanguageUtils.getLocalizedStageTitle(index + 1, lang)`，英文下显示 `Stage 1 / Stage 2`，日文下显示 `第1段階 / 第2段階`，韩文下显示 `1단계 / 2단계`，繁体中文下显示 `第1段時間 / 第2段時間`。

### Bug 4: 单段模式、结果卡片、周数与星座遗漏翻译
- **现象**：单段加减模式的 `+` / `-` 按钮、结果卡片细节描述、周数（如 `第40周`）以及生肖星座在英文模式下显示中文。
- **原因**：部分格式化函数（如 `formatDateWithWeek`）未透传当前 `appLanguage` 参数。
- **解决**：
  1. 为 `DateCalculatorUtils.formatDateWithWeek(date, lang)` 透传 `lang`，英文格式化为 `Sep 27, 2026 (Wk 40)`，韩文格式化为 `2026년 9월 27일 (40주차)`。
  2. 为生肖和 12 星座建立 `LanguageUtils.getLocalizedConstellation()` 与 `getLocalizedZodiac()`，英文格式化为 `Virgo` / `Libra` / `Dragon`。
  3. 补齐天气第 4 天 `LanguageUtils.getDayName("大后天", lang)` 翻译（`3 Days Later` / `明々後日` / `글피`）。

### Bug 5: 桌面小组件无 previewImage 缩略图与 3D 新拟物样式不吻合
- **现象**：在手机“添加桌面小组件”列表面板中无法预览小组件缩略图，且小组件外观与主应用 3D 新拟物风格不一致。
- **原因**：小组件配置文件遗漏了 `android:previewLayout` 与 `android:previewImage`。
- **解决**：
  1. 为 5 款小组件 Provider 配置文件添加 `android:previewLayout` 与 `android:previewImage`，可以在桌面选单面板中直接预览 3D 新拟物外观。
  2. 升级小组件 XML 绘图资源（`widget_bg_neumorphic.xml`），采用 **22dp 柔和圆角、`#F2F5FA` 拟物底色与 `#CBD5E1` 3D 浮雕边框**。
  3. 新增桌面 **`📍 1键打卡`** 广播与桌面 **`+15天 / +30天 / +100天`** 实时日期计算功能。

### Bug 6: 英文快捷按键“Yesterday”超长与切页开关“Calendar Day”不居中
- **现象**：倒数日页面英文快捷按键“Yesterday”文字太长溢出；`Workday / Calendar Day` 胶囊开关在英文下文字不居中。
- **原因**：胶囊开关固定宽度仅 130dp，在英文较长词组下空间不足。
- **解决**：
  1. 将英文快捷按键“Yesterday”精简缩写为 **`Yest`**。
  2. 拓宽 `WorkdayNaturalSwitch` 非中文模式宽度至 **150dp**，并加上 `TextAlign.Center` 与 `TextOverflow.Ellipsis` 保护。

### Bug 7: 模式三向切换按钮底部带有椭圆大阴影
- **现象**：`加减天数` | `区间拆算` | `多段加减` 三个按钮外层容器底部带有大块凹陷椭圆阴影。
- **原因**：外层 `Box` 施加了 `.neumorphicInset` 组合修饰符。
- **解决**：移除外层容器凹陷阴影，更名为 **“多段加减”**（`Multi-Stage Calc`），界面干练流畅。
