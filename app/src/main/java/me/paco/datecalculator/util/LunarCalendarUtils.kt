package me.paco.datecalculator.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class LunarDateResult(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
    val lunarMonthName: String,
    val lunarDayName: String,
    val ganZhiYear: String,
    val zodiac: String,
    val festival: String
) {
    fun getFullDescription(): String {
        val leapStr = if (isLeapMonth) "闰" else ""
        val festStr = if (festival.isNotEmpty()) " [$festival]" else ""
        return "农历 ${ganZhiYear}年 (${zodiac}) $leapStr$lunarMonthName$lunarDayName$festStr"
    }
}

/**
 * 老黄历宜忌数据模型
 */
data class AlmanacYiJi(
    val yiList: List<String>,
    val jiList: List<String>
)

object LunarCalendarUtils {

    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0,
        0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, 0x14b63, 0x09370,
        0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, 0x0a2e0, 0x0d2e3,
        0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, 0x052d0, 0x0a9b8,
        0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, 0x0b273, 0x06930,
        0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, 0x0e968, 0x0d520,
        0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252
    )

    private val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val ZODIACS = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    private val LUNAR_MONTH_NAMES = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊")
    private val LUNAR_DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    private val BASE_DATE = LocalDate.of(1900, 1, 31)

    private fun getLunarYearDays(year: Int): Int {
        var sum = 348
        var i = 0x8000
        while (i > 0x8) {
            if ((LUNAR_INFO[year - 1900] and i) != 0) sum++
            i = i shr 1
        }
        return sum + getLeapMonthDays(year)
    }

    fun getLeapMonth(year: Int): Int {
        return LUNAR_INFO[year - 1900] and 0xf
    }

    private fun getLeapMonthDays(year: Int): Int {
        return if (getLeapMonth(year) != 0) {
            if ((LUNAR_INFO[year - 1900] and 0x10000) != 0) 30 else 29
        } else 0
    }

    private fun getLunarMonthDays(year: Int, month: Int): Int {
        return if ((LUNAR_INFO[year - 1900] and (0x10000 shr month)) != 0) 30 else 29
    }

    fun solarToLunar(date: LocalDate): LunarDateResult {
        var offset = ChronoUnit.DAYS.between(BASE_DATE, date).toInt()
        var year = 1900
        var month = 1
        var day = 1
        var isLeap = false

        while (year < 2100 && offset > 0) {
            val daysInYear = getLunarYearDays(year)
            if (offset < daysInYear) break
            offset -= daysInYear
            year++
        }

        val leapMonth = getLeapMonth(year)
        var isLeapCurrent = false

        for (m in 1..12) {
            if (leapMonth > 0 && m == (leapMonth + 1) && !isLeapCurrent) {
                isLeapCurrent = true
                val daysInLeap = getLeapMonthDays(year)
                if (offset < daysInLeap) {
                    month = m - 1
                    isLeap = true
                    break
                }
                offset -= daysInLeap
            }

            val daysInMonth = getLunarMonthDays(year, m)
            if (offset < daysInMonth) {
                month = m
                break
            }
            offset -= daysInMonth
        }

        day = offset + 1

        val ganZhiYear = getGanZhiYear(year)
        val zodiac = ZODIACS[(year - 4) % 12]
        val festival = getLunarFestival(month, day, isLeap)

        return LunarDateResult(
            year = year,
            month = month,
            day = day,
            isLeapMonth = isLeap,
            lunarMonthName = getLunarMonthName(month),
            lunarDayName = getLunarDayName(day),
            ganZhiYear = ganZhiYear,
            zodiac = zodiac,
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

    /**
     * 计算特定日期的传统老黄历“宜”与“忌”项 (利用 toEpochDay 与强质数 Hash 混合算法，确保天天天变不重样)
     */
    fun getAlmanacYiJi(date: LocalDate): AlmanacYiJi {
        val epochDay = date.toEpochDay()
        val dayHash = abs((epochDay * 1664525L + 1013904223L).toInt())

        val allYi = listOf(
            "祭祀", "祈福", "求嗣", "开光", "出行", "解除", "伐木", "拆卸",
            "修造", "动土", "起基", "安床", "入宅", "开市", "交易", "立券",
            "栽种", "纳畜", "移徙", "理发", "扫舍", "嫁娶", "订盟", "纳采",
            "挂匾", "祭拜", "会亲友", "针灸", "进人口", "安香", "出火"
        )

        val allJi = listOf(
            "安葬", "破土", "作灶", "掘井", "词讼", "探病", "伐木", "架马",
            "安门", "行丧", "乘船", "置产", "针灸", "合脊", "归岫", "开仓",
            "封顶", "分居", "补垣", "塞穴", "筑堤", "平治", "开渠", "架桥"
        )

        val yiCount = 4 + (dayHash % 3)
        val jiCount = 3 + ((dayHash / 7) % 2)

        val selectedYi = mutableListOf<String>()
        for (i in 0 until yiCount) {
            val idx = abs((dayHash * 17 + i * 31 + epochDay.toInt() * 7) % allYi.size)
            val item = allYi[idx]
            if (!selectedYi.contains(item)) selectedYi.add(item)
        }

        val selectedJi = mutableListOf<String>()
        for (i in 0 until jiCount) {
            val idx = abs((dayHash * 23 + i * 19 + epochDay.toInt() * 11) % allJi.size)
            val item = allJi[idx]
            if (!selectedJi.contains(item) && !selectedYi.contains(item)) selectedJi.add(item)
        }

        return AlmanacYiJi(
            yiList = selectedYi,
            jiList = selectedJi
        )
    }
}
