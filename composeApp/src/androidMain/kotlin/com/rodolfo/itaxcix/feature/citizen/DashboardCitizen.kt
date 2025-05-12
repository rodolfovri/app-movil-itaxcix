package com.rodolfo.itaxcix.feature.citizen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DashboardCitizenScreenPreview() {
    DashboardCitizenScreen()
}

@Composable
fun DashboardCitizenScreen() {
    Column {
        Text(text = "¡Bienvenido al panel principal del ciudadano!")
    }
}