package me.paco.datecalculator.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.components.DynamicCalendarWatermarkBg
import me.paco.datecalculator.ui.screens.DateCalculationScreen
import me.paco.datecalculator.ui.screens.DateDiffScreen
import me.paco.datecalculator.ui.screens.LunarConverterScreen
import me.paco.datecalculator.ui.screens.SettingsScreen
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DateCalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 启动时自动加载保存的地区设置 (持久化存储)
    LaunchedEffect(Unit) {
        viewModel.initPreferences(context)
    }

    val navItems = listOf(
        stringResource(R.string.tab_workday) to Icons.Default.CalendarToday,
        stringResource(R.string.tab_date_diff) to Icons.AutoMirrored.Filled.CompareArrows,
        stringResource(R.string.tab_lunar) to Icons.Default.SwapHoriz,
        stringResource(R.string.tab_settings) to Icons.Default.Settings
    )

    // 支持左右手势滑动切换页面的 Pager 状态
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { navItems.size })

    Scaffold(
        bottomBar = {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
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
                    .pointerInput(Unit) {
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
                                // 实时跟手驱动底栏高亮胶囊与页面跟随手指滑动，且不调用 change.consume() 避免手势锁冲突
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

                // 使用统一的 pagerState 实时偏移驱动底栏指示器
                val pageFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
                val indicatorOffsetX = tabWidth * pageFraction

                // 丝滑移动的高亮背景胶囊
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetX)
                        .width(tabWidth)
                        .height(80.dp)
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                // 底部标签按钮 (颜色与 Icon 采用连续线性插值，0ms 即刻响应)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
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
                                tint = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        Box(modifier = Modifier.fillMaxSize()) {
            // 约 10% 清晰度的 3D 动态月历水纹背景 (对应真实年月与休假日动态标注，取消括号)
            DynamicCalendarWatermarkBg(
                weekendRule = uiState.weekendRule,
                enableHolidays = uiState.enableChineseHolidays,
                holidayRegion = uiState.holidayRegion,
                isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek
            )

            // 60 FPS 物理弹簧切页，结合 GPU 离屏渲染隔离，底栏跟手拖拽与主页面滑动 100% 完美共存
            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 1,
                modifier = modifier.fillMaxSize()
            ) { page ->
                key(page) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                    ) {
                        when (page) {
                            0 -> DateCalculationScreen(viewModel = viewModel, uiState = uiState)
                            1 -> DateDiffScreen(viewModel = viewModel, uiState = uiState)
                            2 -> LunarConverterScreen(viewModel = viewModel, uiState = uiState)
                            3 -> SettingsScreen(viewModel = viewModel, uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}
