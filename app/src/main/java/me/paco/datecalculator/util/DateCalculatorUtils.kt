package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.WeekendRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

enum class TimelineBlockType {
    WORKDAY,          // 工作日 (蓝)
    WEEKEND_REST,     // 周末双休 (琥珀金)
    STATUTORY_HOLIDAY // 法定节假日 (玫瑰红)
}

data class ChronologicalBlock(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: TimelineBlockType,
    val daysCount: Long
)

data class TimelineBlock(
    val type: TimelineBlockType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val daysCount: Long
)

data class RangeBreakdownResult(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalCalendarDays: Long,
    val workdays: Long,
    val weekendDays: Long,
    val statutoryHolidays: Long,
    val shiftWorkdays: Long
)

object DateCalculatorUtils {

    val DATE_FORMATTER_ZH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val DATE_FORMATTER_EN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatDate(
        date: LocalDate,
        language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE
    ): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.TRADITIONAL_CHINESE, AppLanguage.JAPANESE -> date.format(DATE_FORMATTER_ZH)
            AppLanguage.KOREAN -> String.format("%d년 %02d월 %02d일", date.year, date.monthValue, date.dayOfMonth)
            else -> {
                val mName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                String.format("%s %02d, %d", mName, date.dayOfMonth, date.year)
            }
        }
    }

    fun formatDateWithWeek(
        date: LocalDate,
        language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE
    ): String {
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekNum = date.get(weekFields.weekOfWeekBasedYear())

        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> String.format("%d年%02d月%02d日 (第%d周)", date.year, date.monthValue, date.dayOfMonth, weekNum)
            AppLanguage.TRADITIONAL_CHINESE -> String.format("%d年%02d月%02d日 (第%d週)", date.year, date.monthValue, date.dayOfMonth, weekNum)
            AppLanguage.JAPANESE -> String.format("%d年%02d月%02d日 (第%d週)", date.year, date.monthValue, date.dayOfMonth, weekNum)
            AppLanguage.KOREAN -> String.format("%d년 %02d월 %02d일 (%d주차)", date.year, date.monthValue, date.dayOfMonth, weekNum)
            else -> {
                val mName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                String.format("%s %02d, %d (Wk %d)", mName, date.dayOfMonth, date.year, weekNum)
            }
        }
    }

    /**
     * 纯粹与所选 HolidayRegion 独立绑定 (完全不依赖系统语言) 的背景水印巨幕月份标语
     */
    fun getWatermarkMonthHeader(monthValue: Int, region: HolidayRegion): String {
        return when (region) {
            HolidayRegion.CHINA, HolidayRegion.TAIWAN, HolidayRegion.HONG_KONG, HolidayRegion.MACAO, HolidayRegion.JAPAN -> "${monthValue}月"
            HolidayRegion.SOUTH_KOREA -> "${monthValue}월"
            HolidayRegion.VIETNAM -> "Tháng $monthValue"
            HolidayRegion.THAILAND -> when (monthValue) {
                1 -> "ม.ค." ; 2 -> "ก.พ." ; 3 -> "มี.ค." ; 4 -> "เม.ย." ; 5 -> "พ.ค." ; 6 -> "มิ.ย."
                7 -> "ก.ค." ; 8 -> "ส.ค." ; 9 -> "ก.ย." ; 10 -> "ต.ค." ; 11 -> "พ.ย." ; 12 -> "ธ.ค."
                else -> "${monthValue}月"
            }
            HolidayRegion.FRANCE -> "SEPTEMBRE"
            HolidayRegion.ITALY -> "SETTEMBRE"
            else -> when (monthValue) { // 美、英、德、新、马、印、澳、新等欧美及英语系国家切换为原生英文月份单词
                1 -> "JANUARY" ; 2 -> "FEBRUARY" ; 3 -> "MARCH" ; 4 -> "APRIL"
                5 -> "MAY" ; 6 -> "JUNE" ; 7 -> "JULY" ; 8 -> "AUGUST"
                9 -> "SEPTEMBER" ; 10 -> "OCTOBER" ; 11 -> "NOVEMBER" ; 12 -> "DECEMBER"
                else -> "SEPTEMBER"
            }
        }
    }

    fun isWorkday(
        date: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Boolean {
        if (!disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(date, holidayRegion)) {
            return true
        }

        if (enableHolidays && RegionalHolidays.isStatutoryHoliday(date, holidayRegion)) {
            return false
        }

        return !weekendRule.isWeekend(date, isCurrentWeekBigWeek)
    }

    fun decomposeChronologicalBlocks(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true
    ): List<ChronologicalBlock> {
        val isReverse = startDate.isAfter(endDate)
        val start = if (isReverse) endDate else startDate
        val end = if (isReverse) startDate else endDate

        val resultList = mutableListOf<ChronologicalBlock>()
        var curr = start
        var currentType: TimelineBlockType? = null
        var blockStart = start
        var blockDays = 0L

        while (!curr.isAfter(end)) {
            val isShift = RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStatutory = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val type = when {
                isShift -> TimelineBlockType.WORKDAY
                isStatutory -> TimelineBlockType.STATUTORY_HOLIDAY
                isWeekend -> TimelineBlockType.WEEKEND_REST
                else -> TimelineBlockType.WORKDAY
            }

            if (currentType == null) {
                currentType = type
                blockStart = curr
                blockDays = 1
            } else if (currentType == type) {
                blockDays++
            } else {
                resultList.add(
                    ChronologicalBlock(
                        startDate = blockStart,
                        endDate = curr.minusDays(1),
                        type = currentType,
                        daysCount = blockDays
                    )
                )
                currentType = type
                blockStart = curr
                blockDays = 1
            }

            curr = curr.plusDays(1)
        }

        if (currentType != null && blockDays > 0) {
            resultList.add(
                ChronologicalBlock(
                    startDate = blockStart,
                    endDate = end,
                    type = currentType,
                    daysCount = blockDays
                )
            )
        }

        return resultList
    }

    fun calculateTargetDate(
        baseDate: LocalDate,
        days: Int,
        type: CalculationType = CalculationType.ADD,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): LocalDate {
        var result = baseDate
        var remainingDays = days

        if (days == 0) return baseDate

        val step = if (type == CalculationType.ADD) 1L else -1L

        if (!isWorkdayMode) {
            return baseDate.plusDays(step * days)
        }

        while (remainingDays > 0) {
            result = result.plusDays(step)
            if (isWorkday(result, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)) {
                remainingDays--
            }
        }

        return result
    }

    fun naturalDaysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun workdaysBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Long {
        val isReverse = startDate.isAfter(endDate)
        val start = if (isReverse) endDate else startDate
        val end = if (isReverse) startDate else endDate

        var count = 0L
        var curr = start.plusDays(1)

        while (!curr.isAfter(end)) {
            if (isWorkday(curr, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)) {
                count++
            }
            curr = curr.plusDays(1)
        }

        return if (isReverse) -count else count
    }

    fun getDateDescription(
        date: LocalDate,
        language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE
    ): String {
        val eff = language.getEffectiveLanguage()
        val weekDayName = when (eff) {
            AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.TRADITIONAL_CHINESE -> when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "星期一"; DayOfWeek.TUESDAY -> "星期二"; DayOfWeek.WEDNESDAY -> "星期三"
                DayOfWeek.THURSDAY -> "星期四"; DayOfWeek.FRIDAY -> "星期五"; DayOfWeek.SATURDAY -> "星期六"; DayOfWeek.SUNDAY -> "星期日"
            }
            AppLanguage.JAPANESE -> when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "月曜日"; DayOfWeek.TUESDAY -> "火曜日"; DayOfWeek.WEDNESDAY -> "水曜日"
                DayOfWeek.THURSDAY -> "木曜日"; DayOfWeek.FRIDAY -> "金曜日"; DayOfWeek.SATURDAY -> "土曜日"; DayOfWeek.SUNDAY -> "日曜日"
            }
            AppLanguage.KOREAN -> when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "월요일"; DayOfWeek.TUESDAY -> "화요일"; DayOfWeek.WEDNESDAY -> "수요일"
                DayOfWeek.THURSDAY -> "목요일"; DayOfWeek.FRIDAY -> "금요일"; DayOfWeek.SATURDAY -> "토요일"; DayOfWeek.SUNDAY -> "일요일"
            }
            else -> when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"; DayOfWeek.TUESDAY -> "Tuesday"; DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"; DayOfWeek.FRIDAY -> "Friday"; DayOfWeek.SATURDAY -> "Saturday"; DayOfWeek.SUNDAY -> "Sunday"
            }
        }

        val dayOfYear = date.dayOfYear
        val totalDaysInYear = date.lengthOfYear()
        val weekOfWeekBasedYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        val isLeap = date.isLeapYear

        return when (eff) {
            AppLanguage.SIMPLIFIED_CHINESE -> "$weekDayName | 当年第 ${dayOfYear}/${totalDaysInYear} 天 | 第 $weekOfWeekBasedYear 周" + if (isLeap) " (闰年)" else ""
            AppLanguage.TRADITIONAL_CHINESE -> "$weekDayName | 當年第 ${dayOfYear}/${totalDaysInYear} 天 | 第 $weekOfWeekBasedYear 週" + if (isLeap) " (閏年)" else ""
            AppLanguage.JAPANESE -> "$weekDayName | 通算 $dayOfYear/${totalDaysInYear} 日 | 第 $weekOfWeekBasedYear 週" + if (isLeap) " (閏年)" else ""
            AppLanguage.KOREAN -> "$weekDayName | 연중 $dayOfYear/${totalDaysInYear} 일 | $weekOfWeekBasedYear 주차" + if (isLeap) " (윤년)" else ""
            else -> "$weekDayName | Day $dayOfYear/$totalDaysInYear | Wk $weekOfWeekBasedYear" + if (isLeap) " (Leap Year)" else ""
        }
    }

    fun formatPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE
    ): String {
        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        val period = Period.between(start, end)
        val years = period.years
        val months = period.months
        val days = period.days

        val parts = mutableListOf<String>()
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}个月")
                if (days > 0 || parts.isEmpty()) parts.add("${days}天")
                parts.joinToString("")
            }
            AppLanguage.TRADITIONAL_CHINESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}個月")
                if (days > 0 || parts.isEmpty()) parts.add("${days}天")
                parts.joinToString("")
            }
            AppLanguage.JAPANESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}ヶ月")
                if (days > 0 || parts.isEmpty()) parts.add("${days}日")
                parts.joinToString("")
            }
            AppLanguage.KOREAN -> {
                if (years > 0) parts.add("${years}년")
                if (months > 0) parts.add("${months}개월")
                if (days > 0 || parts.isEmpty()) parts.add("${days}일")
                parts.joinToString(" ")
            }
            else -> {
                if (years > 0) parts.add("$years yr${if (years > 1) "s" else ""}")
                if (months > 0) parts.add("$months mo${if (months > 1) "s" else ""}")
                if (days > 0 || parts.isEmpty()) parts.add("$days day${if (days > 1) "s" else ""}")
                parts.joinToString(" ")
            }
        }
    }
}
