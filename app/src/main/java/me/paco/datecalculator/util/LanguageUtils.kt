package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * 全量 5 国语言 (简体中文、繁体中文、English、日本語、한국어) 统一高保真本地化与国际化工具
 */
object LanguageUtils {

    private val zhCnStrings = mapOf(
        "app_name" to "万能日期计算器",
        "app_title" to "万能日期计算器",
        "tab_home" to "首页",
        "tab_calc" to "日期计算",
        "tab_diff" to "倒数日",
        "tab_countdown" to "倒数日",
        "tab_anniversary" to "纪念日",
        "tab_lunar" to "农历转换",
        "tab_age" to "年龄计算",
        "today" to "今天",
        "yesterday" to "昨天",
        "plus_1w" to "+1周",
        "minus_1w" to "-1周",
        "base_date" to "起始日期",
        "target_date" to "目标日期",
        "workday" to "工作日",
        "natural_day" to "自然日",
        "diff_natural" to "自然日",
        "diff_workday" to "工作日",
        "calc_rule_title" to "计算规则",
        "mode_forward" to "加减天数",
        "mode_reverse" to "区间拆算",
        "multi_stage_btn" to "多段加减",
        "add_stage_btn" to "添加阶段",
        "timeline_title" to "总时间安排示意",
        "export_csv" to "导出 CSV",
        "stage_add_label" to "多段加",
        "stage_sub_label" to "多段减",
        "reverse_end_date_title" to "目标终止日期",
        "result_title_workday" to "工作日计算结果",
        "result_title_natural" to "自然日计算结果",
        "workday_chip" to "工作日",
        "weekend_chip" to "周末双休",
        "days_unit" to "天",
        "days_passed" to "已过去",
        "days_until_prefix" to "距离",
        "days_until_suffix" to "还有",
        "solar" to "公历",
        "lunar" to "农历",
        "select_start_date" to "选择起始日期",
        "select_target_date" to "选择目标日期",
        "select_birth_date" to "选择出生日期",
        "birth_date_label" to "出生日期",
        "birth_date_title" to "出生日期",
        "exact_age" to "精准年龄",
        "zodiac_sign" to "生肖",
        "total_days" to "生存总天数",
        "months_unit" to "个月",
        "total_weeks" to "生存总周数",
        "weeks_unit" to "周",
        "next_birthday_days" to "距下次生日",
        "common_countdown" to "常用倒数日",
        "add_countdown" to "添加倒数日",
        "custom_btn" to "自定义",
        "fixed_countdown" to "固定显示倒数日",
        "confirm" to "确定",
        "cancel" to "取消",
        "check_in" to "打卡",
        "anniversary_title" to "重要纪念日",
        "add_anniversary" to "添加纪念日",
        "anniversary_name" to "纪念日名称",
        "anniversary_date" to "纪念日日期",
        "anniversary_dialog_title" to "添加重要纪念日",
        "save_anniversary" to "保存纪念日",
        "check_in_dialog_title" to "精准打卡",
        "no_anniversary_record" to "❤️ 暂无记录的纪念日",
        "add_anniversary_hint" to "点击上方“添加纪念日”或“打卡”保存美好时刻",
        "next_anniversary_remains" to "距离下个周年",
        "lunar_converter_title" to "农历与公历转换",
        "solar_to_lunar" to "公历 ➔ 农历",
        "lunar_to_solar" to "农历 ➔ 公历",
        "solar_date_title" to "公历日期",
        "lunar_date_title" to "农历日期",
        "age_calc_title" to "年龄与生存天数精准计算",
        "lived_days" to "已在这个世界上生存",
        "next_birthday" to "距离下次生日还有",
        "chinese_zodiac" to "生肖",
        "constellation" to "星座",
        "yi_label" to "宜",
        "ji_label" to "忌",
        "fortune_suffix" to "每日运势",
        "lucky_number" to "幸运数字",
        "lucky_color" to "幸运颜色",
        "settings_title" to "应用设置",
        "settings_language" to "软件应用语言",
        "settings_home_config" to "首页显示模块配置",
        "settings_region" to "法定节假日安排标准地区",
        "settings_gps_auto" to "GPS 自动精确定位所在国家/地区",
        "settings_sync_holidays" to "获取最新假期",
        "settings_theme" to "主题配色方案",
        "settings_dark_mode" to "深色模式跟随设置",
        "dark_mode_system" to "跟随系统",
        "dark_mode_on" to "开启",
        "dark_mode_off" to "关闭",
        "dark_mode_light" to "浅色模式",
        "dark_mode_dark" to "深色模式",
        "history_title" to "计算历史记录",
        "clear_history" to "清空历史",
        "history_empty" to "暂无历史记录",
        "history_cleared" to "历史记录已清空",
        "history_item_deleted" to "历史记录已删除",
        "confirm_delete_anniversary" to "确认删除该记录？",
        "confirm_delete_btn" to "确认删除",
        "palette_title" to "调色盘",
        "rule_weekend_title" to "周末休息规则模式",
        "rule_five_days" to "双休 (周六、周日休息)",
        "rule_five_days_desc" to "标准 5 天工作制，每周六日算作双休",
        "rule_big_small_weeks" to "大小周 (单双周交替)",
        "rule_big_small_weeks_desc" to "单周休息周日 1 天，双周休息周六周日 2 天",
        "rule_six_days_sunday" to "单休 (仅周日休息)",
        "rule_six_days_sunday_desc" to "每周一至周六上班，仅周日算作休息日",
        "rule_six_days_saturday" to "单休 (仅周六休息)",
        "rule_six_days_saturday_desc" to "每周一至周五与周日上班，仅周六算作休息日",
        "rule_seven_days" to "无休 (7 天全工作)",
        "rule_seven_days_desc" to "全年无休，所有日期均按工作日计算",
        "home_show_screen" to "显示首页",
        "home_calendar" to "日历月视图",
        "home_almanac" to "今日黄历 (宜忌)",
        "home_solar_terms" to "二十四节气与交节倒计时",
        "home_lunar" to "农历与干支神煞",
        "home_zodiac" to "星座与生肖运势评级",
        "home_weather" to "定位实时天气预报"
    )

