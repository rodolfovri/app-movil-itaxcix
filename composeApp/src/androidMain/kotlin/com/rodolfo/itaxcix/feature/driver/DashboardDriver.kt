package com.rodolfo.itaxcix.feature.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DashboardDriverScreenPreview() {
    DashboardDriverScreen()
}

@Composable
fun DashboardDriverScreen() {
    Column {
        Text(text = "¡Bienvenido al panel principal del conductor!")
    }
}