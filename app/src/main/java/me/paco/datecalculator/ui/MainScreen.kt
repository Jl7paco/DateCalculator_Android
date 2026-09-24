package me.paco.datecalculator.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.ui.components.DynamicCalendarWatermarkBg
import me.paco.datecalculator.ui.screens.AgeCalculatorScreen
import me.paco.datecalculator.ui.screens.AnniversaryScreen
import me.paco.datecalculator.ui.screens.DateCalculationScreen
import me.paco.datecalculator.ui.screens.DateDiffScreen
import me.paco.datecalculator.ui.screens.HomeScreen
import me.paco.datecalculator.ui.screens.LunarConverterScreen
import me.paco.datecalculator.ui.theme.DateCalculatorTheme
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.LanguageUtils
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DateCalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initPreferences(context)
    }

    val showHome = uiState.homeConfig.showHomeScreen
    val isChineseLocale = uiState.appLanguage.isChineseLocale
    val lang = uiState.appLanguage

    val isDark = when (uiState.darkThemeMode) {
        DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
        DarkThemeMode.ON -> true
        DarkThemeMode.OFF -> false
    }

    // 纯功能底栏列表（非中文语言环境下自动屏蔽农历转换，且标题跟随当前语言）
    val navItems = remember(showHome, isChineseLocale, lang) {
        val list = mutableListOf<Pair<String, ImageVector>>()
        if (showHome) list.add(LanguageUtils.getString("tab_home", lang) to Icons.Default.Home)
        list.add(LanguageUtils.getString("tab_calc", lang) to Icons.Default.CalendarToday)
        list.add(LanguageUtils.getString("tab_countdown", lang) to Icons.Default.Event)
        list.add(LanguageUtils.getString("tab_anniversary", lang) to Icons.Default.Favorite)
        if (isChineseLocale) {
            list.add(LanguageUtils.getString("tab_lunar", lang) to Icons.Default.SwapHoriz)
        }
        list.add(LanguageUtils.getString("tab_age", lang) to Icons.Default.Cake)
        list
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { navItems.size })

    DateCalculatorTheme(
        darkTheme = isDark,
        themePreset = uiState.themePreset,
        customPrimaryColorHex = uiState.customPrimaryColorHex
    ) {
        Scaffold(
            bottomBar = {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                        .pointerInput(navItems.size) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val tabWidthPx = size.width / navItems.size.toFloat()
                                    val targetIndex = (offset.x / tabWidthPx).toInt().coerceIn(0, navItems.size - 1)
                                    if (targetIndex != pagerState.currentPage) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                page = targetIndex,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(navItems.size) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.targetPage,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.targetPage,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    coroutineScope.launch {
                                        val scaleFactor = navItems.size.toFloat()
                                        pagerState.dispatchRawDelta(dragAmount * scaleFactor)
                                    }
                                }
                            )
                        }
                ) {
                    val totalWidth = maxWidth
                    val tabCount = navItems.size
                    val tabWidth = totalWidth / tabCount

                    val pageFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    val indicatorOffsetX = tabWidth * pageFraction

                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffsetX)
                            .width(tabWidth)
                            .height(68.dp)
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                    ) {
                        navItems.forEachIndexed { index, (label, icon) ->
                            val distance = abs(pageFraction - index)
                            val selectedProgress = (1f - distance).coerceIn(0f, 1f)
                            val textColor = lerp(
                                start = MaterialTheme.colorScheme.onSurfaceVariant,
                                stop = Color.White,
                                fraction = selectedProgress
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (pagerState.currentPage != index) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = index,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = textColor,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)

            val pageFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
            val watermarkAlpha = if (showHome) {
                pageFraction.coerceIn(0f, 1f)
            } else {
                1f
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = watermarkAlpha }
                ) {
                    DynamicCalendarWatermarkBg(
                        weekendRule = uiState.weekendRule,
                        enableHolidays = uiState.enableChineseHolidays,
                        holidayRegion = uiState.holidayRegion,
                        isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    beyondBoundsPageCount = 1,
                    modifier = modifier.fillMaxSize()
                ) { page ->
                    key(page) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navItemTitle = navItems.getOrNull(page)?.first
                            when {
                                navItemTitle == LanguageUtils.getString("tab_home", lang) -> HomeScreen(viewModel = viewModel, uiState = uiState)
                                navItemTitle == LanguageUtils.getString("tab_calc", lang) -> DateCalculationScreen(viewModel = viewModel, uiState = uiState)
                                navItemTitle == LanguageUtils.getString("tab_countdown", lang) -> DateDiffScreen(viewModel = viewModel, uiState = uiState)
                                navItemTitle == LanguageUtils.getString("tab_anniversary", lang) -> AnniversaryScreen(viewModel = viewModel, uiState = uiState)
                                navItemTitle == LanguageUtils.getString("tab_lunar", lang) -> LunarConverterScreen(viewModel = viewModel, uiState = uiState)
                                navItemTitle == LanguageUtils.getString("tab_age", lang) -> AgeCalculatorScreen(viewModel = viewModel, uiState = uiState)
                                else -> DateCalculationScreen(viewModel = viewModel, uiState = uiState)
                            }
                        }
                    }
                }
            }
        }
    }
}
