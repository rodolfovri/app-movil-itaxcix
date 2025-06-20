package com.rodolfo.itaxcix.feature.driver.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.data.remote.websocket.DriverWebSocketService
import com.rodolfo.itaxcix.feature.driver.viewModel.DriverHomeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixPermissionDialog
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = hiltViewModel(),
    driverWebSocketService: DriverWebSocketService,
    onNavigateToTrip: (TripRequestMessage.TripRequestData) -> Unit = {}
) {
    val userData by viewModel.userData.collectAsState()
    val driverHomeState by viewModel.driverHomeState.collectAsState()

    val tripRequests by driverWebSocketService.tripRequests.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission.value) {
            getCurrentLocation(fusedLocationClient, viewModel)
        }
    }

    // Solicitud de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission.value = isGranted
        if (isGranted) {
            getCurrentLocation(fusedLocationClient, viewModel)
            showConfirmDialog = true
        } else {
            // Mostrar mensaje informando que no se puede usar la función sin permisos
            isSuccessSnackbar = false
            showPermissionDialog = false
            scope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de ubicación para activar la disponibilidad.")
            }
        }
    }

    LaunchedEffect(driverHomeState) {
        when (driverHomeState) {
            is DriverHomeViewModel.DriverHomeUiState.Success -> {
                isSuccessSnackbar = true
                val message = (driverHomeState as DriverHomeViewModel.DriverHomeUiState.Success).userData.message
                snackbarHostState.showSnackbar("Disponibilidad activada con éxito")
            }
            is DriverHomeViewModel.DriverHomeUiState.Error -> {
                isSuccessSnackbar = false
                val errorMessage = (driverHomeState as DriverHomeViewModel.DriverHomeUiState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
            }
            is DriverHomeViewModel.DriverHomeUiState.RespondSuccess -> {
                val result = (driverHomeState as DriverHomeViewModel.DriverHomeUiState.RespondSuccess).travel
                val message = if (result.message.isNotEmpty()) {
                    result.message
                } else {
                    "Respuesta enviada con éxito"
                }
                snackbarHostState.showSnackbar(message = message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isSuccessSnackbar) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                    contentColor = Color.White,
                    dismissAction = {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSuccessSnackbar) Icons.Default.Check else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = data.visuals.message)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contenido principal centrado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indicador visual de estado TUC con animación (centrado)
                    TucStatusIndicator(
                        isActive = userData?.isTucActive == true,
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje informativo centrado
                    Text(
                        text = "Para poder recibir solicitudes de transporte, debes activar tu disponibilidad.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información adicional
                    userData?.lastDriverStatusUpdate?.let { lastUpdate ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Última verificación: $lastUpdate",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Botón para consultar estado de TUC
                Button(
                    onClick = {
                        if (hasLocationPermission.value) {
                            showConfirmDialog = true
                        } else {
                            showPermissionDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ITaxCixPaletaColors.Blue1,
                    )
                ) {
                    Text(
                        text = "Activar Disponibilidad",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (tripRequests.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White) // Fondo semi-transparente
                        .clickable(enabled = false, onClick = {}) // Interceptar clicks
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(tripRequests) { request ->
                            TripRequestCard(
                                tripRequest = request,
                                onAccept = { tripId, tripData ->
                                    viewModel.respondToTrip(tripId, true)
                                    onNavigateToTrip(tripData)
                                },
                                onReject = { tripId ->
                                    viewModel.respondToTrip(tripId, false)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }


        }
    }

    // Diálogo de permisos de ubicación
    ITaxCixPermissionDialog(
        showDialog = showPermissionDialog,
        onDismiss = { showPermissionDialog = false },
        onConfirm = {
            showPermissionDialog = false
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        permissionTitle = "Permiso requerido",
        permissionDescription = "Para activar la disponibilidad, necesitamos acceder a tu ubicación.",
        permissionReason = "Esto es necesario para confirmar tu posición actual al conectarte con el sistema.",
        permissionIcon = Icons.Default.LocationOn,
        confirmButtonText = "Permitir ubicación"
    )

    // Modifica el diálogo de confirmación para verificar TUC
    ITaxCixConfirmDialog(
        showDialog = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        onConfirm = {
            // Obtener ubicación actualizada antes de verificar TUC
            if (hasLocationPermission.value) {
                getCurrentLocation(fusedLocationClient, viewModel)
            }
            viewModel.toggleDriverAvailability()
            showConfirmDialog = false
        },
        title = "Activar Disponibilidad",
        message = "¿Estás seguro que deseas activar tu disponibilidad para recibir solicitudes de transporte?",
        confirmButtonColor = ITaxCixPaletaColors.Blue2
    )
}

@Composable
fun TucStatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tucIndicator")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale3"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha3"
    )

    val baseColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFD32F2F)

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2

        // Dibujar círculos animados (ondas)
        drawCircle(
            color = baseColor.copy(alpha = alpha1 * 0.3f),
            radius = maxRadius * scale1,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        drawCircle(
            color = baseColor.copy(alpha = alpha2 * 0.3f),
            radius = maxRadius * scale2,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        drawCircle(
            color = baseColor.copy(alpha = alpha3 * 0.3f),
            radius = maxRadius * scale3,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // Círculo central sólido
        drawCircle(
            color = baseColor,
            radius = maxRadius * 0.3f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

@Composable
fun TripRequestCard(
    tripRequest: TripRequestMessage,
    onAccept: (Int, TripRequestMessage.TripRequestData) -> Unit,
    onReject: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nueva solicitud de viaje",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información del pasajero
            Text(text = "Pasajero: ${tripRequest.data.passengerName}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Calificación: ", fontSize = 16.sp)
                Text(
                    text = "${tripRequest.data.passengerRating}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Información del viaje (si está disponible)
            if (tripRequest.data.price > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Precio: S/ ${tripRequest.data.price}", fontSize = 16.sp)
            }

            if (tripRequest.data.distance > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Distancia: ${tripRequest.data.distance} km", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onReject(tripRequest.data.tripId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Rechazar")
                }

                Button(
                    onClick = { onAccept(tripRequest.data.tripId, tripRequest.data) },
                    colors = ButtonDefaults.buttonColors(containerColor = ITaxCixPaletaColors.Blue1),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: DriverHomeViewModel
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            location?.let {
                viewModel.saveUserLocation(it.latitude, it.longitude)
                Log.d("DriverHomeScreen", "Ubicación obtenida: Latitud ${it.latitude}, Longitud ${it.longitude}")
            }
        }
}