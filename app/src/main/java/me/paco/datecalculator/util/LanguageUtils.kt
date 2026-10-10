package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

object LanguageUtils {

    fun getString(key: String, language: AppLanguage): String {
        val effectiveLang = language.getEffectiveLanguage()
        val map = when (effectiveLang) {
            AppLanguage.SIMPLIFIED_CHINESE -> stringsZh
            AppLanguage.TRADITIONAL_CHINESE -> stringsZhTw
            AppLanguage.ENGLISH -> stringsEn
            AppLanguage.JAPANESE -> stringsJa
            AppLanguage.KOREAN -> stringsKo
            else -> stringsZh
        }
        return map[key] ?: key
    }

    private val stringsZh = mapOf(
        "app_title" to "日期计算器",
        "tab_home" to "首页",
        "tab_calc" to "日期计算",
        "tab_countdown" to "倒数日",
        "tab_anniversary" to "纪念日",
        "tab_lunar" to "农历转换",
        "tab_age" to "年龄计算",
        "settings_language" to "语言设置",
        "settings_theme" to "主题配色方案",
        "settings_dark_mode" to "深色模式",
        "settings_home_config" to "首页功能卡片显隐设置",
        "settings_region" to "选择国家/地区",
        "settings_gps_auto" to "GPS 自动识别所在地",
        "settings_sync_holidays" to "同步最新节假日数据",
        "home_show_screen" to "显示首页 (Home Screen)",
        "home_calendar" to "月历视图",
        "home_almanac" to "当日黄历与宜忌",
        "home_solar_terms" to "二十四节气",
        "home_lunar" to "农历干支与月日",
        "home_zodiac" to "星座每日运势",
        "home_weather" to "GPS 实时天气预报",
        "history_title" to "历史记录",
        "history_empty" to "暂无历史推算记录",
        "history_clear" to "清空全部历史",
        "settings_title" to "系统设置",
        "select_start_date" to "选择起始日期",
        "select_target_date" to "选择目标日期",
        "base_date" to "起始日期",
        "target_date" to "目标日期",
        "today" to "今天",
        "yesterday" to "昨天",
        "plus_1w" to "+1周",
        "minus_1w" to "-1周",
        "workday" to "工作日",
        "natural_day" to "自然日",
        "mode_forward" to "加减天数",
        "mode_reverse" to "区间拆算",
        "multi_stage_btn" to "多段加减",
        "calc_rule_title" to "推算规则 (工作日 / 自然日)",
        "add_stage_btn" to "添加阶段",
        "export_csv_btn" to "导出 CSV",
        "common_countdown" to "常用倒数日",
        "fixed_countdown" to "固定倒数日",
        "add_countdown" to "添加自定义倒数日",
        "solar" to "公历",
        "lunar" to "农历",
        "birth_date_label" to "出生日期",
        "select_birth_date" to "选择出生日期",
        "exact_age" to "您的精确年龄",
        "age_result" to "您的精确年龄",
        "years_unit" to "岁",
        "months_unit" to "个月",
        "weeks_unit" to "周",
        "days_unit" to "天",
        "total_days" to "生存总天数",
        "total_weeks" to "生存总周数",
        "next_birthday_days" to "距离下次生日",
        "zodiac_sign" to "生肖属相",
        "constellation" to "星座",
        "days_until_prefix" to "距离",
        "days_until_suffix" to "还有",
        "lucky_number" to "幸运数字",
        "lucky_color" to "幸运颜色",
        "forecast_title" to "当地及未来三日天气推算",
        "add_anniversary" to "新增纪念日",
        "check_in" to "打卡",
        "save_anniversary" to "保存纪念日",
        "anniversary_name" to "纪念日名称",
        "anniversary_date" to "纪念日日期",
        "dark_mode_system" to "跟随系统",
        "dark_mode_on" to "开启",
        "dark_mode_off" to "关闭",
        "workday_chip" to "工作日",
        "weekend_chip" to "休息日",
        "result_title_workday" to "工作日计算结果",
        "result_title_natural" to "自然日计算结果",
        "cancel" to "取消",
        "confirm" to "确定",
        "export_csv" to "导出 CSV",
        "close_details" to "关闭详情",
        "edit_history_title" to "修改历史记录名称",
        "enter_new_history_title" to "请输入新的历史记录名称:",
        "save_title" to "保存名称",
        "reverse_end_date_title" to "选择终止日期拆算包含的天数:",
        "end_date_label" to "终止日期",
        "anniversary_dialog_title" to "添加重要纪念日",
        "check_in_dialog_title" to "精准打卡",
        "confirm_delete_anniversary" to "确认删除纪念日？",
        "confirm_delete_btn" to "确认删除",
        "days_passed" to "已陪伴过去",
        "days_upcoming" to "距离即将到来",
        "next_anniversary_remains" to "下个周年还剩",
        "stage_remark_hint" to "记下这段时间要安排的事 (如: 方案准备 / 旅程第一站)",
        "timeline_title" to "总时间安排示意",
        "total_duration" to "总历时",
        "weekend_rest" to "周末双休",
        "statutory_holiday" to "法定节假日",
        "stage_add_label" to "多段加",
        "stage_sub_label" to "多段减",
        "rule_weekend_title" to "周末休息模式",
        "rule_five_days" to "双休 (周六日休息)",
        "rule_big_small_weeks" to "大小周 (单双休轮替)",
        "rule_six_days_sunday" to "单休 (仅周日休息)",
        "rule_six_days_saturday" to "单休 (仅周六休息)",
        "rule_seven_days" to "无休 (七天工作)",
        "rule_five_days_desc" to "每周一至周五为工作日，周六周日休息",
        "rule_big_small_weeks_desc" to "一周单休 (仅周日休)，次周双休 (周六日休)，隔周轮替",
        "rule_six_days_sunday_desc" to "每周一至周六为工作日，仅周日休息",
        "rule_six_days_saturday_desc" to "每周日及周一至周五为工作日，仅周六休息",
        "rule_seven_days_desc" to "一周七天均为工作日，不计周末",
        "no_anniversary_record" to "❤️ 暂无记录的重要纪念日",
        "add_anniversary_hint" to "点击上方“新增纪念日”或“打卡”保存美好时刻",
        "fortune_suffix" to "每日运势",
        "custom_btn" to "自定义",
        "yi_label" to "宜",
        "ji_label" to "忌",
        "lunar_to_solar_title" to "公历 ➔ 农历",
        "solar_to_lunar_title" to "农历 ➔ 公历",
        "lunar_convert_title" to "农历与公历转换",
        "custom_color" to "自定义色彩",
        "palette_title" to "调色盘 (18 款精选主色调):"
    )

