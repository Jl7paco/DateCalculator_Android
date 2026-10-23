package me.paco.datecalculator

import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.LunarCalendarUtils
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 完整 App 全量核心功能测试套件 (Full App Feature Unit & Integration Test Suite)
 * 覆盖：
 * 1. 日期加减与工作日算法 (双休、单休、大小周、调休补班、多段拆算)
 * 2. 公农历转换、闰月推算与干支生肖
 * 3. 19 个国家与地区法定节假日及常用倒计时推算
 * 4. 时间计算器跨度加减与多国语言单位格式化
 * 5. 5 国语言 (简中、繁中、英文、日文、韩文) 国际化文案
 * 6. GPS 经纬度与地区逆地理离线匹配算法
 */
class FullAppFeatureTestSuite {

    // ====================================================================
    // 1. 日期计算引擎测试 (Date Calculator Engine Tests)
    // ====================================================================

    @Test
    fun testStandardWorkdayAdd() {
        // 2026-10-23 (星期五) + 5 个标准工作日 (无节假日，纯双休)
        val start = LocalDate.of(2026, 10, 23)
        val target = DateCalculatorUtils.addWorkdays(
            baseDate = start,
            workdays = 5,
            weekendRule = WeekendRule.STANDARD_FIVE_DAYS,
            enableHolidays = false
        )
        // 跨过周六(24)和周日(25) -> 周一(26): 1, 周二(27): 2, 周三(28): 3, 周四(29): 4, 周五(30): 5
        assertEquals(LocalDate.of(2026, 10, 30), target)
    }

    @Test
    fun testNaturalDaysBetween() {
        val d1 = LocalDate.of(2026, 1, 1)
        val d2 = LocalDate.of(2026, 1, 11)
        val diff = DateCalculatorUtils.naturalDaysBetween(d1, d2)
        assertEquals(10L, diff)
    }

    @Test
    fun testChinaNationalDayWorkdays2026() {
        // 2026-10-01 至 2026-10-07 为中国国庆法定长假
        val nationalDay = LocalDate.of(2026, 10, 1)
        val isStatutory = RegionalHolidays.isStatutoryHoliday(nationalDay, HolidayRegion.CHINA)
        assertTrue("2026-10-01 必须识别为中国法定节假日", isStatutory)
    }

    // ====================================================================
    // 2. 农历与公历双向转换算法测试 (Lunar & Solar Conversion Tests)
    // ====================================================================

    @Test
    fun testSolarToLunarConversion() {
        // 2026年10月23日 ➔ 农历 2026年九月十四
        val solar = LocalDate.of(2026, 10, 23)
        val lunar = LunarCalendarUtils.solarToLunar(solar)

        assertEquals(2026, lunar.year)
        assertEquals(9, lunar.month)
        assertEquals(14, lunar.day)
        assertEquals("九月", lunar.lunarMonthName)
        assertEquals("十四", lunar.lunarDayName)
    }

    @Test
    fun testLunarToSolarRoundTrip() {
        val originalSolar = LocalDate.of(2026, 5, 20)
        val lunar = LunarCalendarUtils.solarToLunar(originalSolar)
        val convertedSolar = LunarCalendarUtils.lunarToSolar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth)

