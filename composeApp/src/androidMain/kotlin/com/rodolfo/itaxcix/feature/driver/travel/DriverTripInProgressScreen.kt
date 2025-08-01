package com.rodolfo.itaxcix.feature.driver.travel

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.data.remote.websocket.DriverWebSocketService
import com.rodolfo.itaxcix.feature.driver.travel.driverTravelViewModel.DriverTripInProgressViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@Composable
fun DriverTripInProgressScreen(
    driverTrip: TripRequestMessage.TripRequestData,
    viewModel: DriverTripInProgressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTripGoing: (tripId: Int, passengerId: Int) -> Unit = { _, _ -> },
    driverWebSocketService: DriverWebSocketService
) {

    val context = LocalContext.current
    var originAddress by remember { mutableStateOf("Obteniendo dirección...") }
    var destinationAddress by remember { mutableStateOf("Obteniendo dirección...") }

    // Variables para controlar los diálogos
    var showStartConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showCitizenCancelledDialog by remember { mutableStateOf(false) }

    // Observar actualizaciones de estado del viaje desde WebSocket
    val tripStatusUpdates by driverWebSocketService.tripStatusUpdates.collectAsState()

    // Geocodificar las direcciones al cargar la pantalla
    LaunchedEffect(key1 = driverTrip) {
        getAddressFromLocation(context, driverTrip.origin.lat, driverTrip.origin.lng) { address ->
            originAddress = address
        }

        getAddressFromLocation(context, driverTrip.destination.lat, driverTrip.destination.lng) { address ->
            destinationAddress = address
        }
    }

    val driverTripInProgressState by viewModel.driverTripInProgressState.collectAsState()

    // Variables separadas para mayor claridad
    val isStartLoading = driverTripInProgressState is DriverTripInProgressViewModel.DriverTripInProgressUiState.StartLoading
    val isCancelLoading = driverTripInProgressState is DriverTripInProgressViewModel.DriverTripInProgressUiState.CancelLoading
    val isAcceptSuccess = driverTripInProgressState is DriverTripInProgressViewModel.DriverTripInProgressUiState.AcceptSuccess
    val isCancelSuccess = driverTripInProgressState is DriverTripInProgressViewModel.DriverTripInProgressUiState.CancelSuccess

    // Variables para los mensajes de progreso - separadas por tipo de operación
    val acceptProgressTitle = if (isAcceptSuccess) "Viaje iniciado" else "Iniciando viaje"
    val cancelProgressTitle = if (isCancelSuccess) "Viaje cancelado" else "Cancelando viaje"

    val acceptProgressMessage = if (isAcceptSuccess) "¡El viaje ha comenzado!" else "Por favor espera un momento..."
    val cancelProgressMessage = if (isCancelSuccess) "El viaje ha sido cancelado" else "Por favor espera un momento..."

    // Manejar actualizaciones de estado del viaje vía WebSocket
    LaunchedEffect(tripStatusUpdates) {
        tripStatusUpdates?.let { update ->
            // Verificar si la actualización es para este viaje específico
            if (update.data.tripId == driverTrip.tripId) {
                when (update.data.status) {
                    "canceled" -> {
                        showCitizenCancelledDialog = true
                    }
                    "completed" -> {
                        // El viaje fue completado (si es relevante para esta pantalla)
                        // Manejar según sea necesario
                    }
                    else -> {
                        // Otros estados si es necesario
                    }
                }
                // Resetear el estado después de procesarlo
                driverWebSocketService.resetTripStatusUpdates()
            }
        }
    }

    LaunchedEffect (key1 = driverTripInProgressState) {
        when (driverTripInProgressState) {
            is DriverTripInProgressViewModel.DriverTripInProgressUiState.Error -> {
                val errorMessage = (driverTripInProgressState as DriverTripInProgressViewModel.DriverTripInProgressUiState.Error).message
                Log.e("DriverTripInProgressScreen", "Error: $errorMessage")
                // Aquí puedes mostrar un mensaje de error al usuario
                viewModel.onErrorShown()
            }
            is DriverTripInProgressViewModel.DriverTripInProgressUiState.AcceptSuccess -> {
                val successMessage = (driverTripInProgressState as DriverTripInProgressViewModel.DriverTripInProgressUiState.AcceptSuccess).acceptSuccess.message
                delay(2000)
                viewModel.onAcceptSuccessShown()

                onNavigateToTripGoing(driverTrip.tripId, driverTrip.passengerId)
                Log.d("Navigation", "Navegación completada")

            }
            is DriverTripInProgressViewModel.DriverTripInProgressUiState.CancelSuccess -> {
                val cancelMessage = (driverTripInProgressState as DriverTripInProgressViewModel.DriverTripInProgressUiState.CancelSuccess).cancelSuccess.message
                Log.d("DriverTripInProgressScreen", "Cancel Success: $cancelMessage")
                // Esperar 2 segundos antes de navegar
                delay(2000)

                // Resetear el estado ANTES de navegar
                viewModel.onCancelSuccessShown()

                // Navegar después del delay y reset
                onNavigateBack()
            }
            else -> {}
        }
    }

    LaunchedEffect(showCitizenCancelledDialog) {
        if (showCitizenCancelledDialog) {
            delay(3000) // Esperar 3 segundos para que el usuario lea el mensaje
            showCitizenCancelledDialog = false
            onNavigateBack()
        }
    }

    // Diálogo de confirmación para iniciar viaje
    ITaxCixConfirmDialog(
        showDialog = showStartConfirmDialog,
        onDismiss = { showStartConfirmDialog = false },
        onConfirm = { viewModel.startTrip(driverTrip.tripId) },
        title = "Iniciar viaje",
        message = "¿Estás seguro de que deseas iniciar el viaje con ${driverTrip.passengerName}?",
        confirmButtonText = "Sí, iniciar"
    )

    // Diálogo de confirmación para cancelar viaje
    ITaxCixConfirmDialog(
        showDialog = showCancelConfirmDialog,
        onDismiss = { showCancelConfirmDialog = false },
        onConfirm = { viewModel.cancelTrip(driverTrip.tripId) },
        title = "Cancelar viaje",
        message = "¿Estás seguro de que deseas cancelar el viaje con ${driverTrip.passengerName}?",
        confirmButtonText = "Sí, cancelar"
    )

    // Reemplaza el ITaxCixConfirmDialog con esto:
    ITaxCixProgressRequest(
        isVisible = showCitizenCancelledDialog,
        isSuccess = true,
        loadingTitle = "Viaje cancelado",
        successTitle = "Viaje cancelado",
        loadingMessage = "El pasajero ${driverTrip.passengerName} ha cancelado el viaje.",
        successMessage = "Redirigiendo al dashboard principal...",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ITaxCixPaletaColors.Blue1,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pasajero",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = driverTrip.passengerName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Calificación: ${driverTrip.passengerRating}",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Sección central (origen y destino)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                ) {
                    Text(
                        text = "Detalles del Viaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Origen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ITaxCixPaletaColors.Blue1.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ITaxCixPaletaColors.Blue1
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Punto de recogida",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = originAddress,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Destino
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Destino",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = destinationAddress,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Botones de acción (parte inferior)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showStartConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ITaxCixPaletaColors.Blue1)
                    ) {
                        Text(
                            text = "Iniciar Viaje",
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

        // Diálogo de progreso para iniciar viaje
        ITaxCixProgressRequest(
            isVisible = isStartLoading || isAcceptSuccess,
            isSuccess = isAcceptSuccess,
            loadingTitle = acceptProgressTitle,
            successTitle = acceptProgressTitle,
            loadingMessage = acceptProgressMessage,
            successMessage = acceptProgressMessage
        )

        // Diálogo de progreso para cancelar viaje
        ITaxCixProgressRequest(
            isVisible = isCancelLoading || isCancelSuccess,
            isSuccess = isCancelSuccess,
            loadingTitle = cancelProgressTitle,
            successTitle = cancelProgressTitle,
            loadingMessage = cancelProgressMessage,
            successMessage = cancelProgressMessage
        )
    }
}

// Función para convertir coordenadas en direcciones legibles
private fun getAddressFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    callback: (address: String) -> Unit
) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                val address = if (addresses.isNotEmpty()) {
                    formatAddress(addresses[0])
                } else {
                    "Dirección desconocida"
                }
                callback(address)
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = if (!addresses.isNullOrEmpty()) {
                formatAddress(addresses[0])
            } else {
                "Dirección desconocida"
            }
            callback(address)
        }
    } catch (e: Exception) {
        Log.e("Geocoder", "Error obteniendo dirección: ${e.message}")
        callback("Dirección no disponible")
    }
}

private fun formatAddress(address: Address): String {
    return buildString {
        if (!address.thoroughfare.isNullOrEmpty()) {
            append(address.thoroughfare)
            if (!address.subThoroughfare.isNullOrEmpty()) {
                append(" ").append(address.subThoroughfare)
            }
            append(", ")
        }

        if (!address.subLocality.isNullOrEmpty()) {
            append(address.subLocality).append(", ")
        } else if (!address.locality.isNullOrEmpty()) {
            append(address.locality).append(", ")
        }

        if (!address.locality.isNullOrEmpty() && address.subLocality != address.locality) {
            append(address.locality)
        } else if (!address.subAdminArea.isNullOrEmpty()) {
            append(address.subAdminArea)
        }
    }
}