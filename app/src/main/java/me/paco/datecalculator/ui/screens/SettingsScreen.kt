package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.paco.datecalculator.R
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicRadioButton
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.LocationUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 0 = 规则设置, 1 = 历史记录
    var pageTab by remember { mutableIntStateOf(0) }
    var regionMenuExpanded by remember { mutableStateOf(false) }

    val syncedToastText = stringResource(R.string.toast_holidays_synced)

    // 定位运行时动态权限申请 Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            viewModel.updateGpsAutoDetect(true, context)
            val detected = LocationUtils.detectCurrentRegion(context)
            Toast.makeText(context, "已成功根据 GPS 识别所在地: ${detected.flagEmoji} ${detected.nativeName}", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateGpsAutoDetect(false, context)
            Toast.makeText(context, "需要定位权限以自动识别所在地节假日", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 新拟物规则与历史选项切换
        NeumorphicSegmentedRow(
            items = listOf(stringResource(R.string.label_rules), "${stringResource(R.string.label_history)} (${uiState.historyList.size})"),
            selectedIndex = pageTab,
            onIndexSelected = { pageTab = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pageTab == 1) {
            HistoryScreen(viewModel = viewModel, uiState = uiState)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeumorphicAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_rules_and_history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 节假日地区选择 Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = NeumorphicAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.label_region_selection),
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🛰️ GPS 自动定位识别所在地开关行
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .background(NeumorphicBg, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GPS 自动识别所在地", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary, fontSize = 14.sp)
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
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.label_region_sub_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val selected = uiState.holidayRegion

                        // 新拟物 58dp 下拉菜单选择框
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "当前选择的地区: ${selected.nativeName}，点击展开地区选择列表"
                                    }
                                    .clickable { regionMenuExpanded = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selected.flagEmoji} ${selected.nativeName}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeumorphicTextPrimary
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "展开地区下拉菜单", tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = regionMenuExpanded,
                                onDismissRequest = { regionMenuExpanded = false },
                                width = 280.dp,
                                height = 320.dp
                            ) {
                                HolidayRegion.values().forEach { region ->
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
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "${region.flagEmoji} ${region.nativeName}",
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        val holidaysCount = RegionalHolidays.getHolidaysCount(selected)
                        val shiftCount = RegionalHolidays.getShiftWorkdaysCount(selected)

                        val regionDesc = when (selected) {
                            HolidayRegion.CHINA -> stringResource(R.string.region_desc_cn)
                            HolidayRegion.TAIWAN -> stringResource(R.string.region_desc_tw)
                            HolidayRegion.HONG_KONG -> stringResource(R.string.region_desc_hk)
                            HolidayRegion.MACAO -> stringResource(R.string.region_desc_mo)
                            HolidayRegion.SINGAPORE -> "含新加坡法定公众假期"
                            HolidayRegion.MALAYSIA -> "含马来西亚全国法定公众假期"
                            HolidayRegion.VIETNAM -> "含越南法定节假日及补假"
                            HolidayRegion.JAPAN -> stringResource(R.string.region_desc_jp)
                            HolidayRegion.SOUTH_KOREA -> stringResource(R.string.region_desc_kr)
                            HolidayRegion.AUSTRALIA -> "含澳大利亚全国及州法定公众假期"
                            HolidayRegion.NEW_ZEALAND -> "含新西兰全国法定公众假期"
                            HolidayRegion.UNITED_STATES -> stringResource(R.string.region_desc_us)
                            HolidayRegion.THAILAND -> stringResource(R.string.region_desc_th)
                        }

                        Text(
                            text = stringResource(R.string.label_current_region, selected.flagEmoji, selected.nativeName),
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = regionDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeumorphicTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val shiftStr = if (shiftCount > 0) stringResource(R.string.label_shift_count_fmt, shiftCount) else ""
                        Text(
                            text = stringResource(R.string.label_region_holiday_count, holidaysCount, shiftStr),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 新拟物同步最新假期按钮
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                                .background(NeumorphicAccent, shape = CircleShape)
                                .clip(CircleShape)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "同步最新节假日数据按钮"
                                }
                                .clickable {
                                    Toast.makeText(context, syncedToastText, Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("同步最新节假日数据", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 周末休假模式 Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.label_weekend_mode),
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WeekendRule.values().forEach { rule ->
                                val ruleLabel = when (rule) {
                                    WeekendRule.STANDARD_FIVE_DAYS -> stringResource(R.string.rule_five_days)
                                    WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> stringResource(R.string.rule_big_small_weeks)
                                    WeekendRule.SIX_DAYS_SUNDAY -> stringResource(R.string.rule_six_days_sunday)
                                    WeekendRule.SIX_DAYS_SATURDAY -> stringResource(R.string.rule_six_days_saturday)
                                    WeekendRule.SEVEN_DAYS -> stringResource(R.string.rule_seven_days)
                                }
                                val ruleDesc = when (rule) {
                                    WeekendRule.STANDARD_FIVE_DAYS -> stringResource(R.string.rule_five_days_desc)
                                    WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> stringResource(R.string.rule_big_small_weeks_desc)
                                    WeekendRule.SIX_DAYS_SUNDAY -> stringResource(R.string.rule_six_days_sunday_desc)
                                    WeekendRule.SIX_DAYS_SATURDAY -> stringResource(R.string.rule_six_days_saturday_desc)
                                    WeekendRule.SEVEN_DAYS -> stringResource(R.string.rule_seven_days_desc)
                                }

                                val isSelected = (rule == uiState.weekendRule)
                                val itemShape = RoundedCornerShape(14.dp)
                                val itemModifier = if (isSelected) {
                                    Modifier
                                        .neumorphicInset(shape = itemShape, elevation = 3.dp)
                                        .background(NeumorphicBg, shape = itemShape)
                                } else {
                                    Modifier.background(Color.Transparent)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(itemModifier)
                                        .clip(itemShape)
                                        .semantics {
                                            role = Role.RadioButton
                                            contentDescription = "周末休假规则: $ruleLabel。$ruleDesc"
                                        }
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { viewModel.updateWeekendRule(rule, context) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        NeumorphicRadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateWeekendRule(rule, context) },
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ruleLabel,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isSelected) NeumorphicAccent else NeumorphicTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = ruleDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 若选择大小周规则，显示当前为大周还是小周的配置选项
                        if (uiState.weekendRule == WeekendRule.ALTERNATE_BIG_SMALL_WEEKS) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.label_big_small_setting),
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NeumorphicSegmentedRow(
                                items = listOf(stringResource(R.string.label_this_week_big), stringResource(R.string.label_this_week_small)),
                                selectedIndex = if (uiState.isCurrentWeekBigWeek) 0 else 1,
                                onIndexSelected = { viewModel.updateCurrentWeekBigWeek(it == 0, context) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 数据库支持与应用版本信息 Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .semantics(mergeDescendants = true) {
                            contentDescription = "多地区节假日安排说明及应用版本号 v1.2.1"
                        }
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.label_database_support), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.label_database_support_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeumorphicAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("应用版本", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                            }
                            Text("v1.2.1", fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
