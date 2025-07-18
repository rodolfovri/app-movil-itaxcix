package com.rodolfo.itaxcix.feature.citizen.travel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.data.remote.websocket.CitizenWebSocketService
import com.rodolfo.itaxcix.feature.auth.viewmodel.EmergencyViewModel
import com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel.CitizenTripViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixEmergencyDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenTripScreen(
    tripId: Int,
    driverId: Int,
    driverName: String,
    onBackToHome: () -> Unit = {},
    viewModel: CitizenTripViewModel = hiltViewModel(),
    emergencyViewModel: EmergencyViewModel = hiltViewModel(),
    citizenWebSocketService: CitizenWebSocketService
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showIncidenceDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var isIncidenceExpanded by remember { mutableStateOf(false) }
    val cancelState by viewModel.cancelState.collectAsState()

    // Estados para emergencia
    var showEmergencyDialog by remember { mutableStateOf(false) }
    val emergencyState by emergencyViewModel.emergencyState.collectAsState()
    val emergencyNumber by emergencyViewModel.emergencyNumber.collectAsState()

    val context = LocalContext.current
    val incidenceTypes = remember {
        context.resources.getStringArray(com.rodolfo.itaxcix.R.array.incidence_types).toList()
    }

    // Estados para controlar la visibilidad del indicador de progreso
    val isLoading = cancelState is CitizenTripViewModel.CancelState.Loading
    val isSuccess = cancelState is CitizenTripViewModel.CancelState.Success

    // Estados para controlar el registro de incidencias
    val incidentState by viewModel.incidentState.collectAsState()
    val incidentTypeError by viewModel.incidentTypeError.collectAsState()
    val commentError by viewModel.commentError.collectAsState()

    // Estados para controlar la calificación
    val rateState by viewModel.rateState.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val ratingComment by viewModel.ratingComment.collectAsState()
    val ratingError by viewModel.ratingError.collectAsState()
    val ratingCommentError by viewModel.ratingCommentError.collectAsState()

    // Estados para controlar la visibilidad del indicador de progreso para incidencias
    val incidentIsLoading = incidentState is CitizenTripViewModel.IncidentState.Loading
    val incidentIsSuccess = incidentState is CitizenTripViewModel.IncidentState.Success

    // Estados para controlar la visibilidad del indicador de progreso para calificación
    val rateIsLoading = rateState is CitizenTripViewModel.RateState.Loading
    val rateIsSuccess = rateState is CitizenTripViewModel.RateState.Success

    var userInitiatedCancellation by remember { mutableStateOf(false) }

    // Launcher para solicitar permiso de llamada
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, mostrar el diálogo de emergencia
            showEmergencyDialog = true
        } else {
            // Permiso denegado, usar ACTION_DIAL como alternativa
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$emergencyNumber")
            }
            context.startActivity(intent)
            viewModel.cancelTrip(tripId)
        }
    }

    // Modificar el LaunchedEffect para cancelState
    LaunchedEffect(cancelState) {
        when (cancelState) {
            is CitizenTripViewModel.CancelState.Success -> {
                delay(3000)
                // Solo mostrar el flujo de incidencia si el usuario canceló
                if (userInitiatedCancellation) {
                    if (emergencyState is EmergencyViewModel.EmergencyState.Success) {
                        onBackToHome()
                    } else {
                        showIncidenceDialog = true
                    }
                }
                viewModel.onCancelSuccessShown()
            }
            else -> {}
        }
    }

    // En el LaunchedEffect del incidentState
    LaunchedEffect(incidentState) {
        when (incidentState) {
            is CitizenTripViewModel.IncidentState.Success -> {
                delay(3000)
                onBackToHome()
                viewModel.onIncidentSuccessShown()
            }
            is CitizenTripViewModel.IncidentState.Error -> {
                val errorMessage = (incidentState as CitizenTripViewModel.IncidentState.Error).message
                println("DEBUG: Error message: $errorMessage") // Log temporal
                viewModel.onIncidentErrorShown()
            }
            else -> {}
        }
    }

    // Efecto para manejar el estado de la calificación
    LaunchedEffect(rateState) {
        when (rateState) {
            is CitizenTripViewModel.RateState.Success -> {
                delay(2000)
                onBackToHome()
                viewModel.onRateSuccessShown()
            }
            is CitizenTripViewModel.RateState.Error -> {
                val errorMessage = (rateState as CitizenTripViewModel.RateState.Error).message
                println("DEBUG: Rating Error message: $errorMessage")
                viewModel.onRateErrorShown()
            }
            else -> {}
        }
    }

    LaunchedEffect(emergencyState) {
        Log.d("EmergencyDebug", "Emergency state changed: $emergencyState")
        when (emergencyState) {
            is EmergencyViewModel.EmergencyState.Loading -> {
                Log.d("EmergencyDebug", "Loading emergency number...")
            }
            is EmergencyViewModel.EmergencyState.Success -> {
                Log.d("EmergencyDebug", "Success, requesting call permission for number: $emergencyNumber")
                // En lugar de mostrar directamente el diálogo, solicitar permiso primero
                callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                emergencyViewModel.onEmergencySuccessShown()
            }
            is EmergencyViewModel.EmergencyState.Error -> {
                Log.d("EmergencyDebug", "Error: ${(emergencyState as EmergencyViewModel.EmergencyState.Error).message}")
                emergencyViewModel.onEmergencyErrorShown()
            }
            else -> {
                Log.d("EmergencyDebug", "Initial state")
            }
        }
    }

    // Efecto para manejar actualizaciones del WebSocket
    val tripStatusUpdates by citizenWebSocketService.tripStatusUpdates.collectAsState()
    var isTripCancelled by remember { mutableStateOf(false) }
    var isTripCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        // Limpiar el estado anterior cuando se inicia un nuevo viaje
        citizenWebSocketService.resetTripStatusUpdates()
    }

    // Efecto para manejar las actualizaciones de estado del viaje
    LaunchedEffect(tripStatusUpdates) {
        tripStatusUpdates?.let { update ->
            if (update.data.tripId == tripId) {
                when (update.data.status) {
                    "canceled" -> {
                        // Solo mostrar el mensaje del conductor si el usuario no canceló
                        if (!userInitiatedCancellation) {
                            isTripCancelled = true
                            delay(3000)
                            onBackToHome()
                            Log.d("CitizenTripScreen", "Viaje cancelado por el conductor: $update")
                        }
                    }
                    "completed" -> {
                        showRatingDialog = true
                        Log.d("CitizenTripScreen", "Viaje completado: $update")
                    }
                }
                citizenWebSocketService.resetTripStatusUpdates()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE57373), CircleShape)
                            .clickable {
                                userInitiatedCancellation = true // Marcar que el usuario inició la cancelación
                                emergencyViewModel.getEmergencyNumber()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactEmergency,
                                contentDescription = "Emergencia",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergencia",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Información del conductor (parte superior)
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
                                text = "Tu conductor",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = driverName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        ITaxCixPaletaColors.Blue1.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ITaxCixPaletaColors.Blue1,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Viaje en progreso",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ITaxCixPaletaColors.Blue1
                            )
                        }
                    }
                }

                // Sección central (mensaje informativo)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Disfruta tu viaje",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ITaxCixPaletaColors.Blue1
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ITaxCixPaletaColors.Blue1.copy(alpha = 0.05f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ITaxCixPaletaColors.Blue1,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Si sucede un incidente no dudes en cancelarlo y reportarlo, o llamar al número de emergencia.",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Botón de cancelar viaje (parte inferior)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showCancelDialog = true },
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

        // Mostrar indicador de progreso/éxito para cancelación
        ITaxCixProgressRequest(
            isVisible = isLoading || (isSuccess && !showIncidenceDialog && userInitiatedCancellation),
            isSuccess = isSuccess && !showIncidenceDialog && userInitiatedCancellation,
            loadingTitle = "Cancelando viaje",
            successTitle = "Viaje cancelado",
            loadingMessage = "Procesando tu solicitud...",
            successMessage = "Ahora puedes reportar la incidencia..."
        )

        // Mostrar indicador de progreso/éxito para registro de incidencia
        ITaxCixProgressRequest(
            isVisible = incidentIsLoading || incidentIsSuccess,
            isSuccess = incidentIsSuccess,
            loadingTitle = "Registrando incidencia",
            successTitle = "Incidencia registrada",
            loadingMessage = "Procesando tu reporte...",
            successMessage = "Gracias por tu reporte. Regresando a la pantalla principal..."
        )

        ITaxCixEmergencyDialog(
            showDialog = showEmergencyDialog,
            onDismiss = { showEmergencyDialog = false },
            onConfirmCall = {
                showEmergencyDialog = false
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$emergencyNumber")
                }
                context.startActivity(intent)
                viewModel.cancelTrip(tripId)
            },
            emergencyNumber = emergencyNumber,
            countdownSeconds = 5
        )
    }

    ITaxCixConfirmDialog(
        showDialog = showCancelDialog,
        onDismiss = { showCancelDialog = false },
        onConfirm = {
            showCancelDialog = false
            userInitiatedCancellation = true // Marcar que el usuario inició la cancelación
            viewModel.cancelTrip(tripId)
        },
        title = "Cancelar viaje",
        message = "¿Estás seguro de que deseas cancelar este viaje? Se notificará al conductor.",
        confirmButtonText = "Sí, cancelar",
        dismissButtonText = "No, continuar"
    )

    ITaxCixProgressRequest(
        isVisible = isTripCancelled,
        isSuccess = false,
        loadingTitle = "Viaje cancelado",
        loadingMessage = "El conductor ha cancelado el viaje. Regresando al inicio..."
    )

    // Añade este componente al final del Box, después del ITaxCixProgressRequest para cancelación
    ITaxCixProgressRequest(
        isVisible = isTripCompleted,
        isSuccess = true,
        successTitle = "¡Viaje completado!",
        successMessage = "Has llegado exitosamente a tu destino. Se te redirigirá a la pantalla principal..."
    )

    // Diálogo de incidencia (solo se muestra después de cancelar exitosamente)
    if (showIncidenceDialog) {
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
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reportar incidencia",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "¿Por qué cancelaste el viaje?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.Start)
                    )

                    // Dropdown para tipos de incidencia
                    ExposedDropdownMenuBox(
                        expanded = isIncidenceExpanded,
                        onExpandedChange = { isIncidenceExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.incidentType.collectAsState().value,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIncidenceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Tipo de incidencia") },
                            isError = incidentTypeError != null,
                            supportingText = {
                                if (incidentTypeError != null) {
                                    Text(text = incidentTypeError!!, color = MaterialTheme.colorScheme.error)
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

                        ExposedDropdownMenu(
                            expanded = isIncidenceExpanded,
                            onDismissRequest = { isIncidenceExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            incidenceTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.updateIncidentType(option)
                                        isIncidenceExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = ITaxCixPaletaColors.Blue1
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.comment.collectAsState().value,
                        onValueChange = { viewModel.updateComment(it) },
                        isError = commentError != null,
                        label = { Text("Comentarios") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        supportingText = {
                            if (commentError != null) {
                                Text(text = commentError!!, color = MaterialTheme.colorScheme.error)
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

                    // Botón para registrar la incidencia
                    Button(
                        onClick = {
                            showIncidenceDialog = false
                            viewModel.registerIncident(tripId, driverId)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ITaxCixPaletaColors.Blue1
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar incidencia")
                    }
                }
            }
        }
    }

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
                        text = "¿Cómo fue tu experiencia?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                        label = { Text("Comentarios (opcional)") },
                        placeholder = { Text("Cuéntanos sobre tu experiencia... (opcional)") },
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ITaxCixPaletaColors.Blue1
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
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