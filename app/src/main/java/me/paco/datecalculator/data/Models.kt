package me.paco.datecalculator.data

import java.time.LocalDate
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * 年份类型：0 = 公历, 1 = 农历
 */
enum class CalendarType(val label: String) {
    SOLAR("公历"),
    LUNAR("农历")
}

/**
 * 节假日地区类型，多语言智能适配
 */
enum class HolidayRegion(
    val code: String,
    val flagEmoji: String,
    val nativeName: String,
    val englishName: String
) {
    CHINA("CN", "🇨🇳", "中国大陆", "China Mainland"),
    TAIWAN("TW", "🇹🇼", "中国台湾", "Taiwan"),
    HONG_KONG("HK", "🇭🇰", "中国香港", "Hong Kong SAR"),
    MACAO("MO", "🇲🇴", "中国澳门", "Macao SAR"),
    SINGAPORE("SG", "🇸🇬", "新加坡", "Singapore"),
    MALAYSIA("MY", "🇲🇾", "马来西亚", "Malaysia"),
    VIETNAM("VN", "🇻🇳", "越南", "Vietnam"),
    JAPAN("JP", "🇯🇵", "日本", "Japan"),
    SOUTH_KOREA("KR", "🇰🇷", "韩国", "South Korea"),
    UNITED_KINGDOM("GB", "🇬🇧", "英国", "United Kingdom"),
    GERMANY("DE", "🇩🇪", "德国", "Germany"),
    FRANCE("FR", "🇫🇷", "法国", "France"),
    ITALY("IT", "🇮🇹", "意大利", "Italy"),
    INDIA("IN", "🇮🇳", "印度", "India"),
    INDONESIA("ID", "🇮🇩", "印度尼西亚", "Indonesia"),
    AUSTRALIA("AU", "🇦🇺", "澳大利亚", "Australia"),
    NEW_ZEALAND("NZ", "🇳🇿", "新西兰", "New Zealand"),
    UNITED_STATES("US", "🇺🇸", "美国", "United States"),
    THAILAND("TH", "🇹🇭", "泰国", "Thailand");

    fun getLocalizedName(language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> nativeName
            AppLanguage.TRADITIONAL_CHINESE -> when (this) {
                CHINA -> "中國大陸"; TAIWAN -> "中國台灣"; HONG_KONG -> "中國香港"; MACAO -> "中國澳門"
                SINGAPORE -> "新加坡"; MALAYSIA -> "馬來西亞"; VIETNAM -> "越南"; JAPAN -> "日本"; SOUTH_KOREA -> "韓國"
                UNITED_KINGDOM -> "英國"; GERMANY -> "德國"; FRANCE -> "法國"; ITALY -> "義大利"; INDIA -> "印度"
                INDONESIA -> "印尼"; AUSTRALIA -> "澳洲"; NEW_ZEALAND -> "紐西蘭"; UNITED_STATES -> "美國"; THAILAND -> "泰國"
            }
            AppLanguage.ENGLISH -> englishName
            AppLanguage.JAPANESE -> when (this) {
                CHINA -> "中国本土"; TAIWAN -> "台湾"; HONG_KONG -> "香港"; MACAO -> "マカオ"
                SINGAPORE -> "シンガポール"; MALAYSIA -> "マレーシア"; VIETNAM -> "ベトナム"; JAPAN -> "日本"; SOUTH_KOREA -> "韓国"
                UNITED_KINGDOM -> "イギリス"; GERMANY -> "ドイツ"; FRANCE -> "フランス"; ITALY -> "イタリア"; INDIA -> "インド"
                INDONESIA -> "インドネシア"; AUSTRALIA -> "オーストラリア"; NEW_ZEALAND -> "ニュージーランド"; UNITED_STATES -> "アメリカ"; THAILAND -> "タイ"
            }
            AppLanguage.KOREAN -> when (this) {
                CHINA -> "중국 본토"; TAIWAN -> "대만"; HONG_KONG -> "홍콩"; MACAO -> "마카오"
                SINGAPORE -> "싱가포르"; MALAYSIA -> "말레이시아"; VIETNAM -> "베트남"; JAPAN -> "일본"; SOUTH_KOREA -> "대한민국"
                UNITED_KINGDOM -> "영국"; GERMANY -> "독일"; FRANCE -> "프랑스"; ITALY -> "이탈리아"; INDIA -> "인도"
                INDONESIA -> "인도네시아"; AUSTRALIA -> "호주"; NEW_ZEALAND -> "뉴질랜드"; UNITED_STATES -> "미국"; THAILAND -> "태국"
            }
            else -> nativeName
        }
    }
}

/**
 * 周末休息规则模式
 */
enum class WeekendRule(val label: String) {
    STANDARD_FIVE_DAYS("双休 (周六日休息)"),
    ALTERNATE_BIG_SMALL_WEEKS("大小周 (单双休轮替)"),
    SIX_DAYS_SUNDAY("单休 (仅周日休息)"),
    SIX_DAYS_SATURDAY("单休 (仅周六休息)"),
    SEVEN_DAYS("无休 (七天工作)");

