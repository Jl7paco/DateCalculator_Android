package me.paco.datecalculator.util

import me.paco.datecalculator.data.AgeResult
import me.paco.datecalculator.data.AppLanguage
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
    val solarTerm: String = "",
    val festival: String = ""
) {
    fun getFullDescription(): String {
        val leapTag = if (isLeapMonth) "闰" else ""
        val termTag = if (solarTerm.isNotEmpty()) " [$solarTerm]" else ""
        val festTag = if (festival.isNotEmpty()) " 🎉$festival" else ""
        return "农历 ${ganZhiYear} (${zodiac}) 年 $leapTag$lunarMonthName$lunarDayName$termTag$festTag"
    }
}

data class AlmanacYiJi(
    val yiList: List<String>,
    val jiList: List<String>
)

object LunarCalendarUtils {

    private val LUNAR_MONTH_NAMES = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    private val LUNAR_DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    private val TIANGAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val DIZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val ZODIACS = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    fun getLunarMonthName(month: Int): String {
        return if (month in 1..12) LUNAR_MONTH_NAMES[month - 1] else "${month}月"
    }

    fun getLunarDayName(day: Int): String {
        return if (day in 1..30) LUNAR_DAY_NAMES[day - 1] else "${day}日"
    }

    fun getYearGanZhiAndZodiac(year: Int): String {
        val ganIndex = (year - 4) % 10
        val zhiIndex = (year - 4) % 12
        val gan = TIANGAN[(ganIndex + 10) % 10]
        val zhi = DIZHI[(zhiIndex + 12) % 12]
        val zodiac = ZODIACS[(zhiIndex + 12) % 12]
        return "$gan$zhi ($zodiac)"
    }

    fun solarToLunar(date: LocalDate): LunarDate {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val ganZhiZodiac = getYearGanZhiAndZodiac(year)
        val parts = ganZhiZodiac.split(" ")
        val ganZhiYear = parts[0]
        val zodiac = parts.getOrNull(1)?.replace("(", "")?.replace(")", "") ?: "马"

        val epochDay = date.toEpochDay()
        val lunarDay = ((epochDay % 29) + 1).toInt()
        val lunarMonth = ((month + (if (day < 15) 11 else 0)) % 12) + 1

        val term = getSolarTerm(date)
        val festival = getFestival(month, day, lunarMonth, lunarDay)

        return LunarDate(
            year = year,
            month = lunarMonth,
            day = lunarDay,
            isLeapMonth = false,
            ganZhiYear = ganZhiYear,
            zodiac = zodiac,
            lunarMonthName = getLunarMonthName(lunarMonth),
            lunarDayName = getLunarDayName(lunarDay),
            solarTerm = term,
            festival = festival
        )
    }

    fun getLeapMonth(year: Int): Int {
        return when (year) {
            2023 -> 2
            2025 -> 6
            2028 -> 5
            else -> 0
        }
    }

    fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): LocalDate? {
        if (year !in 1900..2100) return null
        val safeMonth = month.coerceIn(1, 12)
        val safeDay = day.coerceIn(1, 30)

        return try {
            val estimatedSolarMonth = if (safeMonth >= 11) safeMonth - 10 else safeMonth + 1
            val estimatedYear = if (safeMonth >= 11) year + 1 else year

            val isLeapYear = (estimatedYear % 4 == 0 && estimatedYear % 100 != 0) || (estimatedYear % 400 == 0)
            val maxDays = when (estimatedSolarMonth) {
                2 -> if (isLeapYear) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }

            LocalDate.of(estimatedYear, estimatedSolarMonth, safeDay.coerceAtMost(maxDays))
        } catch (_: Exception) {
            LocalDate.of(year, 1, 1)
        }
    }

    private fun getSolarTerm(date: LocalDate): String {
        val month = date.monthValue
        val day = date.dayOfMonth

        return when (month) {
            1 -> if (day in 5..7) "小寒" else if (day in 20..22) "大寒" else ""
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

    fun getLocalizedSolarTerm(term: String, language: AppLanguage): String {
        if (term.isEmpty()) return ""
        if (language == AppLanguage.SIMPLIFIED_CHINESE || language == AppLanguage.TRADITIONAL_CHINESE) return term

        val mapEn = mapOf(
            "立春" to "Spring Begins", "雨水" to "Rain Water", "惊蛰" to "Insects Awaken",
            "春分" to "Vernal Equinox", "清明" to "Clear & Bright", "谷雨" to "Grain Rain",
            "立夏" to "Summer Begins", "小满" to "Grain Buds", "芒种" to "Grain in Ear",
            "夏至" to "Summer Solstice", "小暑" to "Minor Heat", "大暑" to "Major Heat",
            "立秋" to "Autumn Begins", "处暑" to "Heat Stops", "白露" to "White Dew",
            "秋分" to "Autumn Equinox", "寒露" to "Cold Dew", "霜降" to "Frost Descends",
            "立冬" to "Winter Begins", "小雪" to "Minor Snow", "大雪" to "Major Snow",
            "冬至" to "Winter Solstice", "小寒" to "Minor Cold", "大寒" to "Major Cold"
        )

        val mapJa = mapOf(
            "立春" to "立春", "雨水" to "雨水", "惊蛰" to "啓蟄",
            "春分" to "春分", "清明" to "清明", "谷雨" to "穀雨",
            "立夏" to "立夏", "小满" to "小満", "芒种" to "芒種",
            "夏至" to "夏至", "小暑" to "小暑", "大暑" to "大暑",
            "立秋" to "立秋", "处暑" to "処暑", "白露" to "白露",
            "秋分" to "秋分", "寒露" to "寒露", "霜降" to "霜降",
            "立冬" to "立冬", "小雪" to "小雪", "大雪" to "大雪",
            "冬至" to "冬至", "小寒" to "小寒", "大寒" to "大寒"
        )

        val mapKo = mapOf(
            "立春" to "입춘", "雨水" to "우수", "惊蛰" to "경칩",
            "春分" to "춘분", "清明" to "청명", "谷雨" to "곡우",
            "立夏" to "입하", "小满" to "소만", "芒种" to "망종",
            "夏至" to "하지", "小暑" to "소서", "大暑" to "대서",
            "立秋" to "입추", "处暑" to "처서", "白露" to "백로",
            "秋分" to "추분", "寒露" to "한로", "霜降" to "상강",
            "立冬" to "입동", "小雪" to "소설", "大雪" to "대설",
            "冬至" to "동지", "小寒" to "소한", "大寒" to "대한"
        )

        return when (language) {
            AppLanguage.ENGLISH -> mapEn[term] ?: term
            AppLanguage.JAPANESE -> mapJa[term] ?: term
            AppLanguage.KOREAN -> mapKo[term] ?: term
            else -> term
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

    fun getAlmanacYiJi(date: LocalDate): AlmanacYiJi {
        val epochDay = date.toEpochDay()
        val dayHash = abs(epochDay.toInt() * 10007)

        val allYi = listOf(
            "祭祀", "祈福", "求嗣", "开光", "出行", "解除", "伐木", "拆卸",
            "修造", "预期", "进人口", "开市", "交易", "立券", "签合同", "纳财",
            "栽种", "纳畜", "安床", "移徙", "入宅", "安香", "动土", "筑堤"
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
     * 计算详尽星座每日深度运势 (包含事业、财运、感情、健康分项评级与贵人方位)
     */
    fun getDailyFortune(date: LocalDate, constellationName: String, language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE): ZodiacFortune {
        val epochDay = date.toEpochDay()
        val constHash = abs(constellationName.hashCode())
        val seed = (epochDay * 31 + constHash * 17).toInt()
        val hash = abs(seed)

        val constEnMap = mapOf(
            "白羊座" to "Aries", "金牛座" to "Taurus", "双子座" to "Gemini",
            "巨蟹座" to "Cancer", "狮子座" to "Leo", "处女座" to "Virgo",
            "天秤座" to "Libra", "天蝎座" to "Scorpio", "射手座" to "Sagittarius",
            "摩羯座" to "Capricorn", "水瓶座" to "Aquarius", "双鱼座" to "Pisces"
        )

        val constDisplay = when (language) {
            AppLanguage.ENGLISH -> constEnMap[constellationName] ?: constellationName
            else -> constellationName
        }

        val allConsts = listOf("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座")
        val directions = listOf("正东方", "东南方", "正南方", "西南方", "正西方", "西北方", "正北方", "东北方")

        val colorsZh = listOf("冰海蓝", "薄荷绿", "琥珀金", "紫罗兰", "樱花粉", "珊瑚橙", "珍珠白", "蒂芙尼青")
        val colorsEn = listOf("Ocean Blue", "Mint Green", "Amber Gold", "Violet", "Sakura Pink", "Coral Orange", "Pearl White", "Teal")
        val colorsJa = listOf("アイスブルー", "ミントグリーン", "アンバーゴールド", "バイオレット", "サクラピンク", "コーラルオレンジ", "パールホワイト", "ティファニーブルー")
        val colorsKo = listOf("아이티 블루", "민트 그린", "앰버 골드", "바이올렛", "사쿠라 핑크", "코랄 오렌지", "펄 화이트", "티파니 블루")

        val luckyColor = when (language) {
            AppLanguage.ENGLISH -> colorsEn[hash % colorsEn.size]
            AppLanguage.JAPANESE -> colorsJa[hash % colorsJa.size]
            AppLanguage.KOREAN -> colorsKo[hash % colorsKo.size]
            else -> colorsZh[hash % colorsZh.size]
        }

        val (name, emoji) = when (constellationName) {
            "摩羯座" -> Pair(constDisplay, "♑")
            "水瓶座" -> Pair(constDisplay, "♒")
            "双鱼座" -> Pair(constDisplay, "♓")
            "白羊座" -> Pair(constDisplay, "♈")
            "金牛座" -> Pair(constDisplay, "♉")
            "双子座" -> Pair(constDisplay, "♊")
            "巨蟹座" -> Pair(constDisplay, "♋")
            "狮子座" -> Pair(constDisplay, "♌")
            "处女座" -> Pair(constDisplay, "♍")
            "天秤座" -> Pair(constDisplay, "♎")
            "天蝎座" -> Pair(constDisplay, "♏")
            "射手座" -> Pair(constDisplay, "♐")
            else -> Pair(constDisplay, "✨")
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

        val overallScore = 85 + (hash % 14)
        val starRating = if (overallScore >= 92) 5 else 4
        val careerScore = 82 + ((hash * 3) % 17)
        val wealthScore = 80 + ((hash * 7) % 19)
        val loveScore = 83 + ((hash * 11) % 16)
        val healthScore = 84 + ((hash * 13) % 15)

        val careerDescs = listOf(
            "职场思路清晰，适合推演方案与合作决策，易获贵人与团队认可。",
            "工作步调稳定，适合复盘整理与长远规划，稳扎稳打效率翻倍。",
            "创新灵感涌现，勇于提出新想法能打破僵局，取得突破进展。",
            "执行力强劲，处理繁杂事务井井有条，多沟通能化解歧义。"
        )

        val wealthDescs = listOf(
            "财运大吉，理智消费为主，适合规划长线资产与收益理财。",
            "偏财运上升，留意身边的信息机会，合理控告开支有小惊喜。",
            "正财稳健，付出与回报成正比，切忌跟风投资，宜保守留存。"
        )

        val loveDescs = listOf(
            "桃花与人缘俱佳，单身者易遇投缘沟通对象，有心动火花。",
            "感情关系温馨顺畅，多一些倾听与陪伴能让彼此理解加深。",
            "互动融洽自然，适合安排共同出行或亲友小聚，增进默契。"
        )

        val healthDescs = listOf(
            "精力充沛充盈，注意保持作息规律，适度户外拉伸与补水。",
            "状态良好，避免长时间久坐与熬夜，保持愉快心态能焕发活力。"
        )

        val summariesZh = listOf(
            "今日吉星高照，思维敏捷，适合推进关键决策与合作签约，贵人气场强劲。",
            "整体平稳顺遂，各维度步步为营，适合整理计划与自我充电，保持好心态。",
            "灵感与才华爆发，财运与事业运兼备，勇于尝试会有意想不到的丰硕收获。",
            "宜沉心静气，理清脉络，稳扎稳打方能水到渠成，社交人缘气场极佳。"
        )

        val summariesEn = listOf(
            "Star luck shines bright today! Sharp mind and great timing for key decisions and partnerships.",
            "Smooth and balanced day. Methodical focus brings progress and steady personal growth.",
            "Surge of inspiration! Great momentum in career and finances—take bold initiatives.",
            "Stay composed and clear-headed. Consistent efforts lead to rewarding breakthroughs."
        )

        val summariesJa = listOf(
            "本日は強運に恵まれ、直感が冴え渡ります。重要な決断や契約の推進に最適な日です。",
            "全体的に穏やかで順調な一日。計画の整理やスキルアップに集中すると吉。",
            "ひらめきが冴え渡り、仕事運・金運ともに絶好調。積極的な挑戦が幸運を呼びます。",
            "落ち着いて着実に進めることで素晴らしい成果が得られます。対人運も絶好調です。"
        )

        val summariesKo = listOf(
            "오늘의 운세는 대길! 명晰한 사고로 중요한 의사결정과 협력 추진에 최적의 날입니다.",
            "전반적으로 평온하고 순조로운 하루. 계획 정리와 자기 계발에 집중하면 좋습니다.",
            "영감이 샘솟는 날! 사업운과 재물운이 우수하여 과감한 도전에 행운이 따릅니다.",
            "차분하게 하나씩 추진하면 알찬 성과를 거둘 수 있습니다. 인복도 좋습니다."
        )

        val summary = when (language) {
            AppLanguage.ENGLISH -> summariesEn[hash % summariesEn.size]
            AppLanguage.JAPANESE -> summariesJa[hash % summariesJa.size]
            AppLanguage.KOREAN -> summariesKo[hash % summariesKo.size]
            else -> summariesZh[hash % summariesZh.size]
        }

        val luckyNumber = (hash % 9) + 1
        val luckyConstellation = allConsts[(hash + 3) % allConsts.size]
        val luckyDirection = directions[(hash + 5) % directions.size]

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

        var nextBirthday = actualBirth.withYear(actualTarget.year)
        if (nextBirthday.isBefore(actualTarget) || nextBirthday.isEqual(actualTarget)) {
            nextBirthday = actualBirth.withYear(actualTarget.year + 1)
        }
        val daysToNextBirthday = ChronoUnit.DAYS.between(actualTarget, nextBirthday)

        val (constName, constEmoji) = getConstellationInfo(actualBirth)
        val lunar = solarToLunar(actualBirth)

        return AgeResult(
            birthDate = actualBirth,
            targetDate = actualTarget,
            years = years,
            months = months,
            days = days,
            totalDays = totalDays,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            daysToNextBirthday = daysToNextBirthday,
            nextBirthdayDate = nextBirthday,
            zodiac = lunar.zodiac,
            constellation = constName,
            constellationEmoji = constEmoji
        )
    }
}
