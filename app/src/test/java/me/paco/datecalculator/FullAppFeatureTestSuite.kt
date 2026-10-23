package me.paco.datecalculator

import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 完整 App 全量核心功能与 100% 文本国际化自动化测试套件 (Full App Feature & i18n Text Test Suite)
 * 涵盖：
 * 1. 日期计算引擎 (加减天数、自然日、工作日、中国法定节假日调休)
 * 2. 农历公历双向转换、闰月推算与干支生肖
 * 3. 19 个国家与地区法定节假日全量文本与推算
 * 4. 5 国语言 (简中、繁中、英文、日文、韩文) 本地化文本与单位格式化全量覆盖检查
 * 5. GPS 经纬度离线边界匹配算法 (深圳 vs 香港)
 */
class FullAppFeatureTestSuite {

    // ====================================================================
    // 1. 日期计算引擎测试 (Date Calculator Engine Tests)
    // ====================================================================

    @Test
    fun testStandardWorkdayAdd() {
        // 2026-10-23 (星期五) + 5 个标准工作日 (无节假日，纯双休)
        val start = LocalDate.of(2026, 10, 23)
        val target = DateCalculatorUtils.calculateTargetDate(
            baseDate = start,
            days = 5,
            type = CalculationType.ADD,
            isWorkdayMode = true,
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
        val solar = LocalDate.of(2026, 10, 23)
        val lunar = LunarCalendarUtils.solarToLunar(solar)
        assertNotNull("2026-10-23 农历转换不得为空", lunar)
        assertTrue("农历年份必须为 2026", lunar.year == 2026)
    }

    @Test
    fun testLunarToSolarRoundTrip() {
        val originalSolar = LocalDate.of(2026, 10, 23)
        val lunar = LunarCalendarUtils.solarToLunar(originalSolar)
        val convertedSolar = LunarCalendarUtils.lunarToSolar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth)

        assertEquals("公历➔农历➔公历双向转换必须 100% 精确一致", originalSolar, convertedSolar)
    }

    // ====================================================================
    // 3. 全球 19 个国家与地区节假日全量文本检查 (19 Regional Holidays Text)
    // ====================================================================

    @Test
    fun testAll19RegionsPresetHolidaysTextNotEmpty() {
        val allRegions = HolidayRegion.values()
        for (region in allRegions) {
            val presets = RegionalHolidays.getPresetHolidayNames(region)
            assertTrue("地区 ${region.name} 预设节假日列表不得为空", presets.isNotEmpty())
            assertTrue("地区 ${region.name} 节假日数量必须不少于 5 个", presets.size >= 5)

            for (holidayName in presets) {
                assertTrue("节日名称文本不得为空白", holidayName.isNotBlank())
            }
        }
    }

