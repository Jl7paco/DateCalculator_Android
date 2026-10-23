# 🛡️ 万能日期计算器 (Date Calculator) - 防回归检查清单与已知问题指南 (Regression Guard & Known Issues Checklist)

> **版本**: `v3.0.7`  
> **适用对象**: AI Agent、Android 开发者、QA 测试工程师  
> **目的**: 本文档记录了项目核心架构规约、历史易回归坑点与防范检查清单。**在进行下一次版本更新、代码重构或新功能开发前，请务必阅读并严格遵守以下检查点！**

---

## ⚠️ 核心防回归规则清单 (Must-Follow Guard Rules)

### 1. 📷 水印相机与 CameraX 模块 (Watermark Camera Rules)
* **取景框不得裁剪**: `PreviewView.scaleType` 必须保持 `PreviewView.ScaleType.FIT_CENTER`，确保预览画面与传感器捕捉画面 1:1 完整不裁剪。
* **竖拍 EXIF 旋转矩阵校正**: 拍照回调 `ImageProxy` 时，必须读取 `imageInfo.rotationDegrees`，若角度非 0，必须执行 `Matrix().postRotate(rotationDegrees.toFloat())` 将 Bitmap 转正，否则竖拍照片保存后会横置 90 度！
* **拍摄后确认过渡 (Retake / Confirm)**: 按下快门后，必须呈现 `pendingPhotoBmp` 预览确认层，提供 `↺`（重拍 - 删除临时文件并返回相机）与 `✔`（满意 - 保存至系统相册 DCIM 并接轨打卡保存流程）两个图标按键。**不得跳过确认直接退出相机！**
* **摄像头焦段控制**: 仅在**后置镜头 (`LENS_FACING_BACK`)** 显示变焦倍率按钮（如 `0.5x, 1.0x, 2.0x, 3.0x, 5.0x`）；在**前置镜头 (`LENS_FACING_FRONT`)** 必须**完全隐藏变焦按钮**！
* **水印尺寸与 Pinch 缩放限制**: 预览 Badge 与成品照片水印宽度比例必须保持一致（默认占画面宽度的 54%）。双指 Pinch 缩放范围必须严格限制在 `0.5x .. 1.0x`，**绝对不可超过 1.0x**，防止水印过大溢出遮挡！
* **横持手机方向处理**: 相机界面必须强制锁定为 `SCREEN_ORIENTATION_PORTRAIT` 竖屏布局，横持手机时**仅图标与文字按 `90°/270°` 顺滑旋转**，界面控制 Deck 必须维持靠底竖向位置。

---

### 2. 🗓️ 倒数日与 Days Matter 语法规约 (Countdowns Rules)
* **起始日期移除与基准日**: 倒数日页面（`DateDiffScreen`）已彻底移除顶部的“选择起始日期”面板，所有倒数日推算**必须统一以当天 (`LocalDate.now()`) 为基准起点**。
* **卡片无缝推栈 (绝对不可覆盖/删除旧卡片)**: 在 `addResultCard` 方法中，**绝对不能包含 `resultCardList.drop(1)` 逻辑**！新增的卡片必须 prepended 在 `resultCardList` 顶部，并保留下方所有已有卡片。
* **Days Matter 描述短语与遮罩**:
  * **今天 (`natDays == 0`)**: `"$title 就是今天！"`，巨幕数字 `0 天`，天蓝遮罩 (`#E0F2FE`)。
  * **未来 (`targetDate > today`)**: `"距离下一个 $title 还有 N 天"`，天蓝遮罩 (`#E0F2FE`)。
  * **过去 (`targetDate < today`)**: `"$title 已经 N 天了"`，暖橙遮罩 (`#EA580C`)。**对于非重复的自定义或打卡事件，过了日期后绝对不可自动滚动推算至明天，必须保持原发生日期并切换为“已经 N 天了”！**
* **防止标题前缀重复**: 格式化标题时必须对标题调用 `.replace("下一个", "").replace("下一次", "").trim()`，避免出现 `"距离下一个 下一个星期六还有"` 的重复文案！

---

### 3. 🌌 24 节气与农历万年历规约 (Solar Terms & Lunar Calendar)
* **24 节气双位年份公式**: `getSolarTerm` 中计算 24 节气公历日时，必须使用**双位年份 $y = \text{year} \bmod 100$**（如 2026 年取 $y = 26$），绝对不能直接代入 4 位年份 `2026`！否则会导致节气日期算成 483 天而全局失效，使得 10 月份出现双“寒露”或主页提示“立春”的严重 Bug。
* **香港天文台 (HKO `data.gov.hk`) 官方表优先**: 24 节气查询必须优先匹配 `HKO_SOLAR_TERMS_TABLE`（涵盖 2024-2030 年香港天文台官方核准数据），而后降级使用双位年份算术公式。
* **农历 1900-2100 年 bitmask 算法**: `solarToLunar` 必须使用 `LUNAR_INFO` 200 年位图数组与 `lYearDays`/`leapMonth` 推算，绝对不能使用简单的 `epochDay % 29` 估算！