    fun isWeekend(date: LocalDate, isCurrentWeekBigWeek: Boolean = true): Boolean {
        val dayOfWeek = date.dayOfWeek.value // 1 = Mon, 7 = Sun
        return when (this) {
            STANDARD_FIVE_DAYS -> dayOfWeek == 6 || dayOfWeek == 7
            SIX_DAYS_SUNDAY -> dayOfWeek == 7
            SIX_DAYS_SATURDAY -> dayOfWeek == 6
            SEVEN_DAYS -> false
            ALTERNATE_BIG_SMALL_WEEKS -> {
                if (dayOfWeek == 7) return true
                if (dayOfWeek == 6) {
                    val weekOfYear = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
                    val isBigWeekNow = (weekOfYear % 2 != 0) == isCurrentWeekBigWeek
                    return isBigWeekNow
                }
                false
            }
        }
    }
}

enum class CalculationType(val symbol: String, val label: String) {
    ADD("+", "加天数"),
    SUBTRACT("-", "减天数")
}

enum class DateMode(val label: String) {
    WORKDAY("工作日"),
    NATURAL_DAY("自然日")
}

/**
 * 主题颜色预设方案 (保持原有配方，新增 4 款高质感颜色)
 */
enum class ThemeColorPreset(val label: String, val primaryColorHex: Long) {
    SYSTEM("跟随系统", 0xFF2563EB),
    OCEAN("深海蓝", 0xFF0284C7),
    EMERALD("薄荷绿", 0xFF10B981),
    AMBER("琥珀金", 0xFFD97706),
    ROSE("玫瑰红", 0xFFE11D48),
    CUSTOM("自定义色彩", 0xFF2563EB);

    fun getLocalizedName(language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> label
            AppLanguage.TRADITIONAL_CHINESE -> when (this) {
                SYSTEM -> "跟隨系統"; OCEAN -> "深海藍"; EMERALD -> "薄荷綠"; AMBER -> "琥珀金"; ROSE -> "玫瑰紅"; CUSTOM -> "自定義色彩"
            }
            AppLanguage.ENGLISH -> when (this) {
                SYSTEM -> "System Default"; OCEAN -> "Deep Ocean"; EMERALD -> "Emerald Green"; AMBER -> "Amber Gold"; ROSE -> "Rose Red"; CUSTOM -> "Custom Color"
            }
            AppLanguage.JAPANESE -> when (this) {
                SYSTEM -> "システムに従う"; OCEAN -> "ディープブルー"; EMERALD -> "エメラルド"; AMBER -> "アンバーゴールド"; ROSE -> "ローズレッド"; CUSTOM -> "カスタム"
            }
            AppLanguage.KOREAN -> when (this) {
                SYSTEM -> "시스템 기본값"; OCEAN -> "딥 오션"; EMERALD -> "에메랄드 그린"; AMBER -> "앰버 골드"; ROSE -> "로즈 레드"; CUSTOM -> "사용자 정의"
            }
            else -> label
        }
    }
}

enum class DarkThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    ON("开启"),
    OFF("关闭")
}

/**
 * 应用多语言支持配置
 */
enum class AppLanguage(
    val code: String,
    val label: String,
    val nativeName: String,
    val localeTag: String
) {
    SYSTEM("system", "跟随系统", "跟随系统", "system"),
    SIMPLIFIED_CHINESE("zh_CN", "简体中文", "简体中文", "zh-CN"),
    TRADITIONAL_CHINESE("zh_TW", "繁体中文", "繁體中文", "zh-TW"),
    ENGLISH("en", "英语", "English", "en"),
    JAPANESE("ja", "日语", "日本語", "ja"),
    KOREAN("ko", "韩语", "한국어", "ko");

    fun getEffectiveLanguage(): AppLanguage {
        if (this != SYSTEM) return this
        val systemLocale = Locale.getDefault()
        val langCode = systemLocale.language.lowercase()
        val script = systemLocale.script.lowercase()
        val country = systemLocale.country.uppercase()

        return when {
            langCode == "zh" -> {
                if (script == "hant" || country == "TW" || country == "HK" || country == "MO") {
                    TRADITIONAL_CHINESE
                } else {
                    SIMPLIFIED_CHINESE
                }
            }
            langCode == "en" -> ENGLISH
            langCode == "ja" -> JAPANESE
            langCode == "ko" -> KOREAN
            else -> SIMPLIFIED_CHINESE
        }
    }

    val isChineseLocale: Boolean
        get() {
            val eff = getEffectiveLanguage()
            return eff == SIMPLIFIED_CHINESE || eff == TRADITIONAL_CHINESE
        }
}

/**
 * 首页功能模块卡片显隐个性化配置数据模型
 */
