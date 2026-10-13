package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationStage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

object DateCalculatorUtils {

    fun getWatermarkMonthHeader(monthValue: Int, holidayRegion: HolidayRegion): String {
        val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        return "${monthValue}月 · ${monthNames.getOrElse(monthValue - 1) { "" }}"
    }

    fun formatTimestamp(timestamp: Long, language: AppLanguage): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return formatDate(date, language)
    }

    fun formatDate(date: LocalDate, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA))
            AppLanguage.TRADITIONAL_CHINESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.TAIWAN))
            AppLanguage.ENGLISH -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
            AppLanguage.JAPANESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.JAPAN))
            AppLanguage.KOREAN -> date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREA))
            else -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA))
        }
    }

    fun getLocalizedDayOfWeek(date: LocalDate, language: AppLanguage): String {
        val dayOfWeek = date.dayOfWeek.value // 1 = Mon, 7 = Sun
        return when (language.getEffectiveLanguage()) {
            AppLanguage.ENGLISH -> when (dayOfWeek) {
                1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
            }
            AppLanguage.JAPANESE -> when (dayOfWeek) {
                1 -> "月"; 2 -> "火"; 3 -> "水"; 4 -> "木"; 5 -> "金"; 6 -> "土"; else -> "日"
            }
            AppLanguage.KOREAN -> when (dayOfWeek) {
                1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; else -> "일"
            }
            AppLanguage.TRADITIONAL_CHINESE -> when (dayOfWeek) {
                1 -> "週一"; 2 -> "週二"; 3 -> "週三"; 4 -> "週四"; 5 -> "週五"; 6 -> "週六"; else -> "週日"
            }
            else -> when (dayOfWeek) {
                1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
            }
        }
    }

    fun formatDateWithWeek(date: LocalDate, language: AppLanguage): String {
        val dateFormatted = formatDate(date, language)
        val weekNum = getWeekOfYear(date)
        return when (language.getEffectiveLanguage()) {
            AppLanguage.ENGLISH -> "$dateFormatted (Week $weekNum)"
            AppLanguage.JAPANESE -> "$dateFormatted (第${weekNum}週)"
            AppLanguage.KOREAN -> "$dateFormatted (第${weekNum}주)"
            AppLanguage.TRADITIONAL_CHINESE -> "$dateFormatted (第${weekNum}週)"
            else -> "$dateFormatted (第${weekNum}周)"
        }
    }

    fun getWeekOfYear(date: LocalDate): Int {
        val weekFields = WeekFields.of(Locale.getDefault())
        return date.get(weekFields.weekOfWeekBasedYear())
    }

    fun isWorkday(
        date: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Boolean {
        val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(date, holidayRegion)
        val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(date, holidayRegion)
        val isWeekend = weekendRule.isWeekend(date, isCurrentWeekBigWeek)

        return if (isShift) true else if (isStat) false else !isWeekend
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
        if (days == 0) return baseDate

        var currentDate = baseDate
        var step = days
        val isAdd = (type == CalculationType.ADD)

        if (!isWorkdayMode) {
            return if (isAdd) baseDate.plusDays(days.toLong()) else baseDate.minusDays(days.toLong())
        }

        while (step > 0) {
            currentDate = if (isAdd) currentDate.plusDays(1) else currentDate.minusDays(1)

            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(currentDate, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(currentDate, holidayRegion)
            val isWeekend = weekendRule.isWeekend(currentDate, isCurrentWeekBigWeek)

            val isWorkingDay = if (isShiftWorkday) {
                true
            } else if (isStatutoryHoliday) {
                false
            } else if (isWeekend) {
                false
            } else {
                true
            }

            if (isWorkingDay) {
                step--
            }
        }

        return currentDate
    }

    fun calculateDaysBetweenDates(
        startDate: LocalDate,
        targetDate: LocalDate,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Int {
        if (startDate == targetDate) return 0
        val isAdd = !targetDate.isBefore(startDate)
        if (!isWorkdayMode) {
            return abs(ChronoUnit.DAYS.between(startDate, targetDate)).toInt()
        }

        var count = 0
        var curr = if (isAdd) startDate.plusDays(1) else startDate.minusDays(1)
        val limit = if (isAdd) targetDate else startDate.minusDays(1)

        while (if (isAdd) !curr.isAfter(limit) else !curr.isBefore(targetDate)) {
            val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val isWork = if (isShift) true else if (isStat) false else !isWeekend
            if (isWork) {
                count++
            }
            curr = if (isAdd) curr.plusDays(1) else curr.minusDays(1)
        }

        return count
    }

    fun computeMultiStageSequence(
        baseDate: LocalDate,
        stages: List<CalculationStage>,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Pair<LocalDate, List<StageSegmentResult>> {
        var currentStart = baseDate
        val results = mutableListOf<StageSegmentResult>()

        stages.forEachIndexed { idx, stage ->
            val daysVal = stage.days.toInt()
            if (daysVal > 0) {
                val segEnd = calculateTargetDate(
                    baseDate = currentStart,
                    days = daysVal,
                    type = stage.type,
                    isWorkdayMode = isWorkdayMode,
                    weekendRule = weekendRule,
                    enableHolidays = enableHolidays,
                    holidayRegion = holidayRegion,
                    isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                    disableChinaShiftWorkdays = disableChinaShiftWorkdays
                )

                val (_, stat, week) = calculateBreakdown(
                    startDate = currentStart,
                    endDate = segEnd,
                    weekendRule = weekendRule,
                    enableHolidays = enableHolidays,
                    holidayRegion = holidayRegion,
                    isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                    disableChinaShiftWorkdays = disableChinaShiftWorkdays
                )

                val remarkText = stage.remark.ifBlank { "阶段 ${idx + 1}" }

                results.add(
                    StageSegmentResult(
                        stageIndex = idx,
                        remark = remarkText,
                        type = stage.type,
                        daysCount = stage.days,
                        startDate = currentStart,
                        endDate = segEnd,
                        totalCalendarDays = abs(naturalDaysBetween(currentStart, segEnd)),
                        restDaysCount = stat + week
                    )
                )

                currentStart = segEnd
            }
        }

        return Pair(currentStart, results)
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

        val isForward = startDate.isBefore(endDate)
        val start = if (isForward) startDate else endDate
        val end = if (isForward) endDate else startDate

        var workdays = 0L
        var curr = start.plusDays(1)

        while (!curr.isAfter(end)) {
            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val isWorkingDay = if (isShiftWorkday) {
                true
            } else if (isStatutoryHoliday) {
                false
            } else if (isWeekend) {
                false
            } else {
                true
            }

            if (isWorkingDay) {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return if (isForward) workdays else -workdays
    }

    fun naturalDaysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun calculateBreakdown(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Triple<Long, Long, Long> {
        val totalDays = abs(naturalDaysBetween(startDate, endDate))
        if (totalDays == 0L) return Triple(0L, 0L, 0L)

        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        var workdays = 0L
        var holidays = 0L
        var weekends = 0L

        var curr = start.plusDays(1)
        while (!curr.isAfter(end)) {
            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            if (isShiftWorkday) {
                workdays++
            } else if (isStatutoryHoliday) {
                holidays++
            } else if (isWeekend) {
                weekends++
            } else {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return Triple(workdays, holidays, weekends)
    }

    fun formatPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage
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
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
            AppLanguage.TRADITIONAL_CHINESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}個月")
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
            AppLanguage.ENGLISH -> {
                if (years > 0) parts.add("$years ${if (years > 1) "years" else "year"}")
                if (months > 0) parts.add("$months ${if (months > 1) "months" else "month"}")
                if (days > 0) parts.add("$days ${if (days > 1) "days" else "day"}")
                parts.joinToString(" ").ifEmpty { "0 days" }
            }
            AppLanguage.JAPANESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}ヶ月")
                if (days > 0) parts.add("${days}日")
                parts.joinToString(" ").ifEmpty { "0日" }
            }
            AppLanguage.KOREAN -> {
                if (years > 0) parts.add("${years}년")
                if (months > 0) parts.add("${months}개월")
                if (days > 0) parts.add("${days}일")
                parts.joinToString(" ").ifEmpty { "0일" }
            }
            else -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}个月")
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
        }
    }
}
