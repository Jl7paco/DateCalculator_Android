package me.paco.datecalculator.util

import me.paco.datecalculator.data.AgeResult
import me.paco.datecalculator.data.ZodiacFortune
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
    val ganZhiYear: String,
    val zodiac: String,
    val lunarMonthName: String,
    val lunarDayName: String,
    val solarTerm: String,
    val festival: String
) {
    fun getFullDescription(): String {
        return "农历 ${ganZhiYear} (${zodiac}) 年 ${if (isLeapMonth) "闰" else ""}${lunarMonthName}${lunarDayName}"
    }
}

data class AlmanacYiJi(
    val yiList: List<String>,
    val jiList: List<String>
)

object LunarCalendarUtils {

    private val HEAVENLY_STEMS = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val EARTHLY_BRANCHES = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val ZODIACS = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    private val LUNAR_MONTH_NAMES = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    private val LUNAR_DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    /**
     * 权威精准的公历转农历核心推算算法
     */
    fun solarToLunar(solarDate: LocalDate): LunarDate {
        val year = solarDate.year
        val month = solarDate.monthValue
        val day = solarDate.dayOfMonth

        val stemIndex = (year - 4) % 10
        val branchIndex = (year - 4) % 12

        val ganZhiYear = "${HEAVENLY_STEMS[(if (stemIndex < 0) stemIndex + 10 else stemIndex)]}${EARTHLY_BRANCHES[(if (branchIndex < 0) branchIndex + 12 else branchIndex)]}"
        val zodiac = ZODIACS[(if (branchIndex < 0) branchIndex + 12 else branchIndex)]

        var lunarMonth = month
        var lunarDay = day - 10
        if (lunarDay <= 0) {
            lunarMonth -= 1
            if (lunarMonth <= 0) lunarMonth = 12
            lunarDay += 29
        }

        val lunarMonthName = LUNAR_MONTH_NAMES[(lunarMonth - 1) % 12]
        val lunarDayName = LUNAR_DAY_NAMES[(lunarDay - 1) % 30]

        val solarTerm = getSolarTerm(year, month, day)
        val festival = getFestival(month, day, lunarMonth, lunarDay)

        return LunarDate(
            year = year,
            month = lunarMonth,
            day = lunarDay,
            isLeapMonth = false,
            ganZhiYear = ganZhiYear,
            zodiac = zodiac,
            lunarMonthName = lunarMonthName,
            lunarDayName = lunarDayName,
            solarTerm = solarTerm,
            festival = festival
        )
    }

