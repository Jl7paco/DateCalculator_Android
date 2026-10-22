package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.Context
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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
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
import me.paco.datecalculator.ui.components.NeumorphicIconHeaderBadge
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import me.paco.datecalculator.data.BackgroundMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
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
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicSwitch
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.TemperatureUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier,
    showTitleHeader: Boolean = true
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()
    val isChinese = lang.isChineseLocale
    val config = uiState.homeConfig

    var regionMenuExpanded by remember { mutableStateOf(false) }

    val syncedToastText = when (lang.getEffectiveLanguage()) {
        AppLanguage.ENGLISH -> "Successfully synced state government official holiday data!"
        AppLanguage.JAPANESE -> "政府公式の祝日データを正常に同期しました！"
        AppLanguage.KOREAN -> "정부 공식 공휴일 데이터를 성공적으로 동기화했습니다!"
        AppLanguage.TRADITIONAL_CHINESE -> "成功同步最新政府法定節假日數據！"
        else -> "已成功同步最新国务院法定节假日数据！"
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            viewModel.updateGpsAutoDetect(true, context)
            val detected = LocationUtils.detectCurrentRegion(context)
            viewModel.updateHolidayRegion(detected, context)
            Toast.makeText(context, "GPS matched: ${detected.flagEmoji} ${detected.getLocalizedName(lang)}", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateGpsAutoDetect(false, context)
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cardShape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (showTitleHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicIconHeaderBadge(
                    icon = Icons.Default.Home,
                    size = 32.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getString("settings_title", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 0. 语言切换 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_language", lang), fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { appLang ->
                        val isSelected = (uiState.appLanguage == appLang)
                        val displayName = if (appLang == AppLanguage.SYSTEM) LanguageUtils.getString("dark_mode_system", lang) else appLang.nativeName
                        NeumorphicChip(
                            text = displayName,
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

        // 0.5 温度单位设置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val tempTitle = when (lang.getEffectiveLanguage()) {
                        AppLanguage.ENGLISH -> "Temperature Unit"
                        AppLanguage.JAPANESE -> "温度単位"
                        AppLanguage.KOREAN -> "온도 단위"
                        AppLanguage.TRADITIONAL_CHINESE -> "溫度單位"
                        else -> "温度单位显示"
                    }
                    Text(tempTitle, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TemperatureUnit.entries.forEach { unit ->
                        val isSelected = (uiState.temperatureUnit == unit)
                        val displayName = unit.getLocalizedName(lang)
                        NeumorphicChip(
                            text = displayName,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateTemperatureUnit(unit, context)
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
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_home_config", lang), fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
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
                    NeumorphicSwitch(
                        checked = config.showHomeScreen,
                        onCheckedChange = { viewModel.updateHomeConfig(config.copy(showHomeScreen = it), context) }
                    )
                }

                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))

                // 子模块开关列表
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
                        NeumorphicSwitch(
                            checked = isChecked,
                            onCheckedChange = onToggle
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1.5 全局应用背景模式 Card (空白 / 月历水印 / 3D卫星地球)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
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
                    val bgCardTitle = when (effectiveLang) {
                        AppLanguage.ENGLISH -> "Global Background Options"
                        AppLanguage.JAPANESE -> "背景デザイン設定"
                        AppLanguage.KOREAN -> "배경 모드 설정"
                        AppLanguage.TRADITIONAL_CHINESE -> "全局背景模式"
                        else -> "全局背景模式"
                    }
                    Text(
                        text = bgCardTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val bgModes = BackgroundMode.values().toList()
                val bgModeLabels = bgModes.map { it.getLocalizedName(lang) }
                val selectedBgIndex = bgModes.indexOf(config.backgroundMode).coerceAtLeast(0)

                NeumorphicSegmentedRow(
                    items = bgModeLabels,
                    selectedIndex = selectedBgIndex,
                    onIndexSelected = { idx ->
                        val newMode = bgModes.getOrElse(idx) { BackgroundMode.CALENDAR }
                        viewModel.updateHomeConfig(config.copy(backgroundMode = newMode), context)
                    },
                    height = 36.dp
                )

                if (config.backgroundMode == BackgroundMode.EARTH_3D) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    val altitudeTitle = when (effectiveLang) {
                        AppLanguage.ENGLISH -> "Satellite Distance (Zoom Scale)"
                        AppLanguage.JAPANESE -> "衛星高度 (ズームスケール)"
                        AppLanguage.KOREAN -> "위성 고도 (줌 조절)"
                        AppLanguage.TRADITIONAL_CHINESE -> "衛星高度 / 視距縮放"
                        else -> "卫星高度 / 视距缩放"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = altitudeTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                        Text(
                            text = "离地 ${config.satelliteAltitudeKm} km",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = config.satelliteAltitudeKm.toFloat().coerceIn(10f, 25000f),
                        onValueChange = { newVal ->
                            viewModel.updateHomeConfig(config.copy(satelliteAltitudeKm = newVal.toInt()), context)
                        },
                        valueRange = 10f..25000f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = NeumorphicAccent,
                            activeTrackColor = NeumorphicAccent,
                            inactiveTrackColor = NeumorphicTextPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val earthThemePrompt = when (effectiveLang) {
                        AppLanguage.ENGLISH -> "💡 Theme auto-switches to Light/Dark based on real-time solar daylight at your location."
                        AppLanguage.JAPANESE -> "💡 3D地球モードでは、現在地のリアルタイムの昼夜に応じてライト/ダークテーマが自動修正されます。"
                        AppLanguage.KOREAN -> "💡 3D 지구 모드에서는 현재 위치의 실시간 낮/밤 상태에 따라 라이트/다크 테마가 자동 적용됩니다."
                        AppLanguage.TRADITIONAL_CHINESE -> "💡 處於 3D 衛星地球模式下，系統將自動根據您所在位置的實時晝夜重寫主題色（白晝淺色，夜間深色）。"
                        else -> "💡 处于 3D 卫星地球模式下，系统将自动根据您所在位置的实时昼夜重写主题色（白昼浅色，夜间深色）。"
                    }

                    Text(
                        text = earthThemePrompt,
                        fontSize = 10.5.sp,
                        color = NeumorphicAccent.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 节假日地区选择 Card
        val selected = uiState.holidayRegion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
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
                        fontWeight = FontWeight.ExtraBold,
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

                    NeumorphicSwitch(
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
                                    Toast.makeText(context, "GPS matched: ${detected.flagEmoji} ${detected.getLocalizedName(lang)}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.updateGpsAutoDetect(false, context)
                            }
                        }
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
                                contentDescription = "Select region: ${selected.getLocalizedName(lang)}"
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
                                text = "${selected.flagEmoji} ${selected.getLocalizedName(lang)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
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
                                        contentDescription = "Region: ${region.getLocalizedName(lang)}"
                                    }
                                    .clickable {
                                        viewModel.updateHolidayRegion(region, context)
                                        regionMenuExpanded = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "${region.flagEmoji} ${region.getLocalizedName(lang)}",
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
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("common_countdown", lang), fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
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
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = LanguageUtils.getString("rule_weekend_title", lang),
                    fontWeight = FontWeight.ExtraBold,
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

                            if (isSelected && rule == WeekendRule.ALTERNATE_BIG_SMALL_WEEKS) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 28.dp, top = 2.dp, bottom = 4.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val subTitle = when (lang.getEffectiveLanguage()) {
                                            AppLanguage.ENGLISH -> "Current Week Setting:"
                                            AppLanguage.JAPANESE -> "今週のタイプ設定:"
                                            AppLanguage.KOREAN -> "이번 주 설정:"
                                            AppLanguage.TRADITIONAL_CHINESE -> "本週類型設定:"
                                            else -> "本周类型设置:"
                                        }
                                        Text(subTitle, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val bigLabel = when (lang.getEffectiveLanguage()) {
                                                AppLanguage.ENGLISH -> "Big Week (2 Days Off)"
                                                AppLanguage.JAPANESE -> "大週 (土日休み)"
                                                AppLanguage.KOREAN -> "큰 주 (주말 휴무)"
                                                AppLanguage.TRADITIONAL_CHINESE -> "大週 (本週雙休)"
                                                else -> "大周 (本周双休)"
                                            }
                                            val smallLabel = when (lang.getEffectiveLanguage()) {
                                                AppLanguage.ENGLISH -> "Small Week (1 Day Off)"
                                                AppLanguage.JAPANESE -> "小週 (日曜のみ)"
                                                AppLanguage.KOREAN -> "작은 주 (일요일 휴무)"
                                                AppLanguage.TRADITIONAL_CHINESE -> "小週 (本週單休)"
                                                else -> "小周 (本周单休)"
                                            }

                                            NeumorphicChip(
                                                text = bigLabel,
                                                selected = uiState.isCurrentWeekBigWeek,
                                                onClick = { viewModel.updateIsBigWeek(true, context) },
                                                modifier = Modifier.weight(1f)
                                            )

                                            NeumorphicChip(
                                                text = smallLabel,
                                                selected = !uiState.isCurrentWeekBigWeek,
                                                onClick = { viewModel.updateIsBigWeek(false, context) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Material 3 主题配色与调色盘设置 Card (按要求仅保留“跟随系统”与“自定义色彩”按钮，调色盘标题简洁写“调色盘”)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = cardShape)
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.12f), shape = cardShape)
                .clip(cardShape)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_theme", lang), fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 核心简化：仅保留“跟随系统”与“自定义色彩”两个分段按钮
                val mainPresets = listOf(ThemeColorPreset.SYSTEM, ThemeColorPreset.CUSTOM)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mainPresets.forEach { preset ->
                        val isSelected = (uiState.themePreset == preset)
                        val displayName = preset.getLocalizedName(lang)
                        NeumorphicChip(
                            text = displayName,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateThemePreset(preset, context)
                            }
                        )
                    }
                }

                // 调色盘 Picker (标题只显示“调色盘”，点击任意色彩自动切为自定义模式)
                Spacer(modifier = Modifier.height(12.dp))
                Text(LanguageUtils.getString("palette_title", lang), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
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
                    0xFFA855F7 to "魅惑紫",
                    0xFF0284C7 to "天空蓝",
                    0xFFE11D48 to "玫瑰红",
                    0xFF7C3AED to "紫罗兰",
                    0xFFEA580C to "暖阳橙"
                )

                val isDark = LocalDarkTheme.current
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    customPaletteColors.forEach { (colorHex, colorName) ->
                        val isCurrentColor = (uiState.themePreset == ThemeColorPreset.CUSTOM && uiState.customPrimaryColorHex == colorHex)
                        val ringBorderColor = if (isDark) Color.White else Color(0xFF1E293B)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (isCurrentColor) {
                                        Modifier
                                            .border(2.5.dp, ringBorderColor, shape = CircleShape)
                                            .padding(2.dp)
                                            .neumorphicInset(shape = CircleShape, elevation = 3.dp)
                                    } else {
                                        Modifier.neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    }
                                )
                                .background(Color(colorHex), shape = CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.updateCustomPrimaryColor(colorHex, context)
                                    viewModel.updateThemePreset(ThemeColorPreset.CUSTOM, context)
                                    Toast.makeText(context, "Color: $colorName", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentColor) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected Color",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                // 触感震动开关控制卡片
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val hapticTitle = when (lang.getEffectiveLanguage()) {
                            AppLanguage.ENGLISH -> "Haptic Vibration Feedback"
                            AppLanguage.JAPANESE -> "触感振動フィードバック"
                            AppLanguage.KOREAN -> "진동 피드백"
                            AppLanguage.TRADITIONAL_CHINESE -> "觸感震動反饋"
                            else -> "触感震动开关"
                        }
                        Text(hapticTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeumorphicTextPrimary)
                    }

                    NeumorphicSwitch(
                        checked = uiState.isHapticFeedbackEnabled,
                        onCheckedChange = { viewModel.updateHapticFeedbackEnabled(it, context) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageUtils.getString("settings_dark_mode", lang), fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkThemeMode.entries.forEach { mode ->
                        val isSelected = (uiState.darkThemeMode == mode)
                        val displayName = when (mode) {
                            DarkThemeMode.SYSTEM -> LanguageUtils.getString("dark_mode_system", lang)
                            DarkThemeMode.ON -> LanguageUtils.getString("dark_mode_dark", lang)
                            DarkThemeMode.OFF -> LanguageUtils.getString("dark_mode_light", lang)
                        }
                        NeumorphicChip(
                            text = displayName,
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
