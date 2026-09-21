package me.paco.datecalculator.util

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
        isChineseLocale: Boolean = Locale.getDefault().language == "zh"
    ): String {
        return if (isChineseLocale) {
            date.format(DATE_FORMATTER_ZH)
        } else {
            date.format(DATE_FORMATTER_EN)
        }
    }

    fun formatDateWithWeek(
        date: LocalDate,
        isChineseLocale: Boolean = Locale.getDefault().language == "zh"
    ): String {
        val baseDateStr = formatDate(date, isChineseLocale)
        val weekFields = WeekFields.of(if (isChineseLocale) Locale.CHINA else Locale.US)
        val weekNum = date.get(weekFields.weekOfWeekBasedYear())
        return if (isChineseLocale) {
            "$baseDateStr (第${weekNum}周)"
        } else {
            "$baseDateStr (W$weekNum)"
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

        if (start == end) return emptyList()

        val blocks = mutableListOf<ChronologicalBlock>()
        val blockStart = start.plusDays(if (isReverse) 0 else 1)
        val finalEnd = if (isReverse) end.minusDays(1) else end

        if (blockStart.isAfter(finalEnd)) return emptyList()

        var curr = blockStart
        var currentType: TimelineBlockType? = null
        var currentBlockStart = curr

        while (!curr.isAfter(finalEnd)) {
            val isHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isShiftWork = RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val type = when {
                isHoliday -> TimelineBlockType.STATUTORY_HOLIDAY
                isShiftWork -> TimelineBlockType.WORKDAY
                isWeekend -> TimelineBlockType.WEEKEND_REST
                else -> TimelineBlockType.WORKDAY
            }

            if (currentType == null) {
                currentType = type
                currentBlockStart = curr
            } else if (currentType != type) {
                val days = ChronoUnit.DAYS.between(currentBlockStart, curr)
                blocks.add(ChronologicalBlock(currentBlockStart, curr.minusDays(1), currentType, days))
                currentType = type
                currentBlockStart = curr
            }

            curr = curr.plusDays(1)
        }

        if (currentType != null) {
            val days = ChronoUnit.DAYS.between(currentBlockStart, finalEnd.plusDays(1))
            blocks.add(ChronologicalBlock(currentBlockStart, finalEnd, currentType, days))
        }

        return blocks
    }

    fun addWorkdays(
        baseDate: LocalDate,
        workdays: Long,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): LocalDate {
        if (workdays == 0L) return baseDate

        var currentDate = baseDate
        var remaining = abs(workdays)
        val step = if (workdays > 0) 1L else -1L

        while (remaining > 0) {
            currentDate = currentDate.plusDays(step)
            if (isWorkday(currentDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)) {
                remaining--
            }
        }
        return currentDate
    }

    fun addNaturalDays(baseDate: LocalDate, days: Long): LocalDate {
        return baseDate.plusDays(days)
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
        if (startDate == endDate) return 0L

        val isReverse = startDate.isAfter(endDate)
        val start = if (isReverse) endDate else startDate
        val end = if (isReverse) startDate else endDate

        var count = 0L
        var curr = start.plusDays(1)

        while (!curr.isAfter(end)) {
            val isTargetDate = (curr == end)
            val isWork = if (isTargetDate && enableHolidays) {
                !weekendRule.isWeekend(curr, isCurrentWeekBigWeek) || (!disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion))
            } else {
                isWorkday(curr, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)
            }

            if (isWork) {
                count++
            }
            curr = curr.plusDays(1)
        }

        return if (isReverse) -count else count
    }

    fun calculateRangeBreakdown(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): RangeBreakdownResult {
        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        val totalCalendarDays = ChronoUnit.DAYS.between(start, end)
        var workdays = 0L
        var weekendDays = 0L
        var statutoryHolidays = 0L
        var shiftWorkdays = 0L

        var curr = start.plusDays(1)
        while (!curr.isAfter(end)) {
            val isHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isShiftWork = !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            if (isShiftWork) {
                workdays++
                shiftWorkdays++
            } else if (isHoliday) {
                statutoryHolidays++
            } else if (isWeekend) {
                weekendDays++
            } else {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return RangeBreakdownResult(
            startDate = startDate,
            endDate = endDate,
            totalCalendarDays = totalCalendarDays,
            workdays = workdays,
            weekendDays = weekendDays,
            statutoryHolidays = statutoryHolidays,
            shiftWorkdays = shiftWorkdays
        )
    }

    fun calculateTargetDate(
        baseDate: LocalDate,
        days: Int,
        type: CalculationType,
        isWorkdayMode: Boolean,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): LocalDate {
        if (days == 0) return baseDate

        var result = baseDate
        var added = 0
        val isAdd = (type == CalculationType.ADD)
        val step = if (isAdd) 1L else -1L

        while (added < days) {
            result = result.plusDays(step)
            if (!isWorkdayMode) {
                added++
            } else {
                if (isWorkday(result, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)) {
                    added++
                }
            }
        }
        return result
    }

    fun calculateWorkdayDiff(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Int {
        if (startDate == endDate) return 0

        val isReverse = startDate.isAfter(endDate)
        var curr = if (isReverse) endDate else startDate
        val end = if (isReverse) startDate else endDate

        var count = 0
        curr = curr.plusDays(1)

        while (!curr.isAfter(end)) {
            val isTargetDate = (curr == end)
            val isWork = if (isTargetDate && enableHolidays) {
                !weekendRule.isWeekend(curr, isCurrentWeekBigWeek) || (!disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion))
            } else {
                isWorkday(curr, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays)
            }

            if (isWork) {
                count++
            }
            curr = curr.plusDays(1)
        }

        return if (isReverse) -count else count
    }

    fun getDateDescription(
        date: LocalDate,
        isChineseLocale: Boolean = Locale.getDefault().language == "zh"
    ): String {
        val weekDayName = if (isChineseLocale) {
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "星期一"
                DayOfWeek.TUESDAY -> "星期二"
                DayOfWeek.WEDNESDAY -> "星期三"
                DayOfWeek.THURSDAY -> "星期四"
                DayOfWeek.FRIDAY -> "星期五"
                DayOfWeek.SATURDAY -> "星期六"
                DayOfWeek.SUNDAY -> "星期日"
            }
        } else {
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
            }
        }
        val dayOfYear = date.dayOfYear
        val totalDaysInYear = date.lengthOfYear()
        val weekOfWeekBasedYear = date.get(WeekFields.of(if (isChineseLocale) Locale.CHINA else Locale.US).weekOfWeekBasedYear())
        val isLeap = date.isLeapYear

        return if (isChineseLocale) {
            "$weekDayName | 当年第 ${dayOfYear}/${totalDaysInYear} 天 | 第 $weekOfWeekBasedYear 周" +
                    if (isLeap) " (闰年)" else ""
        } else {
            "$weekDayName | Day ${dayOfYear}/${totalDaysInYear} | Week $weekOfWeekBasedYear" +
                    if (isLeap) " (Leap Year)" else ""
        }
    }

    fun formatPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        isChineseLocale: Boolean = Locale.getDefault().language == "zh"
    ): String {
        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        val period = Period.between(start, end)
        val years = period.years
        val months = period.months
        val days = period.days

        val parts = mutableListOf<String>()
        if (isChineseLocale) {
            if (years > 0) parts.add("${years}年")
            if (months > 0) parts.add("${months}个月")
            if (days > 0 || parts.isEmpty()) parts.add("${days}天")
            return parts.joinToString("")
        } else {
            if (years > 0) parts.add("$years Year${if (years > 1) "s" else ""}")
            if (months > 0) parts.add("$months Month${if (months > 1) "s" else ""}")
            if (days > 0 || parts.isEmpty()) parts.add("$days Day${if (days > 1) "s" else ""}")
            return parts.joinToString(" ")
        }
    }
}
