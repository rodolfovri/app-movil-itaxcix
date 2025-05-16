package com.rodolfo.itaxcix.feature.citizen.profile

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
fun CitizenProfileScreenPreview() {
    CitizenProfileScreen()
}

@Composable
fun CitizenProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Perfil del Ciudadano",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}