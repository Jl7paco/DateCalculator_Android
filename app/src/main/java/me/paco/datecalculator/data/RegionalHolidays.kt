package me.paco.datecalculator.data

import me.paco.datecalculator.util.LunarCalendarUtils
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 多地区法定节假日与调休/补假数据库 (支持 19 个国家/地区及智能未来节假日预测)
 */
object RegionalHolidays {

    // 获取特定地区全量预设倒计时节日名称清单
    fun getPresetHolidayNames(region: HolidayRegion): List<String> {
        return when (region) {
            HolidayRegion.CHINA -> listOf(
                "🇨🇳 国庆节", "🎆 元旦", "🧧 春节", "🌿 清明节", "🛠️ 五一劳动节", "🎏 端午节", "📚 高考", "📚 中考", "🥮 中秋节"
            )
            HolidayRegion.TAIWAN -> listOf(
                "🎆 开国纪念日", "🧧 春节", "🕊️ 228和平纪念日", "🧸 儿童节", "🌿 清明节", "🛠️ 劳动节", "🎏 端午节", "🥮 中秋节", "🇹🇼 国庆日"
            )
            HolidayRegion.HONG_KONG -> listOf(
                "🎆 元旦", "🧧 农历新年", "✝️ 耶稣受难节", "🌿 清明节", "🛠️ 劳动节", "☸️ 佛诞", "🎏 端午节", "🇭🇰 特区成立纪念日", "🥮 中秋节", "🇨🇳 国庆节", "🏔️ 重阳节", "🎄 圣诞节"
            )
            HolidayRegion.MACAO -> listOf(
                "🎆 元旦", "🧧 农历新年", "✝️ 耶稣受难节", "🌿 清明节", "🛠️ 劳动节", "☸️ 佛诞", "🎏 端午节", "🥮 中秋节", "🇨🇳 国庆节", "🏔️ 重阳节", "🇲🇴 特区成立纪念日", "🎄 圣诞节"
            )
            HolidayRegion.SINGAPORE -> listOf(
                "🎆 元旦", "🧧 农历新年", "✝️ 耶稣受难节", "☪️ 开斋节", "🛠️ 劳动节", "☸️ 卫塞节", "☪️ 哈芝节", "🇸🇬 国庆日", "🪔 屠妖节", "🎄 圣诞节"
            )
            HolidayRegion.MALAYSIA -> listOf(
                "🎆 元旦", "🧧 农历新年", "☪️ 开斋节", "🛠️ 劳动节", "☸️ 卫塞节", "👑 最高元首诞辰", "☪️ 哈芝节", "🇲🇾 独立日", "🇲🇾 马来西亚日", "🪔 屠妖节", "🎄 圣诞节"
            )
            HolidayRegion.VIETNAM -> listOf(
                "🎆 阳历新年", "🧧 越南春节", "👑 雄王祭祖日", "🇻🇳 南方解放日", "🛠️ 国际劳动节", "🇻🇳 国庆节"
            )
            HolidayRegion.JAPAN -> listOf(
                "🎆 元日", "🌸 成人の日", "🌸 建国記念の日", "🌸 天皇誕生日", "🌿 昭和の日", "🎏 憲法記念日", "🌿 みどりの日", "🎏 こどもの日", "🌊 海の日", "⛰️ 山の日", "🍁 敬老の日", "🍁 秋分の日", "🏃 スポーツの日", "🎨 文化の日", "🍂 勤労感謝の日"
            )
            HolidayRegion.SOUTH_KOREA -> listOf(
                "🎆 신정 (元旦)", "🧧 설날 (春节)", "🇰🇷 삼일절 (三一节)", "🧸 어린이날 (儿童节)", "☸️ 부처님 오신 날", "🌾 현충일 (显忠日)", "🇰🇷 광복절 (光复节)", "🥮 추석 (秋夕/中秋)", "🇰🇷 개천절 (开天节)", "🇰🇷 한글날 (韩文节)", "🎄 성탄절 (圣诞节)"
            )
            HolidayRegion.UNITED_KINGDOM -> listOf(
                "🎆 New Year's Day", "✝️ Good Friday", "✝️ Easter Monday", "🇬🇧 Early May Bank Holiday", "🇬🇧 Spring Bank Holiday", "🇬🇧 Summer Bank Holiday", "🎄 Christmas Day", "🎁 Boxing Day"
            )
            HolidayRegion.GERMANY -> listOf(
                "🎆 Neujahr", "✝️ Karfreitag", "✝️ Ostermontag", "🛠️ Tag der Arbeit", "✝️ Christi Himmelfahrt", "✝️ Pfingstmontag", "🇩🇪 Tag der Deutschen Einheit", "🎄 1. Weihnachtstag", "🎄 2. Weihnachtstag"
            )
            HolidayRegion.FRANCE -> listOf(
                "🎆 Jour de l'An", "✝️ Lundi de Pâques", "🛠️ Fête du Travail", "🎖️ Victoire 1945", "✝️ Ascension", "🇫🇷 Fête Nationale", "✝️ Assomption", "✝️ Toussaint", "🎖️ Armistice 1918", "🎄 Noël"
            )
            HolidayRegion.ITALY -> listOf(
                "🎆 Capodanno", "👑 Epifania", "✝️ Lunedì dell'Angelo", "🇮🇹 Festa della Liberazione", "🛠️ Festa del Lavoro", "🇮🇹 Festa della Repubblica", "☀️ Ferragosto", "✝️ Ognissanti", "🎄 Natale", "🎁 Santo Stefano"
            )
            HolidayRegion.INDIA -> listOf(
                "🎆 New Year's Day", "🇮🇳 Republic Day", "🎨 Holi", "✝️ Good Friday", "☪️ Eid al-Fitr", "🇮🇳 Independence Day", "🪔 Diwali", "🇮🇳 Gandhi Jayanti", "🎄 Christmas"
            )
            HolidayRegion.INDONESIA -> listOf(
                "🎆 Tahun Baru Masehi", "🧧 Tahun Baru Imlek", "🇮🇩 Nyepi", "✝️ Wafat Isa Almasih", "🛠️ Hari Buruh", "☸️ Hari Waisak", "🇮🇩 Hari Lahir Pancasila", "☪️ Idul Fitri", "🇮🇩 Hari Kemerdekaan RI", "🎄 Hari Natal"
            )
            HolidayRegion.AUSTRALIA -> listOf(
                "🎆 New Year's Day", "🇦🇺 Australia Day", "✝️ Good Friday", "✝️ Easter Monday", "🎖️ Anzac Day", "👑 King's Birthday", "🛠️ Labour Day", "🎄 Christmas Day", "🎁 Boxing Day"
            )
            HolidayRegion.NEW_ZEALAND -> listOf(
                "🎆 New Year's Day", "🇳🇿 Waitangi Day", "✝️ Good Friday", "✝️ Easter Monday", "🎖️ Anzac Day", "👑 King's Birthday", "🌌 Matariki", "🛠️ Labour Day", "🎄 Christmas Day", "🎁 Boxing Day"
            )
            HolidayRegion.UNITED_STATES -> listOf(
                "🎆 New Year's Day", "🕊️ MLK Day", "🇺🇸 Presidents' Day", "🎖️ Memorial Day", "🕊️ Juneteenth Day", "🇺🇸 Independence Day", "🛠️ Labor Day", "🌎 Columbus Day", "🎖️ Veterans Day", "🎃 Thanksgiving", "🎄 Christmas"
            )
            HolidayRegion.THAILAND -> listOf(
                "🎆 元旦", "☸️ 万佛节", "👑 扎克里王朝纪念日", "💦 宋干节 (泼水节)", "🛠️ 劳动节", "👑 泰王诞辰", "👑 母亲节", "👑 父亲节", "📜 宪法日"
            )
        }
    }

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