    private val stringsZhTw = mapOf(
        "app_title" to "日期計算器",
        "tab_home" to "首頁",
        "tab_calc" to "日期計算",
        "tab_countdown" to "倒數日",
        "tab_anniversary" to "紀念日",
        "tab_lunar" to "農曆轉換",
        "tab_age" to "年齡計算",
        "settings_language" to "語言設定",
        "settings_theme" to "主題配色方案",
        "settings_dark_mode" to "深色模式",
        "settings_home_config" to "首頁功能顯示與顯隱設定",
        "settings_region" to "選擇國家/地區",
        "settings_gps_auto" to "GPS 自動識別所在地",
        "settings_sync_holidays" to "同步最新節假日數據",
        "home_show_screen" to "顯示首頁 (Home Screen)",
        "home_calendar" to "月曆視圖",
        "home_almanac" to "當日黃曆與宜忌",
        "home_solar_terms" to "二十四節氣",
        "home_lunar" to "農曆干支與月日",
        "home_zodiac" to "星座每日運勢",
        "home_weather" to "GPS 實時天氣預報",
        "history_title" to "歷史記錄",
        "history_empty" to "暫無歷史推算記錄",
        "history_clear" to "清空全部歷史",
        "settings_title" to "系統設定",
        "select_start_date" to "選擇起始日期",
        "select_target_date" to "選擇目標日期",
        "base_date" to "起始日期",
        "target_date" to "目標日期",
        "today" to "今天",
        "yesterday" to "昨天",
        "plus_1w" to "+1周",
        "minus_1w" to "-1周",
        "workday" to "工作日",
        "natural_day" to "自然日",
        "mode_forward" to "加減天數",
        "mode_reverse" to "區間拆算",
        "multi_stage_btn" to "多段加減",
        "calc_rule_title" to "推算規則 (工作日 / 自然日)",
        "add_stage_btn" to "添加階段",
        "export_csv_btn" to "匯出 CSV",
        "common_countdown" to "常用倒數日",
        "fixed_countdown" to "固定倒數日",
        "add_countdown" to "添加自定義倒數日",
        "solar" to "公曆",
        "lunar" to "農曆",
        "birth_date_label" to "出生日期",
        "select_birth_date" to "選擇出生日期",
        "exact_age" to "您的精確年齡",
        "age_result" to "您的精確年齡",
        "years_unit" to "歲",
        "months_unit" to "個月",
        "weeks_unit" to "周",
        "days_unit" to "天",
        "total_days" to "生存總天數",
        "total_weeks" to "生存總周數",
        "next_birthday_days" to "距離下次生日",
        "zodiac_sign" to "生肖屬相",
        "constellation" to "星座",
        "days_until_prefix" to "距離",
        "days_until_suffix" to "還有",
        "lucky_number" to "幸運數字",
        "lucky_color" to "幸運顏色",
        "forecast_title" to "當地及未來三日天氣推算",
        "add_anniversary" to "新增紀念日",
        "check_in" to "打卡",
        "save_anniversary" to "保存紀念日",
        "anniversary_name" to "紀念日名稱",
        "anniversary_date" to "紀念日日期",
        "dark_mode_system" to "跟隨系統",
        "dark_mode_on" to "開啟",
        "dark_mode_off" to "關閉",
        "workday_chip" to "工作日",
        "weekend_chip" to "休息日",
        "result_title_workday" to "工作日計算結果",
        "result_title_natural" to "自然日計算結果",
        "cancel" to "取消",
        "confirm" to "確定",
        "export_csv" to "匯出 CSV",
        "close_details" to "關閉詳情",
        "edit_history_title" to "修改歷史記錄名稱",
        "enter_new_history_title" to "請輸入新的歷史記錄名稱:",
        "save_title" to "保存名稱",
        "reverse_end_date_title" to "選擇終止日期拆算包含的天數:",
        "end_date_label" to "終止日期",
        "anniversary_dialog_title" to "添加重要紀念日",
        "check_in_dialog_title" to "精準打卡",
        "confirm_delete_anniversary" to "確認刪除紀念日？",
        "confirm_delete_btn" to "確認刪除",
        "days_passed" to "已陪伴過去",
        "days_upcoming" to "距離即將到來",
        "next_anniversary_remains" to "下個周年還剩",
        "stage_remark_hint" to "記下這段時間要安排的事 (如: 方案準備 / 旅程第一站)",
        "timeline_title" to "總時間安排示意",
        "total_duration" to "總歷時",
        "weekend_rest" to "周末雙休",
        "statutory_holiday" to "法定節假日",
        "stage_add_label" to "多段加",
        "stage_sub_label" to "多段減",
        "rule_weekend_title" to "周末休息模式",
        "rule_five_days" to "雙休 (周六日休息)",
        "rule_big_small_weeks" to "大小周 (單雙休輪替)",
        "rule_six_days_sunday" to "單休 (僅周日休息)",
        "rule_six_days_saturday" to "單休 (僅周六休息)",
        "rule_seven_days" to "無休 (七天工作)",
        "rule_five_days_desc" to "每周一至周五為工作日，周六周日休息",
        "rule_big_small_weeks_desc" to "一周單休 (僅周日休)，次周雙休 (周六日休)，隔周輪替",
        "rule_six_days_sunday_desc" to "每周一至周六為工作日，僅周日休息",
        "rule_six_days_saturday_desc" to "每周日及周一至周五為工作日，僅周六休息",
        "rule_seven_days_desc" to "一周七天均為工作日，不計周末",
        "no_anniversary_record" to "❤️ 暫無記錄的重要紀念日",
        "add_anniversary_hint" to "點擊上方“新增紀念日”或“打卡”保存美好時刻",
        "fortune_suffix" to "每日運勢",
        "custom_btn" to "自定義",
        "yi_label" to "宜",
        "ji_label" to "忌",
        "lunar_to_solar_title" to "公曆 ➔ 農曆",
        "solar_to_lunar_title" to "農曆 ➔ 公曆",
        "lunar_convert_title" to "農曆與公曆轉換",
        "custom_color" to "自定義色彩",
        "palette_title" to "調色盤 (18 款精選主色調):"
    )