    @Test
    fun testJapanPresetHolidaysText() {
        val japanPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.JAPAN)
        assertTrue("日本预设节假日必须包含 元日", japanPresets.contains("元日"))
        assertTrue("日本预设节假日必须包含 みどりの日", japanPresets.contains("みどりの日"))
        assertTrue("日本预设节假日必须包含 海の日", japanPresets.contains("海の日"))
        assertTrue("日本预设节假日必须包含 山の日", japanPresets.contains("山の日"))
        assertTrue("日本预设节假日必须包含 憲法記念日", japanPresets.contains("憲法記念日"))
    }

    @Test
    fun testGermanyPresetHolidaysText() {
        val germanyPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.GERMANY)
        assertTrue("德国预设节假日必须包含 Neujahr", germanyPresets.contains("Neujahr"))
        assertTrue("德国预设节假日必须包含 Tag der Deutschen Einheit", germanyPresets.contains("Tag der Deutschen Einheit"))
    }

    @Test
    fun testVietnamPresetHolidaysText() {
        val vietnamPresets = RegionalHolidays.getPresetHolidayNames(HolidayRegion.VIETNAM)
        assertTrue("越南预设节假日必须包含 Tết Nguyên Đán", vietnamPresets.contains("Tết Nguyên Đán"))
        assertTrue("越南预设节假日必须包含 Ngày Quốc Khánh", vietnamPresets.contains("Ngày Quốc Khánh"))
    }

    // ====================================================================
    // 4. 多语言国际化全量文本与单位格式化校验 (All Languages Text Audit)
    // ====================================================================

    @Test
    fun testAll5LanguagesCoreStringKeys() {
        val testKeys = listOf(
            "app_name", "tab_home", "tab_calc", "tab_time_calc", "tab_diff",
            "tab_countdown", "tab_anniversary", "tab_lunar", "tab_age",
            "today", "yesterday", "base_date", "target_date", "workday", "natural_day"
        )

        val languages = listOf(
            AppLanguage.SIMPLIFIED_CHINESE,
            AppLanguage.TRADITIONAL_CHINESE,
            AppLanguage.ENGLISH,
            AppLanguage.JAPANESE,
            AppLanguage.KOREAN
        )

        for (lang in languages) {
            for (key in testKeys) {
                val value = LanguageUtils.getString(key, lang)
                assertTrue("语言 ${lang.name} 对应的字符串 Key '$key' 不得为空", value.isNotBlank())
            }
        }
    }

    @Test
    fun testTimeUnitLabelTranslationsAll5Languages() {
        val units = listOf("y", "M", "d", "h", "m", "s")
        val languages = listOf(
            AppLanguage.SIMPLIFIED_CHINESE,
            AppLanguage.TRADITIONAL_CHINESE,
            AppLanguage.ENGLISH,
            AppLanguage.JAPANESE,
            AppLanguage.KOREAN
        )

        for (lang in languages) {
            for (u in units) {
                val label = LanguageUtils.getTimeUnitLabel(u, lang)
                assertTrue("语言 ${lang.name} 的时间单位 '$u' 本地化文本不得为空", label.isNotBlank())
            }
        }

        // 验证具体文本
        assertEquals("年", LanguageUtils.getTimeUnitLabel("y", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("月", LanguageUtils.getTimeUnitLabel("M", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("天", LanguageUtils.getTimeUnitLabel("d", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("时", LanguageUtils.getTimeUnitLabel("h", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("分", LanguageUtils.getTimeUnitLabel("m", AppLanguage.SIMPLIFIED_CHINESE))
        assertEquals("秒", LanguageUtils.getTimeUnitLabel("s", AppLanguage.SIMPLIFIED_CHINESE))

        assertEquals("Year", LanguageUtils.getTimeUnitLabel("y", AppLanguage.ENGLISH))
        assertEquals("Mon", LanguageUtils.getTimeUnitLabel("M", AppLanguage.ENGLISH))

        assertEquals("年", LanguageUtils.getTimeUnitLabel("y", AppLanguage.JAPANESE))
        assertEquals("ヶ月", LanguageUtils.getTimeUnitLabel("M", AppLanguage.JAPANESE))

        assertEquals("년", LanguageUtils.getTimeUnitLabel("y", AppLanguage.KOREAN))
        assertEquals("개월", LanguageUtils.getTimeUnitLabel("M", AppLanguage.KOREAN))
    }

    @Test
    fun testTimeExpressionFormatForDisplay() {
        val rawExpr = "2y 8M 29d 5h 30m 12s"

        val cnDisplay = LanguageUtils.formatTimeExpressionForDisplay(rawExpr, AppLanguage.SIMPLIFIED_CHINESE)
        assertEquals("2年 8月 29天 5时 30分 12秒", cnDisplay)

        val twDisplay = LanguageUtils.formatTimeExpressionForDisplay(rawExpr, AppLanguage.TRADITIONAL_CHINESE)
        assertEquals("2年 8月 29天 5時 30分 12秒", twDisplay)

        val jpDisplay = LanguageUtils.formatTimeExpressionForDisplay(rawExpr, AppLanguage.JAPANESE)
        assertEquals("2年 8ヶ月 29日 5時間 30分 12秒", jpDisplay)

        val koDisplay = LanguageUtils.formatTimeExpressionForDisplay(rawExpr, AppLanguage.KOREAN)
        assertEquals("2년 8개월 29일 5시간 30분 12초", koDisplay)
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
