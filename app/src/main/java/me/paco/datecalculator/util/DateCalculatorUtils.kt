package me.paco.datecalculator.util

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

object DateCalculatorUtils {

    val CHINESE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatDate(date: LocalDate, isChineseLocale: Boolean = Locale.getDefault().language == "zh"): String {
        return if (isChineseLocale) {
            date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
        } else {
            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }

    /**
     * 格式化日期并带上当天的年周数 (例如: 2026年09月17日 (第38周) / Sep 17, 2026 (Week 38))
     */
    fun formatDateWithWeek(date: LocalDate, isChineseLocale: Boolean = Locale.getDefault().language == "zh"): String {
        val dateStr = formatDate(date, isChineseLocale)
        val weekFields = WeekFields.of(if (isChineseLocale) Locale.CHINA else Locale.US)
        val weekNum = date.get(weekFields.weekOfWeekBasedYear())
        return if (isChineseLocale) {
            "$dateStr (第${weekNum}周)"
        } else {
            "$dateStr (Week $weekNum)"
        }
    }

    fun isWorkday(
        date: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true
    ): Boolean {
        if (enableHolidays && holidayRegion != HolidayRegion.NONE) {
            if (RegionalHolidays.isShiftWorkday(date, holidayRegion)) {
                return true
            }
            if (RegionalHolidays.isStatutoryHoliday(date, holidayRegion)) {
                return false
            }
        }
        return !weekendRule.isWeekend(date, isCurrentWeekBigWeek)
    }

    fun addWorkdays(
        baseDate: LocalDate,
        workdays: Long,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true
    ): LocalDate {
        if (workdays == 0L) return baseDate

        var currentDate = baseDate
        var remaining = abs(workdays)
        val step = if (workdays > 0) 1L else -1L

        while (remaining > 0) {
            currentDate = currentDate.plusDays(step)
            if (isWorkday(currentDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek)) {
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
        isCurrentWeekBigWeek: Boolean = true
    ): Long {
        if (startDate == endDate) return 0L

        val isReverse = startDate.isAfter(endDate)
        val start = if (isReverse) endDate else startDate
        val end = if (isReverse) startDate else endDate

        var count = 0L
        var curr = start.plusDays(1)
        while (!curr.isAfter(end)) {
            if (isWorkday(curr, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek)) {
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