    private val zhTwStrings = mapOf(
        "app_name" to "萬能日期計算器",
        "app_title" to "萬能日期計算器",
        "tab_home" to "首頁",
        "tab_calc" to "日期計算",
        "tab_diff" to "倒數日",
        "tab_countdown" to "倒數日",
        "tab_anniversary" to "紀念日",
        "tab_lunar" to "農曆轉換",
        "tab_age" to "年齡計算",
        "today" to "今天",
        "yesterday" to "昨天",
        "plus_1w" to "+1週",
        "minus_1w" to "-1週",
        "base_date" to "起始日期",
        "target_date" to "目標日期",
        "workday" to "工作日",
        "natural_day" to "自然日",
        "diff_natural" to "自然日",
        "diff_workday" to "工作日",
        "calc_rule_title" to "計算規則",
        "mode_forward" to "加減天數",
        "mode_reverse" to "區間拆算",
        "multi_stage_btn" to "多段加減",
        "add_stage_btn" to "新增階段",
        "timeline_title" to "總時間安排示意",
        "export_csv" to "匯出 CSV",
        "stage_add_label" to "多段加",
        "stage_sub_label" to "多段減",
        "reverse_end_date_title" to "目標終止日期",
        "result_title_workday" to "工作日計算結果",
        "result_title_natural" to "自然日計算結果",
        "workday_chip" to "工作日",
        "weekend_chip" to "週末雙休",
        "days_unit" to "天",
        "days_passed" to "已過去",
        "days_until_prefix" to "距離",
        "days_until_suffix" to "還有",
        "solar" to "公曆",
        "lunar" to "農曆",
        "select_start_date" to "選擇起始日期",
        "select_target_date" to "選擇目標日期",
        "select_birth_date" to "選擇出生日期",
        "birth_date_label" to "出生日期",
        "birth_date_title" to "出生日期",
        "exact_age" to "精准年齡",
        "zodiac_sign" to "生肖",
        "total_days" to "生存總天數",
        "months_unit" to "個月",
        "total_weeks" to "生存總週數",
        "weeks_unit" to "週",
        "next_birthday_days" to "距下次生日",
        "common_countdown" to "常用倒數日",
        "add_countdown" to "新增倒數日",
        "custom_btn" to "自訂",
        "fixed_countdown" to "固定顯示倒數日",
        "confirm" to "確定",
        "cancel" to "取消",
        "check_in" to "打卡",
        "anniversary_title" to "重要紀念日",
        "add_anniversary" to "新增紀念日",
        "anniversary_name" to "紀念日名稱",
        "anniversary_date" to "紀念日日期",
        "anniversary_dialog_title" to "新增重要紀念日",
        "save_anniversary" to "保存紀念日",
        "check_in_dialog_title" to "精準打卡",
        "no_anniversary_record" to "❤️ 暫無記錄的紀念日",
        "add_anniversary_hint" to "點擊上方“新增紀念日”或“打卡”保存美好時刻",
        "next_anniversary_remains" to "距離下個週年",
        "lunar_converter_title" to "農曆與公曆轉換",
        "solar_to_lunar" to "公曆 ➔ 農曆",
        "lunar_to_solar" to "農曆 ➔ 公曆",
        "solar_date_title" to "公曆日期",
        "lunar_date_title" to "農曆日期",
        "age_calc_title" to "年齡與生存天數精准計算",
        "lived_days" to "已在這個世界上生存",
        "next_birthday" to "距離下次生日還有",
        "chinese_zodiac" to "生肖",
        "constellation" to "星座",
        "yi_label" to "宜",
        "ji_label" to "忌",
        "fortune_suffix" to "每日運勢",
        "lucky_number" to "幸運數字",
        "lucky_color" to "幸運顏色",
        "settings_title" to "應用設定",
        "settings_language" to "軟體應用語言",
        "settings_home_config" to "首頁顯示模組配置",
        "settings_region" to "法定節假日安排標準地區",
        "settings_gps_auto" to "GPS 自動精確定位所在國家/地區",
        "settings_sync_holidays" to "獲取最新假期",
        "settings_theme" to "主題配色方案",
        "settings_dark_mode" to "深色模式跟隨設定",
        "dark_mode_system" to "跟隨系統",
        "dark_mode_on" to "開啟",
        "dark_mode_off" to "關閉",
        "dark_mode_light" to "淺色模式",
        "dark_mode_dark" to "深色模式",
        "history_title" to "計算歷史記錄",
        "clear_history" to "清空歷史",
        "history_empty" to "暫無歷史記錄",
        "history_cleared" to "歷史記錄已清空",
        "history_item_deleted" to "歷史記錄已刪除",
        "confirm_delete_anniversary" to "確認刪除該記錄？",
        "confirm_delete_btn" to "確認刪除",
        "palette_title" to "調色盤",
        "rule_weekend_title" to "週末休息規則模式",
        "rule_five_days" to "雙休 (週六、週日休息)",
        "rule_five_days_desc" to "標準 5 天工作制，每週六日算作雙休",
        "rule_big_small_weeks" to "大小週 (單雙週交替)",
        "rule_big_small_weeks_desc" to "單週休息週日 1 天，雙週休息週六週日 2 天",
        "rule_six_days_sunday" to "單休 (僅週日休息)",
        "rule_six_days_sunday_desc" to "每週一至週六上班，僅週日算作休息日",
        "rule_six_days_saturday" to "單休 (僅週六休息)",
        "rule_six_days_saturday_desc" to "每週一至週五與週日上班，僅週六算作休息日",
        "rule_seven_days" to "無休 (7 天全工作)",
        "rule_seven_days_desc" to "全年無休，所有日期均按工作日計算",
        "home_show_screen" to "顯示首頁",
        "home_calendar" to "日曆月視圖",
        "home_almanac" to "今日黃曆 (宜忌)",
        "home_solar_terms" to "二十四節氣與交節倒計時",
        "home_lunar" to "農曆與幹支神煞",
        "home_zodiac" to "星座與生肖運勢評級",
        "home_weather" to "定位實時天氣預報"
    )

