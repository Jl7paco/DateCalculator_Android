package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage

object LanguageUtils {

    fun getString(key: String, language: AppLanguage): String {
        val stringsZhCn = mapOf(
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
            "settings_home_config" to "首页功能显示与显隐设置",
            "home_show_screen" to "显示首页",
            "home_calendar" to "月历",
            "home_almanac" to "当日黄历",
            "home_solar_terms" to "二十四节气",
            "home_lunar" to "农历日期",
            "home_zodiac" to "星座与运势",
            "home_weather" to "天气预报",
            "workday" to "工作日",
            "natural_day" to "自然日",
            "solar" to "公历",
            "lunar" to "农历",
            "target_date" to "目标日期",
            "base_date" to "起始日期",
            "settings_title" to "系统设置",
            "history_title" to "历史记录",
            "add_countdown" to "+ 新增倒数日",
            "common_countdown" to "常用倒数日",
            "fixed_countdown" to "固定倒数日"
        )

        val stringsZhTw = mapOf(
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
            "home_show_screen" to "顯示首頁",
            "home_calendar" to "月曆",
            "home_almanac" to "當日黃曆",
            "home_solar_terms" to "二十四節氣",
            "home_lunar" to "農曆日期",
            "home_zodiac" to "星座與運勢",
            "home_weather" to "天氣預報",
            "workday" to "工作日",
            "natural_day" to "自然日",
            "solar" to "公曆",
            "lunar" to "農曆",
            "target_date" to "目標日期",
            "base_date" to "起始日期",
            "settings_title" to "系統設定",
            "history_title" to "歷史記錄",
            "add_countdown" to "+ 新增倒數日",
            "common_countdown" to "常用倒數日",
            "fixed_countdown" to "固定倒數日"
        )

        val stringsEn = mapOf(
            "app_title" to "Date Calculator",
            "tab_home" to "Home",
            "tab_calc" to "Date Calc",
            "tab_countdown" to "Countdown",
            "tab_anniversary" to "Anniversary",
            "tab_lunar" to "Lunar Conv",
            "tab_age" to "Age Calc",
            "settings_language" to "Language Settings",
            "settings_theme" to "Theme Color Scheme",
            "settings_dark_mode" to "Dark Mode",
            "settings_home_config" to "Home Screen Display Items",
            "home_show_screen" to "Show Home Screen",
            "home_calendar" to "Monthly Calendar",
            "home_almanac" to "Almanac",
            "home_solar_terms" to "Solar Terms",
            "home_lunar" to "Lunar Date",
            "home_zodiac" to "Horoscope",
            "home_weather" to "Weather Forecast",
            "workday" to "Workday",
            "natural_day" to "Calendar Day",
            "solar" to "Gregorian",
            "lunar" to "Lunar",
            "target_date" to "Target Date",
            "base_date" to "Start Date",
            "settings_title" to "Settings",
            "history_title" to "History",
            "add_countdown" to "+ Add Countdown",
            "common_countdown" to "Preset Countdowns",
            "fixed_countdown" to "Pinned Countdowns"
        )

        val stringsJa = mapOf(
            "app_title" to "日付電卓",
            "tab_home" to "ホーム",
            "tab_calc" to "日付計算",
            "tab_countdown" to "カウントダウン",
            "tab_anniversary" to "記念日",
            "tab_lunar" to "旧暦変換",
            "tab_age" to "年齢計算",
            "settings_language" to "言語設定",
            "settings_theme" to "テーマ配色",
            "settings_dark_mode" to "ダークモード",
            "settings_home_config" to "ホーム表示項目設定",
            "home_show_screen" to "ホーム画面を表示",
            "home_calendar" to "月カレンダー",
            "home_almanac" to "暦宜忌",
            "home_solar_terms" to "二十四節気",
            "home_lunar" to "旧暦日付",
            "home_zodiac" to "星座と運勢",
            "home_weather" to "天気予報",
            "workday" to "稼働日",
            "natural_day" to "自然日",
            "solar" to "新暦",
            "lunar" to "旧暦",
            "target_date" to "目標日付",
            "base_date" to "開始日付",
            "settings_title" to "設定",
            "history_title" to "履歴",
            "add_countdown" to "+ カウントダウン追加",
            "common_countdown" to "プリセット",
            "fixed_countdown" to "固定カウントダウン"
        )

        val stringsKo = mapOf(
            "app_title" to "날짜 계산기",
            "tab_home" to "홈",
            "tab_calc" to "날짜 계산",
            "tab_countdown" to "디데이",
            "tab_anniversary" to "기념일",
            "tab_lunar" to "음력 변환",
            "tab_age" to "나이 계산",
            "settings_language" to "언어 설정",
            "settings_theme" to "테마 색상",
            "settings_dark_mode" to "다크 모드",
            "settings_home_config" to "홈 화면 표시 항목 설정",
            "home_show_screen" to "홈 화면 표시",
            "home_calendar" to "달력",
            "home_almanac" to "황력",
            "home_solar_terms" to "24절기",
            "home_lunar" to "음력 날짜",
            "home_zodiac" to "별자리 운세",
            "home_weather" to "날씨 예보",
            "workday" to "근무일",
            "natural_day" to "자연일",
            "solar" to "양력",
            "lunar" to "음력",
            "target_date" to "목표 날짜",
            "base_date" to "시작 날짜",
            "settings_title" to "설정",
            "history_title" to "히스토리",
            "add_countdown" to "+ 디데이 추가",
            "common_countdown" to "추천 디데이",
            "fixed_countdown" to "고정 디데이"
        )

        val map = when (language) {
            AppLanguage.SIMPLIFIED_CHINESE -> stringsZhCn
            AppLanguage.TRADITIONAL_CHINESE -> stringsZhTw
            AppLanguage.ENGLISH -> stringsEn
            AppLanguage.JAPANESE -> stringsJa
            AppLanguage.KOREAN -> stringsKo
        }

        return map[key] ?: stringsZhCn[key] ?: key
    }
}
