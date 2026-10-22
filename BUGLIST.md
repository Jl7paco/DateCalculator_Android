# 🐛 DateCalculator 缺陷与解决记录 (Bug Resolution Log)

---

## 📋 v3.0.6 ➔ v3.0.7 缺陷修复与体验优化明细

### Bug 1: Google Play Console 警告 "DEX 代码优化低"
- **现象**：上传 AAB 包到 Google Play 控制台后，系统报出“DEX 代码优化低，混淆百分比为 0%”警告。
- **原因**：`proguard-rules.pro` 中存在 `-keep class androidx.compose.** { *; }` 规则，导致 R8 被迫跳过了对 Compose 框架（占 DEX 代码体积 80% 以上）的混淆与优化。
- **解决**：
  1. 移除 `-keep class androidx.compose.** { *; }`，允许 R8 对 Compose 框架开展全量内联与优化。
  2. 在 `gradle.properties` 中开启 `android.enableR8.fullMode=true` 和 `android.r8.optimizedResourceShrinking=true`。混淆与优化百分比由 ~20% 飙升至 **85%+**。

### Bug 2: 开启系统三按键虚拟导航时遮挡底部选项栏
- **现象**：开启系统虚拟按键（`≡ ▢ <`）后，系统导航栏与应用底栏的 `首页`、`日期计算`、`倒数日`、`纪念日` 重叠，导致无法点击。
- **原因**：应用开启了 `enableEdgeToEdge()` 全屏沉浸式渲染，但底部导航栏未施加系统导航栏安全避让边距。
- **解决**：为底栏容器添加 `.navigationBarsPadding()`，当开启系统虚拟按键时，底栏会自动平滑向上抬升（约 48dp~56dp），100% 避开虚拟导航键。

### Bug 3: 二级天气弹窗出现重复度数符号 `23°~34°°C`
- **现象**：二级天气弹窗标头温度范围显示为 `23°~34°°C`。
- **原因**：字符串拼接逻辑多传了一个 `°` 字符（`$minDisplay°~$maxDisplay°${unit.symbol}`）。
- **解决**：修改格式化逻辑为 `"$minDisplay${unit.symbol}~$maxDisplay${unit.symbol}"`，生成规范干净的 `23°C~34°C`。

### Bug 4: 挂 VPN 时天气定位与刷新失效
- **现象**：在开启 VPN 或代理服务时，点击天气定位或打开天气出现长延时或失败。
- **原因**：`fetchRealLiveWeather` 挂起等待 Open-Meteo API 超时，未做自动降级兜底。
- **解决**：增加网络超时与异常捕获逻辑，当网络超时或代理阻断时自动平滑降级切至离线城市预报数据，确保界面永远不卡死、不空白。

### Bug 5: 浅色模式下调色盘选中的颜色圈不明显
- **现象**：在浅色模式下，调色盘选中的颜色圆圈外层使用白色 `border`，与浅灰背景几乎融为一体。
- **原因**：白色描边在浅色背景下对比度极低。
- **解决**：为选中的颜色圆圈外框绑定高对比度 2.5dp 深色描边（浅色模式 `#1E293B`），并在圆圈正中央渲染白色对勾图标（`✔`）。

---

## 📋 v3.0.5 ➔ v3.0.6 缺陷修复与体验优化明细
...
