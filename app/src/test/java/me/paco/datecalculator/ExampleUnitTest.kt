package me.paco.datecalculator

import me.paco.datecalculator.data.CalculationType
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
        val target = DateCalculatorUtils.calculateTargetDate(
            baseDate = start,
            days = 5,
            type = CalculationType.ADD,
            isWorkdayMode = true,
            weekendRule = WeekendRule.STANDARD_FIVE_DAYS,
            enableHolidays = false
        )
        // 跨过周六(19)和周日(20)：周一(21) 1, 周二(22) 2, 周三(23) 3, 周四(24) 4, 周五(25) 5
        assertEquals(LocalDate.of(2026, 9, 25), target)
    }

    @Test
    fun testWorkdaysBetweenMidAutumn() {
        // 2026-09-18 (星期五) 至 2026-09-25 (中秋节, 星期五，法定假日扣除 1 天)
        val start = LocalDate.of(2026, 9, 18)
        val end = LocalDate.of(2026, 9, 25)

        val workdays = DateCalculatorUtils.workdaysBetween(
            startDate = start,
            endDate = end,
            weekendRule = WeekendRule.STANDARD_FIVE_DAYS,
            enableHolidays = true,
            holidayRegion = HolidayRegion.CHINA
        )
        assertEquals(4L, workdays)
    }

    @Test
    fun testLunarSolarConversionRoundTrip() {
        val solar = LocalDate.of(2026, 10, 23)
        val lunar = LunarCalendarUtils.solarToLunar(solar)
        assertNotNull(lunar)

        val convertedSolar = LunarCalendarUtils.lunarToSolar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth)
        assertEquals("公历➔农历➔公历双向转换必须 100% 精确一致", solar, convertedSolar)
    }
}
