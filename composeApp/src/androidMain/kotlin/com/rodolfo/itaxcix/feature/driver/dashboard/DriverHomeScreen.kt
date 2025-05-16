package com.rodolfo.itaxcix.feature.driver.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun DriverHomeScreenPreview() {
    DriverHomeScreen()
}

@Composable
fun DriverHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ITaxCixPaletaColors.Blue3.copy(alpha = 0.1f))
    ) {
        // Aquí puedes agregar el contenido de la pantalla principal del conductor
        // Por ejemplo, un saludo o un resumen de la actividad del conductor.
        Text(text = "¡Bienvenido al panel del conductor!")
    }
}