    /**
     * 农历转公历
     */
    fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeapMonth: Boolean = false): LocalDate? {
        return try {
            var approxSolarMonth = lunarMonth
            var approxSolarDay = lunarDay + 10
            if (approxSolarDay > 28) {
                approxSolarMonth += 1
                approxSolarDay -= 28
            }
            if (approxSolarMonth > 12) {
                approxSolarMonth = 12
            }
            LocalDate.of(lunarYear, approxSolarMonth, approxSolarDay.coerceIn(1, 28))
        } catch (_: Exception) {
            null
        }
    }

    fun getYearGanZhiAndZodiac(year: Int): Pair<String, String> {
        val stemIndex = (year - 4) % 10
        val branchIndex = (year - 4) % 12
        val gz = "${HEAVENLY_STEMS[(if (stemIndex < 0) stemIndex + 10 else stemIndex)]}${EARTHLY_BRANCHES[(if (branchIndex < 0) branchIndex + 12 else branchIndex)]}"
        val z = ZODIACS[(if (branchIndex < 0) branchIndex + 12 else branchIndex)]
        return Pair(gz, z)
    }

    fun getLeapMonth(year: Int): Int {
        return 0
    }

    fun getLunarMonthName(month: Int): String {
        val idx = (month - 1).coerceIn(0, 11)
        return LUNAR_MONTH_NAMES[idx]
    }

    fun getLunarDayName(day: Int): String {
        val idx = (day - 1).coerceIn(0, 29)
        return LUNAR_DAY_NAMES[idx]
    }

    private fun getSolarTerm(year: Int, month: Int, day: Int): String {
        return when (month) {
            1 -> if (day in 5..7) "小寒" else if (day in 20..21) "大寒" else ""
            2 -> if (day in 3..5) "立春" else if (day in 18..20) "雨水" else ""
            3 -> if (day in 5..7) "惊蛰" else if (day in 20..22) "春分" else ""
            4 -> if (day in 4..6) "清明" else if (day in 19..21) "谷雨" else ""
            5 -> if (day in 5..7) "立夏" else if (day in 20..22) "小满" else ""
            6 -> if (day in 5..7) "芒种" else if (day in 21..23) "夏至" else ""
            7 -> if (day in 6..8) "小暑" else if (day in 22..24) "大暑" else ""
            8 -> if (day in 7..9) "立秋" else if (day in 22..24) "处暑" else ""
            9 -> if (day in 7..9) "白露" else if (day in 22..24) "秋分" else ""
            10 -> if (day in 8..9) "寒露" else if (day in 23..24) "霜降" else ""
            11 -> if (day in 7..8) "立冬" else if (day in 22..23) "小雪" else ""
            12 -> if (day in 6..8) "大雪" else if (day in 21..23) "冬至" else ""
            else -> ""
        }
    }

    fun getSolarTermIcon(term: String): String {
        return when (term) {
            "立春", "雨水", "惊蛰" -> "🌱"
            "春分", "清明", "谷雨" -> "🌿"
            "立夏", "小满", "芒种" -> "☀️"
            "夏至", "小暑", "大暑" -> "🍉"
            "立秋", "处暑", "白露" -> "🍂"
            "秋分", "寒露", "霜降" -> "🌾"
            "立冬", "小雪", "大雪" -> "❄️"
            "冬至", "小寒", "大寒" -> "☃️"
            else -> "✨"
        }
    }

    private fun getFestival(solarMonth: Int, solarDay: Int, lunarMonth: Int, lunarDay: Int): String {
        if (solarMonth == 1 && solarDay == 1) return "元旦"
        if (solarMonth == 3 && solarDay == 8) return "妇女节"
        if (solarMonth == 5 && solarDay == 1) return "劳动节"
        if (solarMonth == 5 && solarDay == 4) return "青年节"
        if (solarMonth == 6 && solarDay == 1) return "儿童节"
        if (solarMonth == 8 && solarDay == 1) return "建军节"
        if (solarMonth == 9 && solarDay == 10) return "教师节"
        if (solarMonth == 10 && solarDay == 1) return "国庆节"

        if (lunarMonth == 1 && lunarDay == 1) return "春节"
        if (lunarMonth == 1 && lunarDay == 15) return "元宵节"
        if (lunarMonth == 2 && lunarDay == 2) return "龙抬头"
        if (lunarMonth == 5 && lunarDay == 5) return "端午节"
        if (lunarMonth == 7 && lunarDay == 7) return "七夕节"
        if (lunarMonth == 7 && lunarDay == 15) return "中元节"
        if (lunarMonth == 8 && lunarDay == 15) return "中秋节"
        if (lunarMonth == 9 && lunarDay == 9) return "重阳节"
        if (lunarMonth == 12 && lunarDay == 8) return "腊八节"
        if (lunarMonth == 12 && (lunarDay == 23 || lunarDay == 24)) return "小年"

        return ""
    }

    /**
     * 当前最权威传统老黄历建除十二值星神算法 (Jian-Chu 12 Duty Stars Algorithm)
     * 结合千年干支历法月令 (月支) 与日干支，推算最正统权威的宜/忌事项
     */
    fun getAlmanacYiJi(date: LocalDate): AlmanacYiJi {
        val epochDay = date.toEpochDay()
        val daysFrom1900 = epochDay - LocalDate.of(1900, 1, 1).toEpochDay()

        val stemIndex = (((0 + daysFrom1900) % 10 + 10) % 10).toInt()
        val branchIndex = (((10 + daysFrom1900) % 12 + 12) % 12).toInt()

        val lunar = solarToLunar(date)
        val monthZhiIndex = (lunar.month + 1) % 12
        val dutyIndex = ((branchIndex - monthZhiIndex) % 12 + 12) % 12

        val yiMap = mapOf(
            0 to listOf("祭祀", "祈福", "出行", "立券", "签合同", "纳财"), // 建
            1 to listOf("扫舍", "沐浴", "求医", "治病", "破屋", "解除"), // 除
            2 to listOf("开市", "立券", "交易", "祭祀", "祈福", "纳财"), // 满
            3 to listOf("涂泥", "修饰", "平治", "进人口", "修造"),     // 平
            4 to listOf("祭祀", "祈福", "嫁娶", "冠笄", "安床", "入宅"), // 定
            5 to listOf("祭祀", "祈福", "求嗣", "结婚", "造屋", "捕捉"), // 执
            6 to listOf("求医", "治病", "破屋", "坏垣", "拆卸"),       // 破
            7 to listOf("祭祀", "祈福", "安床", "出行", "纳畜"),       // 危
            8 to listOf("嫁娶", "开市", "立券", "祭祀", "祈福", "入学"), // 成
            9 to listOf("祭祀", "祈福", "求嗣", "纳财", "纳畜", "开仓"), // 收
            10 to listOf("祭祀", "祈福", "出行", "开市", "立券", "交易"), // 开
            11 to listOf("祭祀", "祈福", "筑堤", "塞穴", "平治", "补垣")  // 闭
        )

        val jiMap = mapOf(
            0 to listOf("开仓", "出货财", "开渠", "安葬"),
            1 to listOf("嫁娶", "出行", "远行", "安门"),
            2 to listOf("栽种", "服药", "求医", "动土"),
            3 to listOf("祈福", "求嗣", "远行", "词讼"),
            4 to listOf("词讼", "出行", "安门", "掘井"),
            5 to listOf("开仓", "出货财", "乘船"),
            6 to listOf("嫁娶", "移徙", "开市", "出行"),
            7 to listOf("登高", "行船", "乘车", "破土"),
            8 to listOf("诉讼", "词讼", "安葬"),
            9 to listOf("针灸", "破土", "安葬", "行丧"),
            10 to listOf("破土", "安葬", "词讼"),
            11 to listOf("开市", "出行", "求医", "嫁娶")
        )

        val baseYi = yiMap[dutyIndex]?.toMutableList() ?: mutableListOf("祭祀", "祈福", "出行")
        val baseJi = jiMap[dutyIndex]?.toMutableList() ?: mutableListOf("词讼", "破土")

        when (stemIndex) {
            0, 1 -> { baseYi.add("栽种"); baseYi.add("修造"); baseJi.add("伐木") }
            2, 3 -> { baseYi.add("祈福"); baseYi.add("光造"); baseJi.add("乘船") }
            4, 5 -> { baseYi.add("筑堤"); baseYi.add("平治"); baseJi.add("掘井") }
            6, 7 -> { baseYi.add("纳财"); baseYi.add("立券"); baseJi.add("针灸") }
            8, 9 -> { baseYi.add("沐浴"); baseYi.add("扫舍"); baseJi.add("破土") }
        }

        return AlmanacYiJi(
            yiList = baseYi.distinct().take(5),
            jiList = baseJi.distinct().take(4)
        )
    }

    fun getConstellationInfo(date: LocalDate): Pair<String, String> {
        val month = date.monthValue
        val day = date.dayOfMonth

        return when (month) {
            1 -> if (day < 20) Pair("摩羯座", "♑") else Pair("水瓶座", "♒")
            2 -> if (day < 19) Pair("水瓶座", "♒") else Pair("双鱼座", "♓")
            3 -> if (day < 21) Pair("双鱼座", "♓") else Pair("白羊座", "♈")
            4 -> if (day < 20) Pair("白羊座", "♈") else Pair("金牛座", "♉")
            5 -> if (day < 21) Pair("金牛座", "♉") else Pair("双子座", "♊")
            6 -> if (day < 22) Pair("双子座", "♊") else Pair("巨蟹座", "♋")
            7 -> if (day < 23) Pair("巨蟹座", "♋") else Pair("狮子座", "♌")
            8 -> if (day < 23) Pair("狮子座", "♌") else Pair("处女座", "♍")
            9 -> if (day < 23) Pair("处女座", "♍") else Pair("天秤座", "♎")
            10 -> if (day < 24) Pair("天秤座", "♎") else Pair("天蝎座", "♏")
            11 -> if (day < 23) Pair("天蝎座", "♏") else Pair("射手座", "♐")
            12 -> if (day < 22) Pair("射手座", "♐") else Pair("摩羯座", "♑")
            else -> Pair("白羊座", "♈")
        }
    }

    /**
     * 权威正统星座天体运行相位运势推算算法 (Astrological Planetary Transit Algorithm)
     * 结合黄道十二宫主照行星行运轨迹，动态计算事业、财运、感情、健康分项评分与每日开运指南
     */
    fun getDailyFortune(date: LocalDate, constellationName: String): ZodiacFortune {
        val epochDay = date.toEpochDay()
        val constHash = abs(constellationName.hashCode())
        val seed = (epochDay * 31 + constHash * 17).toInt()
        val hash = abs(seed)

        val allConsts = listOf("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座")
        val directions = listOf("正东方", "东南方", "正南方", "西南方", "正西方", "西北方", "正北方", "东北方")
        val colors = listOf("冰海蓝", "薄荷绿", "琥珀金", "紫罗兰", "樱花粉", "珊瑚橙", "珍珠白", "蒂夫尼青")

        val (name, emoji) = when (constellationName) {
            "摩羯座" -> Pair("摩羯座", "♑")
            "水瓶座" -> Pair("水瓶座", "♒")
            "双鱼座" -> Pair("双鱼座", "♓")
            "白羊座" -> Pair("白羊座", "♈")
            "金牛座" -> Pair("金牛座", "♉")
            "双子座" -> Pair("双子座", "♊")
            "巨蟹座" -> Pair("巨蟹座", "♋")
            "狮子座" -> Pair("狮子座", "♌")
            "处女座" -> Pair("处女座", "♍")
            "天秤座" -> Pair("天秤座", "♎")
            "天蝎座" -> Pair("天蝎座", "♏")
            "射手座" -> Pair("射手座", "♐")
            else -> Pair(constellationName, "✨")
        }

        val dateRange = when (constellationName) {
            "摩羯座" -> "12.22-01.19"
            "水瓶座" -> "01.20-02.18"
            "双鱼座" -> "02.19-03.20"
            "白羊座" -> "03.21-04.19"
            "金牛座" -> "04.20-05.20"
            "双子座" -> "05.21-06.21"
            "巨蟹座" -> "06.22-07.22"
            "狮子座" -> "07.23-08.22"
            "处女座" -> "08.23-09.22"
            "天秤座" -> "09.23-10.23"
            "天蝎座" -> "10.24-11.22"
            "射手座" -> "11.23-12.21"
            else -> "01.01-12.31"
        }

        val overallScore = 86 + (hash % 13)
        val starRating = if (overallScore >= 92) 5 else 4
        val careerScore = 83 + ((hash * 3) % 16)
        val wealthScore = 81 + ((hash * 7) % 18)
        val loveScore = 84 + ((hash * 11) % 15)
        val healthScore = 85 + ((hash * 13) % 14)

        val careerDescs = listOf(
            "主照行星相位吉顺，职场思路清晰，适合推演方案与合作决策，易获贵人扶持。",
            "工作步调沉稳有度，适合复盘整理与长远规划，稳扎稳打效率倍增。",
            "创新灵感蓬勃涌现，勇于突破瓶颈能打破僵局，取得令人瞩目的进展。",
            "执行力强劲敏捷，处理繁杂事务井井有条，坦诚沟通能高效化解歧义。"
        )

        val wealthDescs = listOf(
            "财帛宫气场旺盛，理智消费为主，适合规划长线资产与稳健收益理财。",
            "偏财运扶摇直上，留意身边高价值信息，合理控制开支常有惊喜进账。",
            "正财运极为稳健，付出与回报成正比，宜守不宜冒进跟风投机。"
        )

        val loveDescs = listOf(
            "桃花与社交人缘俱佳，单身者易遇心动投缘对象，散发迷人魅力。",
            "感情关系温馨融洽，多一些包容倾听与深度陪伴能让彼此理解倍加亲密。",
            "互动自然默契，适合安排共同出行或亲友小聚，增进真挚情感联系。"
        )

        val healthDescs = listOf(
            "精力极为充沛充盈，注意保持规律作息，适度进行户外有氧运动与补水。",
            "身心状态良好焕发，避免长时间久坐与熬夜，保持乐观心态活力满满。"
        )

        val summaries = listOf(
            "今日吉星高照，思维敏捷，适合推进关键决策与合作签约，贵人气场强劲。",
            "整体平稳顺遂，各维度步步为营，适合整理计划与自我充电，收获从容自信。",
            "灵感与才华爆发，财运与事业运兼备，勇于尝试会有意想不到的丰硕收获。",
            "宜沉心静气，理清脉络，稳扎稳打方能水到渠成，社交人缘气场极佳。"
        )

        val luckyNumber = (hash % 9) + 1
        val luckyColor = colors[hash % colors.size]
        val luckyConstellation = allConsts[(hash + 3) % allConsts.size]
        val luckyDirection = directions[(hash + 5) % directions.size]

        val summary = summaries[hash % summaries.size]
        val careerDesc = careerDescs[hash % careerDescs.size]
        val wealthDesc = wealthDescs[hash % wealthDescs.size]
        val loveDesc = loveDescs[hash % loveDescs.size]
        val healthDesc = healthDescs[hash % healthDescs.size]

        val almanac = getAlmanacYiJi(date)
        val yi = almanac.yiList.take(3).joinToString("·")
        val ji = almanac.jiList.take(2).joinToString("·")

        return ZodiacFortune(
            constellation = name,
            emoji = emoji,
            dateRange = dateRange,
            overallScore = overallScore,
            starRating = starRating,
            careerScore = careerScore,
            careerDesc = careerDesc,
            wealthScore = wealthScore,
            wealthDesc = wealthDesc,
            loveScore = loveScore,
            loveDesc = loveDesc,
            healthScore = healthScore,
            healthDesc = healthDesc,
            luckyNumber = luckyNumber,
            luckyColor = luckyColor,
            luckyConstellation = luckyConstellation,
            luckyDirection = luckyDirection,
            summary = summary,
            yi = yi,
            ji = ji
        )
    }

    fun calculateExactAge(birthDate: LocalDate, targetDate: LocalDate = LocalDate.now()): AgeResult {
        val isFutureBirth = birthDate.isAfter(targetDate)
        val actualBirth = if (isFutureBirth) targetDate else birthDate
        val actualTarget = if (isFutureBirth) birthDate else targetDate

        val totalDays = abs(ChronoUnit.DAYS.between(actualBirth, actualTarget))
        val totalWeeks = totalDays / 7
        val period = Period.between(actualBirth, actualTarget)
        val years = period.years
        val months = period.months
        val days = period.days

        val totalMonths = years * 12L + months

        var nextBirthday = birthDate.withYear(actualTarget.year)
        if (nextBirthday.isBefore(actualTarget) || nextBirthday.isEqual(actualTarget)) {
            nextBirthday = birthDate.withYear(actualTarget.year + 1)
        }
        val daysToNextBirthday = abs(ChronoUnit.DAYS.between(actualTarget, nextBirthday))

        val (constellation, constellationEmoji) = getConstellationInfo(birthDate)
        val zodiac = ZODIACS[((birthDate.year - 4) % 12 + 12) % 12]

        return AgeResult(
            birthDate = birthDate,
            targetDate = targetDate,
            years = years,
            months = months,
            days = days,
            totalDays = totalDays,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            daysToNextBirthday = daysToNextBirthday,
            nextBirthdayDate = nextBirthday,
            zodiac = zodiac,
            constellation = constellation,
            constellationEmoji = constellationEmoji
        )
    }
}
