package com.rodolfo.itaxcix.feature.citizen.travel

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel.WaitingForDriverViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@Composable
fun WaitingForDriverScreen(
    tripId: Int,
    driverName: String,
    onBackToHome: () -> Unit = {},
    onTripAccepted: (Int, Int, String) -> Unit = { _, _, _ -> },
    viewModel: WaitingForDriverViewModel = hiltViewModel()
) {

    val waitingState by viewModel.waitingState.collectAsState()
    val tripResponseState by viewModel.tripResponseState.collectAsState()

    val isLoading = waitingState is WaitingForDriverViewModel.WaitingState.Loading
    val isSuccess = waitingState is WaitingForDriverViewModel.WaitingState.Success
    var showCancelDialog by remember { mutableStateOf(false) }

    // Añade un LaunchedEffect para detectar cuando la cancelación es exitosa
    LaunchedEffect(waitingState) {
        when (waitingState) {
            is WaitingForDriverViewModel.WaitingState.Success -> {
                // Espera un momento para mostrar el mensaje de éxito
                delay(2000)
                onBackToHome() // Navega de vuelta al dashboard
            }
            else -> {}
        }
    }

    // Manejar navegación basada en la respuesta del conductor
    LaunchedEffect(tripResponseState) {
        when (tripResponseState) {
            is WaitingForDriverViewModel.TripResponseState.Accepted -> {
                val accepted = tripResponseState as WaitingForDriverViewModel.TripResponseState.Accepted
                Log.d("WaitingForDriverScreen", "Trip accepted: $accepted")
                delay(2000) // Dar tiempo para ver el mensaje
                viewModel.resetTripResponseState()
                onTripAccepted(tripId, accepted.driverId, accepted.driverName)
            }
            is WaitingForDriverViewModel.TripResponseState.Rejected -> {
                delay(2000) // Dar tiempo para ver el mensaje
                viewModel.resetTripResponseState()
                onBackToHome()
            }
            else -> {}
        }
    }

    ITaxCixConfirmDialog(
        showDialog = showCancelDialog,
        onDismiss = { showCancelDialog = false },
        onConfirm = { viewModel.cancelTrip(tripId) },
        title = "Cancelar solicitud",
        message = "¿Estás seguro de que deseas cancelar la solicitud de viaje?",
        confirmButtonText = "Sí, cancelar",
    )

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Mostrar mensaje de aceptado/rechazado o pantalla de espera
            when (tripResponseState) {
                is WaitingForDriverViewModel.TripResponseState.Accepted -> {
                    val accepted = tripResponseState as WaitingForDriverViewModel.TripResponseState.Accepted
                    ITaxCixProgressRequest(
                        isVisible = true,
                        isSuccess = true,
                        successTitle = "¡Viaje aceptado!",
                        successMessage = "El conductor ${accepted.driverName} estará llegando pronto. Preparando tu viaje..."
                    )
                }
                is WaitingForDriverViewModel.TripResponseState.Rejected -> {
                    ITaxCixProgressRequest(
                        isVisible = true,
                        isSuccess = false,
                        loadingTitle = "Viaje rechazado",
                        loadingMessage = "El conductor no está disponible en este momento. Regresando al inicio..."
                    )
                }
                else -> {
                    // Pantalla de espera normal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ITaxCixPaletaColors.Blue1,
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Esperando a que $driverName responda tu solicitud de viaje...",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ITaxCixPaletaColors.Blue1,
                                contentColor = Color.White
                            ),
                            ) {
                                Text("Cancelar solicitud")
                        }
                    }
                }
            }
        }

        // Añade el ITaxCixProgressRequest fuera del Box principal
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Cancelando viaje",
            successTitle = "Viaje cancelado",
            loadingMessage = "Procesando tu solicitud...",
            successMessage = "Regresando a la pantalla principal..."
        )
    }
}