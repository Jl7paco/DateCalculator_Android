package me.paco.datecalculator.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class HolidayRegion(
    val code: String,
    val label: String,
    val nativeName: String,
    val flagEmoji: String,
    val description: String
) {
    CHINA("CN", "中国大陆", "中国大陆", "🇨🇳", "含法定节假日及调休补班"),
    TAIWAN("TW", "台湾（中国）", "台湾（中国）", "🇹🇼", "含法定节假日及补假/补班安排"),
    HONG_KONG("HK", "中国香港", "香港", "🇭🇰", "含公众假期与补假安排"),
    MACAO("MO", "中国澳门", "澳門", "🇲🇴", "含强制性假日及公众假期"),
    SINGAPORE("SG", "新加坡", "Singapore", "🇸🇬", "含新加坡法定公众假期"),
    MALAYSIA("MY", "马来西亚", "Malaysia", "🇲🇾", "含马来西亚全国法定公众假期"),
    VIETNAM("VN", "越南", "Việt Nam", "🇻🇳", "含越南法定节假日及补假"),
    JAPAN("JP", "日本", "日本", "🇯🇵", "含国民之祝日与振替休日"),
    SOUTH_KOREA("KR", "韩国", "대한민국", "🇰🇷", "含公休日与替代公休日"),
    UNITED_KINGDOM("GB", "英国", "United Kingdom", "🇬🇧", "含英国 Bank Holidays 法定假期"),
    GERMANY("DE", "德国", "Deutschland", "🇩🇪", "含德国全国及联邦州法定节假日"),
    FRANCE("FR", "法国", "France", "🇫🇷", "含法国法定公众假期 (Jours Fériés)"),
    ITALY("IT", "意大利", "Italia", "🇮🇹", "含意大利法定公众假期 (Giorni Festivi)"),
    INDIA("IN", "印度", "India", "🇮🇳", "含印度全国及各邦法定节假日"),
    INDONESIA("ID", "印尼", "Indonesia", "🇮🇩", "含印尼全国法定公众假期 (Hari Libur)"),
    AUSTRALIA("AU", "澳大利亚", "Australia", "🇦🇺", "含澳大利亚全国及州法定公众假期"),
    NEW_ZEALAND("NZ", "新西兰", "New Zealand", "🇳🇿", "含新西兰全国法定公众假期"),
    UNITED_STATES("US", "美国", "United States", "🇺🇸", "含联邦法定节假日 (Federal Holidays)"),
    THAILAND("TH", "泰国", "ประเทศไทย", "🇹🇭", "含泰国法定公众假期及补假");

    fun getLocalizedName(language: AppLanguage): String {
        if (language == AppLanguage.SIMPLIFIED_CHINESE) return label
        if (language == AppLanguage.TRADITIONAL_CHINESE) {
            return when (this) {
                CHINA -> "中國大陸"
                TAIWAN -> "台灣（中國）"
                HONG_KONG -> "中國香港"
                MACAO -> "中國澳門"
                SINGAPORE -> "新加坡"
                JAPAN -> "日本"
                SOUTH_KOREA -> "韓國"
                UNITED_STATES -> "美國"
                UNITED_KINGDOM -> "英國"
                GERMANY -> "德國"
                FRANCE -> "法國"
                ITALY -> "義大利"
                AUSTRALIA -> "澳大利亞"
                NEW_ZEALAND -> "紐西蘭"
                else -> label
            }
        }
        return when (this) {
            CHINA -> "Mainland China"
            TAIWAN -> "Taiwan"
            HONG_KONG -> "Hong Kong"
            MACAO -> "Macau"
            SINGAPORE -> "Singapore"
            MALAYSIA -> "Malaysia"
            VIETNAM -> "Vietnam"
            JAPAN -> "Japan"
            SOUTH_KOREA -> "South Korea"
            UNITED_KINGDOM -> "United Kingdom"
            GERMANY -> "Germany"
            FRANCE -> "France"
            ITALY -> "Italy"
            INDIA -> "India"
            INDONESIA -> "Indonesia"
            AUSTRALIA -> "Australia"
            NEW_ZEALAND -> "New Zealand"
            UNITED_STATES -> "United States"
            THAILAND -> "Thailand"
        }
    }
}

