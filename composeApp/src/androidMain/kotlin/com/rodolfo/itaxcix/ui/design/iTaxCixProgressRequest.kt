package com.rodolfo.itaxcix.ui.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Composable
fun ITaxCixProgressRequest(
    isVisible: Boolean,
    isSuccess: Boolean = false,
    loadingTitle: String = "Procesando",
    successTitle: String = "Preparando tu viaje",
    loadingMessage: String = "Por favor espera un momento...",
    successMessage: String = "Redirigiendo...",
    loadingImageRes: Int = R.drawable.loading_request,
    successImageRes: Int = R.drawable.loading_success,
    progressValue: Float? = null,
    backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
    primaryColor: Color = ITaxCixPaletaColors.Blue1,
    secondaryColor: Color = ITaxCixPaletaColors.Blue3
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                    onClick = { /* Bloquea interacciones */ }
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Imagen que cambia según el estado
                    Image(
                        painter = painterResource(
                            id = if (isSuccess) successImageRes else loadingImageRes
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Título según estado
                    Text(
                        text = if (isSuccess) successTitle else loadingTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Barra de progreso
                    if (progressValue != null) {
                        // Barra de progreso con valor conocido (0f a 1f)
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = primaryColor,
                            trackColor = secondaryColor
                        )
                    } else {
                        // Barra de progreso indeterminada
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = primaryColor,
                            trackColor = secondaryColor
                        )
                    }

                    // Mensaje según estado
                    Text(
                        text = if (isSuccess) successMessage else loadingMessage,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}