data class HomeConfig(
    val showHomeScreen: Boolean = true,
    val showCalendar: Boolean = true,
    val showAlmanac: Boolean = true,
    val showSolarTerms: Boolean = true,
    val showLunar: Boolean = true,
    val showZodiacFortune: Boolean = true,
    val showWeather: Boolean = true
)

/**
 * 多阶段推算单个步骤定义
 */
data class CalculationStage(
    val id: Long = System.nanoTime(),
    val type: CalculationType = CalculationType.ADD,
    val daysInput: String = "10",
    val remark: String = ""
) {
    val days: Long
        get() = daysInput.toLongOrNull() ?: 0L
}

/**
 * 多阶段推算计算出的单个时间段推算结果
 */
data class StageSegmentResult(
    val stageIndex: Int,
    val remark: String,
    val type: CalculationType,
    val daysCount: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalCalendarDays: Long,
    val restDaysCount: Long
)

/**
 * 重要纪念日数据模型
 */
data class AnniversaryItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate,
    val iconEmoji: String = "❤️",
    val isCheckIn: Boolean = false,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val checkInTimeStr: String = "",
    val remark: String = "",
    val isPinned: Boolean = false
) {
    fun getNextUpcomingDate(baseDate: LocalDate = LocalDate.now()): LocalDate {
        var upcoming = date
        while (upcoming.isBefore(baseDate)) {
            upcoming = upcoming.plusYears(1)
        }
        return upcoming
    }
}

/**
 * 历史记录数据模型
 */
data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val category: String,
    val title: String,
    val detail: String,
    val regionTag: String = "🇨🇳 中国大陆",
    val timestamp: Long = System.currentTimeMillis(),
    val resultDate: LocalDate? = null,
    val resultDays: Long? = null
)

/**
 * 精确年龄计算结果
 */
data class AgeResult(
    val birthDate: LocalDate,
    val targetDate: LocalDate = LocalDate.now(),
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalMonths: Long,
    val totalWeeks: Long,
    val daysToNextBirthday: Long,
    val nextBirthdayDate: LocalDate = LocalDate.now(),
    val zodiac: String,
    val constellation: String,
    val constellationEmoji: String = "⭐"
)

/**
 * 星座运势数据
 */
data class ZodiacFortune(
    val constellation: String,
    val emoji: String = "⭐",
    val dateRange: String,
    val overallScore: Int = 85,
    val starRating: Int,
    val careerScore: Int = 80,
    val careerDesc: String = "",
    val wealthScore: Int = 80,
    val wealthDesc: String = "",
    val loveScore: Int = 80,
    val loveDesc: String = "",
    val healthScore: Int = 80,
    val healthDesc: String = "",
    val luckyNumber: Int,
    val luckyColor: String,
    val luckyConstellation: String = "",
    val luckyDirection: String = "",
    val summary: String = "",
    val yi: String = "",
    val ji: String = ""
)

enum class TimelineBlockType {
    WORKDAY,
    WEEKEND,
    STATUTORY_HOLIDAY,
    SHIFT_WORKDAY
}

data class ChronologicalBlock(
    val type: TimelineBlockType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val daysCount: Int
)

fun decomposeChronologicalBlocks(
    startDate: LocalDate,
    endDate: LocalDate,
    weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    enableHolidays: Boolean = true,
    holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    isCurrentWeekBigWeek: Boolean = true,
    disableChinaShiftWorkdays: Boolean = false
): List<ChronologicalBlock> {
    if (startDate == endDate) return emptyList()

    val isForward = startDate.isBefore(endDate)
    val start = if (isForward) startDate else endDate
    val end = if (isForward) endDate else startDate

    val blocks = mutableListOf<ChronologicalBlock>()
    var curr = start.plusDays(1)

    fun getType(d: LocalDate): TimelineBlockType {
        val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(d, holidayRegion)
        val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(d, holidayRegion)
        val isWeekend = weekendRule.isWeekend(d, isCurrentWeekBigWeek)

        return when {
            isShift -> TimelineBlockType.SHIFT_WORKDAY
            isStat -> TimelineBlockType.STATUTORY_HOLIDAY
            isWeekend -> TimelineBlockType.WEEKEND
            else -> TimelineBlockType.WORKDAY
        }
    }

    if (curr.isAfter(end)) return emptyList()

    var blockStart = curr
    var currentType = getType(curr)
    var count = 1

    curr = curr.plusDays(1)
    while (!curr.isAfter(end)) {
        val t = getType(curr)
        if (t == currentType) {
            count++
        } else {
            blocks.add(ChronologicalBlock(currentType, blockStart, curr.minusDays(1), count))
            blockStart = curr
            currentType = t
            count = 1
        }
        curr = curr.plusDays(1)
    }

    blocks.add(ChronologicalBlock(currentType, blockStart, end, count))
    return blocks
}