---

### 4. ⏱️ 时间计算器规约 (Time Calculator Rules)
* **单位组**: 必须完整提供 `y`（年）、`M`（月）、`d`（日）、`h`（时）、`m`（分）、`s`（秒）6 个单位输入按键。
* **自动规范换算**: `999d` 按等于号必须自动规范换算为 **`2y 8M 29d`**！
* **日期加减**: 支持如 `2026y 12M 9d + 20d` 按等于号推算出 **`2026y 12M 29d`**。
* **括号同行**: 左括号 `(` 与右括号 `)` 必须放置在**同一行（Row 1: Clear, ⌫, (, )）**，最右列统一为 `÷`, `×`, `-`, `+`, `=`。

---

### 5. 🛡️ 系统兼容性与性能规约 (System & Performance)
* **Android 15/16 16 KB 页长对齐**: `app/build.gradle.kts` 中必须包含 `jniLibs { useLegacyPackaging = false; excludes += "**/libimage_processing_util_jni.so" }`，彻底避免系统弹出 16 KB ELF 页长对齐检查失败的对话框。
* **零主题闪烁**: `MainActivity` 初始化 Compose `setContent` 前，必须通过 `PreferenceUtils.getDarkThemeMode(this)` 同步读取 SharedPreferences 初始主题，杜绝 1-2 秒的主题切换闪烁。
* **多语言 100% 动态本地化**: 绝不允许在 Compose 页面中硬编码中文字符串。所有文案必须通过 `LanguageUtils.getString(key, lang)` 或多语言分支判断（`AppLanguage.SIMPLIFIED_CHINESE`, `TRADITIONAL_CHINESE`, `ENGLISH`, `JAPANESE`, `KOREAN`）。
* **真机营销型号映射**: `getDeviceLaunchInfo()` 中需维护主流真机型号代码对照表（如 `SM-S9280` $\to$ `Samsung Galaxy S24 Ultra`），给用户呈现公众熟知的官方市场名称。

---

## 📋 下次更新前回归测试检查清单 (Pre-Release Checklist)

在准备发布新版本前，请逐一勾选以下测试用例：

- [ ] **用例 1 (节假日更新)**: 进入设置 $\to$ 点击“获取最新假期”，确认 Toast 提示文案为 **`节假日已经更新`**。
- [ ] **用例 2 (水印相机全流程)**:
  - 打开水印相机，确认后置显示变焦倍数，切前置自动隐藏变焦按键。
  - 双指 Pinch 缩放水印，确认最大只能放大到初始 1.0x 尺寸。
  - 按下快门，确认出现 **`↺ 重拍`** 与 **`✔ 满意/使用照片`** 确认层。
  - 点击 **`✔ 满意`**，确认照片正向不旋转、右下角带有防伪水印，且**系统相册 (`DCIM/DateCalculator`) 中能找到该带水印的照片**。
- [ ] **用例 3 (倒数日与打卡)**:
  - 点击“常用倒数日”中的“星期六”，确认标题为 **`距离下一个 星期六 还有 N 天`**（无重复“下一个”）。
  - 点击“打卡”，记录打卡后，确认打卡卡片背景调取的是**真实的 OpenStreetMap/Esri 地图瓦片 PNG 图片**（无 "API KEY REQUIRED" 水印字样）。
  - 连续点击不同倒数日，确认**新增卡片无缝压在最上方，旧卡片完全保留不被替换**。
- [ ] **用例 4 (主页 24 节气与农历)**:
  - 翻阅主页月历至 2026年10月，确认 **10月8日为唯一的“寒露”，10月23日为唯一的“霜降”**（无双寒露 Bug）。
  - 在 10月24日 查看主页节气卡片，确认显示 **`距离下一个节气 [立冬] 还有 14 天`**（绝无“立春”错误提示）。
- [ ] **用例 5 (时间计算器)**:
  - 进入底栏最右侧“时间计算器”，输入 `999d` 按等于号，确认结果为 **`2y 8M 29d`**。
  - 输入 `2026y 12M 9d + 20d` 按等于号，确认结果为 **`2026y 12M 29d`**。
  - 确认 `(` 和 `)` 按键位于同一行。
- [ ] **用例 6 (系统兼容性)**:
  - 在 Android 15/16 设备上启动，确认**无 16 KB 页长警告弹窗**。
  - 强制设置深色模式并冷启动 App，确认**首帧即呈现深色主题，无 1-2 秒闪烁切换**。
