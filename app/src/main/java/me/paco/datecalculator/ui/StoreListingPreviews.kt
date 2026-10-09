package me.paco.datecalculator.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.paco.datecalculator.ui.screens.AgeCalculatorScreen
import me.paco.datecalculator.ui.screens.AnniversaryScreen
import me.paco.datecalculator.ui.screens.DateCalculationScreen
import me.paco.datecalculator.ui.screens.DateDiffScreen
import me.paco.datecalculator.ui.screens.HomeScreen
import me.paco.datecalculator.ui.theme.DateCalculatorTheme
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel

@Preview(showBackground = true, name = "PreviewHomeScreen", widthDp = 380, heightDp = 800)
@Composable
fun PreviewHomeScreen() {
    DateCalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen(viewModel = DateCalculatorViewModel(), uiState = DateCalculatorUiState())
        }
    }
}

@Preview(showBackground = true, name = "PreviewDateCalcScreen", widthDp = 380, heightDp = 800)
@Composable
fun PreviewDateCalcScreen() {
    DateCalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            DateCalculationScreen(viewModel = DateCalculatorViewModel(), uiState = DateCalculatorUiState())
        }
    }
}

@Preview(showBackground = true, name = "PreviewDateDiffScreen", widthDp = 380, heightDp = 800)
@Composable
fun PreviewDateDiffScreen() {
    DateCalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            DateDiffScreen(viewModel = DateCalculatorViewModel(), uiState = DateCalculatorUiState())
        }
    }
}

@Preview(showBackground = true, name = "PreviewAnniversaryScreen", widthDp = 380, heightDp = 800)
@Composable
fun PreviewAnniversaryScreen() {
    DateCalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AnniversaryScreen(viewModel = DateCalculatorViewModel(), uiState = DateCalculatorUiState())
        }
    }
}

@Preview(showBackground = true, name = "PreviewAgeCalcScreen", widthDp = 380, heightDp = 800)
@Composable
fun PreviewAgeCalcScreen() {
    DateCalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AgeCalculatorScreen(viewModel = DateCalculatorViewModel(), uiState = DateCalculatorUiState())
        }
    }
}
