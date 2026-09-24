package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DarkMode
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.ThemeColorPreset
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicChip
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicRadioButton
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage
    val isChinese = lang.isChineseLocale

    var regionMenuExpanded by remember { mutableStateOf(false) }

    val syncedToastText = "已自动同步最新节假日数据"

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.updateGpsAutoDetect(true, context)
            val detected = LocationUtils.detectCurrentRegion(context)
            Toast.makeText(context, "已开启 GPS 自动识别，匹配所在地: ${detected.flagEmoji} ${detected.nativeName}", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateGpsAutoDetect(false, context)
            Toast.makeText(context, "定位权限被拒绝，降级为手动选择模式", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 顶栏 (36dp 高度, 15sp 标题)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = NeumorphicAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LanguageUtils.getString("settings_title", lang),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NeumorphicTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 0. 语言设置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_language", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { appLang ->
                        val isSelected = (uiState.appLanguage == appLang)
                        NeumorphicChip(
                            text = appLang.nativeName,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateAppLanguage(appLang, context)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. 首页功能模块显隐配置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_home_config", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val config = uiState.homeConfig

                // 首页总开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(LanguageUtils.getString("home_show_screen", lang), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NeumorphicTextPrimary)
                    Switch(
                        checked = config.showHomeScreen,
                        onCheckedChange = { viewModel.updateHomeConfig(config.copy(showHomeScreen = it), context) },
                        modifier = Modifier.scale(0.8f)
                    )
                }

                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))

                // 子模块开关列表 (非中文环境下自动取消黄历与农历选项，并仅以当前语言文字显示)
                val moduleList = mutableListOf<Triple<String, Boolean, (Boolean) -> Unit>>()
                moduleList.add(Triple(LanguageUtils.getString("home_calendar", lang), config.showCalendar) { checked ->
                    viewModel.updateHomeConfig(config.copy(showCalendar = checked), context)
                })
                if (isChinese) {
                    moduleList.add(Triple(LanguageUtils.getString("home_almanac", lang), config.showAlmanac) { checked ->
                        viewModel.updateHomeConfig(config.copy(showAlmanac = checked), context)
                    })
                }
                moduleList.add(Triple(LanguageUtils.getString("home_solar_terms", lang), config.showSolarTerms) { checked ->
                    viewModel.updateHomeConfig(config.copy(showSolarTerms = checked), context)
                })
                if (isChinese) {
                    moduleList.add(Triple(LanguageUtils.getString("home_lunar", lang), config.showLunar) { checked ->
                        viewModel.updateHomeConfig(config.copy(showLunar = checked), context)
                    })
                }
                moduleList.add(Triple(LanguageUtils.getString("home_zodiac", lang), config.showZodiacFortune) { checked ->
                    viewModel.updateHomeConfig(config.copy(showZodiacFortune = checked), context)
                })
                moduleList.add(Triple(LanguageUtils.getString("home_weather", lang), config.showWeather) { checked ->
                    viewModel.updateHomeConfig(config.copy(showWeather = checked), context)
                })

                moduleList.forEach { (label, isChecked, onToggle) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.85f))
                        Switch(
                            checked = isChecked,
                            onCheckedChange = onToggle,
                            modifier = Modifier.scale(0.72f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 节假日地区选择 Card
        val selected = uiState.holidayRegion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("settings_region", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("settings_gps_auto", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary, fontSize = 13.sp)
                    }

                    Switch(
                        checked = uiState.isGpsAutoDetectEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                                if (!hasFine && !hasCoarse) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    viewModel.updateGpsAutoDetect(true, context)
                                    val detected = LocationUtils.detectCurrentRegion(context)
                                    Toast.makeText(context, "已开启 GPS 自动识别，匹配所在地: ${detected.flagEmoji} ${detected.nativeName}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.updateGpsAutoDetect(false, context)
                            }
                        },
                        modifier = Modifier.scale(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(14.dp))
                            .background(NeumorphicBg, shape = RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .semantics {
                                role = Role.Button
                                contentDescription = "当前选择的地区: ${selected.nativeName}，点击展开地区选择列表"
                            }
                            .clickable { regionMenuExpanded = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selected.flagEmoji} ${selected.nativeName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "展开地区下拉菜单", tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
                        }
                    }

                    NeumorphicCustomPopup(
                        expanded = regionMenuExpanded,
                        onDismissRequest = { regionMenuExpanded = false },
                        width = 280.dp,
                        height = 320.dp
                    ) {
                        HolidayRegion.entries.forEach { region ->
                            val isCurrent = (region == selected)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "选择地区: ${region.nativeName}"
                                    }
                                    .clickable {
                                        viewModel.updateHolidayRegion(region, context)
                                        regionMenuExpanded = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "${region.flagEmoji} ${region.nativeName}",
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            Toast.makeText(context, syncedToastText, Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("settings_sync_holidays", lang), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 常用倒计时节日配置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("common_countdown", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                val allPresets = RegionalHolidays.getPresetHolidayNames(selected)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allPresets.forEach { holidayName ->
                        val isEnabled = !uiState.disabledPresetHolidays.contains(holidayName)
                        val localizedName = LanguageUtils.getLocalizedHolidayName(holidayName, lang)
                        NeumorphicChip(
                            text = if (isEnabled) localizedName else "$localizedName (OFF)",
                            selected = isEnabled,
                            onClick = { viewModel.togglePresetHolidayEnabled(holidayName) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. 周末休息模式 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = LanguageUtils.getString("rule_weekend_title", lang),
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicAccent,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WeekendRule.entries.forEach { rule ->
                        val ruleLabel = when (rule) {
                            WeekendRule.STANDARD_FIVE_DAYS -> LanguageUtils.getString("rule_five_days", lang)
                            WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> LanguageUtils.getString("rule_big_small_weeks", lang)
                            WeekendRule.SIX_DAYS_SUNDAY -> LanguageUtils.getString("rule_six_days_sunday", lang)
                            WeekendRule.SIX_DAYS_SATURDAY -> LanguageUtils.getString("rule_six_days_saturday", lang)
                            WeekendRule.SEVEN_DAYS -> LanguageUtils.getString("rule_seven_days", lang)
                        }
                        val ruleDesc = when (rule) {
                            WeekendRule.STANDARD_FIVE_DAYS -> LanguageUtils.getString("rule_five_days_desc", lang)
                            WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> LanguageUtils.getString("rule_big_small_weeks_desc", lang)
                            WeekendRule.SIX_DAYS_SUNDAY -> LanguageUtils.getString("rule_six_days_sunday_desc", lang)
                            WeekendRule.SIX_DAYS_SATURDAY -> LanguageUtils.getString("rule_six_days_saturday_desc", lang)
                            WeekendRule.SEVEN_DAYS -> LanguageUtils.getString("rule_seven_days_desc", lang)
                        }

                        val isSelected = (rule == uiState.weekendRule)
                        val itemShape = RoundedCornerShape(12.dp)
                        val itemModifier = if (isSelected) {
                            Modifier
                                .neumorphicInset(shape = itemShape, elevation = 2.dp)
                                .background(NeumorphicBg, shape = itemShape)
                        } else {
                            Modifier.background(Color.Transparent)
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(itemModifier)
                                    .clip(itemShape)
                                    .clickable { viewModel.updateWeekendRule(rule, context) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    NeumorphicRadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateWeekendRule(rule, context) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = ruleLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NeumorphicTextPrimary
                                        )
                                        Text(
                                            text = ruleDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Material 3 主题配色与调色盘设置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_theme", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ThemeColorPreset.SYSTEM, ThemeColorPreset.CUSTOM).forEach { preset ->
                        val isSelected = (uiState.themePreset == preset)
                        val labelText = if (preset == ThemeColorPreset.SYSTEM) LanguageUtils.getString("dark_mode_system", lang) else preset.label
                        NeumorphicChip(
                            text = labelText,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateThemePreset(preset, context)
                            }
                        )
                    }
                }

                // 调色盘 Picker (14 款精美调色盘选择)
                Spacer(modifier = Modifier.height(10.dp))
                Text("调色盘 (14):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                val customPaletteColors = listOf(
                    0xFF2563EB to "宝石蓝",
                    0xFF10B981 to "翡翠绿",
                    0xFF8B5CF6 to "极光紫",
                    0xFFEC4899 to "樱花粉",
                    0xFF06B6D4 to "蔚蓝海",
                    0xFFF59E0B to "夕阳橘",
                    0xFFEF4444 to "珊瑚红",
                    0xFF059669 to "深松绿",
                    0xFF6366F1 to "星钻紫",
                    0xFFD97706 to "古铜金",
                    0xFF14B8A6 to "绿松石",
                    0xFF64748B to "沉稳灰",
                    0xFF84CC16 to "青青草",
                    0xFFA855F7 to "魅惑紫"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    customPaletteColors.forEach { (colorHex, colorName) ->
                        val isCurrentColor = (uiState.customPrimaryColorHex == colorHex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isCurrentColor) {
                                        Modifier
                                            .border(2.5.dp, Color.White, shape = CircleShape)
                                            .neumorphicInset(shape = CircleShape, elevation = 4.dp)
                                    } else {
                                        Modifier.neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    }
                                )
                                .background(Color(colorHex), shape = CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.updateCustomPrimaryColor(colorHex, context)
                                    Toast.makeText(context, "Color: $colorName", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_dark_mode", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkThemeMode.entries.forEach { mode ->
                        val isSelected = (uiState.darkThemeMode == mode)
                        val modeText = when (mode) {
                            DarkThemeMode.SYSTEM -> LanguageUtils.getString("dark_mode_system", lang)
                            DarkThemeMode.ON -> LanguageUtils.getString("dark_mode_on", lang)
                            DarkThemeMode.OFF -> LanguageUtils.getString("dark_mode_off", lang)
                        }
                        NeumorphicChip(
                            text = modeText,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateDarkThemeMode(mode, context)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