    private val enStrings = mapOf(
        "app_name" to "Date Calculator",
        "app_title" to "Date Calculator",
        "tab_home" to "Home",
        "tab_calc" to "Date Calc",
        "tab_diff" to "Countdowns",
        "tab_countdown" to "Countdowns",
        "tab_anniversary" to "Anniversary",
        "tab_lunar" to "Lunar Convert",
        "tab_age" to "Age Calc",
        "today" to "Today",
        "yesterday" to "Yesterday",
        "plus_1w" to "+1W",
        "minus_1w" to "-1W",
        "base_date" to "Base Date",
        "target_date" to "Target Date",
        "workday" to "Workday",
        "natural_day" to "Calendar Day",
        "diff_natural" to "Calendar Day",
        "diff_workday" to "Workday",
        "calc_rule_title" to "Calculation Rule",
        "mode_forward" to "Add/Sub Days",
        "mode_reverse" to "Date Breakdown",
        "multi_stage_btn" to "Multi-Stage",
        "add_stage_btn" to "Add Stage",
        "timeline_title" to "Total Timeline Overview",
        "export_csv" to "Export CSV",
        "stage_add_label" to "Add",
        "stage_sub_label" to "Sub",
        "reverse_end_date_title" to "Target End Date",
        "result_title_workday" to "Workday Result",
        "result_title_natural" to "Calendar Day Result",
        "workday_chip" to "Workday",
        "weekend_chip" to "Weekend",
        "days_unit" to "Days",
        "days_passed" to "Passed",
        "days_until_prefix" to "In",
        "days_until_suffix" to "remaining",
        "solar" to "Solar",
        "lunar" to "Lunar",
        "select_start_date" to "Select Start Date",
        "select_target_date" to "Select Target Date",
        "select_birth_date" to "Select Date of Birth",
        "birth_date_label" to "Birth Date",
        "birth_date_title" to "Date of Birth",
        "exact_age" to "Exact Age",
        "zodiac_sign" to "Chinese Zodiac",
        "total_days" to "Total Days Lived",
        "months_unit" to "Months",
        "total_weeks" to "Total Weeks Lived",
        "weeks_unit" to "Weeks",
        "next_birthday_days" to "Days to Next Birthday",
        "common_countdown" to "Preset Countdowns",
        "add_countdown" to "Add Countdown",
        "custom_btn" to "Custom",
        "fixed_countdown" to "Pinned Countdowns",
        "confirm" to "Confirm",
        "cancel" to "Cancel",
        "check_in" to "Check-in",
        "anniversary_title" to "Important Anniversaries",
        "add_anniversary" to "Add Anniversary",
        "anniversary_name" to "Event Title",
        "anniversary_date" to "Anniversary Date",
        "anniversary_dialog_title" to "Add Important Anniversary",
        "save_anniversary" to "Save Anniversary",
        "check_in_dialog_title" to "Location Check-in",
        "no_anniversary_record" to "❤️ No anniversaries recorded yet",
        "add_anniversary_hint" to "Tap 'Add Anniversary' or 'Check-in' above to save sweet moments",
        "next_anniversary_remains" to "Next Anniversary In",
        "lunar_converter_title" to "Lunar & Solar Converter",
        "solar_to_lunar" to "Solar ➔ Lunar",
        "lunar_to_solar" to "Lunar ➔ Solar",
        "solar_date_title" to "Solar Date",
        "lunar_date_title" to "Lunar Date",
        "age_calc_title" to "Precise Age & Lived Days Calculator",
        "lived_days" to "Days Lived in World",
        "next_birthday" to "Days Until Next Birthday",
        "chinese_zodiac" to "Zodiac Animal",
        "constellation" to "Zodiac Sign",
        "yi_label" to "Good",
        "ji_label" to "Bad",
        "fortune_suffix" to "Daily Horoscope",
        "lucky_number" to "Lucky No.",
        "lucky_color" to "Lucky Color",
        "settings_title" to "Settings",
        "settings_language" to "App Language",
        "settings_home_config" to "Home Modules Configuration",
        "settings_region" to "Holiday Calendar Region Standard",
        "settings_gps_auto" to "GPS Auto Detect Location Country/Region",
        "settings_sync_holidays" to "Fetch Latest Holidays",
        "settings_theme" to "Theme Color Scheme",
        "settings_dark_mode" to "Dark Mode Option",
        "dark_mode_system" to "Follow System",
        "dark_mode_on" to "On",
        "dark_mode_off" to "Off",
        "dark_mode_light" to "Light Mode",
        "dark_mode_dark" to "Dark Mode",
        "history_title" to "Calculation History",
        "clear_history" to "Clear History",
        "history_empty" to "No history recorded yet",
        "history_cleared" to "History cleared!",
        "history_item_deleted" to "History item deleted",
        "confirm_delete_anniversary" to "Delete this record?",
        "confirm_delete_btn" to "Confirm Delete",
        "palette_title" to "Palette",
        "rule_weekend_title" to "Weekend Rest Rule",
        "rule_five_days" to "5-Day Workweek (Sat & Sun off)",
        "rule_five_days_desc" to "Standard 5 days workweek, Sat and Sun are weekends",
        "rule_big_small_weeks" to "Alternating Weeks (Big/Small)",
        "rule_big_small_weeks_desc" to "1 day off (Sun) on odd weeks, 2 days off (Sat & Sun) on even weeks",
        "rule_six_days_sunday" to "6-Day Workweek (Sunday off)",
        "rule_six_days_sunday_desc" to "Work Mon-Sat, only Sunday is rest day",
        "rule_six_days_saturday" to "6-Day Workweek (Saturday off)",
        "rule_six_days_saturday_desc" to "Work Mon-Fri & Sun, only Saturday is rest day",
        "rule_seven_days" to "7-Day Workweek (No rest days)",
        "rule_seven_days_desc" to "Work every day, all days are counted as workdays",
        "home_show_screen" to "Show Home Tab",
        "home_calendar" to "Calendar Month View",
        "home_almanac" to "Today's Almanac (Yi/Ji)",
        "home_solar_terms" to "24 Solar Terms Countdown",
        "home_lunar" to "Lunar Calendar & Ganzhi",
        "home_zodiac" to "Zodiac & Horoscope Fortune",
        "home_weather" to "Live Location Weather Forecast"
    )

