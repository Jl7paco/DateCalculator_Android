package me.paco.datecalculator

import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testWorkdayAdd() {
        // 2026-09-18 (星期五) + 5 个标准工作日 (无节假日规则，纯双休)
        val start = LocalDate.of(2026, 9, 18)
        val target = DateCalculatorUtils.addWorkdays(
            baseDate = start,
            workdays = 5,
            weekendRule = WeekendRule.STANDARD_FIVE_DAYS,
            enableHolidays = false
        )
        // 跨过周六(19)和周日(20)：周一(21) 1, 周二(22) 2, 周三(23) 3, 周四(24) 4, 周五(25) 5
        assertEquals(LocalDate.of(2026, 9, 25), target)
    }

    @Test
    fun testWorkdaysBetweenMidAutumn() {
        // 2026-09-18 (星期五) 至 2026-09-25 (中秋节, 星期五)
        val start = LocalDate.of(2026, 9, 18)
        val end = LocalDate.of(2026, 9, 25)

        val workdays = DateCalculatorUtils.workdaysBetween(
            startDate = start,
            endDate = end,
            weekendRule = WeekendRule.STANDARD_FIVE_DAYS,
            enableHolidays = true,
            holidayRegion = HolidayRegion.CHINA
        )
        // 期待为 5 个工作日
        assertEquals(5L, workdays)
    }

    @Test
    fun testLunarSolarConversionRoundTrip() {
        val solar = LocalDate.of(2026, 9, 18)
        val lunar = LunarCalendarUtils.solarToLunar(solar)

        assertEquals(2026, lunar.year)
        assertEquals(8, lunar.month)
        assertEquals(8, lunar.day)
        assertEquals("八月", lunar.lunarMonthName)
        assertEquals("初八", lunar.lunarDayName)

        val convertedSolar = LunarCalendarUtils.lunarToSolar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth)
        assertEquals(solar, convertedSolar)
    }

    @Test
    fun testLeapMonthConversion2025() {
        // 2025年有闰六月 (Leap 6th month in 2025)
        val leapMonth = LunarCalendarUtils.getLeapMonth(2025)
        assertEquals(6, leapMonth)

        // 2025年农历六月十五 ➔ 2025-07-09
        val solarNormalSix = LunarCalendarUtils.lunarToSolar(2025, 6, 15, false)
        assertNotNull(solarNormalSix)

        // 2025年农历闰六月十五 ➔ 2025-08-08
        val solarLeapSix = LunarCalendarUtils.lunarToSolar(2025, 6, 15, true)
        assertNotNull(solarLeapSix)

        assertNotEquals(solarNormalSix, solarLeapSix)
    }
}
