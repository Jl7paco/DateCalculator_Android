package me.paco.datecalculator.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 农历 (阴历) 数据模型
 */
data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean = false,
    val ganZhiYear: String = "",
    val zodiac: String = "",
    val lunarMonthName: String = "",
    val lunarDayName: String = "",
    val solarTerm: String = "",
    val festival: String = ""
) {
    fun getFullDescription(): String {
        val leapStr = if (isLeapMonth) "闰" else ""
        val festivalStr = if (festival.isNotEmpty()) " [$festival]" else ""
        val solarTermStr = if (solarTerm.isNotEmpty()) " [$solarTerm]" else ""
        return "农历 $ganZhiYear${zodiac}年 $leapStr$lunarMonthName$lunarDayName$festivalStr$solarTermStr"
    }
}

/**
 * 农历 / 阳历 (公历) 核心算法与互转工具类 (支持 1900 年 - 2100 年)
 */
object LunarCalendarUtils {

    // 天干
    private val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

    // 地支
    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    // 生肖
    private val ZODIACS = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    // 农历月份名称
    private val LUNAR_MONTH_NAMES = arrayOf(
        "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊"
    )

    // 农历日期名称
    private val LUNAR_DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d260,
        0x0e960, 0x0d520, 0x0faa5, 0x05aa0, 0x076a0, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6,
        0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50
    )

    private val BASE_DATE = LocalDate.of(1900, 1, 31)

    fun getLeapMonth(year: Int): Int {
        val index = year - 1900
        if (index < 0 || index >= LUNAR_INFO.size) return 0
        return LUNAR_INFO[index] and 0xf
    }

    private fun getLeapMonthDays(year: Int): Int {
        val leapMonth = getLeapMonth(year)
        if (leapMonth == 0) return 0
        val index = year - 1900
        return if ((LUNAR_INFO[index] and 0x10000) != 0) 30 else 29
    }

    private fun getLunarMonthDays(year: Int, month: Int): Int {
        val index = year - 1900
        if (index < 0 || index >= LUNAR_INFO.size) return 29
        return if ((LUNAR_INFO[index] and (0x8000 shr (month - 1))) != 0) 30 else 29
    }

    private fun getLunarYearDays(year: Int): Int {
        var sum = 0
        for (m in 1..12) {
            sum += getLunarMonthDays(year, m)
        }
        sum += getLeapMonthDays(year)
        return sum
    }

    fun solarToLunar(solarDate: LocalDate): LunarDate {
        var offset = ChronoUnit.DAYS.between(BASE_DATE, solarDate).toInt()

        if (offset < 0) {
            return LunarDate(1900, 1, 1, false, "庚子", "鼠", "正", "初一")
        }

        var year = 1900
        var daysInYear: Int

        while (year <= 2100) {
            daysInYear = getLunarYearDays(year)
            if (offset < daysInYear) break
            offset -= daysInYear
            year++
        }

        val leapMonth = getLeapMonth(year)
        var isLeap = false
        var month = 1
        var daysInMonth: Int

        while (month <= 12) {
            if (isLeap) {
                daysInMonth = getLeapMonthDays(year)
            } else {
                daysInMonth = getLunarMonthDays(year, month)
            }

            if (offset < daysInMonth) break
            offset -= daysInMonth

            if (leapMonth > 0 && month == leapMonth && !isLeap) {
                isLeap = true
            } else {
                if (isLeap) isLeap = false
                month++
            }
        }

        val day = offset + 1

        val ganZhiYear = getGanZhiYear(year)
        val zodiac = ZODIACS[(year - 4) % 12]
        val lunarMonthName = LUNAR_MONTH_NAMES[(month - 1).coerceIn(0, 11)] + "月"
        val lunarDayName = LUNAR_DAY_NAMES[(day - 1).coerceIn(0, 29)]

        val festival = getLunarFestival(month, day, isLeap)

        return LunarDate(
            year = year,
            month = month,
            day = day,
            isLeapMonth = isLeap,
            ganZhiYear = ganZhiYear,
            zodiac = zodiac,
            lunarMonthName = lunarMonthName,
            lunarDayName = lunarDayName,
            festival = festival
        )
    }

    fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeapMonth: Boolean = false): LocalDate? {
        if (lunarYear < 1900 || lunarYear > 2100) return null

        var offset = 0

        for (y in 1900 until lunarYear) {
            offset += getLunarYearDays(y)
        }

        val leapMonth = getLeapMonth(lunarYear)

        for (m in 1 until lunarMonth) {
            offset += getLunarMonthDays(lunarYear, m)
            if (leapMonth > 0 && m == leapMonth) {
                offset += getLeapMonthDays(lunarYear)
            }
        }

        if (isLeapMonth && leapMonth == lunarMonth) {
            offset += getLunarMonthDays(lunarYear, lunarMonth)
        }

        offset += (lunarDay - 1)

        return BASE_DATE.plusDays(offset.toLong())
    }

    fun getYearGanZhiAndZodiac(year: Int): String {
        if (year < 1900 || year > 2100) return ""
        val ganIdx = (year - 4) % 10
        val zhiIdx = (year - 4) % 12
        val gan = TIAN_GAN[if (ganIdx < 0) ganIdx + 10 else ganIdx]
        val zhi = DI_ZHI[if (zhiIdx < 0) zhiIdx + 12 else zhiIdx]
        val zodiac = ZODIACS[if (zhiIdx < 0) zhiIdx + 12 else zhiIdx]
        return "$gan$zhi ($zodiac) 年"
    }

    private fun getGanZhiYear(year: Int): String {
        val ganIdx = (year - 4) % 10
        val zhiIdx = (year - 4) % 12
        val gan = TIAN_GAN[if (ganIdx < 0) ganIdx + 10 else ganIdx]
        val zhi = DI_ZHI[if (zhiIdx < 0) zhiIdx + 12 else zhiIdx]
        return "$gan$zhi"
    }

    private fun getLunarFestival(month: Int, day: Int, isLeap: Boolean): String {
        if (isLeap) return ""
        return when {
            month == 1 && day == 1 -> "春节"
            month == 1 && day == 15 -> "元宵节"
            month == 2 && day == 2 -> "龙抬头"
            month == 3 && day == 3 -> "上巳节"
            month == 5 && day == 5 -> "端午节"
            month == 7 && day == 7 -> "七夕节"
            month == 7 && day == 15 -> "中元节"
            month == 8 && day == 15 -> "中秋节"
            month == 9 && day == 9 -> "重阳节"
            month == 10 && day == 15 -> "下元节"
            month == 12 && day == 8 -> "腊八节"
            month == 12 && (day == 29 || day == 30) -> "除夕"
            else -> ""
        }
    }

    fun getLunarMonthName(month: Int): String {
        return LUNAR_MONTH_NAMES.getOrNull(month - 1) + "月"
    }

    fun getLunarDayName(day: Int): String {
        return LUNAR_DAY_NAMES.getOrNull(day - 1) ?: "${day}日"
    }
}
