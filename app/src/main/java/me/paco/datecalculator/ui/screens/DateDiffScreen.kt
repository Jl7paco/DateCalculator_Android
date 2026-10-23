package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import me.paco.datecalculator.ui.components.NeumorphicIconHeaderBadge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.asImageBitmap
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.WeatherUtils
import java.io.File
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import me.paco.datecalculator.ui.components.QuickDateChips
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicChip
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.SolarLunarSwitch
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.ui.viewmodel.EventRepeatMode
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.NotificationUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.abs

data class CountdownCardData(
    val id: Long,
    val eventName: String,
    val targetDate: LocalDate,
    val baseDate: LocalDate,
    val photoPath: String? = null
)

data class HolidayThemePattern(
    val photoResId: Int,
    val overlayMaskColor: Color,
    val accentColor: Color
)

@Composable
private fun getHolidayThemePattern(label: String, region: HolidayRegion, isDark: Boolean): HolidayThemePattern {
    // 包含中文、日文平假名/片假名 (\u3040-\u30ff)、韩文 (\uac00-\ud7af) 与英文字母，避免日韩文字被误剔除
    val clean = label.replace(Regex("[^\\u4e00-\\u9fa5\\u3040-\\u30ff\\uac00-\\ud7afa-zA-Z]"), "").lowercase()
    val maskAlpha = if (isDark) 0.35f else 0.18f

    // 100% 严格遵循路径 C:\Users\ThxiL\Documents\Codex\2026-10-22\w\outputs\android_holiday_backgrounds 下的 holiday_background_manifest.json 映射规约！
    val resId = when {
        // 核心节日强制 1:1 指定资源匹配规约 (用户指定：清明 -> bg_olive, 高考 -> bg_books, 中秋 -> bg_autumn_moon, 春节 -> bg_lunar_festival, 端午 -> bg_zongzi, 元旦 -> bg_fireworks)
        clean.contains("清明") || clean.contains("tombsweeping") -> R.drawable.bg_olive
        clean.contains("高考") || clean.contains("collegeentrance") || clean.contains("수능") -> R.drawable.bg_books
        clean.contains("中考") || clean.contains("highschoolentrance") -> R.drawable.bg_exam
        clean.contains("元旦") || clean.contains("元日") || clean.contains("newyear") || clean.contains("neujahr") || clean.contains("jourdelan") || clean.contains("capodanno") || clean.contains("신정") -> R.drawable.bg_fireworks
        clean.contains("中秋") || clean.contains("midautumn") || clean.contains("추석") -> R.drawable.bg_autumn_moon
        clean.contains("春节") || clean.contains("春節") || clean.contains("lunar") || clean.contains("springfestival") || clean.contains("农历新年") || clean.contains("農曆新年") || clean.contains("설날") -> R.drawable.bg_lunar_festival
        clean.contains("端午") || clean.contains("dragonboat") || clean.contains("zongzi") -> R.drawable.bg_zongzi
        region == HolidayRegion.CHINA && (clean.contains("国庆") || clean.contains("國慶") || clean.contains("nationalday")) -> R.drawable.bg_lantern_string
        region == HolidayRegion.CHINA && (clean.contains("劳动") || clean.contains("五一") || clean.contains("laborday") || clean.contains("labourday")) -> R.drawable.bg_paper_fan
        region == HolidayRegion.CHINA && (clean.contains("高考") || clean.contains("collegeentrance")) -> R.drawable.bg_books
        region == HolidayRegion.CHINA && (clean.contains("中考") || clean.contains("highschoolentrance")) -> R.drawable.bg_exam
        region == HolidayRegion.CHINA && (clean.contains("儿童") || clean.contains("兒童") || clean.contains("childrensday")) -> R.drawable.bg_children

        (region == HolidayRegion.TAIWAN || region == HolidayRegion.MACAO) && (clean.contains("农历新年") || clean.contains("農曆新年")) -> R.drawable.bg_red_envelope
        region == HolidayRegion.HONG_KONG && (clean.contains("农历新年") || clean.contains("農曆新年") || clean.contains("lunar")) -> R.drawable.bg_lion_dance
        region == HolidayRegion.HONG_KONG && (clean.contains("香港") || clean.contains("特区成立") || clean.contains("特區成立") || clean.contains("hkreunification") || clean.contains("tram")) -> R.drawable.bg_hk_tram
        region == HolidayRegion.MACAO && (clean.contains("澳门") || clean.contains("澳門") || clean.contains("特区成立") || clean.contains("特區成立") || clean.contains("macaureunification") || clean.contains("macautiles")) -> R.drawable.bg_macau_tiles
        (region == HolidayRegion.HONG_KONG || region == HolidayRegion.MACAO) && (clean.contains("重阳") || clean.contains("重陽") || clean.contains("chungyeung")) -> R.drawable.bg_ancestral
        (region == HolidayRegion.HONG_KONG || region == HolidayRegion.MACAO) && (clean.contains("冬至") || clean.contains("wintersolstice")) -> R.drawable.bg_winter_solstice
        region == HolidayRegion.MACAO && (clean.contains("平安夜") || clean.contains("圣诞节前日") || clean.contains("christmaseve")) -> R.drawable.bg_christmas_eve

        region == HolidayRegion.JAPAN && (clean.contains("春分") || clean.contains("sakura") || clean.contains("cherry")) -> R.drawable.bg_sakura
        region == HolidayRegion.JAPAN && (clean.contains("绿之日") || clean.contains("綠之日") || clean.contains("みどりの日") || clean.contains("みどり") || clean.contains("greenery") || clean.contains("bamboo")) -> R.drawable.bg_bamboo
        region == HolidayRegion.JAPAN && (clean.contains("文化") || clean.contains("kimono")) -> R.drawable.bg_kimono_fan
        region == HolidayRegion.JAPAN && (clean.contains("成人") || clean.contains("seijin") || clean.contains("comingofage")) -> R.drawable.bg_seijin
        region == HolidayRegion.JAPAN && (clean.contains("儿童之日") || clean.contains("こどもの日") || clean.contains("koinobori")) -> R.drawable.bg_koinobori
        region == HolidayRegion.JAPAN && (clean.contains("体育") || clean.contains("スポーツ") || clean.contains("sports")) -> R.drawable.bg_sports
        region == HolidayRegion.JAPAN && (clean.contains("海之日") || clean.contains("海の日")) -> R.drawable.bg_summer
        region == HolidayRegion.JAPAN && (clean.contains("山之日") || clean.contains("山の日")) -> R.drawable.bg_mountain_day

        region == HolidayRegion.SOUTH_KOREA && (clean.contains("설날") || clean.contains("seollal") || clean.contains("hanbok")) -> R.drawable.bg_hanbok
        region == HolidayRegion.SOUTH_KOREA && (clean.contains("开天节") || clean.contains("개천절") || clean.contains("foundation")) -> R.drawable.bg_korea_hanok
        region == HolidayRegion.SOUTH_KOREA && (clean.contains("显忠日") || clean.contains("현충일") || clean.contains("memorial")) -> R.drawable.bg_memorial

        (region == HolidayRegion.SINGAPORE || region == HolidayRegion.INDONESIA) && (clean.contains("开斋") || clean.contains("eid") || clean.contains("ramadan")) -> R.drawable.bg_ramadan_dates
        region == HolidayRegion.SINGAPORE && (clean.contains("国庆") || clean.contains("nationalday") || clean.contains("singaporegarden")) -> R.drawable.bg_singapore_garden
        region == HolidayRegion.MALAYSIA && (clean.contains("哈芝") || clean.contains("hajj")) -> R.drawable.bg_hajj
        region == HolidayRegion.MALAYSIA && (clean.contains("马来西亚日") || clean.contains("malaysiaday") || clean.contains("batik")) -> R.drawable.bg_malaysia_batik
        (region == HolidayRegion.MALAYSIA || region == HolidayRegion.AUSTRALIA || region == HolidayRegion.NEW_ZEALAND) && (clean.contains("元首诞辰") || clean.contains("国王生日") || clean.contains("kingsbirthday") || clean.contains("crown")) -> R.drawable.bg_crown

        region == HolidayRegion.VIETNAM && (clean.contains("国庆") || clean.contains("nationalday") || clean.contains("vietnamhat")) -> R.drawable.bg_vietnam_hat

        region == HolidayRegion.UNITED_KINGDOM && (clean.contains("银行假日") || clean.contains("bankholiday") || clean.contains("uktea")) -> R.drawable.bg_uk_tea
        region == HolidayRegion.GERMANY && (clean.contains("统一") || clean.contains("unity") || clean.contains("pretzel") || clean.contains("deutscheneinheit")) -> R.drawable.bg_germany_pretzel
        region == HolidayRegion.GERMANY && (clean.contains("karfreitag") || clean.contains("受难")) -> R.drawable.bg_winter
        region == HolidayRegion.GERMANY && (clean.contains("ostermontag") || clean.contains("复活")) -> R.drawable.bg_easter
        region == HolidayRegion.FRANCE && (clean.contains("国庆") || clean.contains("bastille") || clean.contains("croissant")) -> R.drawable.bg_france_croissant
        region == HolidayRegion.FRANCE && (clean.contains("劳动") || clean.contains("labour") || clean.contains("olive")) -> R.drawable.bg_olive
        region == HolidayRegion.FRANCE && (clean.contains("万圣") || clean.contains("toussaint") || clean.contains("halloween")) -> R.drawable.bg_halloween
        region == HolidayRegion.ITALY && (clean.contains("共和国") || clean.contains("republic") || clean.contains("gondola")) -> R.drawable.bg_italy_gondola
        region == HolidayRegion.ITALY && (clean.contains("狂欢") || clean.contains("carnival")) -> R.drawable.bg_carnival
        region == HolidayRegion.INDIA && (clean.contains("共和") || clean.contains("republic") || clean.contains("rangoli")) -> R.drawable.bg_india_rangoli
        region == HolidayRegion.INDIA && (clean.contains("洒红") || clean.contains("holi")) -> R.drawable.bg_holi
        region == HolidayRegion.INDIA && (clean.contains("圣诞") || clean.contains("christmas") || clean.contains("bells")) -> R.drawable.bg_bells
        (region == HolidayRegion.INDONESIA || region == HolidayRegion.THAILAND) && (clean.contains("静居") || clean.contains("万佛") || clean.contains("nyepi") || clean.contains("temple")) -> R.drawable.bg_temple
        region == HolidayRegion.INDONESIA && (clean.contains("独立") || clean.contains("independence") || clean.contains("batik")) -> R.drawable.bg_indonesia_batik
        region == HolidayRegion.AUSTRALIA && (clean.contains("圣诞") || clean.contains("christmas") || clean.contains("gift")) -> R.drawable.bg_gift
        region == HolidayRegion.NEW_ZEALAND && (clean.contains("圣诞") || clean.contains("christmas") || clean.contains("sunflower")) -> R.drawable.bg_sunflower
        (region == HolidayRegion.AUSTRALIA || region == HolidayRegion.NEW_ZEALAND) && (clean.contains("anzac") || clean.contains("poppy")) -> R.drawable.bg_poppy
        region == HolidayRegion.NEW_ZEALAND && (clean.contains("matariki") || clean.contains("毛利新年")) -> R.drawable.bg_matariki
        region == HolidayRegion.THAILAND && (clean.contains("宋干") || clean.contains("songkran") || clean.contains("tuktuk")) -> R.drawable.bg_tuktuk
        region == HolidayRegion.THAILAND && (clean.contains("水灯") || clean.contains("thailandlantern") || clean.contains("loykrathong")) -> R.drawable.bg_thai_lantern
        region == HolidayRegion.UNITED_STATES && (clean.contains("阵亡") || clean.contains("退伍") || clean.contains("memorial") || clean.contains("veterans")) -> R.drawable.bg_memorial
        region == HolidayRegion.UNITED_STATES && (clean.contains("感恩") || clean.contains("thanksgiving")) -> R.drawable.bg_thanksgiving

        // 2. 通用跨语言多语种降级逻辑 (All Regions Fallbacks in Manifest)
        clean.contains("春") || clean.contains("年") || clean.contains("설날") || clean.contains("tết") || clean.contains("lunar") || clean.contains("springfestival") -> R.drawable.bg_lunar_festival
        clean.contains("清明") || clean.contains("春分") || clean.contains("绿之日") || clean.contains("守夏") || clean.contains("tombsweeping") || clean.contains("springrain") -> R.drawable.bg_spring_rain
        clean.contains("端午") || clean.contains("dragonboat") || clean.contains("zongzi") -> R.drawable.bg_dragon_boat
        clean.contains("中秋") || clean.contains("秋分") || clean.contains("추석") || clean.contains("midautumn") || clean.contains("mooncake") -> R.drawable.bg_autumn_moon
        clean.contains("劳动") || clean.contains("五一") || clean.contains("勞動") || clean.contains("labor") || clean.contains("labour") || clean.contains("勤労") -> R.drawable.bg_spring_festival
        clean.contains("高考") || clean.contains("수능") || clean.contains("collegeentrance") || clean.contains("books") -> R.drawable.bg_books
        clean.contains("中考") || clean.contains("highschoolentrance") || clean.contains("exam") -> R.drawable.bg_exam
        clean.contains("受难") || clean.contains("节礼") || clean.contains("主显") || clean.contains("boxingday") || clean.contains("goodfriday") -> R.drawable.bg_winter
        clean.contains("开斋") || clean.contains("哈芝") || clean.contains("伊斯兰新年") || clean.contains("先知") || clean.contains("eid") || clean.contains("ramadan") -> R.drawable.bg_eid
        clean.contains("屠妖") || clean.contains("diwali") || clean.contains("deepavali") -> R.drawable.bg_diwali
        clean.contains("songkran") || clean.contains("泼水") || clean.contains("水灯") || clean.contains("怀唐伊") || clean.contains("water") -> R.drawable.bg_water_festival
        clean.contains("山之日") || clean.contains("山の日") || clean.contains("mountain") -> R.drawable.bg_mountain_day
        clean.contains("海之日") || clean.contains("夏") || clean.contains("summer") -> R.drawable.bg_summer
        clean.contains("重阳") || clean.contains("重陽") || clean.contains("敬老") || clean.contains("显忠") || clean.contains("停战") || clean.contains("阵亡") || clean.contains("退伍") || clean.contains("丰收") || clean.contains("harvest") -> R.drawable.bg_harvest
        clean.contains("圣诞") || clean.contains("聖誕") || clean.contains("christmas") || clean.contains("noel") -> R.drawable.bg_christmas
        clean.contains("复活") || clean.contains("easter") -> R.drawable.bg_easter
        clean.contains("佛诞") || clean.contains("佛誕") || clean.contains("卫塞") || clean.contains("衛塞") || clean.contains("vesak") || clean.contains("buddha") -> R.drawable.bg_lotus
        clean.contains("狂欢") || clean.contains("carnival") -> R.drawable.bg_carnival
        clean.contains("洒红") || clean.contains("holi") -> R.drawable.bg_holi
        clean.contains("感恩") || clean.contains("thanksgiving") -> R.drawable.bg_thanksgiving
        clean.contains("万圣") || clean.contains("萬聖") || clean.contains("halloween") -> R.drawable.bg_halloween
        clean.contains("儿童") || clean.contains("兒童") || clean.contains("children") -> R.drawable.bg_children
        clean.contains("港湾") || clean.contains("维港") || clean.contains("harbour") || clean.contains("harbor") -> R.drawable.bg_harbour
        clean.contains("澳门") || clean.contains("macau") -> R.drawable.bg_macau_city
        else -> R.drawable.bg_fireworks
    }

    val accent = when {
        clean.contains("春") || clean.contains("国庆") || clean.contains("圣诞") || clean.contains("lunar") || clean.contains("national") || clean.contains("christmas") -> Color(0xFFEF4444)
        clean.contains("清明") || clean.contains("tombsweeping") -> Color(0xFF10B981)
        clean.contains("特区") || clean.contains("元旦") || clean.contains("newyear") -> Color(0xFF3B82F6)
        clean.contains("中秋") || clean.contains("感恩") || clean.contains("万圣") || clean.contains("midautumn") || clean.contains("thanksgiving") || clean.contains("halloween") -> Color(0xFFD97706)
        else -> NeumorphicAccent
    }

    return HolidayThemePattern(
        photoResId = resId,
        overlayMaskColor = accent.copy(alpha = maskAlpha),
        accentColor = accent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DateDiffScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()

    var showPickerForStart by remember { mutableStateOf(false) }
    var showPickerForEnd by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var checkInTitleInput by remember { mutableStateOf("") }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val currentCity = uiState.currentCityName
            val coords = WeatherUtils.getCityCoordinates(currentCity)
            val nowTimeStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val titleText = checkInTitleInput.ifBlank { "打卡地标" }
            val localizedCity = LanguageUtils.getLocalizedCityName(currentCity, lang)

            val watermarkedPath = DateCalculatorUtils.addCheckInWatermark(
                context = context,
                srcBitmap = bitmap,
                locationName = localizedCity,
                coords = coords,
                timestampStr = nowTimeStr,
                titleText = titleText
            )
            if (watermarkedPath.isNotBlank()) {
                capturedPhotoPath = watermarkedPath
                Toast.makeText(context, "水印相机照片已自动生成！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Start Date Type: 0 = 公历, 1 = 农历
    var startCalendarType by remember { mutableIntStateOf(0) }

    // Target Date Type: 0 = 公历, 1 = 农历
    var targetCalendarType by remember { mutableIntStateOf(0) }

    // 农历输入状态
    val todayLunar = remember { LunarCalendarUtils.solarToLunar(LocalDate.now()) }

    var startLunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var startLunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var startLunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var startIsLeapMonth by remember { mutableStateOf(false) }

    var targetLunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var targetLunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var targetLunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var targetIsLeapMonth by remember { mutableStateOf(false) }

    var currentTargetEventName by remember { mutableStateOf("") }

    // 默认提供 2 个 Days Matter 原汁原味倒计时 (1: 下一个星期六; 2: 本机手机型号发布至今)
    val defaultSaturdayDate = remember {
        LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY))
    }
    val defaultDeviceInfo = remember { DateCalculatorUtils.getDeviceLaunchInfo() }

    var resultCardList by remember {
        mutableStateOf(
            listOf(
                CountdownCardData(
                    id = 1001L,
                    eventName = "下一个星期六",
                    targetDate = defaultSaturdayDate,
                    baseDate = LocalDate.now()
                ),
                CountdownCardData(
                    id = 1002L,
                    eventName = "${defaultDeviceInfo.deviceName} 发布",
                    targetDate = defaultDeviceInfo.launchDate,
                    baseDate = LocalDate.now()
                )
            )
        )
    }

    fun addResultCard(eventName: String, targetDate: LocalDate, photoPath: String? = null) {
        val name = eventName.ifBlank { LanguageUtils.getString("target_date", lang) }
        val newCard = CountdownCardData(
            id = System.currentTimeMillis() + resultCardList.size,
            eventName = name,
            targetDate = targetDate,
            baseDate = LocalDate.now(),
            photoPath = photoPath
        )

        val firstCard = resultCardList.firstOrNull()
        val isFirstCardPinned = firstCard != null && (
            uiState.customEvents.any { (it.name == firstCard.eventName || it.name.contains(firstCard.eventName)) && it.isPinned } ||
            uiState.pinnedPresetHolidays.contains(firstCard.eventName)
        )

        resultCardList = if (firstCard == null) {
            listOf(newCard)
        } else if (!isFirstCardPinned) {
            listOf(newCard) + resultCardList.drop(1).filterNot { it.eventName == name }
        } else {
            listOf(newCard) + resultCardList.filterNot { it.eventName == name }
        }
    }

    val targetLunarYear = targetLunarYearInput.toIntOrNull() ?: todayLunar.year
    val effectiveEndDate: LocalDate = if (targetCalendarType == 0) {
        uiState.endDate
    } else {
        LunarCalendarUtils.lunarToSolar(targetLunarYear, targetLunarMonth, targetLunarDay, targetIsLeapMonth)
            ?: uiState.endDate
    }

    val reminderMessage = "${LanguageUtils.getString("days_until_prefix", lang)} ${currentTargetEventName.ifBlank { LanguageUtils.getString("target_date", lang) }} ($effectiveEndDate) ${LanguageUtils.getString("days_until_suffix", lang)}"

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationUtils.showCountdownNotification(
                context = context,
                title = "🎉 Reminder",
                message = reminderMessage
            )
            Toast.makeText(context, "Notification Set!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPickerForStart) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = { date ->
                viewModel.updateBaseDate(date)
                showPickerForStart = false
            },
            onDismiss = { showPickerForStart = false }
        )
    }

    if (showPickerForEnd) {
        DatePickerModal(
            selectedDate = uiState.endDate,
            onDateSelected = { date ->
                viewModel.updateEndDate(date)
                showPickerForEnd = false
            },
            onDismiss = { showPickerForEnd = false }
        )
    }

    HistoryOverlayDialog(
        visible = showHistoryDialog,
        onDismiss = { showHistoryDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    SettingsOverlayDialog(
        visible = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    // 添加自定义倒数日 Dialog
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customNameInput by remember { mutableStateOf("") }
    var customDateInput by remember { mutableStateOf(LocalDate.now().plusDays(10)) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("🎂") }
    var selectedRepeatMode by remember { mutableStateOf(EventRepeatMode.NONE) }
    var eventToDelete by remember { mutableStateOf<CustomEventItem?>(null) }

    if (showCustomDatePicker) {
        DatePickerModal(
            selectedDate = customDateInput,
            onDateSelected = { d ->
                customDateInput = d
                showCustomDatePicker = false
            },
            onDismiss = { showCustomDatePicker = false }
        )
    }

    if (showAddCustomDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        val customTitlePlaceholder = when (effectiveLang) {
            AppLanguage.ENGLISH -> "e.g. Exam / Concert / Vacation"
            AppLanguage.JAPANESE -> "例: 試験 / コンサート / 旅行"
            AppLanguage.KOREAN -> "예: 시험 / 콘서트 / 휴가"
            AppLanguage.TRADITIONAL_CHINESE -> "如: 考研倒數 / 演唱會 / 假期"
            else -> "例如: 考研倒计时 / 看演唱会 / 毕业旅行"
        }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text(LanguageUtils.getString("add_countdown", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Emoji Icon", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val emojiOptions = listOf("🎂", "🎉", "✈️", "💍", "🎓", "🎮", "🚗", "🏠", "📚", "❤️", "🏆", "📌")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojiOptions.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeumorphicAccent else Color.Transparent)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    // 1. 倒数日名称
                    Text(LanguageUtils.getString("anniversary_name", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (customNameInput.isEmpty()) {
                            Text(
                                text = customTitlePlaceholder,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            cursorBrush = SolidColor(NeumorphicAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. 目标日期选择框
                    Text(LanguageUtils.getString("target_date", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val inputShape14 = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neumorphicInset(shape = inputShape14, elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape14)
                            .background(NeumorphicBg, shape = inputShape14)
                            .clip(inputShape14)
                            .clickable { showCustomDatePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = NeumorphicAccent,
                                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                                )
                                Text(
                                    text = DateCalculatorUtils.formatDateWithWeek(customDateInput, lang),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Select Date",
                                tint = NeumorphicAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventRepeatMode.entries.forEach { mode ->
                            val isSel = selectedRepeatMode == mode
                            val modeLabel = mode.getLocalizedName(lang)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeumorphicAccent else NeumorphicSunkenBg)
                                    .clickable { selectedRepeatMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(modeLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else NeumorphicTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customNameInput.isNotBlank()) {
                            val fullName = "$selectedEmoji $customNameInput"
                            viewModel.addCustomEvent(
                                name = fullName,
                                targetDate = customDateInput,
                                iconEmoji = selectedEmoji,
                                repeatMode = selectedRepeatMode,
                                isPinned = true,
                                context = context
                            )
                            viewModel.updateEndDate(customDateInput)
                            currentTargetEventName = fullName
                            addResultCard(fullName, customDateInput)

                            showAddCustomDialog = false
                            customNameInput = ""
                        }
                    }
                ) {
                    Text(LanguageUtils.getString("confirm", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 精准打卡 Dialog (支持水印相机照片现场捕捉与 OSM 地图融合)
    if (showCheckInDialog) {
        val currentCity = uiState.currentCityName
        val coords = WeatherUtils.getCityCoordinates(currentCity)
        val nowTimeStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageUtils.getString("check_in_dialog_title", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg)
                            .padding(10.dp)
                    ) {
                        Column {
                            val localizedCity = LanguageUtils.getLocalizedCityName(currentCity, lang)
                            Text("📍 定位地点: $localizedCity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                            val (dispLat, dispLon) = LocationUtils.getDisplayCoordinates(coords.first, coords.second)
                            Text("GPS 经纬度: (${String.format(Locale.US, "%.3f", dispLat)}°, ${String.format(Locale.US, "%.3f", dispLon)}°)", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                            Text("打卡时刻: $nowTimeStr", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("打卡事件名称", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val checkInTitlePlaceholder = when (effectiveLang) {
                        AppLanguage.ENGLISH -> "e.g. Arrived in Paris / First Concert"
                        AppLanguage.TRADITIONAL_CHINESE -> "如: 抵達巴黎 / 首次看演唱會"
                        AppLanguage.JAPANESE -> "例: パリ到着 / 初コンサート"
                        AppLanguage.KOREAN -> "예: 파리 도착 / 첫 콘서트"
                        else -> "例如: 抵达深圳 / 打卡地标 / 首次看演唱会"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (checkInTitleInput.isEmpty()) {
                            Text(
                                text = checkInTitlePlaceholder,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = checkInTitleInput,
                            onValueChange = { checkInTitleInput = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(NeumorphicAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 需求 1 & 3: 拍照打卡与水印相机按键
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { cameraLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (capturedPhotoPath != null) "📷 已生成防伪水印照片 (点击重拍)" else "📷 拍照生成现场防伪水印照片",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent
                            )
                        }
                    }

                    if (capturedPhotoPath != null) {
                        val photoFile = File(capturedPhotoPath!!)
                        if (photoFile.exists()) {
                            val photoBmp = remember(capturedPhotoPath) {
                                try { BitmapFactory.decodeFile(photoFile.absolutePath) } catch (_: Exception) { null }
                            }
                            if (photoBmp != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.30f), shape = RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        bitmap = photoBmp.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputTitle = checkInTitleInput.ifBlank { "打卡事件" }
                        val localizedCity = LanguageUtils.getLocalizedCityName(currentCity, lang)
                        val checkInEventName = "打卡: $inputTitle ($localizedCity)"
                        addResultCard(checkInEventName, LocalDate.now(), photoPath = capturedPhotoPath)
                        checkInTitleInput = ""
                        capturedPhotoPath = null
                        showCheckInDialog = false
                        Toast.makeText(context, "打卡成功！", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeumorphicAccent)
                ) {
                    Text("记录打卡", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCheckInDialog = false
                    capturedPhotoPath = null
                }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary)
                }
            },
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (eventToDelete != null) {
        val targetEvent = eventToDelete!!
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text(LanguageUtils.getString("confirm_delete_anniversary", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("${targetEvent.name}", fontSize = 13.sp, color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomEvent(targetEvent, context)
                        eventToDelete = null
                    }
                ) {
                    Text(LanguageUtils.getString("confirm_delete_btn", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary)
                }
            },
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 顶栏 (36dp 高度, 15sp 标题)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeumorphicIconHeaderBadge(
                        icon = Icons.Default.EventRepeat,
                        size = 28.dp,
                        iconSize = 14.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("tab_countdown", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeumorphicIconButton(
                        icon = Icons.Default.History,
                        onClick = { showHistoryDialog = true },
                        contentDescription = LanguageUtils.getString("history_title", lang),
                        size = 32.dp
                    )

                    NeumorphicIconButton(
                        icon = Icons.Default.Settings,
                        onClick = { showSettingsDialog = true },
                        contentDescription = LanguageUtils.getString("settings_title", lang),
                        size = 32.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 预计算 19 个地区全量预设常用节假日列表
            val base = uiState.baseDate
            val rawPresets = RegionalHolidays.getPresetCountdowns(uiState.holidayRegion, base)

            // 汇总全量置顶固定倒数日（包含联动同步的自定义事件与置顶的常用节假日）
            val allPinnedCards = mutableListOf<Pair<CustomEventItem?, Pair<String, LocalDate>>>()

            // 1. 自定义或同步建立的置顶事件
            uiState.customEvents.filter { it.isPinned }.forEach { item ->
                allPinnedCards.add(Pair(item, item.name to item.getNextUpcomingDate(uiState.baseDate)))
            }

            // 2. 常用节假日被置顶的事件
            rawPresets.filter { (label, _) -> uiState.pinnedPresetHolidays.contains(label) }.forEach { (label, date) ->
                val localizedName = LanguageUtils.getLocalizedHolidayName(label, lang)
                if (allPinnedCards.none { it.second.first == localizedName || it.second.first == label }) {
                    allPinnedCards.add(Pair(null, localizedName to date))
                }
            }

            val cardColorPalette = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                Color(0xFFD1FAE5),
                Color(0xFFFFEDD5),
                Color(0xFFF3E8FF),
                Color(0xFFFCE7F3)
            )

            if (allPinnedCards.isNotEmpty()) {
                Text(LanguageUtils.getString("fixed_countdown", lang), fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = NeumorphicAccent)
                Spacer(modifier = Modifier.height(6.dp))

                allPinnedCards.forEachIndexed { pIdx, (customItem, pair) ->
                    val (pinnedName, upcoming) = pair
                    val diffDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, upcoming)
                    val cardBgColor = if (isDark) {
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            Color(0xFF065F46).copy(alpha = 0.40f),
                            Color(0xFF7C2D12).copy(alpha = 0.40f),
                            Color(0xFF581C87).copy(alpha = 0.40f),
                            Color(0xFF831843).copy(alpha = 0.40f)
                        )[pIdx % 5]
                    } else {
                        cardColorPalette[pIdx % cardColorPalette.size]
                    }

                    val cardShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .background(cardBgColor, shape = cardShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = cardShape)
                            .clip(cardShape)
                            .clickable {
                                targetCalendarType = 0
                                viewModel.updateEndDate(upcoming)
                                currentTargetEventName = pinnedName
                                addResultCard(pinnedName, upcoming)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(pinnedName, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = NeumorphicTextPrimary)
                                    Text("${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(upcoming, lang)}", fontSize = 11.5.sp, color = NeumorphicTextPrimary.copy(alpha = 0.65f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${abs(diffDays)} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)

                                Spacer(modifier = Modifier.width(10.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Unpin",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            if (customItem != null) {
                                                viewModel.togglePinCustomEvent(customItem, context)
                                            } else {
                                                val rawLabel = rawPresets.firstOrNull { LanguageUtils.getLocalizedHolidayName(it.first, lang) == pinnedName }?.first ?: pinnedName
                                                viewModel.togglePinPresetHoliday(rawLabel, context)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. 目标日期 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageUtils.getString("select_target_date", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                SolarLunarSwitch(
                    isSolar = (targetCalendarType == 0),
                    onCalendarTypeChanged = { isSolar ->
                        targetCalendarType = if (isSolar) 0 else 1
                    },
                    language = lang
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val inputShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .neumorphicInset(shape = inputShape, elevation = 4.dp)
                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape)
                        .background(NeumorphicBg, shape = inputShape)
                        .clip(inputShape)
                        .clickable { showPickerForEnd = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(uiState.endDate, lang)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                val btnShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(58.dp)
                        .neumorphicExtruded(shape = btnShape, elevation = 5.dp)
                        .background(Color(0xFFEF4444), shape = btnShape)
                        .clip(btnShape)
                        .clickable {
                            val targetName = if (currentTargetEventName.isNotBlank()) currentTargetEventName else LanguageUtils.getString("target_date", lang)
                            addResultCard(targetName, effectiveEndDate)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 常用倒数日功能区 (3 栏等宽矩阵对齐 + 纯文字取消 Emoji + 节日专属半透明气场背景)
            Text(
                text = LanguageUtils.getString("common_countdown", lang),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeumorphicTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val presetCountdowns = rawPresets.filterNot { (label, _) ->
                uiState.disabledPresetHolidays.contains(label)
            }

            val chipShape14 = RoundedCornerShape(14.dp)
            val isDark = LocalDarkTheme.current

            // 矩阵排版：每行 3 个 100% 相同高宽按键 (3 栏等宽矩阵，取消 Emoji，带有节日专属半透明气场背景)
            val rows = presetCountdowns.chunked(3)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (label, targetDate) ->
                            val isPinned = uiState.pinnedPresetHolidays.contains(label)
                            val isSelected = resultCardList.firstOrNull()?.eventName == label

                            // 100% 彻底清洗全量 Emoji，纯粹地道文字
                            val rawLocalized = LanguageUtils.getLocalizedHolidayName(label, lang)
                            val cleanName = rawLocalized.replace(Regex("[\\x{1F300}-\\x{1F9FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}\\x{1F600}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{1F1E6}-\\x{1F1FF}\\u2600-\\u27BF]"), "").trim()

                            val pattern = getHolidayThemePattern(label, uiState.holidayRegion, isDark)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .neumorphicExtruded(shape = chipShape14, elevation = 4.dp)
                                    .background(NeumorphicBg, shape = chipShape14)
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) pattern.accentColor else pattern.accentColor.copy(alpha = 0.30f),
                                        shape = chipShape14
                                    )
                                    .clip(chipShape14)
                                    .combinedClickable(
                                        onClick = {
                                            targetCalendarType = 0
                                            viewModel.updateEndDate(targetDate)
                                            currentTargetEventName = cleanName
                                            addResultCard(cleanName, targetDate)
                                        },
                                        onLongClick = {
                                            viewModel.togglePinPresetHoliday(label, context)
                                            val msg = if (isPinned) "Unpinned" else "Pinned!"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // 1. 矢量/插画素材背景 (采用 ContentScale.Crop 保持原比例，无失真切合按键)
                                Image(
                                    painter = painterResource(pattern.photoResId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alpha = if (isDark) 0.65f else 0.55f,
                                    modifier = Modifier.matchParentSize()
                                )

                                // 2. 需求 6: 倒计时按钮的高考，中考，端午，五一劳动节完全移除颜色遮罩，展现清爽矢量素材原图！
                                val cleanTag = label.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z]"), "").lowercase()
                                val isNoMaskHoliday = cleanTag.contains("高考") || cleanTag.contains("中考") || cleanTag.contains("端午") || cleanTag.contains("劳动") || cleanTag.contains("五一") || cleanTag.contains("labor") || cleanTag.contains("labour")

                                if (!isNoMaskHoliday) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(pattern.overlayMaskColor)
                                    )
                                }

                                // 3. 前景层：深色主题色/高对比度文字 (浅色模式下采用深色主题色/ Slate 800 保证 100% 极佳可读性)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = cleanName,
                                        color = if (isDark) Color.White else (if (isSelected) pattern.accentColor else NeumorphicTextPrimary),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (lang.isChineseLocale) 12.sp else 10.5.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isPinned) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("📌", fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        // 若最后一行不足 3 项：补充等宽空位，保持所有卡片尺寸 100% 绝对统一！
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 需求 2 & 3: “新增倒数日”与“打卡”按钮同列响组合（按钮文本由自定义改为新增倒数日，右侧增加打卡按钮）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { showAddCustomDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageUtils.getString("add_countdown_btn", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicAccent)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { showCheckInDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageUtils.getString("check_in_btn", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 多卡片推栈倒计时结果展示层 (需求 1/4/5/7: 参照 Days Matter 智能语法, 包含未来"距离下一个X还有"与过去"X已经", 未来卡片天蓝色遮罩, 过去卡片橙色遮罩)
            if (resultCardList.isNotEmpty()) {
                resultCardList.forEachIndexed { cardIdx, cardData ->
                    val natDays = DateCalculatorUtils.naturalDaysBetween(cardData.baseDate, cardData.targetDate)
                    val isPast = cardData.targetDate.isBefore(cardData.baseDate)
                    val absDays = abs(natDays)

                    // 需求 7: 结果卡片颜色遮罩 - 未来的加入浅蓝色遮罩，过去的加入橙色遮罩
                    val cardMaskColor = if (isPast) {
                        Color(0xFFEA580C).copy(alpha = if (isDark) 0.35f else 0.22f)
                    } else {
                        Color(0xFF0284C7).copy(alpha = if (isDark) 0.35f else 0.22f)
                    }

                    val cardBorderColor = if (isPast) Color(0xFFEA580C).copy(alpha = 0.45f) else Color(0xFF0284C7).copy(alpha = 0.45f)

                    // 动态多语言翻译节假日文本
                    val localizedCardTitle = LanguageUtils.getLocalizedHolidayName(cardData.eventName, lang)
                        .replace(Regex("[\\x{1F300}-\\x{1F9FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}\\x{1F600}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{1F1E6}-\\x{1F1FF}\\u2600-\\u27BF]"), "").trim()

                    // 需求 2: 星期六倒计时 (若当天为星期六则直接提醒“今天就是星期六！”)
                    val isSaturdayCard = cardData.eventName.contains("星期六") || cardData.eventName.contains("Saturday")
                    val isTodaySaturday = isSaturdayCard && (cardData.baseDate.dayOfWeek == java.time.DayOfWeek.SATURDAY)

                    // 需求 4 & 5: Days Matter 倒计时智能描述短语 (例如: "距离下一个中考还有", "小米 13 发布已经")
                    val phraseText = if (isTodaySaturday) {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Today is Saturday!"
                            AppLanguage.TRADITIONAL_CHINESE -> "今天就是星期六！"
                            AppLanguage.JAPANESE -> "今日は土曜日です！"
                            AppLanguage.KOREAN -> "오늘이 바로 토요일입니다!"
                            else -> "今天就是星期六！"
                        }
                    } else if (isPast) {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "$localizedCardTitle Has Passed"
                            AppLanguage.TRADITIONAL_CHINESE -> "${localizedCardTitle} 已經"
                            AppLanguage.JAPANESE -> "${localizedCardTitle} 経過"
                            AppLanguage.KOREAN -> "${localizedCardTitle} 경과"
                            else -> "${localizedCardTitle} 已经"
                        }
                    } else {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Days Until Next $localizedCardTitle"
                            AppLanguage.TRADITIONAL_CHINESE -> "距離下一次 ${localizedCardTitle} 還有"
                            AppLanguage.JAPANESE -> "次なる ${localizedCardTitle} まで"
                            AppLanguage.KOREAN -> "다음 ${localizedCardTitle} 까지"
                            else -> "距离下一个 ${localizedCardTitle} 还有"
                        }
                    }

                    val suffixText = if (isTodaySaturday) {
                        ""
                    } else if (isPast) {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Days"
                            AppLanguage.TRADITIONAL_CHINESE -> "天了"
                            AppLanguage.JAPANESE -> "日目"
                            AppLanguage.KOREAN -> "일 지남"
                            else -> "天了"
                        }
                    } else {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Days"
                            AppLanguage.TRADITIONAL_CHINESE -> "天"
                            AppLanguage.JAPANESE -> "日"
                            AppLanguage.KOREAN -> "일"
                            else -> "天"
                        }
                    }

                    // 根据路径下 Manifest 映射规则获得 VectorDrawable 背景素材与色调
                    val cardPattern = getHolidayThemePattern(cardData.eventName, uiState.holidayRegion, isDark)
                    val bgDrawableResId = if (cardData.eventName.contains("打卡")) {
                        R.drawable.bg_open_street_map
                    } else {
                        cardPattern.photoResId
                    }

                    val isCardPinned = uiState.pinnedPresetHolidays.contains(cardData.eventName) || uiState.customEvents.any { (it.name == cardData.eventName || it.name.contains(cardData.eventName)) && it.isPinned }

                    val resultCardShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .neumorphicExtruded(shape = resultCardShape, elevation = 6.dp)
                            .background(NeumorphicBg, shape = resultCardShape)
                            .border(1.2.dp, cardBorderColor, shape = resultCardShape)
                            .clip(resultCardShape)
                    ) {
                        // 1. 照片水印/ VectorDrawable 背景矢量素材层
                        val photoFile = cardData.photoPath?.let { File(it) }
                        val photoBitmap = remember(cardData.photoPath) {
                            if (photoFile != null && photoFile.exists()) {
                                try { BitmapFactory.decodeFile(photoFile.absolutePath) } catch (_: Exception) { null }
                            } else null
                        }

                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(bgDrawableResId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alpha = if (isDark) 0.60f else 0.50f,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        // 2. 需求 7: 结果卡片颜色遮罩 (未来的加入浅蓝色遮罩，过去的加入橙色遮罩)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(cardMaskColor)
                        )

                        // 3. 前景倒计时结果展示层 (需求 1: 恢复置顶与删除工具栏，且不替换/覆盖现有卡片，压入新卡片)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = phraseText,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else (if (isPast) Color(0xFFC2410C) else Color(0xFF0369A1)),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 置顶按钮
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                            .background(if (isCardPinned) NeumorphicAccent else NeumorphicBg, shape = CircleShape)
                                            .clip(CircleShape)
                                            .clickable {
                                                val fullName = cardData.eventName
                                                val foundCustom = uiState.customEvents.find { it.name == fullName || it.name.contains(cardData.eventName) }
                                                if (foundCustom != null) {
                                                    viewModel.togglePinCustomEvent(foundCustom, context)
                                                } else {
                                                    viewModel.togglePinPresetHoliday(fullName, context)
                                                }
                                                val msg = if (isCardPinned) "已取消置顶" else "已成功置顶！"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pin",
                                            tint = if (isCardPinned) Color.White else NeumorphicAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // 删除卡片按钮
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = if (isDark) Color.White.copy(alpha = 0.70f) else NeumorphicTextPrimary.copy(alpha = 0.60f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                resultCardList = resultCardList.filterNot { it.id == cardData.id }
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 巨幕 42sp 极速倒计时天数
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$absDays",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDark) Color.White else (if (isPast) Color(0xFFEA580C) else Color(0xFF0284C7))
                                )
                                if (suffixText.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = suffixText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White.copy(alpha = 0.90f) else (if (isPast) Color(0xFFC2410C) else Color(0xFF0369A1)),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun calculateNextSolarDate(baseDate: LocalDate, month: Int, day: Int): LocalDate {
    var candidate = LocalDate.of(baseDate.year, month, day)
    if (candidate.isBefore(baseDate)) {
        candidate = candidate.plusYears(1)
    }
    return candidate
}

private fun calculateNextLunarDate(baseDate: LocalDate, lunarMonth: Int, lunarDay: Int): LocalDate {
    var year = baseDate.year
    var candidate = LunarCalendarUtils.lunarToSolar(year, lunarMonth, lunarDay, false) ?: baseDate
    if (candidate.isBefore(baseDate)) {
        candidate = LunarCalendarUtils.lunarToSolar(year + 1, lunarMonth, lunarDay, false) ?: baseDate.plusYears(1)
    }
    return candidate
}