enum class WeekendRule(val label: String, val description: String) {
    STANDARD_FIVE_DAYS("双休 (周六日休息)", "每周一至周五为工作日，周六周日休息"),
    ALTERNATE_BIG_SMALL_WEEKS("大小周 (单双休轮替)", "一周单休 (仅周日休)，次周双休 (周六日休)，隔周轮替"),
    SIX_DAYS_SUNDAY("单休 (仅周日休息)", "每周一至周六为工作日，仅周日休息"),
    SIX_DAYS_SATURDAY("单休 (仅周六休息)", "每周日及周一至周五为工作日，仅周六休息"),
    SEVEN_DAYS("无休 (七天工作)", "一周七天均为工作日，不计周末");

    fun isWeekend(date: LocalDate, isCurrentWeekBigWeek: Boolean = true): Boolean {
        val dayOfWeek = date.dayOfWeek
        return when (this) {
            STANDARD_FIVE_DAYS -> dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            ALTERNATE_BIG_SMALL_WEEKS -> {
                if (dayOfWeek == DayOfWeek.SUNDAY) return true
                if (dayOfWeek == DayOfWeek.SATURDAY) {
                    val currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY)
                    val targetWeekStart = date.with(DayOfWeek.MONDAY)
                    val weeksDiff = ChronoUnit.WEEKS.between(currentWeekStart, targetWeekStart)
                    val isBigWeek = if (isCurrentWeekBigWeek) {
                        abs(weeksDiff) % 2L == 0L
                    } else {
                        abs(weeksDiff) % 2L == 1L
                    }
                    !isBigWeek
                } else false
            }
            SIX_DAYS_SUNDAY -> dayOfWeek == DayOfWeek.SUNDAY
            SIX_DAYS_SATURDAY -> dayOfWeek == DayOfWeek.SATURDAY
            SEVEN_DAYS -> false
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

enum class ThemeColorPreset(val label: String, val primaryColorHex: Long) {
    SYSTEM("跟随系统", 0xFF2563EB),
    CUSTOM("自定义色彩", 0xFF2563EB)
}

/**
 * 深色/黑暗模式选择配置 (更名："开启"、"关闭")
 */
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
    SIMPLIFIED_CHINESE("zh_CN", "简体中文", "简体中文", "zh-CN"),
    TRADITIONAL_CHINESE("zh_TW", "繁体中文", "繁體中文", "zh-TW"),
    ENGLISH("en", "英语", "English", "en"),
    JAPANESE("ja", "日语", "日本語", "ja"),
    KOREAN("ko", "韩语", "한국어", "ko");

    val isChineseLocale: Boolean
        get() = this == SIMPLIFIED_CHINESE || this == TRADITIONAL_CHINESE
}

data class HomeConfig(
    val showHomeScreen: Boolean = true,
    val showCalendar: Boolean = true,
    val showAlmanac: Boolean = true,
    val showSolarTerms: Boolean = true,
    val showLunar: Boolean = true,
    val showZodiacFortune: Boolean = true,
    val showWeather: Boolean = true
)

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
    val nextBirthdayDate: LocalDate,
    val zodiac: String,
    val constellation: String,
    val constellationEmoji: String
)

data class ZodiacFortune(
    val constellation: String,
    val emoji: String,
    val dateRange: String,
    val overallScore: Int,
    val starRating: Int,
    val careerScore: Int,
    val careerDesc: String,
    val wealthScore: Int,
    val wealthDesc: String,
    val loveScore: Int,
    val loveDesc: String,
    val healthScore: Int,
    val healthDesc: String,
    val luckyNumber: Int,
    val luckyColor: String,
    val luckyConstellation: String,
    val luckyDirection: String,
    val summary: String,
    val yi: String,
    val ji: String
)

/**
 * 重要纪念日与打卡模型 (最高存储 999 个卡片，支持记录经纬度、地点与精准打卡时间)
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

data class CalculationStage(
    val id: Long = System.nanoTime(),
    var type: CalculationType = CalculationType.ADD,
    var daysInput: String = "",
    var remark: String = ""
) {
    val days: Long
        get() = daysInput.toLongOrNull() ?: 15L
}

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

data class HistoryItem(
    val id: Long = System.nanoTime(),
    val category: String = "日期计算",
    val title: String,
    val detail: String,
    val regionTag: String = "🇨🇳 中国大陆",
    val resultDate: LocalDate? = null,
    val resultDays: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
