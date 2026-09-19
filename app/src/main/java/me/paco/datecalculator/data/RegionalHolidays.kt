package me.paco.datecalculator.data

import me.paco.datecalculator.util.LunarCalendarUtils
import java.time.LocalDate

/**
 * 多地区法定节假日与调休/补假数据库 (支持 13 个国家/地区)
 */
object RegionalHolidays {

    // ==================== 中国大陆 (Mainland China 精确 1999-2026 调休集合) ====================
    private val holidaysChinaPrecise = setOf(
        // 1999
        LocalDate.of(1999, 10, 1), LocalDate.of(1999, 10, 2), LocalDate.of(1999, 10, 3),
        LocalDate.of(1999, 10, 4), LocalDate.of(1999, 10, 5), LocalDate.of(1999, 10, 6), LocalDate.of(1999, 10, 7),
        // 2000
        LocalDate.of(2000, 1, 1),
        LocalDate.of(2000, 2, 4), LocalDate.of(2000, 2, 5), LocalDate.of(2000, 2, 6),
        LocalDate.of(2000, 2, 7), LocalDate.of(2000, 2, 8), LocalDate.of(2000, 2, 9), LocalDate.of(2000, 2, 10),
        LocalDate.of(2000, 5, 1), LocalDate.of(2000, 5, 2), LocalDate.of(2000, 5, 3),
        LocalDate.of(2000, 5, 4), LocalDate.of(2000, 5, 5), LocalDate.of(2000, 5, 6), LocalDate.of(2000, 5, 7),
        LocalDate.of(2000, 10, 1), LocalDate.of(2000, 10, 2), LocalDate.of(2000, 10, 3),
        LocalDate.of(2000, 10, 4), LocalDate.of(2000, 10, 5), LocalDate.of(2000, 10, 6), LocalDate.of(2000, 10, 7),
        // 2024
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
        LocalDate.of(2024, 10, 7),
        // 2025
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
        LocalDate.of(2025, 10, 7), LocalDate.of(2025, 10, 8),
        // 2026
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21),
        LocalDate.of(2026, 2, 22), LocalDate.of(2026, 2, 23),
        LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3),
        LocalDate.of(2026, 6, 19),
        LocalDate.of(2026, 9, 25),
        LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 3),
        LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6),
        LocalDate.of(2026, 10, 7)
    )

    private val shiftWorkdaysChina = setOf(
        LocalDate.of(2024, 2, 4), LocalDate.of(2024, 2, 18),
        LocalDate.of(2024, 4, 7), LocalDate.of(2024, 4, 28), LocalDate.of(2024, 5, 11),
        LocalDate.of(2024, 9, 14), LocalDate.of(2024, 9, 29), LocalDate.of(2024, 10, 12),
        LocalDate.of(2025, 1, 26), LocalDate.of(2025, 2, 8),
        LocalDate.of(2025, 4, 27), LocalDate.of(2025, 9, 28), LocalDate.of(2025, 10, 11),
        LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 28),
        LocalDate.of(2026, 9, 27), LocalDate.of(2026, 10, 10)
    )

    private fun isChinaHistoricalHoliday(date: LocalDate): Boolean {
        val year = date.year
        if (year < 1949) return false
        if (year >= 1999) return holidaysChinaPrecise.contains(date)
        val month = date.monthValue
        val day = date.dayOfMonth
        if (month == 1 && day == 1) return true
        if (month == 5 && day == 1) return true
        if (month == 10 && (day == 1 || day == 2)) return true
        val lunar = LunarCalendarUtils.solarToLunar(date)
        return lunar.month == 1 && lunar.day in 1..3 && !lunar.isLeapMonth
    }

    // ==================== 台湾 (Taiwan) ====================
    private val holidaysTaiwan = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 8), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10),
        LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13), LocalDate.of(2024, 2, 14),
        LocalDate.of(2024, 2, 28), LocalDate.of(2024, 4, 4), LocalDate.of(2024, 4, 5), LocalDate.of(2024, 6, 10),
        LocalDate.of(2024, 9, 17), LocalDate.of(2024, 10, 10),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29),
        LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 4, 3),
        LocalDate.of(2025, 4, 4), LocalDate.of(2025, 5, 30), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 10, 10),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 4, 4),
        LocalDate.of(2026, 4, 5), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 10)
    )

    private val shiftWorkdaysTaiwan = setOf(
        LocalDate.of(2024, 2, 17),
        LocalDate.of(2025, 2, 8)
    )

    // ==================== 中国香港 (Hong Kong) ====================
    private val holidaysHongKong = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13),
        LocalDate.of(2024, 3, 29), LocalDate.of(2024, 3, 30), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 4),
        LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 7, 1),
        LocalDate.of(2024, 9, 18), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 11), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 19), LocalDate.of(2025, 4, 21),
        LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 31), LocalDate.of(2025, 7, 1),
        LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 7), LocalDate.of(2025, 10, 29), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1),
        LocalDate.of(2026, 5, 24), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 26),
        LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 18), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    // ==================== 中国澳门 (Macao) ====================
    private val holidaysMacao = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12),
        LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 4), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 15),
        LocalDate.of(2024, 6, 10), LocalDate.of(2024, 9, 18), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 2),
        LocalDate.of(2024, 10, 11), LocalDate.of(2024, 12, 8), LocalDate.of(2024, 12, 20), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30),
        LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 31), LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 2), LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 24),
        LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2),
        LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 25)
    )

    // ==================== 新加坡 (Singapore) ====================
    private val holidaysSingapore = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12),
        LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22),
        LocalDate.of(2024, 6, 17), LocalDate.of(2024, 8, 9), LocalDate.of(2024, 10, 31), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 3, 31),
        LocalDate.of(2025, 4, 18), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 12), LocalDate.of(2025, 6, 7),
        LocalDate.of(2025, 8, 9), LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 27),
        LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    // ==================== 马来西亚 (Malaysia) ====================
    private val holidaysMalaysia = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 4, 10),
        LocalDate.of(2024, 4, 11), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22), LocalDate.of(2024, 6, 3),
        LocalDate.of(2024, 6, 17), LocalDate.of(2024, 7, 7), LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 16),
        LocalDate.of(2024, 10, 31), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 3, 31),
        LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 12), LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 7),
        LocalDate.of(2025, 8, 31), LocalDate.of(2025, 9, 16), LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 27),
        LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 16), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    // ==================== 越南 (Vietnam) ====================
    private val holidaysVietnam = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 8), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10),
        LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13), LocalDate.of(2024, 2, 14),
        LocalDate.of(2024, 4, 18), LocalDate.of(2024, 4, 30), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 9, 2), LocalDate.of(2024, 9, 3),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29),
        LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 4, 7), LocalDate.of(2025, 4, 30),
        LocalDate.of(2025, 5, 1), LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 2),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 4, 26), LocalDate.of(2026, 4, 30),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)
    )

    // ==================== 日本 (Japan) ====================
    private val holidaysJapan = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 23),
        LocalDate.of(2024, 3, 20), LocalDate.of(2024, 4, 29), LocalDate.of(2024, 5, 3), LocalDate.of(2024, 5, 4),
        LocalDate.of(2024, 5, 5), LocalDate.of(2024, 7, 15), LocalDate.of(2024, 8, 11), LocalDate.of(2024, 9, 16),
        LocalDate.of(2024, 9, 22), LocalDate.of(2024, 10, 14), LocalDate.of(2024, 11, 3), LocalDate.of(2024, 11, 23),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 13), LocalDate.of(2025, 2, 11), LocalDate.of(2025, 2, 23),
        LocalDate.of(2025, 3, 20), LocalDate.of(2025, 4, 29), LocalDate.of(2025, 5, 3), LocalDate.of(2025, 5, 4),
        LocalDate.of(2025, 5, 5), LocalDate.of(2025, 7, 21), LocalDate.of(2025, 8, 11), LocalDate.of(2025, 9, 15),
        LocalDate.of(2025, 10, 13), LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 23),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 23),
        LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 4),
        LocalDate.of(2026, 5, 5), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 11), LocalDate.of(2026, 9, 21),
        LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 23)
    )

    // ==================== 韩国 (South Korea) ====================
    private val holidaysSouthKorea = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12),
        LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 15),
        LocalDate.of(2024, 6, 6), LocalDate.of(2024, 8, 15), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 9, 17),
        LocalDate.of(2024, 10, 3), LocalDate.of(2024, 10, 9), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30),
        LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 6, 6), LocalDate.of(2025, 8, 15),
        LocalDate.of(2025, 10, 3), LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 10, 7), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 5), LocalDate.of(2026, 6, 6), LocalDate.of(2026, 8, 15),
        LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 9), LocalDate.of(2026, 12, 25)
    )

    // ==================== 澳大利亚 (Australia) ====================
    private val holidaysAustralia = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 26), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1),
        LocalDate.of(2024, 4, 25), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 10, 7), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21),
        LocalDate.of(2025, 4, 25), LocalDate.of(2025, 6, 9), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6),
        LocalDate.of(2026, 4, 25), LocalDate.of(2026, 6, 8), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    // ==================== 新西兰 (New Zealand) ====================
    private val holidaysNewZealand = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 6), LocalDate.of(2024, 3, 29),
        LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 25), LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 28),
        LocalDate.of(2024, 10, 28), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), LocalDate.of(2025, 2, 6), LocalDate.of(2025, 4, 18),
        LocalDate.of(2025, 4, 21), LocalDate.of(2025, 4, 25), LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 20),
        LocalDate.of(2025, 10, 27), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), LocalDate.of(2026, 2, 6), LocalDate.of(2026, 4, 3),
        LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 10),
        LocalDate.of(2026, 10, 26), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    // ==================== 美国 (United States) ====================
    private val holidaysUnitedStates = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 19), LocalDate.of(2024, 5, 27),
        LocalDate.of(2024, 6, 19), LocalDate.of(2024, 7, 4), LocalDate.of(2024, 9, 2), LocalDate.of(2024, 10, 14),
        LocalDate.of(2024, 11, 11), LocalDate.of(2024, 11, 28), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 20), LocalDate.of(2025, 2, 17), LocalDate.of(2025, 5, 26),
        LocalDate.of(2025, 6, 19), LocalDate.of(2025, 7, 4), LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 13),
        LocalDate.of(2025, 11, 11), LocalDate.of(2025, 11, 27), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 19), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 5, 25),
        LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 4), LocalDate.of(2026, 9, 7),
        LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 26), LocalDate.of(2026, 12, 25)
    )

    // ==================== 泰国 (Thailand) ====================
    private val holidaysThailand = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 26), LocalDate.of(2024, 4, 6), LocalDate.of(2024, 4, 13),
        LocalDate.of(2024, 4, 14), LocalDate.of(2024, 4, 15), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22),
        LocalDate.of(2024, 6, 3), LocalDate.of(2024, 7, 28), LocalDate.of(2024, 8, 12), LocalDate.of(2024, 10, 13),
        LocalDate.of(2024, 10, 23), LocalDate.of(2024, 12, 5), LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 31),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 12), LocalDate.of(2025, 4, 6), LocalDate.of(2025, 4, 13),
        LocalDate.of(2025, 4, 14), LocalDate.of(2025, 4, 15), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 11),
        LocalDate.of(2025, 6, 3), LocalDate.of(2025, 7, 28), LocalDate.of(2025, 8, 12), LocalDate.of(2025, 10, 13),
        LocalDate.of(2025, 10, 23), LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 31),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 13),
        LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
        LocalDate.of(2026, 6, 3), LocalDate.of(2026, 7, 28), LocalDate.of(2026, 8, 12), LocalDate.of(2026, 10, 13),
        LocalDate.of(2026, 10, 23), LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 31)
    )

    fun isStatutoryHoliday(date: LocalDate, region: HolidayRegion): Boolean {
        return when (region) {
            HolidayRegion.CHINA -> isChinaHistoricalHoliday(date)
            HolidayRegion.TAIWAN -> holidaysTaiwan.contains(date)
            HolidayRegion.HONG_KONG -> holidaysHongKong.contains(date)
            HolidayRegion.MACAO -> holidaysMacao.contains(date)
            HolidayRegion.SINGAPORE -> holidaysSingapore.contains(date)
            HolidayRegion.MALAYSIA -> holidaysMalaysia.contains(date)
            HolidayRegion.VIETNAM -> holidaysVietnam.contains(date)
            HolidayRegion.JAPAN -> holidaysJapan.contains(date)
            HolidayRegion.SOUTH_KOREA -> holidaysSouthKorea.contains(date)
            HolidayRegion.AUSTRALIA -> holidaysAustralia.contains(date)
            HolidayRegion.NEW_ZEALAND -> holidaysNewZealand.contains(date)
            HolidayRegion.UNITED_STATES -> holidaysUnitedStates.contains(date)
            HolidayRegion.THAILAND -> holidaysThailand.contains(date)
        }
    }

    fun isShiftWorkday(date: LocalDate, region: HolidayRegion): Boolean {
        return when (region) {
            HolidayRegion.CHINA -> shiftWorkdaysChina.contains(date)
            HolidayRegion.TAIWAN -> shiftWorkdaysTaiwan.contains(date)
            else -> false
        }
    }

    fun getHolidaysCount(region: HolidayRegion): Int {
        return when (region) {
            HolidayRegion.CHINA -> holidaysChinaPrecise.size
            HolidayRegion.TAIWAN -> holidaysTaiwan.size
            HolidayRegion.HONG_KONG -> holidaysHongKong.size
            HolidayRegion.MACAO -> holidaysMacao.size
            HolidayRegion.SINGAPORE -> holidaysSingapore.size
            HolidayRegion.MALAYSIA -> holidaysMalaysia.size
            HolidayRegion.VIETNAM -> holidaysVietnam.size
            HolidayRegion.JAPAN -> holidaysJapan.size
            HolidayRegion.SOUTH_KOREA -> holidaysSouthKorea.size
            HolidayRegion.AUSTRALIA -> holidaysAustralia.size
            HolidayRegion.NEW_ZEALAND -> holidaysNewZealand.size
            HolidayRegion.UNITED_STATES -> holidaysUnitedStates.size
            HolidayRegion.THAILAND -> holidaysThailand.size
        }
    }

    fun getShiftWorkdaysCount(region: HolidayRegion): Int {
        return when (region) {
            HolidayRegion.CHINA -> shiftWorkdaysChina.size
            HolidayRegion.TAIWAN -> shiftWorkdaysTaiwan.size
            else -> 0
        }
    }
}