    private val jaStrings = mapOf(
        "app_name" to "万能日付電卓",
        "app_title" to "万能日付電卓",
        "tab_home" to "ホーム",
        "tab_calc" to "日付計算",
        "tab_diff" to "カウントダウン",
        "tab_countdown" to "カウントダウン",
        "tab_anniversary" to "記念日",
        "tab_lunar" to "旧暦変換",
        "tab_age" to "年齢計算",
        "today" to "今日",
        "yesterday" to "昨日",
        "plus_1w" to "+1週",
        "minus_1w" to "-1週",
        "base_date" to "開始日",
        "target_date" to "目標日",
        "workday" to "稼働日",
        "natural_day" to "暦日",
        "diff_natural" to "暦日",
        "diff_workday" to "稼働日",
        "calc_rule_title" to "計算ルール",
        "mode_forward" to "日数加減",
        "mode_reverse" to "期間分解",
        "multi_stage_btn" to "複数段階",
        "add_stage_btn" to "段階を追加",
        "timeline_title" to "全所要時間スケジュール概要",
        "export_csv" to "CSV出力",
        "stage_add_label" to "加算",
        "stage_sub_label" to "減算",
        "reverse_end_date_title" to "目標終了日",
        "result_title_workday" to "稼働日計算結果",
        "result_title_natural" to "暦日計算結果",
        "workday_chip" to "稼働日",
        "weekend_chip" to "週末休日",
        "days_unit" to "日",
        "days_passed" to "経過",
        "days_until_prefix" to "まであと",
        "days_until_suffix" to "",
        "solar" to "新暦",
        "lunar" to "旧暦",
        "select_start_date" to "開始日を選択",
        "select_target_date" to "目標日を選択",
        "select_birth_date" to "生年月日を選択",
        "birth_date_label" to "生年月日",
        "birth_date_title" to "生年月日",
        "exact_age" to "正確な年齢",
        "zodiac_sign" to "干支",
        "total_days" to "総生存日数",
        "months_unit" to "ヶ月",
        "total_weeks" to "総生存週数",
        "weeks_unit" to "週",
        "next_birthday_days" to "次の誕生日まで",
        "common_countdown" to "プリセットカウントダウン",
        "add_countdown" to "カウントダウン追加",
        "custom_btn" to "カスタム",
        "fixed_countdown" to "固定カウントダウン",
        "confirm" to "確定",
        "cancel" to "キャンセル",
        "check_in" to "チェックイン",
        "anniversary_title" to "大切な記念日",
        "add_anniversary" to "記念日を追加",
        "anniversary_name" to "イベント名",
        "anniversary_date" to "記念日の日付",
        "anniversary_dialog_title" to "大切な記念日を追加",
        "save_anniversary" to "記念日を保存",
        "check_in_dialog_title" to "位置チェックイン",
        "no_anniversary_record" to "❤️ 登録された記念日はありません",
        "add_anniversary_hint" to "上の「記念日を追加」または「チェックイン」をタップして思い出を保存",
        "next_anniversary_remains" to "次の周年まであと",
        "lunar_converter_title" to "旧暦・新暦相互変換",
        "solar_to_lunar" to "新暦 ➔ 旧暦",
        "lunar_to_solar" to "旧暦 ➔ 新暦",
        "solar_date_title" to "新暦日付",
        "lunar_date_title" to "旧暦日付",
        "age_calc_title" to "年齢＆生存日数精密計算",
        "lived_days" to "この世界での生存日数",
        "next_birthday" to "次の誕生日まであと",
        "chinese_zodiac" to "干支",
        "constellation" to "星座",
        "yi_label" to "吉",
        "ji_label" to "凶",
        "fortune_suffix" to "今日の運勢",
        "lucky_number" to "ラッキーナンバー",
        "lucky_color" to "ラッキーカラー",
        "settings_title" to "アプリ設定",
        "settings_language" to "言語設定",
        "settings_home_config" to "ホーム画面モジュール設定",
        "settings_region" to "祝日カレンダー基準地域",
        "settings_gps_auto" to "GPS位置自動判定",
        "settings_sync_holidays" to "最新の祝日データを取得",
        "settings_theme" to "テーマカラー",
        "settings_dark_mode" to "ダークモード設定",
        "dark_mode_system" to "システムに従う",
        "dark_mode_on" to "オン",
        "dark_mode_off" to "オフ",
        "dark_mode_light" to "ライトモード",
        "dark_mode_dark" to "ダークモード",
        "history_title" to "計算履歴",
        "clear_history" to "履歴を消去",
        "history_empty" to "履歴はありません",
        "history_cleared" to "履歴を全消去しました",
        "history_item_deleted" to "履歴アイテムを削除しました",
        "confirm_delete_anniversary" to "この記録を削除しますか？",
        "confirm_delete_btn" to "削除",
        "palette_title" to "カラーパレット",
        "rule_weekend_title" to "週末ルール設定",
        "rule_five_days" to "完全週休2日 (土日休み)",
        "rule_five_days_desc" to "標準的な週5日勤務、土日を休日として計算",
        "rule_big_small_weeks" to "隔週週休2日 (大・小週交替)",
        "rule_big_small_weeks_desc" to "奇数週は日曜1日休み、偶数週は土日2日休み",
        "rule_six_days_sunday" to "週休1日 (日曜のみ休み)",
        "rule_six_days_sunday_desc" to "月〜土勤務、日曜日のみ休日として計算",
        "rule_six_days_saturday" to "週休1日 (土曜のみ休み)",
        "rule_six_days_saturday_desc" to "月〜金・日勤務、土曜日のみ休日として計算",
        "rule_seven_days" to "無休 (7全日勤務)",
        "rule_seven_days_desc" to "年中無休、すべての日付を稼働日として計算",
        "home_show_screen" to "ホームタブを表示",
        "home_calendar" to "カレンダー月表示",
        "home_almanac" to "今日の暦・吉凶",
        "home_solar_terms" to "二十四節気カウントダウン",
        "home_lunar" to "旧暦・六曜",
        "home_zodiac" to "星座・干支運勢",
        "home_weather" to "現在地リアルタイム天気"
    )

