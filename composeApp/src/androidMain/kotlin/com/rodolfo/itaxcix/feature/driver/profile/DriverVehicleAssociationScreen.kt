package com.rodolfo.itaxcix.feature.driver.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel.DriverProfileViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVehicleAssociationScreen(
    viewModel: DriverProfileViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onSuccess: () -> Unit
) {
    // Recolectar estados
    val vehicleAssociationState by viewModel.vehicleAssociationState.collectAsState()
    val isLoading = vehicleAssociationState is DriverProfileViewModel.VehicleAssociationState.Loading
    val isSuccess = vehicleAssociationState is DriverProfileViewModel.VehicleAssociationState.Success
    val focusManager = LocalFocusManager.current

    // Estados del formulario
    val plateValue by viewModel.plateValue.collectAsState()
    val plateValueError by viewModel.plateValueError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Efecto para mostrar Snackbar cuando hay error o éxito
    LaunchedEffect(key1 = vehicleAssociationState) {
        when (val state = vehicleAssociationState) {
            is DriverProfileViewModel.VehicleAssociationState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onVehicleAssociationErrorShown()
            }
            is DriverProfileViewModel.VehicleAssociationState.Success -> {
                isSuccessSnackbar = true
                delay(2000)
                onSuccess()
                viewModel.onVehicleAssociationSuccessShown()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                contentDescription = if (isSuccessSnackbar) "Éxito" else "Error",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = data.visuals.message)
                        }
                    }
                }
            },
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Contenedor principal del formulario
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Asociar Nuevo Vehículo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ingresa la placa de tu vehículo para asociarlo a tu cuenta de conductor.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 30.dp),
                        )

                        OutlinedTextField(
                            value = plateValue,
                            onValueChange = { newValue ->
                                // Limitar a 6 caracteres y convertir a mayúsculas
                                val filteredValue = newValue
                                    .filter { it.isLetterOrDigit() } // Solo letras y números
                                    .uppercase() // Convertir a mayúsculas
                                    .take(6) // Limitar a máximo 6 caracteres
                                viewModel.onPlateValueChange(filteredValue)
                            },
                            label = { Text(text = "Ingresa la placa de tu vehículo") },
                            isError = plateValueError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp),
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

                        if (plateValueError != null) {
                            Text(
                                text = plateValueError ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, start = 4.dp)
                            )
                        }

                        Text(
                            text = "• La placa debe tener exactamente 6 caracteres\n• Solo se permiten letras y números\n• Ejemplo: ABC123",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.associateVehicle()
                            },
                            enabled = plateValue.isNotEmpty() && plateValueError == null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ITaxCixPaletaColors.Blue1,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Asociar Vehículo",
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }

                    // Texto informativo en la parte inferior
                    Text(
                        text = buildAnnotatedString {
                            append("Al asociar tu vehículo, podrás comenzar a recibir solicitudes de viaje. ")
                            withStyle(
                                style = MaterialTheme.typography.labelLarge.toSpanStyle()
                                    .copy(color = ITaxCixPaletaColors.Blue1)
                            ) {
                                append("¡Asegúrate de que la placa sea correcta!")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Overlay bloqueador de interacciones cuando está cargando
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Asociando vehículo",
            successTitle = "¡Vehículo asociado!",
            loadingMessage = "Por favor espera un momento...",
            successMessage = "Regresando al perfil..."
        )
    }
}