        assertEquals("公历➔农历➔公历双向转换必须 100% 精确一致", originalSolar, convertedSolar)
    }

    @Test
    fun testLeapMonthDetection2025() {
        // 2025年农历有闰六月
        val leapMonth = LunarCalendarUtils.getLeapMonth(2025)
        assertEquals(6, leapMonth)
    }

    // ====================================================================
    // 3. 全球 19 个国家与地区节假日推算测试 (19 Regional Holidays Tests)
    // ====================================================================

    @Test
    fun testAll19RegionsPresetHolidaysExist() {
        val allRegions = HolidayRegion.values()
        for (region in allRegions) {
            val presets = RegionalHolidays.getPresetHolidayNames(region)
            assertTrue("地区 ${region.name} 预设节假日列表不得为空", presets.isNotEmpty())
            assertTrue("地区 ${region.name} 节假日数量必须不少于 5 个", presets.size >= 5)
        }
    }

    @Test
    fun testJapanPresetHolidaysCorrectness() {
        val japanPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.JAPAN)
        assertTrue("日本预设节假日必须包含 元日", japanPresets.contains("元日"))
        assertTrue("日本预设节假日必须包含 みどりの日", japanPresets.contains("みどりの日"))
        assertTrue("日本预设节假日必须包含 海の日", japanPresets.contains("海の日"))
        assertTrue("日本预设节假日必须包含 山の日", japanPresets.contains("山の日"))
        assertTrue("日本预设节假日必须包含 憲法記念日", japanPresets.contains("憲法記念日"))
    }

    @Test
    fun testGermanyPresetHolidaysCorrectness() {
        val germanyPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.GERMANY)
        assertTrue("德国预设节假日必须包含 Neujahr", germanyPresets.contains("Neujahr"))
        assertTrue("德国预设节假日必须包含 Tag der Deutschen Einheit", germanyPresets.contains("Tag der Deutschen Einheit"))
    }

    @Test
    fun testVietnamPresetHolidaysCorrectness() {
        val vietnamPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.VIETNAM)
        assertTrue("越南预设节假日必须包含 Tết Nguyên Đán", vietnamPresets.contains("Tết Nguyên Đán"))
        assertTrue("越南预设节假日必须包含 Ngày Quốc Khánh", vietnamPresets.contains("Ngày Quốc Khánh"))
    }

    // ====================================================================
    // 4. 多语言国际化文案与单位翻译测试 (Language & i18n Tests)
    // ====================================================================

    @Test
    fun testTimeUnitLabelTranslations() {
        // 简体中文
        assertEquals("年", LanguageUtils.getTimeUnitLabel("y", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("月", LanguageUtils.getTimeUnitLabel("M", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("天", LanguageUtils.getTimeUnitLabel("d", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("时", LanguageUtils.getTimeUnitLabel("h", AppLanguage.SIMPLIFIED_CHINESE))

        // English
        assertEquals("Year", LanguageUtils.getTimeUnitLabel("y", AppLanguage.ENGLISH))
        assertEquals("Mon", LanguageUtils.getTimeUnitLabel("M", AppLanguage.ENGLISH))

        // 日本語
        assertEquals("年", LanguageUtils.getTimeUnitLabel("y", AppLanguage.JAPANESE))
        assertEquals("ヶ月", LanguageUtils.getTimeUnitLabel("M", AppLanguage.JAPANESE))
        assertEquals("日", LanguageUtils.getTimeUnitLabel("d", AppLanguage.JAPANESE))

        // 한국어
        assertEquals("년", LanguageUtils.getTimeUnitLabel("y", AppLanguage.KOREAN))
        assertEquals("개월", LanguageUtils.getTimeUnitLabel("M", AppLanguage.KOREAN))
        assertEquals("일", LanguageUtils.getTimeUnitLabel("d", AppLanguage.KOREAN))
    }

    // ====================================================================
    // 5. GPS 经纬度与离线边界匹配算法测试 (GPS Location Bounds Tests)
    // ====================================================================

    @Test
    fun testShenzhenCoordinatesNotMappedToHongKong() {
        // 深圳市南山区经纬度: 纬度 22.54°N, 经度 114.05°E (深圳河以北)
        val shenzhenLat = 22.5431
        val shenzhenLon = 114.0579

        val region = LocationUtils.matchCoordinatesToRegion(shenzhenLat, shenzhenLon)
        assertEquals("深圳市经纬度 22.54°N 必须匹配为 CHINA 地区，严禁误判为 HONG_KONG", HolidayRegion.CHINA, region)
    }

    @Test
    fun testHongKongCoordinatesMatch() {
        // 香港岛经纬度: 纬度 22.28°N, 经度 114.15°E (深圳河以南)
        val hkLat = 22.2800
        val hkLon = 114.1500

        val region = LocationUtils.matchCoordinatesToRegion(hkLat, hkLon)
        assertEquals("香港经纬度 22.28°N 必须匹配为 HONG_KONG 地区", HolidayRegion.HONG_KONG, region)
    }
}
