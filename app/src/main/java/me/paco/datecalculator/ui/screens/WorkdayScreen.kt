package me.paco.datecalculator.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel

@Composable
fun WorkdayScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    DateCalculationScreen(viewModel = viewModel, uiState = uiState, modifier = modifier)
}