    /**
     * 智能未来节假日算法推算 (针对 2027 年及以后的远期推算)
     */
    private fun isChinaPredictedHoliday(date: LocalDate): Boolean {
        val month = date.monthValue
        val day = date.dayOfMonth

        // 1. 元旦 (1/1)
        if (month == 1 && day == 1) return true

        // 2. 五一劳动节 (5/1 - 5/5)
        if (month == 5 && day in 1..5) return true

        // 3. 国庆节 (10/1 - 10/7)
        if (month == 10 && day in 1..7) return true

        // 4. 清明节 (~4/4 或 4/5)
        if (month == 4 && (day == 4 || day == 5)) return true

        // 5. 农历传统节日 (春节农历除夕至正月初六, 端午, 中秋)
        val lunar = LunarCalendarUtils.solarToLunar(date)
        if (!lunar.isLeapMonth) {
            // 春节 (除夕至正月初六)
            if (lunar.month == 12 && lunar.day >= 29) return true
            if (lunar.month == 1 && lunar.day in 1..6) return true
            // 端午 (五月初五)
            if (lunar.month == 5 && lunar.day in 5..7 && date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY, DayOfWeek.MONDAY)) return true
            // 中秋 (八月十五)
            if (lunar.month == 8 && lunar.day in 15..17 && date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY, DayOfWeek.MONDAY)) return true
        }

        return false
    }

    private fun isChinaPredictedShiftWorkday(date: LocalDate): Boolean {
        val year = date.year
        if (year <= 2026) return shiftWorkdaysChina.contains(date)

        // 预测 2027+ 国庆节/五一节/春节前后的调休周末
        val month = date.monthValue
        val day = date.dayOfMonth
        val dayOfWeek = date.dayOfWeek

        // 五一调休补班 (4月底或5月上旬周末)
        if (month == 4 && day in 25..30 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) return true
        if (month == 5 && day in 6..12 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) return true

        // 国庆调休补班 (9月底或10月上旬周末)
        if (month == 9 && day in 25..30 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) return true
        if (month == 10 && day in 8..14 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) return true

        return false
    }

    private fun isChinaHistoricalHoliday(date: LocalDate): Boolean {
        val year = date.year
        if (year < 1949) return false
        if (year in 1999..2026) return holidaysChinaPrecise.contains(date)
        if (year > 2026) return isChinaPredictedHoliday(date)

        val month = date.monthValue
        val day = date.dayOfMonth
        if (month == 1 && day == 1) return true
        if (month == 5 && day == 1) return true
        if (month == 10 && (day == 1 || day == 2)) return true
        val lunar = LunarCalendarUtils.solarToLunar(date)
        return lunar.month == 1 && lunar.day in 1..3 && !lunar.isLeapMonth
    }

    // 欧洲与美洲、亚洲各地区全量公休字典
    private val holidaysUnitedKingdom = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 27), LocalDate.of(2024, 8, 26), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 26), LocalDate.of(2025, 8, 25), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysGermany = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 20), LocalDate.of(2024, 10, 3), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 29), LocalDate.of(2025, 6, 9), LocalDate.of(2025, 10, 3), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysFrance = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 8), LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 20), LocalDate.of(2024, 7, 14), LocalDate.of(2024, 8, 15), LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 11), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 8), LocalDate.of(2025, 5, 29), LocalDate.of(2025, 6, 9), LocalDate.of(2025, 7, 14), LocalDate.of(2025, 8, 15), LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 11), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 7, 14), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 12, 25)
    )

    private val holidaysItaly = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 6), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 25), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 2), LocalDate.of(2024, 8, 15), LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 8), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 6), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 4, 25), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 2), LocalDate.of(2025, 8, 15), LocalDate.of(2025, 11, 1), LocalDate.of(2025, 12, 8), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 2), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysIndia = setOf(
        LocalDate.of(2024, 1, 26), LocalDate.of(2024, 3, 25), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 11), LocalDate.of(2024, 8, 15), LocalDate.of(2024, 10, 2), LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 26), LocalDate.of(2025, 3, 14), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 8, 15), LocalDate.of(2025, 10, 2), LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 26), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 10, 2), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    private val holidaysIndonesia = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 11), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 4, 11), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 23), LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 17), LocalDate.of(2024, 7, 7), LocalDate.of(2024, 8, 17), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 3, 29), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 12), LocalDate.of(2025, 5, 29), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 7), LocalDate.of(2025, 6, 27), LocalDate.of(2025, 8, 17), LocalDate.of(2025, 9, 5), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 3, 19), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 27), LocalDate.of(2026, 6, 16), LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 25), LocalDate.of(2026, 12, 25)
    )

    private val holidaysTaiwan = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 8), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13), LocalDate.of(2024, 2, 14), LocalDate.of(2024, 2, 28), LocalDate.of(2024, 4, 4), LocalDate.of(2024, 4, 5), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 9, 17), LocalDate.of(2024, 10, 10),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 4, 3), LocalDate.of(2025, 4, 4), LocalDate.of(2025, 5, 30), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 10, 10),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 5), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 10)
    )

    private val shiftWorkdaysTaiwan = setOf(
        LocalDate.of(2024, 2, 17),
        LocalDate.of(2025, 2, 8)
    )

    private val holidaysHongKong = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 3, 30), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 4), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 7, 1), LocalDate.of(2024, 9, 18), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 11), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 19), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 31), LocalDate.of(2025, 7, 1), LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 7), LocalDate.of(2025, 10, 29), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 24), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 18), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysMacao = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 4), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 9, 18), LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 2), LocalDate.of(2024, 10, 11), LocalDate.of(2024, 12, 8), LocalDate.of(2024, 12, 20), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 4, 4), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 31), LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 2), LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 4), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 24), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 25)
    )

    private val holidaysSingapore = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22), LocalDate.of(2024, 6, 17), LocalDate.of(2024, 8, 9), LocalDate.of(2024, 10, 31), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 12), LocalDate.of(2025, 6, 7), LocalDate.of(2025, 8, 9), LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 27), LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    private val holidaysMalaysia = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 4, 11), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22), LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 17), LocalDate.of(2024, 7, 7), LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 10, 31), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 12), LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 7), LocalDate.of(2025, 8, 31), LocalDate.of(2025, 9, 16), LocalDate.of(2025, 10, 20), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 27), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 16), LocalDate.of(2026, 11, 8), LocalDate.of(2026, 12, 25)
    )

    private val holidaysVietnam = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 8), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 13), LocalDate.of(2024, 2, 14), LocalDate.of(2024, 4, 18), LocalDate.of(2024, 4, 30), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 9, 2), LocalDate.of(2024, 9, 3),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 4, 7), LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 2),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 4, 26), LocalDate.of(2026, 4, 30), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)
    )

    private val holidaysJapan = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 23), LocalDate.of(2024, 3, 20), LocalDate.of(2024, 4, 29), LocalDate.of(2024, 5, 3), LocalDate.of(2024, 5, 4), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 7, 15), LocalDate.of(2024, 8, 11), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 9, 22), LocalDate.of(2024, 10, 14), LocalDate.of(2024, 11, 3), LocalDate.of(2024, 11, 23),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 13), LocalDate.of(2025, 2, 11), LocalDate.of(2025, 2, 23), LocalDate.of(2025, 3, 20), LocalDate.of(2025, 4, 29), LocalDate.of(2025, 5, 3), LocalDate.of(2025, 5, 4), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 7, 21), LocalDate.of(2025, 8, 11), LocalDate.of(2025, 9, 15), LocalDate.of(2025, 10, 13), LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 23),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 23), LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 5), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 11), LocalDate.of(2026, 9, 21), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 23)
    )

    private val holidaysSouthKorea = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 6), LocalDate.of(2024, 8, 15), LocalDate.of(2024, 9, 16), LocalDate.of(2024, 9, 17), LocalDate.of(2024, 10, 3), LocalDate.of(2024, 10, 9), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 28), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 1, 30), LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 5), LocalDate.of(2025, 6, 6), LocalDate.of(2025, 8, 15), LocalDate.of(2025, 10, 3), LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 10, 7), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 5), LocalDate.of(2026, 6, 6), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 9), LocalDate.of(2026, 12, 25)
    )

    private val holidaysAustralia = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 26), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 25), LocalDate.of(2024, 6, 10), LocalDate.of(2024, 10, 7), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 27), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 4, 25), LocalDate.of(2025, 6, 9), LocalDate.of(2025, 10, 6), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 6, 8), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysNewZealand = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 6), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 25), LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 28), LocalDate.of(2024, 10, 28), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), LocalDate.of(2025, 2, 6), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 4, 25), LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 20), LocalDate.of(2025, 10, 27), LocalDate.of(2025, 12, 25), LocalDate.of(2025, 12, 26),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), LocalDate.of(2026, 2, 6), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 10), LocalDate.of(2026, 10, 26), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)
    )

    private val holidaysUnitedStates = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 19), LocalDate.of(2024, 5, 27), LocalDate.of(2024, 6, 19), LocalDate.of(2024, 7, 4), LocalDate.of(2024, 9, 2), LocalDate.of(2024, 10, 14), LocalDate.of(2024, 11, 11), LocalDate.of(2024, 11, 28), LocalDate.of(2024, 12, 25),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 20), LocalDate.of(2025, 2, 17), LocalDate.of(2025, 5, 26), LocalDate.of(2025, 6, 19), LocalDate.of(2025, 7, 4), LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 13), LocalDate.of(2025, 11, 11), LocalDate.of(2025, 11, 27), LocalDate.of(2025, 12, 25),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 19), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 19), LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 4), LocalDate.of(2026, 9, 7), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 26), LocalDate.of(2026, 12, 25)
    )

    private val holidaysThailand = setOf(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 26), LocalDate.of(2024, 4, 6), LocalDate.of(2024, 4, 13), LocalDate.of(2024, 4, 14), LocalDate.of(2024, 4, 15), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 22), LocalDate.of(2024, 6, 3), LocalDate.of(2024, 7, 28), LocalDate.of(2024, 8, 12), LocalDate.of(2024, 10, 13), LocalDate.of(2024, 10, 23), LocalDate.of(2024, 12, 5), LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 31),
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 12), LocalDate.of(2025, 4, 6), LocalDate.of(2025, 4, 13), LocalDate.of(2025, 4, 14), LocalDate.of(2025, 4, 15), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 11), LocalDate.of(2025, 6, 3), LocalDate.of(2025, 7, 28), LocalDate.of(2025, 8, 12), LocalDate.of(2025, 10, 13), LocalDate.of(2025, 10, 23), LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 31),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 3), LocalDate.of(2026, 7, 28), LocalDate.of(2026, 8, 12), LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 23), LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 31)
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
            HolidayRegion.UNITED_KINGDOM -> holidaysUnitedKingdom.contains(date)
            HolidayRegion.GERMANY -> holidaysGermany.contains(date)
            HolidayRegion.FRANCE -> holidaysFrance.contains(date)
            HolidayRegion.ITALY -> holidaysItaly.contains(date)
            HolidayRegion.INDIA -> holidaysIndia.contains(date)
            HolidayRegion.INDONESIA -> holidaysIndonesia.contains(date)
            HolidayRegion.AUSTRALIA -> holidaysAustralia.contains(date)
            HolidayRegion.NEW_ZEALAND -> holidaysNewZealand.contains(date)
            HolidayRegion.UNITED_STATES -> holidaysUnitedStates.contains(date)
            HolidayRegion.THAILAND -> holidaysThailand.contains(date)
        }
    }

    fun isShiftWorkday(date: LocalDate, region: HolidayRegion): Boolean {
        return when (region) {
            HolidayRegion.CHINA -> isChinaPredictedShiftWorkday(date)
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
            HolidayRegion.UNITED_KINGDOM -> holidaysUnitedKingdom.size
            HolidayRegion.GERMANY -> holidaysGermany.size
            HolidayRegion.FRANCE -> holidaysFrance.size
            HolidayRegion.ITALY -> holidaysItaly.size
            HolidayRegion.INDIA -> holidaysIndia.size
            HolidayRegion.INDONESIA -> holidaysIndonesia.size
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