    private val koStrings = mapOf(
        "app_name" to "만능 날짜 계산기",
        "app_title" to "만능 날짜 계산기",
        "tab_home" to "홈",
        "tab_calc" to "날짜 계산",
        "tab_diff" to "카운트다운",
        "tab_countdown" to "카운트다운",
        "tab_anniversary" to "기념일",
        "tab_lunar" to "음력 변환",
        "tab_age" to "나이 계산",
        "today" to "오늘",
        "yesterday" to "어제",
        "plus_1w" to "+1주",
        "minus_1w" to "-1주",
        "base_date" to "시작일",
        "target_date" to "목표일",
        "workday" to "근무일",
        "natural_day" to "달력일",
        "diff_natural" to "달력일",
        "diff_workday" to "근무일",
        "calc_rule_title" to "계산 규칙",
        "mode_forward" to "일수 더하기/빼기",
        "mode_reverse" to "기간 분할",
        "multi_stage_btn" to "다단계 계산",
        "add_stage_btn" to "단계 추가",
        "timeline_title" to "전체 일정 요약",
        "export_csv" to "CSV 내보내기",
        "stage_add_label" to "더하기",
        "stage_sub_label" to "빼기",
        "reverse_end_date_title" to "목표 종료일",
        "result_title_workday" to "근무일 계산 결과",
        "result_title_natural" to "달력일 계산 결과",
        "workday_chip" to "근무일",
        "weekend_chip" to "주말 휴일",
        "days_unit" to "일",
        "days_passed" to "지남",
        "days_until_prefix" to "까지",
        "days_until_suffix" to "남음",
        "solar" to "양력",
        "lunar" to "음력",
        "select_start_date" to "시작일 선택",
        "select_target_date" to "목표일 선택",
        "select_birth_date" to "생년월일 선택",
        "birth_date_label" to "생년월일",
        "birth_date_title" to "생년월일",
        "exact_age" to "정확한 만 나이",
        "zodiac_sign" to "띠",
        "total_days" to "총 살아온 날수",
        "months_unit" to "개월",
        "total_weeks" to "총 살아온 주수",
        "weeks_unit" to "주",
        "next_birthday_days" to "다음 생일까지",
        "common_countdown" to "추천 디데이",
        "add_countdown" to "디데이 추가",
        "custom_btn" to "사용자 지정",
        "fixed_countdown" to "고정 디데이",
        "confirm" to "확인",
        "cancel" to "취소",
        "check_in" to "체크인",
        "anniversary_title" to "소중한 기념일",
        "add_anniversary" to "기념일 추가",
        "anniversary_name" to "이벤트 이름",
        "anniversary_date" to "기념일 날짜",
        "anniversary_dialog_title" to "소중한 기념일 추가",
        "save_anniversary" to "기념일 저장",
        "check_in_dialog_title" to "위치 체크인",
        "no_anniversary_record" to "❤️ 기록된 기념일이 없습니다",
        "add_anniversary_hint" to "상단의 '기념일 추가' 또는 '체크인'을 눌러 소중한 순간을 저장하세요",
        "next_anniversary_remains" to "다음 주기까지 남은 일수",
        "lunar_converter_title" to "음력/양력 상호 변환",
        "solar_to_lunar" to "양력 ➔ 음력",
        "lunar_to_solar" to "음력 ➔ 양력",
        "solar_date_title" to "양력 날짜",
        "lunar_date_title" to "음력 날짜",
        "age_calc_title" to "정확한 나이 및 살아온 날수 계산",
        "lived_days" to "태어나서 살아온 날수",
        "next_birthday" to "다음 생일까지 남은 날수",
        "chinese_zodiac" to "띠",
        "constellation" to "별자리",
        "yi_label" to "길",
        "ji_label" to "흉",
        "fortune_suffix" to "오늘의 운세",
        "lucky_number" to "행운의 숫자",
        "lucky_color" to "행운의 색상",
        "settings_title" to "앱 설정",
        "settings_language" to "앱 언어 설정",
        "settings_home_config" to "홈 모듈 구성",
        "settings_region" to "공휴일 기준 국가/지역",
        "settings_gps_auto" to "GPS 자동 위치 감지",
        "settings_sync_holidays" to "최신 휴일 정보 가져오기",
        "settings_theme" to "테마 색상",
        "settings_dark_mode" to "다크 모드 설정",
        "dark_mode_system" to "시스템 설정에 따름",
        "dark_mode_on" to "켜기",
        "dark_mode_off" to "끄기",
        "dark_mode_light" to "라이트 모드",
        "dark_mode_dark" to "다크 모드",
        "history_title" to "계산 기록",
        "clear_history" to "기록 삭제",
        "history_empty" to "계산 기록이 없습니다",
        "history_cleared" to "계산 기록이 모두 삭제되었습니다",
        "history_item_deleted" to "기록 항목이 삭제되었습니다",
        "confirm_delete_anniversary" to "이 기록을 삭제하시겠습니까?",
        "confirm_delete_btn" to "삭제",
        "palette_title" to "팔레트",
        "rule_weekend_title" to "주말 휴무 규칙",
        "rule_five_days" to "주 5일 근무 (토/일 휴무)",
        "rule_five_days_desc" to "표준 주 5일 근무, 토요일과 일요일을 휴일로 계산",
        "rule_big_small_weeks" to "격주 휴무 (격주 토요일 근무)",
        "rule_big_small_weeks_desc" to "홀수 주 일요일 1일 휴무, 짝수 주 토/일 2일 휴무",
        "rule_six_days_sunday" to "주 6일 근무 (일요일만 휴무)",
        "rule_six_days_sunday_desc" to "월~토 근무, 일요일만 휴일로 계산",
        "rule_six_days_saturday" to "주 6일 근무 (토요일만 휴무)",
        "rule_six_days_saturday_desc" to "월~금, 일 근무, 토요일만 휴일로 계산",
        "rule_seven_days" to "연중무휴 (7일 모두 근무)",
        "rule_seven_days_desc" to "연중무휴, 모든 날짜를 근무일로 계산",
        "home_show_screen" to "홈 탭 표시",
        "home_calendar" to "달력 월별 보기",
        "home_almanac" to "오늘의 길일/길시",
        "home_solar_terms" to "24절기 카운트다운",
        "home_lunar" to "음력 및 간지",
        "home_zodiac" to "별자리 및 띠 운세",
        "home_weather" to "현재 위치 실시간 날씨"
    )

