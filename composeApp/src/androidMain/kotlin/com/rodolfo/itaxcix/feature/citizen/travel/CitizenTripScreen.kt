package com.rodolfo.itaxcix.feature.citizen.travel

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
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
import com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel.CitizenTripViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenTripScreen(
    tripId: Int,
    driverId: Int,
    driverName: String,
    onBackToHome: () -> Unit = {},
    viewModel: CitizenTripViewModel = hiltViewModel()
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val cancelState by viewModel.cancelState.collectAsState()
    var showIncidenceDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isIncidenceExpanded by remember { mutableStateOf(false) }

    // Lista de tipos de incidencias
    val incidenceTypes = listOf(
        "FALTA DE RESPETO",
        "FALTA DE PUNTUALIDAD",
        "FALTA DE HIGIENE",
        "FALTA DE SEGURIDAD",
        "FALTA DE CUMPLIMIENTO",
        "OTRO"
    )

    // Estados para controlar la visibilidad del indicador de progreso
    val isLoading = cancelState is CitizenTripViewModel.CancelState.Loading
    val isSuccess = cancelState is CitizenTripViewModel.CancelState.Success

    // Estados para controlar el registro de incidencias
    val incidentState by viewModel.incidentState.collectAsState()
    val incidentTypeError by viewModel.incidentTypeError.collectAsState()
    val commentError by viewModel.commentError.collectAsState()

    // Estados para controlar la visibilidad del indicador de progreso para incidencias
    val incidentIsLoading = incidentState is CitizenTripViewModel.IncidentState.Loading
    val incidentIsSuccess = incidentState is CitizenTripViewModel.IncidentState.Success

    // Efecto para manejar el flujo de cancelación
    LaunchedEffect(cancelState) {
        when (cancelState) {
            is CitizenTripViewModel.CancelState.Success -> {
                // Cuando se cancela exitosamente, mostrar el diálogo de incidencia
                delay(1000) // Pequeño delay para que el usuario vea el éxito
                showIncidenceDialog = true
            }
            else -> {}
        }
    }

    // Efecto para manejar el estado de la incidencia
    // En el LaunchedEffect del incidentState
    LaunchedEffect(incidentState) {
        println("DEBUG: incidentState changed to: $incidentState") // Log temporal
        when (incidentState) {
            is CitizenTripViewModel.IncidentState.Success -> {
                delay(2000)
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
            isVisible = isLoading || (isSuccess && !showIncidenceDialog),
            isSuccess = isSuccess && !showIncidenceDialog,
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
    }

    // Diálogo de confirmación para cancelar viaje
    ITaxCixConfirmDialog(
        showDialog = showCancelDialog,
        onDismiss = { showCancelDialog = false },
        onConfirm = {
            showCancelDialog = false
            viewModel.cancelTrip(tripId)
        },
        title = "Cancelar viaje",
        message = "¿Estás seguro de que deseas cancelar este viaje? Se notificará al conductor.",
        confirmButtonText = "Sí, cancelar",
        dismissButtonText = "No, continuar"
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
}