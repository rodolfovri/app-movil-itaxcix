package com.rodolfo.itaxcix.feature.driver.travel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.driver.travel.driverTravelViewModel.DriverTripGoingViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DriverTripGoingScreen(
    tripId: Int,
    passengerId: Int,
    viewModel: DriverTripGoingViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {}
) {
    val driverTripGoingState by viewModel.driverTripGoingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados para los diálogos de confirmación
    var showFinishConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    // Estado adicional para controlar el progreso
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressSuccess by remember { mutableStateOf(false) }

    val rateState by viewModel.rateState.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val ratingComment by viewModel.ratingComment.collectAsState()
    val ratingError by viewModel.ratingError.collectAsState()
    val ratingCommentError by viewModel.ratingCommentError.collectAsState()

    // Variables para los estados de progreso
    val isLoading = driverTripGoingState is DriverTripGoingViewModel.DriverTripOngoingUiState.Loading
    val rateIsLoading = rateState is DriverTripGoingViewModel.RateState.Loading
    val rateIsSuccess = rateState is DriverTripGoingViewModel.RateState.Success

    // Efectos para manejar estados
    LaunchedEffect(key1 = driverTripGoingState) {
        when (driverTripGoingState) {
            is DriverTripGoingViewModel.DriverTripOngoingUiState.Loading -> {
                showProgressDialog = true
                progressSuccess = false
            }
            is DriverTripGoingViewModel.DriverTripOngoingUiState.Error -> {
                showProgressDialog = false
                val errorMessage = (driverTripGoingState as DriverTripGoingViewModel.DriverTripOngoingUiState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
                viewModel.onErrorShown()
            }
            is DriverTripGoingViewModel.DriverTripOngoingUiState.AcceptSuccess -> {
                // Mostrar progreso de éxito
                progressSuccess = true
                delay(2000) // Mostrar mensaje de éxito por 2 segundos
                showProgressDialog = false
                showRatingDialog = true // Mostrar diálogo de calificación
                viewModel.onAcceptSuccessShown()
            }
            is DriverTripGoingViewModel.DriverTripOngoingUiState.CancelSuccess -> {
                // Mostrar progreso de éxito
                progressSuccess = true
                delay(2000) // Mostrar mensaje de éxito por 2 segundos
                showProgressDialog = false
                viewModel.onCancelSuccessShown()
                onNavigateToHome() // Navegar al dashboard del conductor
            }
            is DriverTripGoingViewModel.DriverTripOngoingUiState.Initial -> {
                if (showProgressDialog && !showRatingDialog) {
                    showProgressDialog = false
                }
            }
        }
    }

    // Efecto para manejar el estado de la calificación
    LaunchedEffect(rateState) {
        when (rateState) {
            is DriverTripGoingViewModel.RateState.Success -> {
                delay(2000)
                onNavigateToHome()
                viewModel.onRateSuccessShown()
            }
            is DriverTripGoingViewModel.RateState.Error -> {
                val errorMessage = (rateState as DriverTripGoingViewModel.RateState.Error).message
                println("DEBUG: Rating Error message: $errorMessage")
                viewModel.onRateErrorShown()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Sección superior - Tarjeta de información
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Viaje en curso",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sección central - Mensajes informativos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ITaxCixPaletaColors.Blue1,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Viaje en progreso",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cuando hayas llegado al destino, pulsa 'Finalizar Viaje'.\n\nSi ocurre alguna incidencia que impida completar el viaje, selecciona 'Cancelar Viaje'.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Sección inferior - Botones
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showFinishConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ITaxCixPaletaColors.Blue1)
                    ) {
                        Text(
                            text = "Finalizar Viaje",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCancelConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(
                            text = "Cancelar Viaje",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Diálogo de progreso mejorado
        ITaxCixProgressRequest(
            isVisible = showProgressDialog,
            isSuccess = progressSuccess,
            loadingTitle = when {
                driverTripGoingState is DriverTripGoingViewModel.DriverTripOngoingUiState.Loading -> "Procesando solicitud"
                progressSuccess && driverTripGoingState is DriverTripGoingViewModel.DriverTripOngoingUiState.AcceptSuccess -> "¡Viaje completado!"
                progressSuccess && driverTripGoingState is DriverTripGoingViewModel.DriverTripOngoingUiState.CancelSuccess -> "Viaje cancelado"
                else -> "Procesando solicitud"
            },
            successTitle = when (driverTripGoingState) {
                is DriverTripGoingViewModel.DriverTripOngoingUiState.AcceptSuccess -> "¡Viaje completado!"
                is DriverTripGoingViewModel.DriverTripOngoingUiState.CancelSuccess -> "Viaje cancelado"
                else -> "¡Éxito!"
            },
            loadingMessage = "Por favor espera un momento...",
            successMessage = when (driverTripGoingState) {
                is DriverTripGoingViewModel.DriverTripOngoingUiState.AcceptSuccess -> "El viaje se completó exitosamente. Ahora podrás calificar al pasajero."
                is DriverTripGoingViewModel.DriverTripOngoingUiState.CancelSuccess -> "El viaje ha sido cancelado correctamente."
                else -> "La operación se completó exitosamente."
            }
        )
    }

    // Diálogos de confirmación
    ITaxCixConfirmDialog(
        showDialog = showFinishConfirmDialog,
        onDismiss = { showFinishConfirmDialog = false },
        onConfirm = {
            showFinishConfirmDialog = false
            viewModel.completeTrip(tripId)
        },
        title = "Finalizar viaje",
        message = "¿Estás seguro de que el pasajero ha llegado a su destino?",
        confirmButtonText = "Sí, finalizar",
        dismissButtonText = "Cancelar"
    )

    ITaxCixConfirmDialog(
        showDialog = showCancelConfirmDialog,
        onDismiss = { showCancelConfirmDialog = false },
        onConfirm = {
            showCancelConfirmDialog = false
            viewModel.cancelTrip(tripId)
        },
        title = "Cancelar viaje",
        message = "¿Estás seguro de que deseas cancelar este viaje?",
        confirmButtonText = "Sí, cancelar",
        dismissButtonText = "No, continuar"
    )

    // Diálogo de calificación
    if (showRatingDialog) {
        Dialog(
            onDismissRequest = { /* No permitir cerrar al tocar fuera */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Viaje completado!",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "¿Cómo fue tu experiencia con el pasajero?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Calificación con estrellas
                    Text(
                        text = "Calificación",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrella ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(horizontal = 4.dp)
                                    .clickable { viewModel.updateRating(index + 1) }
                            )
                        }
                    }

                    if (ratingError != null) {
                        Text(
                            text = ratingError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { viewModel.updateRatingComment(it) },
                        isError = ratingCommentError != null,
                        label = { Text("Comentarios") },
                        placeholder = { Text("Cuéntanos sobre tu experiencia...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        supportingText = {
                            if (ratingCommentError != null) {
                                Text(text = ratingCommentError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1,
                            selectionColors = TextSelectionColors(
                                handleColor = ITaxCixPaletaColors.Blue1,
                                backgroundColor = ITaxCixPaletaColors.Blue3
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showRatingDialog = false
                            viewModel.rateTrip(tripId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ITaxCixPaletaColors.Blue1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enviar calificación")
                    }
                }
            }
        }
    }

    // Mostrar indicador de progreso/éxito para calificación
    ITaxCixProgressRequest(
        isVisible = rateIsLoading || rateIsSuccess,
        isSuccess = rateIsSuccess,
        loadingTitle = "Enviando calificación",
        successTitle = "¡Gracias por tu calificación!",
        loadingMessage = "Procesando tu valoración...",
        successMessage = "Tu calificación ha sido registrada. Regresando a la pantalla principal..."
    )
}