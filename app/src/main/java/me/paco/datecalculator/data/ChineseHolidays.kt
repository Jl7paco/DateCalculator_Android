package me.paco.datecalculator.data

import java.time.LocalDate

/**
 * 中国法定节假日及调休补班数据 (2024 - 2025)
 */
object ChineseHolidays {

    // 2024年法定休假日
    private val holidays2024 = setOf(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12),
        LocalDate.of(2024, 2, 13), LocalDate.of(2024, 2, 14), LocalDate.of(2024, 2, 15),
        LocalDate.of(2024, 2, 16), LocalDate.of(2024, 2, 17),
        LocalDate.of(2024, 4, 4), LocalDate.of(2024, 4, 5), LocalDate.of(2024, 4, 6),
        LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 2), LocalDate.of(2024, 5, 3),
        LocalDate.of(2024, 5, 4), LocalDate.of(2024, 5, 5),
        LocalDate.of(2024, 6, 10),
        LocalDate.of(2024, 9, 15), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 9, 17),
        LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 2), LocalDate.of(2024, 10, 3),
        LocalDate.of(2024, 10, 4), LocalDate.of(2024, 10, 5), LocalDate.of(2024, 10, 6),
        LocalDate.of(2024, 10, 7)
    )

    // 2024年调休上班日
    private val workdays2024 = setOf(
        LocalDate.of(2024, 2, 4), LocalDate.of(2024, 2, 18),
        LocalDate.of(2024, 4, 7),
        LocalDate.of(2024, 4, 28), LocalDate.of(2024, 5, 11),
        LocalDate.of(2024, 9, 14),
        LocalDate.of(2024, 9, 29), LocalDate.of(2024, 10, 12)
    )

    // 2025年法定休假日
    private val holidays2025 = setOf(
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30),
        LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 2),
        LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 4),
        LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 5), LocalDate.of(2025, 4, 6),
        LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2), LocalDate.of(2025, 5, 3),
        LocalDate.of(2025, 5, 4), LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 31), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 2),
        LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 2), LocalDate.of(2025, 10, 3),
        LocalDate.of(2025, 10, 4), LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6),
        LocalDate.of(2025, 10, 7), LocalDate.of(2025, 10, 8)
    )

    // 2025年调休上班日
    private val workdays2025 = setOf(
        LocalDate.of(2025, 1, 26), LocalDate.of(2025, 2, 8),
        LocalDate.of(2025, 4, 27),
        LocalDate.of(2025, 9, 28), LocalDate.of(2025, 10, 11)
    )

    val allHolidays: Set<LocalDate> = holidays2024 + holidays2025
    val allShiftWorkdays: Set<LocalDate> = workdays2024 + workdays2025

    fun isStatutoryHoliday(date: LocalDate): Boolean = allHolidays.contains(date)

    fun isShiftWorkday(date: LocalDate): Boolean = allShiftWorkdays.contains(date)
}
