package me.paco.datecalculator.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumericCalculatorInput(
    daysInput: String,
    onDaysInputChange: (String) -> Unit,
    selectedType: CalculationType,
    onTypeSelected: (CalculationType) -> Unit,
    onEqualClick: () -> Unit,
    dayUnitLabel: String = "天",
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    quickOptions: List<Int> = listOf(5, 15, 30),
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val placeholderText = when (language) {
        AppLanguage.ENGLISH -> "Enter $dayUnitLabel (e.g. 30)"
        AppLanguage.JAPANESE -> "$dayUnitLabel 数を入力 (例: 30)"
        AppLanguage.KOREAN -> "$dayUnitLabel 일수 입력 (예: 30)"
        else -> "输入${dayUnitLabel}天数 (如: 30)"
    }

    Column(modifier = modifier.fillMaxWidth()) {

        // 同一行整合: [ 天数输入框 ] + [ 加号 (+) & 减号 (-) 同时显示 ] + [ 红色等于号按键 (=) ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 天数输入框 (58dp 高度)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(18.dp))
                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                OutlinedTextField(
                    value = daysInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '-' }) {
                            onDaysInputChange(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            color = NeumorphicTextPrimary.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = NeumorphicTextPrimary,
                        unfocusedTextColor = NeumorphicTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 2. 加号 (+) 按钮
            val isAddSelected = selectedType == CalculationType.ADD
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(58.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                    .background(
                        if (isAddSelected) NeumorphicAccent else NeumorphicBg,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onTypeSelected(CalculationType.ADD) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAddSelected) Color.White else NeumorphicAccent
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 3. 减号 (-) 按钮
            val isSubSelected = selectedType == CalculationType.SUBTRACT
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(58.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                    .background(
                        if (isSubSelected) Color(0xFFEF4444) else NeumorphicBg,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onTypeSelected(CalculationType.SUBTRACT) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSubSelected) Color.White else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. 红色等于号按键 (=)
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(58.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                    .background(Color(0xFFEF4444), shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onEqualClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "=",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3 个新拟物选天数胶囊选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickOptions.forEach { num ->
                val labelText = "$num $dayUnitLabel"
                val isSelected = daysInput == num.toString()

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "QuickChipPressScale"
                )

                val chipModifier = if (isSelected) {
                    Modifier
                        .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                } else {
                    Modifier
                        .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .then(chipModifier)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onDaysInputChange(num.toString())
                            onEqualClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelText,
                        color = if (isSelected) Color.White else NeumorphicTextPrimary,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
