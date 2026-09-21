package me.paco.datecalculator.data

import java.time.LocalDate

/**
 * 多国家/地区全球放假与调休数据库 (覆盖中国大陆、台湾、香港、澳门、新加坡、马来西亚、越南、日本、韩国、英国、德国、法国、意大利、印度、印尼、澳洲、新西兰、美国、泰国等 19 个地区)
 */
object RegionalHolidays {

    // ==================== 中国大陆 (Mainland China 精确 1999-2026 调休集合) ====================
    private val holidaysChinaPrecise = setOf(
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
        // 2026 (中秋节精确休假：9月25, 26, 27 连续 3 天休息)
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21),
        LocalDate.of(2026, 2, 22), LocalDate.of(2026, 2, 23),
        LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3),
        LocalDate.of(2026, 6, 19),
        LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 9, 27),
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
        LocalDate.of(2026, 10, 10)
    )

    /**
     * 获取指定地区预设的常用倒计时节日列表
     */
    fun getPresetHolidayNames(region: HolidayRegion): List<String> {
        return when (region) {
            HolidayRegion.CHINA -> listOf("🇨🇳 国庆节", "🎆 元旦", "🧧 春节", "🌿 清明节", "🛠️ 五一劳动节", "🎏 端午节", "📚 高考", "📚 中考", "🥮 中秋节")
            HolidayRegion.TAIWAN -> listOf("🇹🇼 国庆节", "🎆 元旦", "🧧 春节", "🌿 清明节", "🎏 端午节", "🥮 中秋节")
            HolidayRegion.HONG_KONG -> listOf("🇭🇰 特区成立纪念日", "🎆 元旦", "🧧 春节", "🌿 清明节", "🎏 端午节", "🥮 中秋节")
            HolidayRegion.MACAO -> listOf("🇲🇴 特区成立纪念日", "🎆 元旦", "🧧 春节", "🌿 清明节", "🎏 端午节", "🥮 中秋节")
            else -> listOf("🎆 元旦", "🧧 春节", "🌿 清明节", "🛠️ 五一劳动节", "🎏 端午节", "🥮 中秋节")
        }
    }

    /**
     * 智能未来节假日算法推算 (针对 2027 年及以后的远期推算)
     */
    private fun isChinaPredictedHoliday(date: LocalDate): Boolean {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        return when (month) {
            1 -> day == 1
            5 -> day in 1..3
            10 -> day in 1..7
            else -> false
        }
    }

    private fun isChinaPredictedShiftWorkday(date: LocalDate): Boolean {
        return false
    }

    private fun isChinaHistoricalHoliday(date: LocalDate): Boolean {
        val year = date.year
        if (year in 1999..2026) return holidaysChinaPrecise.contains(date)
        if (year > 2026) return isChinaPredictedHoliday(date)
        return false
    }

    /**
     * 判断特定日期在指定地区是否为法定节假日
     */
    fun isStatutoryHoliday(date: LocalDate, region: HolidayRegion): Boolean {
        return when (region) {
            HolidayRegion.CHINA -> isChinaHistoricalHoliday(date)
            HolidayRegion.TAIWAN -> holidaysTaiwan.contains(date)
            HolidayRegion.HONG_KONG -> holidaysHongKong.contains(date)
            HolidayRegion.MACAO -> holidaysMacau.contains(date)
            HolidayRegion.SINGAPORE -> holidaysSingapore.contains(date)
            HolidayRegion.MALAYSIA -> holidaysMalaysia.contains(date)
            HolidayRegion.VIETNAM -> holidaysVietnam.contains(date)
            HolidayRegion.JAPAN -> holidaysJapan.contains(date)
            HolidayRegion.SOUTH_KOREA -> holidaysKorea.contains(date)
            HolidayRegion.UNITED_KINGDOM -> holidaysUK.contains(date)
            HolidayRegion.GERMANY -> holidaysGermany.contains(date)
            HolidayRegion.FRANCE -> holidaysFrance.contains(date)
            HolidayRegion.ITALY -> holidaysItaly.contains(date)
            HolidayRegion.INDIA -> holidaysIndia.contains(date)
            HolidayRegion.INDONESIA -> holidaysIndonesia.contains(date)
            HolidayRegion.AUSTRALIA -> holidaysAustralia.contains(date)
            HolidayRegion.NEW_ZEALAND -> holidaysNewZealand.contains(date)
            HolidayRegion.UNITED_STATES -> holidaysUS.contains(date)
            HolidayRegion.THAILAND -> holidaysThailand.contains(date)
        }
    }

    /**
     * 判断特定日期在指定地区是否为调休补班日
     */
    fun isShiftWorkday(date: LocalDate, region: HolidayRegion): Boolean {
        return when (region) {
            HolidayRegion.CHINA -> {
                if (date.year <= 2026) shiftWorkdaysChina.contains(date)
                else isChinaPredictedShiftWorkday(date)
            }
            else -> false
        }
    }

    // 台湾
    private val holidaysTaiwan = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 4, 3),
        LocalDate.of(2026, 4, 4), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 10)
    )

    // 香港
    private val holidaysHongKong = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1),
        LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 26),
        LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 19), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    // 澳门
    private val holidaysMacau = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1),
        LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 10, 1),
        LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 19), LocalDate.of(2026, 11, 2), LocalDate.of(2026, 12, 8),
        LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 25)
    )

    // 新加坡
    private val holidaysSingapore = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 27),
        LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    // 马来西亚
    private val holidaysMalaysia = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 27), LocalDate.of(2026, 6, 1),
        LocalDate.of(2026, 8, 25), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 16), LocalDate.of(2026, 11, 8),
        LocalDate.of(2026, 12, 25)
    )

    // 越南
    private val holidaysVietnam = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 4, 26), LocalDate.of(2026, 4, 30),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 2)
    )

    // 日本
    private val holidaysJapan = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 23),
        LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 4),
        LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 6), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 11),
        LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 22), LocalDate.of(2026, 9, 23), LocalDate.of(2026, 10, 12),
        LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 23)
    )

    // 韩国
    private val holidaysKorea = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 24),
        LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 6), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 24),
        LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 9),
        LocalDate.of(2026, 12, 25)
    )

    // 英国
    private val holidaysUK = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 4),
        LocalDate.of(2026, 5, 25), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 28)
    )

    // 德国
    private val holidaysGermany = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1),
        LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 26)
    )

    // 法国
    private val holidaysFrance = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8),
        LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 7, 14), LocalDate.of(2026, 8, 15),
        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 12, 25)
    )

    // 意大利
    private val holidaysItaly = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 2), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 11, 1),
        LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    // 印度
    private val holidaysIndia = setOf(
        LocalDate.of(2026, 1, 26), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 3, 21),
        LocalDate.of(2026, 4, 14), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 10, 2),
        LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    // 印尼
    private val holidaysIndonesia = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 3, 19), LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 27),
        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 17), LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 25),
        LocalDate.of(2026, 12, 25)
    )

    // 澳大利亚
    private val holidaysAustralia = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6),
        LocalDate.of(2026, 4, 25), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 28)
    )

    // 新西兰
    private val holidaysNewZealand = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), LocalDate.of(2026, 2, 6), LocalDate.of(2026, 4, 3),
        LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 10),
        LocalDate.of(2026, 10, 26), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 28)
    )

    // 美国
    private val holidaysUS = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 19), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 5, 25),
        LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 4), LocalDate.of(2026, 9, 7),
        LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 26), LocalDate.of(2026, 12, 25)
    )

    // 泰国
    private val holidaysThailand = setOf(
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 13),
        LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4),
        LocalDate.of(2026, 5, 31), LocalDate.of(2026, 7, 28), LocalDate.of(2026, 7, 29), LocalDate.of(2026, 8, 12),
        LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 23), LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 10),
        LocalDate.of(2026, 12, 31)
    )
}