    private val stringsEn = mapOf(
        "app_title" to "Date Calculator",
        "tab_home" to "Home",
        "tab_calc" to "Date Calc",
        "tab_countdown" to "Countdown",
        "tab_anniversary" to "Anniversary",
        "tab_lunar" to "Lunar",
        "tab_age" to "Age Calc",
        "settings_language" to "Language Settings",
        "settings_theme" to "Theme Color Preset",
        "settings_dark_mode" to "Dark Mode",
        "settings_home_config" to "Home Modules Visibility",
        "settings_region" to "Country / Region",
        "settings_gps_auto" to "Auto GPS Location Detection",
        "settings_sync_holidays" to "Sync Latest Holidays Data",
        "home_show_screen" to "Show Home Screen",
        "home_calendar" to "Calendar View",
        "home_almanac" to "Almanac & Daily Guidance",
        "home_solar_terms" to "24 Solar Terms",
        "home_lunar" to "Lunar Date Info",
        "home_zodiac" to "Daily Horoscope & Zodiac",
        "home_weather" to "Live Weather Forecast",
        "history_title" to "Calculation History",
        "history_empty" to "No history records yet",
        "history_clear" to "Clear History",
        "settings_title" to "Settings",
        "select_start_date" to "Select Start Date",
        "select_target_date" to "Select Target Date",
        "base_date" to "Start Date",
        "target_date" to "Target Date",
        "today" to "Today",
        "yesterday" to "Yest",
        "plus_1w" to "+1 Wk",
        "minus_1w" to "-1 Wk",
        "workday" to "Workday",
        "natural_day" to "Calendar Day",
        "mode_forward" to "Days Calc",
        "mode_reverse" to "Interval Breakdown",
        "multi_stage_btn" to "Multi-Stage Calc",
        "calc_rule_title" to "Calculation Rule",
        "add_stage_btn" to "Add Stage",
        "export_csv_btn" to "Export CSV",
        "common_countdown" to "Preset Countdowns",
        "fixed_countdown" to "Pinned Countdowns",
        "add_countdown" to "Add Custom Countdown",
        "solar" to "Solar",
        "lunar" to "Lunar",
        "birth_date_label" to "Date of Birth",
        "select_birth_date" to "Select Date of Birth",
        "exact_age" to "Your Exact Age",
        "age_result" to "Exact Age Breakdown",
        "years_unit" to "Years Old",
        "months_unit" to "Months",
        "weeks_unit" to "Weeks",
        "days_unit" to "Days",
        "total_days" to "Total Days Lived",
        "total_weeks" to "Total Weeks Lived",
        "next_birthday_days" to "Days to Next Birthday",
        "zodiac_sign" to "Zodiac Animal",
        "constellation" to "Astrological Sign",
        "days_until_prefix" to "",
        "days_until_suffix" to "Days Remaining",
        "lucky_number" to "Lucky Number",
        "lucky_color" to "Lucky Color",
        "forecast_title" to "Weather Forecast",
        "add_anniversary" to "Add Anniversary",
        "check_in" to "Check-in",
        "save_anniversary" to "Save Anniversary",
        "anniversary_name" to "Title",
        "anniversary_date" to "Date",
        "dark_mode_system" to "System Default",
        "dark_mode_on" to "On",
        "dark_mode_off" to "Off",
        "workday_chip" to "Workday",
        "weekend_chip" to "Weekend",
        "result_title_workday" to "Workday Result",
        "result_title_natural" to "Calendar Day Result",
        "cancel" to "Cancel",
        "confirm" to "Confirm",
        "export_csv" to "Export CSV",
        "close_details" to "Close",
        "edit_history_title" to "Rename Record",
        "enter_new_history_title" to "Enter new title:",
        "save_title" to "Save",
        "reverse_end_date_title" to "Select End Date to Breakdown Interval:",
        "end_date_label" to "End Date",
        "anniversary_dialog_title" to "New Anniversary",
        "check_in_dialog_title" to "Location Check-In",
        "confirm_delete_anniversary" to "Delete Anniversary?",
        "confirm_delete_btn" to "Delete",
        "days_passed" to "Days Passed",
        "days_upcoming" to "Days Remaining",
        "next_anniversary_remains" to "Next Anniversary in",
        "stage_remark_hint" to "Notes (e.g. Planning / Stop 1)",
        "timeline_title" to "Timeline Overview",
        "total_duration" to "Total Duration",
        "weekend_rest" to "Weekend Rest",
        "statutory_holiday" to "Holidays",
        "stage_add_label" to "Add Stage",
        "stage_sub_label" to "Subtract Stage",
        "rule_weekend_title" to "Weekend Rule",
        "rule_five_days" to "5-Day Workweek (Sat & Sun off)",
        "rule_big_small_weeks" to "Alternating Weeks (Big/Small)",
        "rule_six_days_sunday" to "6-Day Workweek (Sun off)",
        "rule_six_days_saturday" to "6-Day Workweek (Sat off)",
        "rule_seven_days" to "7-Day Continuous (No weekend)",
        "rule_five_days_desc" to "Mon-Fri workdays, Sat-Sun rest",
        "rule_big_small_weeks_desc" to "Alternate between 6-day and 5-day workweeks",
        "rule_six_days_sunday_desc" to "Mon-Sat workdays, Sun rest",
        "rule_six_days_saturday_desc" to "Sun-Fri workdays, Sat rest",
        "rule_seven_days_desc" to "All 7 days are workdays",
        "no_anniversary_record" to "❤️ No Anniversaries Saved",
        "add_anniversary_hint" to "Tap 'Add Anniversary' or 'Check-in' above",
        "fortune_suffix" to "Horoscope",
        "custom_btn" to "Custom",
        "yi_label" to "Suitable",
        "ji_label" to "Avoid",
        "lunar_to_solar_title" to "Solar ➔ Lunar",
        "solar_to_lunar_title" to "Lunar ➔ Solar",
        "lunar_convert_title" to "Lunar & Solar Converter",
        "custom_color" to "Custom Color",
        "palette_title" to "Palette Presets (18 Selected Colors):"
    )

