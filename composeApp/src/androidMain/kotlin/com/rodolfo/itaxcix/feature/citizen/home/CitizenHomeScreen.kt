package com.rodolfo.itaxcix.feature.citizen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun CitizenHomeScreenPreview() {
    CitizenHomeScreen()
}

@Composable
fun CitizenHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Inicio de Ciudadano",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}