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
    THAILAND("TH", "泰国", "ประเทศไทย", "🇹🇭", "含泰国法定公众假期及补假")
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

/**
 * 链式多阶段计算阶段模型 (daysInput 默认空串以实现灰色 15 占位符，默认天数为 15L)
 */
data class CalculationStage(
    val id: Long = System.nanoTime(),
    var type: CalculationType = CalculationType.ADD,
    var daysInput: String = "",
    var remark: String = ""
) {
    val days: Long
        get() = daysInput.toLongOrNull() ?: 15L
}

/**
 * 阶段节点推算结果模型 (供线性时间轴渲染与 CSV 导出)
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