    private val stringsJa = mapOf(
        "app_title" to "日付電卓",
        "tab_home" to "ホーム",
        "tab_calc" to "日付計算",
        "tab_countdown" to "カウントダウン",
        "tab_anniversary" to "記念日",
        "tab_lunar" to "旧暦変換",
        "tab_age" to "年齢計算",
        "settings_language" to "言語設定",
        "settings_theme" to "テーマカラー",
        "settings_dark_mode" to "ダークモード",
        "settings_home_config" to "ホームカード表示設定",
        "settings_region" to "国・地域選択",
        "settings_gps_auto" to "GPS自動位置識別",
        "settings_sync_holidays" to "最新祝日データ同期",
        "home_show_screen" to "ホーム画面を表示",
        "home_calendar" to "カレンダー表示",
        "home_almanac" to "暦と吉凶",
        "home_solar_terms" to "二十四節気",
        "home_lunar" to "旧暦情報",
        "home_zodiac" to "今日の運勢",
        "home_weather" to "リアルタイム天気",
        "history_title" to "計算履歴",
        "history_empty" to "履歴はありません",
        "history_clear" to "全履歴削除",
        "settings_title" to "設定",
        "select_start_date" to "開始日を選択",
        "select_target_date" to "目標日を選択",
        "base_date" to "開始日",
        "target_date" to "目標日",
        "today" to "今日",
        "yesterday" to "昨日",
        "plus_1w" to "+1週",
        "minus_1w" to "-1週",
        "workday" to "稼働日",
        "natural_day" to "暦日",
        "mode_forward" to "日数加減",
        "mode_reverse" to "期間内訳",
        "multi_stage_btn" to "複数段階計算",
        "calc_rule_title" to "計算ルール",
        "add_stage_btn" to "段階追加",
        "export_csv_btn" to "CSV出力",
        "common_countdown" to "よく使うカウントダウン",
        "fixed_countdown" to "固定カウントダウン",
        "add_countdown" to "カスタムカウントダウン追加",
        "solar" to "新暦",
        "lunar" to "旧暦",
        "birth_date_label" to "生年月日",
        "select_birth_date" to "生年月日を選択",
        "exact_age" to "正確な年齢",
        "age_result" to "正確な年齢の内訳",
        "years_unit" to "歳",
        "months_unit" to "ヶ月",
        "weeks_unit" to "週間",
        "days_unit" to "日",
        "total_days" to "総生存日数",
        "total_weeks" to "総生存週数",
        "next_birthday_days" to "次の誕生日まで",
        "zodiac_sign" to "十二支",
        "constellation" to "星座",
        "days_until_prefix" to "まで",
        "days_until_suffix" to "あと",
        "lucky_number" to "ラッキーナンバー",
        "lucky_color" to "ラッキーカラー",
        "forecast_title" to "天気予報",
        "add_anniversary" to "記念日追加",
        "check_in" to "チェックイン",
        "save_anniversary" to "記念日保存",
        "anniversary_name" to "タイトル",
        "anniversary_date" to "日付",
        "dark_mode_system" to "システムに追従",
        "dark_mode_on" to "オン",
        "dark_mode_off" to "オフ",
        "workday_chip" to "稼働日",
        "weekend_chip" to "休日",
        "result_title_workday" to "稼働日計算結果",
        "result_title_natural" to "暦日計算結果",
        "cancel" to "キャンセル",
        "confirm" to "確定",
        "export_csv" to "CSV出力",
        "close_details" to "閉じる",
        "edit_history_title" to "履歴名を変更",
        "enter_new_history_title" to "新しい名前を入力:",
        "save_title" to "保存",
        "reverse_end_date_title" to "終了日を選択して期間の内訳を計算:",
        "end_date_label" to "終了日",
        "anniversary_dialog_title" to "記念日追加",
        "check_in_dialog_title" to "位置チェックイン",
        "confirm_delete_anniversary" to "記念日を削除しますか？",
        "confirm_delete_btn" to "削除",
        "days_passed" to "経過日数",
        "days_upcoming" to "残り日数",
        "next_anniversary_remains" to "次の周年まであと",
        "stage_remark_hint" to "メモ (例: 準備 / 最初の目的地)",
        "timeline_title" to "タイムライン概要",
        "total_duration" to "総所要期間",
        "weekend_rest" to "週末休日",
        "statutory_holiday" to "祝日",
        "stage_add_label" to "加算",
        "stage_sub_label" to "減算",
        "rule_weekend_title" to "週末ルール",
        "rule_five_days" to "完全週休2日 (土日休み)",
        "rule_big_small_weeks" to "隔週週休2日",
        "rule_six_days_sunday" to "週休1日 (日曜日休み)",
        "rule_six_days_saturday" to "週休1日 (土曜日休み)",
        "rule_seven_days" to "休みなし (7日連続稼働)",
        "rule_five_days_desc" to "月～金が稼働日、土日が休日",
        "rule_big_small_weeks_desc" to "隔週で土曜日が出勤日",
        "rule_six_days_sunday_desc" to "月～土が稼働日、日曜日が休日",
        "rule_six_days_saturday_desc" to "日～金が稼働日、土曜日が休日",
        "rule_seven_days_desc" to "全7日すべてが稼働日",
        "no_anniversary_record" to "❤️ 保存された記念日はありません",
        "add_anniversary_hint" to "上の「記念日追加」または「チェックイン」をタップ",
        "fortune_suffix" to "今日の運勢",
        "custom_btn" to "カスタム",
        "yi_label" to "吉",
        "ji_label" to "凶",
        "lunar_to_solar_title" to "新暦 ➔ 旧暦",
        "solar_to_lunar_title" to "旧暦 ➔ 新暦",
        "lunar_convert_title" to "旧暦・新暦変換",
        "custom_color" to "カスタムカラー",
        "palette_title" to "カラーパレット (18色):"
    )

