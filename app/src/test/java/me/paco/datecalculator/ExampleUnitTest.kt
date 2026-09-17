package me.paco.datecalculator

import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testWorkdayAdd() {
        val start = LocalDate.of(2025, 1, 1)
        val target = DateCalculatorUtils.addWorkdays(start, 5, WeekendRule.STANDARD_FIVE_DAYS, false)
        assertNotNull(target)
    }
}