    fun getString(key: String, language: AppLanguage): String {
        val map = when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> zhCnStrings
            AppLanguage.TRADITIONAL_CHINESE -> zhTwStrings
            AppLanguage.ENGLISH -> enStrings
            AppLanguage.JAPANESE -> jaStrings
            AppLanguage.KOREAN -> koStrings
            else -> zhCnStrings
        }
        return map[key] ?: zhCnStrings[key] ?: key
    }

    fun getWatermarkMonthHeader(yearMonth: YearMonth, language: AppLanguage): String {
        return getLocalizedYearMonth(yearMonth, language)
    }

    fun formatTimestamp(timestamp: Long, language: AppLanguage): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return DateCalculatorUtils.formatDate(date, language)
    }

    fun getLocalizedYearMonth(yearMonth: YearMonth, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.TRADITIONAL_CHINESE -> "${yearMonth.year}年 ${yearMonth.monthValue}月"
            AppLanguage.ENGLISH -> "${yearMonth.month.name.take(3)} ${yearMonth.year}"
            AppLanguage.JAPANESE -> "${yearMonth.year}年 ${yearMonth.monthValue}月"
            AppLanguage.KOREAN -> "${yearMonth.year}년 ${yearMonth.monthValue}월"
            else -> "${yearMonth.year}年 ${yearMonth.monthValue}月"
        }
    }

    fun getWeekHeaders(language: AppLanguage): List<String> {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> listOf("日", "一", "二", "三", "四", "五", "六")
            AppLanguage.TRADITIONAL_CHINESE -> listOf("日", "一", "二", "三", "四", "五", "六")
            AppLanguage.ENGLISH -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            AppLanguage.JAPANESE -> listOf("日", "月", "火", "水", "木", "金", "土")
            AppLanguage.KOREAN -> listOf("일", "월", "화", "수", "목", "금", "토")
            else -> listOf("日", "一", "二", "三", "四", "五", "六")
        }
    }

    fun getLocalizedStageTitle(index: Int, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> "第${index}段时间"
            AppLanguage.TRADITIONAL_CHINESE -> "第${index}段時間"
            AppLanguage.ENGLISH -> "Stage $index"
            AppLanguage.JAPANESE -> "第${index}段階"
            AppLanguage.KOREAN -> "${index}단계"
            else -> "第${index}段时间"
        }
    }

    fun getLocalizedHolidayName(rawName: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE || effective == AppLanguage.TRADITIONAL_CHINESE) {
            return rawName
        }

        val cleanName = rawName.replace(Regex("[^\\u4e00-\\u9fa5]"), "").trim()
        return when (cleanName) {
            "国庆节", "國慶節" -> if (effective == AppLanguage.ENGLISH) "National Day" else if (effective == AppLanguage.JAPANESE) "建国記念日" else "국경절"
            "元旦" -> if (effective == AppLanguage.ENGLISH) "New Year's Day" else if (effective == AppLanguage.JAPANESE) "元日" else "신정"
            "春节", "春節" -> if (effective == AppLanguage.ENGLISH) "Spring Festival" else if (effective == AppLanguage.JAPANESE) "旧正月" else "설날"
            "清明节", "清明節" -> if (effective == AppLanguage.ENGLISH) "Tomb Sweeping Day" else if (effective == AppLanguage.JAPANESE) "清明" else "청명절"
            "劳动节", "五一劳动节", "勞動節" -> if (effective == AppLanguage.ENGLISH) "Labor Day" else if (effective == AppLanguage.JAPANESE) "メーデー" else "노동절"
            "端午节", "端午節" -> if (effective == AppLanguage.ENGLISH) "Dragon Boat Festival" else if (effective == AppLanguage.JAPANESE) "端午の節句" else "단오"
            "高考" -> if (effective == AppLanguage.ENGLISH) "College Entrance Exam" else if (effective == AppLanguage.JAPANESE) "大学入学試験" else "수능"
            "中考" -> if (effective == AppLanguage.ENGLISH) "High School Entrance Exam" else if (effective == AppLanguage.JAPANESE) "高校入学試験" else "고교 입학 시험"
            "中秋节", "中秋節" -> if (effective == AppLanguage.ENGLISH) "Mid-Autumn Festival" else if (effective == AppLanguage.JAPANESE) "中秋の名月" else "추석"
            else -> rawName
        }
    }

    fun getDayName(rawDayName: String, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> rawDayName
            AppLanguage.TRADITIONAL_CHINESE -> when (rawDayName) {
                "今天" -> "今天"; "明天" -> "明天"; "后天" -> "後天"; "大后天" -> "大後天"; else -> rawDayName
            }
            AppLanguage.ENGLISH -> when (rawDayName) {
                "今天" -> "Today"; "明天" -> "Tomorrow"; "后天" -> "In 2 Days"; "大后天" -> "3 Days Later"; else -> rawDayName
            }
            AppLanguage.JAPANESE -> when (rawDayName) {
                "今天" -> "今日"; "明天" -> "明日"; "后天" -> "明後日"; "大后天" -> "明々後日"; else -> rawDayName
            }
            AppLanguage.KOREAN -> when (rawDayName) {
                "今天" -> "오늘"; "明天" -> "내일"; "后天" -> "모레"; "大后天" -> "글피"; else -> rawDayName
            }
            else -> rawDayName
        }
    }

    fun getWeatherCondition(rawCondition: String, language: AppLanguage): String {
        return WeatherUtils.getLocalizedCondition(rawCondition, language)
    }

    fun getToastPleaseEnterDays(language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.ENGLISH -> "Please enter number of days"
            AppLanguage.TRADITIONAL_CHINESE -> "請輸入天數"
            AppLanguage.JAPANESE -> "日数を入力してください"
            AppLanguage.KOREAN -> "일수를 입력해 주세요"
            else -> "请输入天数"
        }
    }

    fun getLocalizedConstellation(rawConstellation: String, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> rawConstellation
            AppLanguage.TRADITIONAL_CHINESE -> when (rawConstellation) {
                "白羊座" -> "白羊座"; "金牛座" -> "金牛座"; "双子座" -> "雙子座"; "巨蟹座" -> "巨蟹座"
                "狮子座" -> "獅子座"; "处女座" -> "處女座"; "天秤座" -> "天秤座"; "天蝎座" -> "天蠍座"
                "射手座" -> "射手座"; "摩羯座" -> "摩羯座"; "水瓶座" -> "水瓶座"; "双鱼座" -> "雙魚座"
                else -> rawConstellation
            }
            AppLanguage.ENGLISH -> when (rawConstellation) {
                "白羊座" -> "Aries"; "金牛座" -> "Taurus"; "双子座" -> "Gemini"; "巨蟹座" -> "Cancer"
                "狮子座" -> "Leo"; "处女座" -> "Virgo"; "天秤座" -> "Libra"; "天蝎座" -> "Scorpio"
                "射手座" -> "Sagittarius"; "摩羯座" -> "Capricorn"; "水瓶座" -> "Aquarius"; "双鱼座" -> "Pisces"
                else -> rawConstellation
            }
            AppLanguage.JAPANESE -> when (rawConstellation) {
                "白羊座" -> "牡羊座"; "金牛座" -> "牡牛座"; "双子座" -> "双子座"; "巨蟹座" -> "蟹座"
                "狮子座" -> "獅子座"; "处女座" -> "乙女座"; "天秤座" -> "天秤座"; "天蝎座" -> "蠍座"
                "射手座" -> "射手座"; "摩羯座" -> "山羊座"; "水瓶座" -> "水瓶座"; "双鱼座" -> "魚座"
                else -> rawConstellation
            }
            AppLanguage.KOREAN -> when (rawConstellation) {
                "白羊座" -> "양자리"; "金牛座" -> "황소자리"; "双子座" -> "쌍둥이자리"; "巨蟹座" -> "게자리"
                "狮子座" -> "사자자리"; "处女座" -> "처녀자리"; "天秤座" -> "천칭자리"; "天蝎座" -> "전갈자리"
                "射手座" -> "궁수자리"; "摩羯座" -> "염소자리"; "水瓶座" -> "물병자리"; "双鱼座" -> "물고기자리"
                else -> rawConstellation
            }
            else -> rawConstellation
        }
    }

    fun getLocalizedZodiac(rawZodiac: String, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.TRADITIONAL_CHINESE -> rawZodiac
            AppLanguage.ENGLISH -> when (rawZodiac) {
                "鼠" -> "Rat"; "牛" -> "Ox"; "虎" -> "Tiger"; "兔" -> "Rabbit"
                "龙" -> "Dragon"; "蛇" -> "Snake"; "马" -> "Horse"; "羊" -> "Goat"
                "猴" -> "Monkey"; "鸡" -> "Rooster"; "狗" -> "Dog"; "猪" -> "Pig"
                else -> rawZodiac
            }
            AppLanguage.JAPANESE -> when (rawZodiac) {
                "鼠" -> "子"; "牛" -> "丑"; "虎" -> "寅"; "兔" -> "卯"
                "龙" -> "辰"; "蛇" -> "巳"; "马" -> "午"; "羊" -> "未"
                "猴" -> "申"; "鸡" -> "酉"; "狗" -> "戌"; "猪" -> "亥"
                else -> rawZodiac
            }
            AppLanguage.KOREAN -> when (rawZodiac) {
                "鼠" -> "쥐띠"; "牛" -> "소띠"; "虎" -> "호랑이띠"; "兔" -> "토끼띠"
                "龙" -> "용띠"; "蛇" -> "뱀띠"; "马" -> "말띠"; "羊" -> "양띠"
                "猴" -> "원숭이띠"; "鸡" -> "닭띠"; "狗" -> "개띠"; "猪" -> "돼지띠"
                else -> rawZodiac
            }
            else -> rawZodiac
        }
    }

    fun getLocalizedHistoryTitle(rawTitle: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE || effective == AppLanguage.TRADITIONAL_CHINESE) {
            return rawTitle
        }
        var title = rawTitle
        title = title.replace("工作日计算结果", "Workday Result")
            .replace("自然日计算结果", "Calendar Day Result")
            .replace("工作日", " Workdays")
            .replace("自然日", " Days")
        return title
    }

    fun getLocalizedHistoryDetail(rawDetail: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE || effective == AppLanguage.TRADITIONAL_CHINESE) {
            return rawDetail
        }
        var detail = rawDetail
        detail = detail.replace("起始日期:", "Start:").replace("目标日期:", "Target:").replace("工作日", " Workdays").replace("自然日", " Days")
        return detail
    }

    fun getLocalizedHistoryCategory(rawCat: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE || effective == AppLanguage.TRADITIONAL_CHINESE) {
            return rawCat
        }
        return when (rawCat) {
            "日期计算" -> "Date Calc"
            "农历公历" -> "Lunar & Solar"
            "倒数日" -> "Countdown"
            "年龄计算" -> "Age Calc"
            else -> rawCat
        }
    }

    fun getLocalizedRegionTag(rawTag: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE || effective == AppLanguage.TRADITIONAL_CHINESE) {
            return rawTag
        }
        return rawTag
    }

    fun getLocalizedCityName(rawLocation: String, language: AppLanguage): String {
        val effective = language.getEffectiveLanguage()
        if (effective == AppLanguage.SIMPLIFIED_CHINESE) {
            return rawLocation
        }

        val parts = rawLocation.split("·").map { it.trim() }
        val cityPart = parts.firstOrNull() ?: rawLocation
        val districtPart = if (parts.size > 1) parts[1] else ""

        val translatedCity = translateSingleCity(cityPart, effective)
        val translatedDistrict = if (districtPart.isNotBlank()) translateSingleDistrict(districtPart, effective) else ""

        return if (translatedDistrict.isNotBlank()) {
            if (effective == AppLanguage.ENGLISH) {
                "$translatedDistrict, $translatedCity"
            } else {
                "$translatedCity · $translatedDistrict"
            }
        } else {
            translatedCity
        }
    }

    private fun translateSingleCity(cityName: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.TRADITIONAL_CHINESE -> when {
                cityName.contains("北京") -> "北京市"
                cityName.contains("上海") -> "上海市"
                cityName.contains("广州") -> "廣州市"
                cityName.contains("深圳") -> "深圳市"
                cityName.contains("杭州") -> "杭州市"
                cityName.contains("成都") -> "成都市"
                cityName.contains("南京") -> "南京市"
                cityName.contains("香港") -> "香港特別行政區"
                cityName.contains("澳门") -> "澳門特別行政區"
                else -> cityName
            }
            AppLanguage.ENGLISH -> when {
                cityName.contains("北京") -> "Beijing"
                cityName.contains("上海") -> "Shanghai"
                cityName.contains("广州") -> "Guangzhou"
                cityName.contains("深圳") -> "Shenzhen"
                cityName.contains("杭州") -> "Hangzhou"
                cityName.contains("成都") -> "Chengdu"
                cityName.contains("武汉") -> "Wuhan"
                cityName.contains("南京") -> "Nanjing"
                cityName.contains("重庆") -> "Chongqing"
                cityName.contains("天津") -> "Tianjin"
                cityName.contains("西安") -> "Xi'an"
                cityName.contains("台北") -> "Taipei"
                cityName.contains("香港") -> "Hong Kong"
                cityName.contains("澳门") -> "Macau"
                cityName.contains("新加坡") -> "Singapore"
                cityName.contains("东京") -> "Tokyo"
                cityName.contains("首尔") -> "Seoul"
                cityName.contains("伦敦") -> "London"
                cityName.contains("纽约") -> "New York"
                else -> cityName
            }
            AppLanguage.JAPANESE -> when {
                cityName.contains("北京") -> "北京"
                cityName.contains("上海") -> "上海"
                cityName.contains("广州") -> "広州"
                cityName.contains("深圳") -> "深セン"
                cityName.contains("杭州") -> "杭州"
                cityName.contains("成都") -> "成都"
                cityName.contains("台北") -> "台北"
                cityName.contains("香港") -> "香港"
                else -> cityName
            }
            AppLanguage.KOREAN -> when {
                cityName.contains("北京") -> "베이징"
                cityName.contains("上海") -> "상하이"
                cityName.contains("广州") -> "광저우"
                cityName.contains("深圳") -> "선전"
                cityName.contains("杭州") -> "항저우"
                cityName.contains("成都") -> "청두"
                cityName.contains("台北") -> "타이베이"
                cityName.contains("香港") -> "홍콩"
                else -> cityName
            }
            else -> cityName
        }
    }

    private fun translateSingleDistrict(districtName: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.TRADITIONAL_CHINESE -> when {
                districtName.contains("南山") -> "南山區"
                districtName.contains("福田") -> "福田區"
                districtName.contains("宝安") -> "寶安區"
                districtName.contains("龙岗") -> "龍崗區"
                districtName.contains("罗湖") -> "羅湖區"
                districtName.contains("天河") -> "天河區"
                districtName.contains("朝阳") -> "朝陽區"
                districtName.contains("浦东") -> "浦東新區"
                else -> districtName
            }
            AppLanguage.ENGLISH -> when {
                districtName.contains("南山") -> "Nanshan"
                districtName.contains("福田") -> "Futian"
                districtName.contains("宝安") -> "Bao'an"
                districtName.contains("龙岗") -> "Longgang"
                districtName.contains("罗湖") -> "Luohu"
                districtName.contains("龙华") -> "Longhua"
                districtName.contains("坪山") -> "Pingshan"
                districtName.contains("光明") -> "Guangming"
                districtName.contains("盐田") -> "Yantian"
                districtName.contains("天河") -> "Tianhe"
                districtName.contains("越秀") -> "Yuexiu"
                districtName.contains("海珠") -> "Haizhu"
                districtName.contains("朝阳") -> "Chaoyang"
                districtName.contains("海淀") -> "Haidian"
                districtName.contains("浦东") -> "Pudong"
                districtName.contains("黄浦") -> "Huangpu"
                districtName.contains("静安") -> "Jing'an"
                else -> districtName
            }
            AppLanguage.JAPANESE -> when {
                districtName.contains("南山") -> "南山区"
                districtName.contains("福田") -> "福田区"
                districtName.contains("宝安") -> "宝安区"
                districtName.contains("天河") -> "天河区"
                districtName.contains("浦东") -> "浦東新区"
                else -> districtName
            }
            AppLanguage.KOREAN -> when {
                districtName.contains("南山") -> "남산구"
                districtName.contains("福田") -> "푸톈구"
                districtName.contains("宝安") -> "바오안구"
                districtName.contains("天河") -> "톈허구"
                else -> districtName
            }
            else -> districtName
        }
    }

    fun getLocalizedAlmanacItem(rawItem: String, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> rawItem
            AppLanguage.TRADITIONAL_CHINESE -> when (rawItem) {
                "祭祀" -> "祭祀"; "祈福" -> "祈福"; "求嗣" -> "求嗣"; "开光" -> "開光"
                "出行" -> "出行"; "拆卸" -> "拆卸"; "修造" -> "修造"; "动土" -> "動土"
                "进人口" -> "進人口"; "开市" -> "開市"; "交易" -> "交易"; "立券" -> "立券"
                "挂匾" -> "掛匾"; "入宅" -> "入宅"; "移徙" -> "移徙"; "安床" -> "安床"
                "栽种" -> "栽種"; "纳畜" -> "納畜"; "入殓" -> "入殮"; "移柩" -> "移柩"
                "安葬" -> "安葬"; "谢土" -> "謝土"; "求医" -> "求醫"; "治病" -> "治病"
                "作灶" -> "作灶"; "扫舍" -> "掃舍"; "纳财" -> "納財"; "签合同" -> "簽合同"
                "探病" -> "探病"; "开仓" -> "開倉"; "乘船" -> "乘船"; "伐木" -> "伐木"
                "筑堤" -> "築堤"; "预期" -> "預期"
                else -> rawItem
            }
            else -> rawItem
        }
    }
}