    private val stringsKo = mapOf(
        "app_title" to "날짜 계산기",
        "tab_home" to "홈",
        "tab_calc" to "날짜 계산",
        "tab_countdown" to "디데이",
        "tab_anniversary" to "기념일",
        "tab_lunar" to "음력 변환",
        "tab_age" to "나이 계산",
        "settings_language" to "언어 설정",
        "settings_theme" to "테마 컬러",
        "settings_dark_mode" to "다크 모드",
        "settings_home_config" to "홈 카드 표시 설정",
        "settings_region" to "국가 / 지역 선택",
        "settings_gps_auto" to "GPS 자동 위치 감지",
        "settings_sync_holidays" to "최신 공휴일 데이터 동기화",
        "home_show_screen" to "홈 화면 표시",
        "home_calendar" to "달력 보기",
        "home_almanac" to "오늘의 운세 및 길흉",
        "home_solar_terms" to "24절기",
        "home_lunar" to "음력 정보",
        "home_zodiac" to "별자리 운세",
        "home_weather" to "실시간 날씨",
        "history_title" to "계산 기록",
        "history_empty" to "계산 기록이 없습니다",
        "history_clear" to "전체 기록 삭제",
        "settings_title" to "설정",
        "select_start_date" to "시작일 선택",
        "select_target_date" to "목표일 선택",
        "base_date" to "시작일",
        "target_date" to "목표일",
        "today" to "오늘",
        "yesterday" to "어제",
        "plus_1w" to "+1주",
        "minus_1w" to "-1주",
        "workday" to "근무일",
        "natural_day" to "달력일",
        "mode_forward" to "일수 계산",
        "mode_reverse" to "기간 분석",
        "multi_stage_btn" to "다단계 계산",
        "calc_rule_title" to "계산 규칙",
        "add_stage_btn" to "단계 추가",
        "export_csv_btn" to "CSV 내보내기",
        "common_countdown" to "자주 쓰는 디데이",
        "fixed_countdown" to "고정 디데이",
        "add_countdown" to "사용자 디데이 추가",
        "solar" to "양력",
        "lunar" to "음력",
        "birth_date_label" to "생년월일",
        "select_birth_date" to "생년월일 선택",
        "exact_age" to "정확한 나이",
        "age_result" to "정확한 나이 내역",
        "years_unit" to "세",
        "months_unit" to "개월",
        "weeks_unit" to "주",
        "days_unit" to "일",
        "total_days" to "총 살아온 일수",
        "total_weeks" to "총 살아온 주수",
        "next_birthday_days" to "다음 생일까지",
        "zodiac_sign" to "띠",
        "constellation" to "별자리",
        "days_until_prefix" to "까지",
        "days_until_suffix" to "남음",
        "lucky_number" to "행운의 숫자",
        "lucky_color" to "행운의 색상",
        "forecast_title" to "날씨 예보",
        "add_anniversary" to "기념일 추가",
        "check_in" to "체크인",
        "save_anniversary" to "기념일 저장",
        "anniversary_name" to "제목",
        "anniversary_date" to "날짜",
        "dark_mode_system" to "시스템 기본값",
        "dark_mode_on" to "켜기",
        "dark_mode_off" to "끄기",
        "workday_chip" to "근무일",
        "weekend_chip" to "휴일",
        "result_title_workday" to "근무일 계산 결과",
        "result_title_natural" to "달력일 계산 결과",
        "cancel" to "취소",
        "confirm" to "확인",
        "export_csv" to "CSV 내보내기",
        "close_details" to "닫기",
        "edit_history_title" to "기록 이름 변경",
        "enter_new_history_title" to "새 이름을 입력하세요:",
        "save_title" to "저장",
        "reverse_end_date_title" to "종료일을 선택하여 기간을 분석합니다:",
        "end_date_label" to "종료일",
        "anniversary_dialog_title" to "기념일 추가",
        "check_in_dialog_title" to "위치 체크인",
        "confirm_delete_anniversary" to "기념일을 삭제하시겠습니까?",
        "confirm_delete_btn" to "삭제",
        "days_passed" to "함께한 일수",
        "days_upcoming" to "남은 일수",
        "next_anniversary_remains" to "다음 주기까지 남은 일수",
        "stage_remark_hint" to "메모 (예: 준비 / 첫 목적지)",
        "timeline_title" to "타임라인 개요",
        "total_duration" to "총 소요 기간",
        "weekend_rest" to "주말 휴일",
        "statutory_holiday" to "공휴일",
        "stage_add_label" to "더하기",
        "stage_sub_label" to "빼기",
        "rule_weekend_title" to "주말 규칙",
        "rule_five_days" to "주 5일 근무 (토/일 휴무)",
        "rule_big_small_weeks" to "격주 휴무",
        "rule_six_days_sunday" to "주 6일 근무 (일요일 휴무)",
        "rule_six_days_saturday" to "주 6일 근무 (토요일 휴무)",
        "rule_seven_days" to "휴무 없음 (7일 근무)",
        "rule_five_days_desc" to "월~금 근무일, 토~일 휴일",
        "rule_big_small_weeks_desc" to "격주로 토요일 근무",
        "rule_six_days_sunday_desc" to "월~토 근무일, 일요일 휴일",
        "rule_six_days_saturday_desc" to "일~금 근무일, 토요일 휴일",
        "rule_seven_days_desc" to "7일 모두 근무일",
        "no_anniversary_record" to "❤️ 저장된 기념일이 없습니다",
        "add_anniversary_hint" to "상단의 '기념일 추가' 또는 '체크인'을 누르세요",
        "fortune_suffix" to "오늘의 운세",
        "custom_btn" to "사용자 정의",
        "yi_label" to "길",
        "ji_label" to "흉",
        "lunar_to_solar_title" to "양력 ➔ 음력",
        "solar_to_lunar_title" to "음력 ➔ 양력",
        "lunar_convert_title" to "음력·양력 변환",
        "custom_color" to "사용자 정의 색상",
        "palette_title" to "팔레트 프셋 (18가지 색상):"
    )

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
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> rawCondition
            AppLanguage.TRADITIONAL_CHINESE -> when (rawCondition) {
                "晴朗" -> "晴朗"; "多云" -> "多雲"; "阴天" -> "陰天"; "小雨" -> "小雨"; "雷阵雨" -> "雷陣雨"; else -> rawCondition
            }
            AppLanguage.ENGLISH -> when (rawCondition) {
                "晴朗" -> "Sunny"; "多云" -> "Cloudy"; "阴天" -> "Overcast"; "小雨" -> "Light Rain"; "雷阵雨" -> "Thunderstorm"; else -> rawCondition
            }
            AppLanguage.JAPANESE -> when (rawCondition) {
                "晴朗" -> "快晴"; "多云" -> "晴れ時々曇り"; "阴天" -> "くもり"; "小雨" -> "小雨"; "雷阵雨" -> "雷雨"; else -> rawCondition
            }
            AppLanguage.KOREAN -> when (rawCondition) {
                "晴朗" -> "맑음"; "多云" -> "구름조금"; "阴天" -> "흐림"; "小雨" -> "가랑비"; "雷阵雨" -> "뇌우"; else -> rawCondition
            }
            else -> rawCondition
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
